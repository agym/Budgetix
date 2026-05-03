package com.budgetix.budget.dto;

import com.budgetix.budget.entity.Budget;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(
    UUID id,
    BigDecimal amount,
    BigDecimal spent,
    BigDecimal remaining,
    double usagePercent,
    int month,
    int year,
    boolean rollover,
    BigDecimal rolledAmount,
    CategoryRef category
) {
    public record CategoryRef(UUID id, String name, String icon, String color) {}

    public static BudgetResponse from(Budget b) {
        CategoryRef cat = b.getCategory() != null
            ? new CategoryRef(b.getCategory().getId(), b.getCategory().getName(),
                b.getCategory().getIcon(), b.getCategory().getColor())
            : null;
        return new BudgetResponse(b.getId(), b.getAmount(), b.getSpent(), b.getRemaining(),
            b.getUsagePercent(), b.getMonth() != null ? b.getMonth() : 0,
            b.getYear() != null ? b.getYear() : 0,
            b.isRollover(), b.getRolledAmount(), cat);
    }
}
