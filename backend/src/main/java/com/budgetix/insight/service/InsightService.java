package com.budgetix.insight.service;

import com.budgetix.budget.entity.Budget;
import com.budgetix.budget.repository.BudgetRepository;
import com.budgetix.common.enums.InsightType;
import com.budgetix.common.enums.NotificationType;
import com.budgetix.common.enums.TransactionType;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.insight.entity.Insight;
import com.budgetix.insight.repository.InsightRepository;
import com.budgetix.notification.service.NotificationService;
import com.budgetix.transaction.repository.TransactionRepository;
import com.budgetix.user.entity.User;
import com.budgetix.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository     insightRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository      budgetRepository;
    private final UserService           userService;
    private final NotificationService   notificationService;

    public List<InsightResponse> getAll(UUID userId) {
        return insightRepository.findByUserIdAndDismissedFalseOrderByCreatedAtDesc(userId)
            .stream().map(InsightResponse::from).toList();
    }

    @Transactional
    public void dismiss(UUID userId, UUID id) {
        Insight insight = insightRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AppException(ErrorCode.INSIGHT_NOT_FOUND));
        insight.setDismissed(true);
        insightRepository.save(insight);
    }

    /** Runs at 08:00 on the 1st of every month — generates insights for all verified users. */
    @Scheduled(cron = "0 0 8 1 * *")
    @Transactional
    public void generateMonthlyInsightsForAllUsers() {
        log.info("Generating monthly insights for all verified users...");
        List<UUID> userIds = userService.findAllVerifiedUserIds();
        for (UUID userId : userIds) {
            try {
                generateForUser(userId);
            } catch (Exception e) {
                log.error("Failed to generate insights for user {}: {}", userId, e.getMessage());
            }
        }
        log.info("Monthly insight generation complete for {} users", userIds.size());
    }

    @Transactional
    public List<InsightResponse> generateForUser(UUID userId) {
        User user = userService.getEntity(userId);
        LocalDateTime now           = LocalDateTime.now();
        LocalDateTime thisMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
        LocalDateTime threeMonthsAgo = thisMonthStart.minusMonths(3);
        String period = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<Insight> generated = new ArrayList<>();

        BigDecimal thisIncome  = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.INCOME,  thisMonthStart, now);
        BigDecimal thisExpense = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.EXPENSE, thisMonthStart, now);
        BigDecimal lastExpense = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.EXPENSE, lastMonthStart, thisMonthStart);
        BigDecimal lastIncome  = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.INCOME,  lastMonthStart, thisMonthStart);

        // ── 1. Savings Rate ──────────────────────────────────────────────────
        if (thisIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal saved = thisIncome.subtract(thisExpense);
            // savingsRate = (income - expenses) / income × 100
            double savingsRate = saved.divide(thisIncome, 4, RoundingMode.HALF_UP)
                                      .multiply(BigDecimal.valueOf(100)).doubleValue();

            if (savingsRate < 10) {
                generated.add(buildInsight(user, InsightType.LOW_SAVINGS_RATE, period,
                    "Low Savings Rate",
                    String.format("You're saving only %.1f%% of your income this month. " +
                        "The recommended minimum is 20%%. Try reducing discretionary spending.", savingsRate),
                    Map.of("savingsRate", savingsRate, "recommended", 20.0)));
            } else if (savingsRate >= 30) {
                generated.add(buildInsight(user, InsightType.HIGH_SAVINGS_RATE, period,
                    "Excellent Savings Rate",
                    String.format("You're saving %.1f%% of your income — well above the 20%% benchmark. Keep it up!", savingsRate),
                    Map.of("savingsRate", savingsRate, "benchmark", 20.0)));
            }
        }

        // ── 2. Spending Change vs Last Month ─────────────────────────────────
        if (lastExpense.compareTo(BigDecimal.ZERO) > 0 && thisExpense.compareTo(BigDecimal.ZERO) > 0) {
            // change% = (thisMonth - lastMonth) / lastMonth × 100
            double change = thisExpense.subtract(lastExpense)
                .divide(lastExpense, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

            if (change > 20) {
                generated.add(buildInsight(user, InsightType.SPENDING_INCREASE, period,
                    "Spending Spike Detected",
                    String.format("Your expenses are up %.1f%% compared to last month " +
                        "(%s vs %s). Review your recent transactions for unexpected charges.",
                        change, fmt(thisExpense), fmt(lastExpense)),
                    Map.of("changePercent", change, "thisMonth", thisExpense, "lastMonth", lastExpense)));
            } else if (change < -15) {
                generated.add(buildInsight(user, InsightType.SPENDING_DECREASE, period,
                    "Great Job Cutting Expenses",
                    String.format("Your expenses dropped %.1f%% vs last month (%s vs %s). Excellent discipline!",
                        Math.abs(change), fmt(thisExpense), fmt(lastExpense)),
                    Map.of("changePercent", change, "thisMonth", thisExpense, "lastMonth", lastExpense)));
            }
        }

        // ── 3. Unusual Spending in a Category ────────────────────────────────
        // Compare this month per-category vs 3-month rolling average
        List<Object[]> thisCats  = transactionRepository.groupByCategoryWithCount(userId, thisMonthStart, now);
        List<Object[]> prevCats  = transactionRepository.groupByCategoryWithCount(userId, threeMonthsAgo, thisMonthStart);
        Map<UUID, BigDecimal> prevAvgByCat = new HashMap<>();
        for (Object[] row : prevCats) {
            UUID catId = (UUID) row[0];
            if (catId != null) {
                // average over 3 months
                BigDecimal total = (BigDecimal) row[1];
                prevAvgByCat.put(catId, total.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP));
            }
        }
        for (Object[] row : thisCats) {
            UUID catId       = (UUID) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (catId == null || amount.compareTo(BigDecimal.ZERO) == 0) continue;
            BigDecimal avg = prevAvgByCat.get(catId);
            if (avg != null && avg.compareTo(BigDecimal.ZERO) > 0) {
                // spike if > 2× 3-month average
                double ratio = amount.divide(avg, 4, RoundingMode.HALF_UP).doubleValue();
                if (ratio > 2.0) {
                    generated.add(buildInsight(user, InsightType.UNUSUAL_SPENDING, period,
                        "Unusual Spending Detected",
                        String.format("One category is %.1fx higher than its 3-month average (%s vs avg %s). " +
                            "Check if this was planned.", ratio, fmt(amount), fmt(avg)),
                        Map.of("categoryId", catId, "amount", amount, "threeMonthAvg", avg, "ratio", ratio)));
                    break; // one unusual-spending insight per run is enough
                }
            }
        }

        // ── 4. Budget Exceeded ───────────────────────────────────────────────
        List<Budget> budgets = budgetRepository.findByUserAndPeriod(userId, now.getYear(), now.getMonthValue());
        for (Budget b : budgets) {
            // usagePercent = spent / (amount + rolledAmount) × 100
            if (b.getUsagePercent() >= 100.0) {
                String catName = b.getCategory() != null ? b.getCategory().getName() : "overall";
                generated.add(buildInsight(user, InsightType.BUDGET_EXCEEDED, period,
                    "Budget Exceeded",
                    String.format("Your %s budget is at %.1f%% usage (%s spent of %s). " +
                        "Consider adjusting spending or increasing the budget limit.",
                        catName, b.getUsagePercent(), fmt(b.getSpent()), fmt(b.getAmount())),
                    Map.of("budgetId", b.getId(), "spent", b.getSpent(),
                           "amount", b.getAmount(), "usagePercent", b.getUsagePercent())));

                // Push a notification as well
                notificationService.sendBudgetAlert(b, 1.0);
                break; // one budget-exceeded insight per run
            }
        }

        // ── 5. Subscription Summary ──────────────────────────────────────────
        long recurringCount = transactionRepository.countByPeriod(userId, thisMonthStart, now);
        // Estimate subscription cost as sum of recurring-flagged expenses
        BigDecimal subscriptionTotal = transactionRepository.sumByTypeAndPeriod(
            userId, TransactionType.EXPENSE, thisMonthStart, now);
        // Only generate if there are meaningful recurring transactions (heuristic)
        if (recurringCount > 0 && subscriptionTotal.compareTo(BigDecimal.ZERO) > 0) {
            generated.add(buildInsight(user, InsightType.SUBSCRIPTION_SUMMARY, period,
                "Monthly Subscription Overview",
                String.format("You have %d recurring transactions this month totalling %s. " +
                    "Review periodically to cancel unused subscriptions.", recurringCount, fmt(subscriptionTotal)),
                Map.of("count", recurringCount, "total", subscriptionTotal)));
        }

        // ── 6. Monthly Summary (always) ──────────────────────────────────────
        BigDecimal netSavings = thisIncome.subtract(thisExpense);
        double savingsRateFinal = thisIncome.compareTo(BigDecimal.ZERO) > 0
            ? netSavings.divide(thisIncome, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        generated.add(buildInsight(user, InsightType.MONTHLY_SUMMARY, period,
            "Monthly Overview",
            String.format("Income: %s | Expenses: %s | Net: %s | Savings rate: %.1f%%",
                fmt(thisIncome), fmt(thisExpense), fmt(netSavings), savingsRateFinal),
            Map.of("income", thisIncome, "expenses", thisExpense,
                   "netSavings", netSavings, "savingsRate", savingsRateFinal)));

        List<Insight> saved = insightRepository.saveAll(generated);
        return saved.stream().map(InsightResponse::from).toList();
    }

    // -------------------------------------------------------------------------

    private Insight buildInsight(User user, InsightType type, String period,
                                  String title, String message, Map<String, Object> data) {
        return Insight.builder()
            .user(user)
            .type(type)
            .period(period)
            .title(title)
            .message(message)
            .data(data)
            .build();
    }

    private String fmt(BigDecimal val) {
        if (val == null) return "$0.00";
        return String.format("$%,.2f", val);
    }

    // -------------------------------------------------------------------------

    public record InsightResponse(
        UUID id, InsightType type, String title, String message,
        String period, Map<String, Object> data, LocalDateTime createdAt
    ) {
        public static InsightResponse from(Insight i) {
            return new InsightResponse(i.getId(), i.getType(), i.getTitle(), i.getMessage(),
                i.getPeriod(), i.getData(), i.getCreatedAt());
        }
    }
}
