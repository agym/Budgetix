package com.budgetix.budget.service;

import com.budgetix.budget.dto.BudgetRequest;
import com.budgetix.budget.dto.BudgetResponse;
import com.budgetix.budget.entity.Budget;
import com.budgetix.budget.entity.BudgetAlert;
import com.budgetix.budget.repository.BudgetRepository;
import com.budgetix.category.entity.Category;
import com.budgetix.category.repository.CategoryRepository;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.notification.service.NotificationService;
import com.budgetix.user.entity.User;
import com.budgetix.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public List<BudgetResponse> getByPeriod(UUID userId, int month, int year) {
        return budgetRepository.findByUserAndPeriod(userId, year, month)
            .stream().map(BudgetResponse::from).toList();
    }

    public BudgetResponse getById(UUID userId, UUID id) {
        return BudgetResponse.from(findOwned(userId, id));
    }

    @Transactional
    public BudgetResponse create(UUID userId, BudgetRequest req) {
        User user = userService.getEntity(userId);
        Category category = null;

        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            budgetRepository.findByUserAndCategoryAndPeriod(userId, req.categoryId(), req.year(), req.month())
                .ifPresent(b -> { throw new AppException(ErrorCode.BUDGET_ALREADY_EXISTS); });
        } else {
            budgetRepository.findGlobalBudget(userId, req.year(), req.month())
                .ifPresent(b -> { throw new AppException(ErrorCode.BUDGET_ALREADY_EXISTS); });
        }

        Budget budget = Budget.builder()
            .user(user)
            .category(category)
            .amount(req.amount())
            .month(req.month())
            .year(req.year())
            .rollover(req.rollover())
            .build();

        budget.getAlerts().add(BudgetAlert.builder().budget(budget).threshold(new BigDecimal("0.8")).build());
        budget.getAlerts().add(BudgetAlert.builder().budget(budget).threshold(new BigDecimal("1.0")).build());

        return BudgetResponse.from(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetResponse update(UUID userId, UUID id, BudgetRequest req) {
        Budget budget = findOwned(userId, id);
        budget.setAmount(req.amount());
        budget.setRollover(req.rollover());
        return BudgetResponse.from(budgetRepository.save(budget));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        budgetRepository.delete(findOwned(userId, id));
    }

    public void checkAndFireAlerts(Budget budget) {
        double usage = budget.getUsagePercent();
        budget.getAlerts().forEach(alert -> {
            if (!alert.isTriggered() && usage >= alert.getThreshold().doubleValue() * 100) {
                alert.setTriggered(true);
                alert.setTriggeredAt(java.time.LocalDateTime.now());
                budgetRepository.save(budget);
                notificationService.sendBudgetAlert(budget, alert.getThreshold().doubleValue());
            }
        });
    }

    @Scheduled(cron = "0 0 1 1 * *")
    @Transactional
    public void rolloverBudgets() {
        LocalDate now = LocalDate.now();
        int prevMonth = now.minusMonths(1).getMonthValue();
        int prevYear = now.minusMonths(1).getYear();

        budgetRepository.findRolloverBudgets(prevYear, prevMonth).forEach(b -> {
            BigDecimal remaining = b.getRemaining();
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                budgetRepository.findByUserAndCategoryAndPeriod(
                    b.getUser().getId(),
                    b.getCategory() != null ? b.getCategory().getId() : null,
                    now.getYear(), now.getMonthValue()
                ).ifPresent(newBudget -> {
                    newBudget.setRolledAmount(remaining);
                    budgetRepository.save(newBudget);
                });
            }
        });
        log.info("Budget rollover completed for {}/{}", prevMonth, prevYear);
    }

    private Budget findOwned(UUID userId, UUID id) {
        return budgetRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));
    }
}
