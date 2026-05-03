package com.budgetix.notification.service;

import com.budgetix.budget.entity.Budget;
import com.budgetix.common.dto.PageResponse;
import com.budgetix.common.enums.NotificationType;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.goal.entity.SavingsGoal;
import com.budgetix.notification.entity.Notification;
import com.budgetix.notification.repository.NotificationRepository;
import com.budgetix.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public PageResponse<NotificationDto> getAll(UUID userId, boolean unreadOnly, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        var paged = unreadOnly
            ? notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pr)
            : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pr);
        return PageResponse.from(paged.map(NotificationDto::from));
    }

    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(UUID userId, UUID id) {
        Notification n = notificationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllRead(userId);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        notificationRepository.delete(
            notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND))
        );
    }

    public void send(User user, NotificationType type, String title, String message, Map<String, Object> data) {
        Notification n = Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .data(data)
            .build();
        notificationRepository.save(n);
    }

    public void sendBudgetAlert(Budget budget, double threshold) {
        String pct = threshold >= 1.0 ? "exceeded" : "reached " + (int)(threshold * 100) + "%";
        String categoryName = budget.getCategory() != null ? budget.getCategory().getName() : "Global";
        send(budget.getUser(), NotificationType.BUDGET_ALERT,
            "Budget Alert – " + categoryName,
            String.format("Your %s budget has %s.", categoryName, pct),
            Map.of("budgetId", budget.getId().toString(), "threshold", threshold));
    }

    public void sendGoalCompleted(SavingsGoal goal) {
        send(goal.getUser(), NotificationType.GOAL_COMPLETED,
            "Goal Reached! 🎉",
            String.format("Congratulations! You've reached your goal: %s", goal.getName()),
            Map.of("goalId", goal.getId().toString()));
    }

    public record NotificationDto(
        UUID id, String title, String message, NotificationType type,
        boolean read, java.time.LocalDateTime createdAt
    ) {
        public static NotificationDto from(Notification n) {
            return new NotificationDto(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.isRead(), n.getCreatedAt());
        }
    }
}
