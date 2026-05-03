package com.budgetix.goal.service;

import com.budgetix.common.enums.GoalStatus;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.goal.dto.GoalRequest;
import com.budgetix.goal.dto.GoalResponse;
import com.budgetix.goal.entity.GoalContribution;
import com.budgetix.goal.entity.SavingsGoal;
import com.budgetix.goal.repository.GoalContributionRepository;
import com.budgetix.goal.repository.SavingsGoalRepository;
import com.budgetix.notification.service.NotificationService;
import com.budgetix.user.entity.User;
import com.budgetix.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final SavingsGoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public List<GoalResponse> getAll(UUID userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(GoalResponse::from).toList();
    }

    public GoalResponse getById(UUID userId, UUID id) {
        return GoalResponse.from(findOwned(userId, id));
    }

    @Transactional
    public GoalResponse create(UUID userId, GoalRequest req) {
        User user = userService.getEntity(userId);
        SavingsGoal goal = SavingsGoal.builder()
            .user(user)
            .name(req.name())
            .targetAmount(req.targetAmount())
            .deadline(req.deadline())
            .icon(req.icon())
            .color(req.color())
            .build();
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse update(UUID userId, UUID id, GoalRequest req) {
        SavingsGoal goal = findOwned(userId, id);
        if (req.name() != null) goal.setName(req.name());
        if (req.targetAmount() != null) goal.setTargetAmount(req.targetAmount());
        if (req.deadline() != null) goal.setDeadline(req.deadline());
        if (req.icon() != null) goal.setIcon(req.icon());
        if (req.color() != null) goal.setColor(req.color());
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse contribute(UUID userId, UUID goalId, BigDecimal amount, String note) {
        SavingsGoal goal = findOwned(userId, goalId);

        GoalContribution contribution = GoalContribution.builder()
            .goal(goal)
            .amount(amount)
            .note(note)
            .build();
        contributionRepository.save(contribution);

        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
            notificationService.sendGoalCompleted(goal);
        }

        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional
    public void updateStatus(UUID userId, UUID id, GoalStatus status) {
        SavingsGoal goal = findOwned(userId, id);
        goal.setStatus(status);
        goalRepository.save(goal);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        goalRepository.delete(findOwned(userId, id));
    }

    private SavingsGoal findOwned(UUID userId, UUID id) {
        return goalRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AppException(ErrorCode.GOAL_NOT_FOUND));
    }
}
