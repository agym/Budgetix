package com.budgetix.dashboard.service;

import com.budgetix.account.repository.AccountRepository;
import com.budgetix.common.enums.TransactionType;
import com.budgetix.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Map<String, Object> getOverview(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);

        BigDecimal income   = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.INCOME,  monthStart, now);
        BigDecimal expenses = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.EXPENSE, monthStart, now);
        BigDecimal lastMonthExpenses = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.EXPENSE, lastMonthStart, monthStart);
        BigDecimal lastMonthIncome   = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.INCOME,  lastMonthStart, monthStart);

        BigDecimal netWorth = accountRepository.findByUserIdOrderByIsDefaultDescNameAsc(userId)
            .stream().map(a -> a.getBalance()).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = income.subtract(expenses);

        // Savings rate = (income - expenses) / income × 100  (0 when no income)
        double savingsRate = income.compareTo(BigDecimal.ZERO) > 0
            ? netSavings.divide(income, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        // Expense change % vs last month
        double expenseChangePercent = lastMonthExpenses.compareTo(BigDecimal.ZERO) > 0
            ? expenses.subtract(lastMonthExpenses)
                      .divide(lastMonthExpenses, 4, RoundingMode.HALF_UP)
                      .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        // Income change % vs last month
        double incomeChangePercent = lastMonthIncome.compareTo(BigDecimal.ZERO) > 0
            ? income.subtract(lastMonthIncome)
                    .divide(lastMonthIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        // Daily metrics
        int daysPassed    = Math.max(now.getDayOfMonth(), 1);
        int daysInMonth   = now.toLocalDate().lengthOfMonth();
        int daysRemaining = daysInMonth - now.getDayOfMonth();

        // avgDailySpend = totalExpenses / daysPassed
        BigDecimal avgDailySpend = expenses.divide(BigDecimal.valueOf(daysPassed), 2, RoundingMode.HALF_UP);

        // avgDailyIncome = totalIncome / daysPassed
        BigDecimal avgDailyIncome = income.divide(BigDecimal.valueOf(daysPassed), 2, RoundingMode.HALF_UP);

        // Projected month-end expense = avgDailySpend × daysInMonth
        BigDecimal projectedMonthlyExpense = avgDailySpend.multiply(BigDecimal.valueOf(daysInMonth));

        // Projected month-end income = avgDailyIncome × daysInMonth
        BigDecimal projectedMonthlyIncome = avgDailyIncome.multiply(BigDecimal.valueOf(daysInMonth));

        // Projected end balance = netWorth + avgDailyNet × daysRemaining
        BigDecimal avgDailyNet = avgDailyIncome.subtract(avgDailySpend);
        BigDecimal projectedEndBalance = netWorth.add(
            avgDailyNet.multiply(BigDecimal.valueOf(daysRemaining)));

        // Burn rate: days until net worth hits 0 at current daily net spend pace
        // Only meaningful when spending > income (negative daily net)
        Double daysUntilZero = null;
        if (avgDailyNet.compareTo(BigDecimal.ZERO) < 0 && netWorth.compareTo(BigDecimal.ZERO) > 0) {
            daysUntilZero = netWorth.divide(avgDailyNet.abs(), 0, RoundingMode.FLOOR).doubleValue();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalIncome",              income);
        result.put("totalExpenses",            expenses);
        result.put("netSavings",               netSavings);
        result.put("netWorth",                 netWorth);
        result.put("savingsRate",              savingsRate);
        result.put("expenseChangePercent",     expenseChangePercent);
        result.put("incomeChangePercent",      incomeChangePercent);
        result.put("avgDailySpend",            avgDailySpend);
        result.put("avgDailyIncome",           avgDailyIncome);
        result.put("projectedMonthlyExpense",  projectedMonthlyExpense);
        result.put("projectedMonthlyIncome",   projectedMonthlyIncome);
        result.put("projectedEndBalance",      projectedEndBalance);
        result.put("daysRemainingInMonth",     daysRemaining);
        if (daysUntilZero != null) result.put("daysUntilZero", daysUntilZero);
        return result;
    }

    public List<Map<String, Object>> getSpendingByCategory(UUID userId, LocalDateTime from, LocalDateTime to) {
        List<Object[]> raw = transactionRepository.groupByCategory(userId, from, to);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            result.add(Map.of("categoryId", row[0], "amount", row[1]));
        }
        return result;
    }

    public List<Map<String, Object>> getIncomeVsExpenses(UUID userId, int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end   = start.plusMonths(1);

            BigDecimal inc = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.INCOME,  start, end);
            BigDecimal exp = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.EXPENSE, start, end);
            BigDecimal net = inc.subtract(exp);

            // Month savings rate for trend
            double mSavingsRate = inc.compareTo(BigDecimal.ZERO) > 0
                ? net.divide(inc, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month",       start.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH));
            entry.put("year",        start.getYear());
            entry.put("income",      inc);
            entry.put("expenses",    exp);
            entry.put("net",         net);
            entry.put("savingsRate", mSavingsRate);
            result.add(entry);
        }
        return result;
    }

    public List<Map<String, Object>> getDailyTrend(UUID userId, int month, int year) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end   = start.plusMonths(1);
        List<Object[]> raw  = transactionRepository.getDailyTrend(userId, start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        BigDecimal runningNet = BigDecimal.ZERO;
        for (Object[] row : raw) {
            BigDecimal dayIncome   = row[1] instanceof BigDecimal b ? b : BigDecimal.ZERO;
            BigDecimal dayExpenses = row[2] instanceof BigDecimal b ? b : BigDecimal.ZERO;
            runningNet = runningNet.add(dayIncome).subtract(dayExpenses);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date",        row[0]);
            entry.put("income",      dayIncome);
            entry.put("expenses",    dayExpenses);
            entry.put("net",         dayIncome.subtract(dayExpenses));
            entry.put("runningNet",  runningNet);
            result.add(entry);
        }
        return result;
    }
}
