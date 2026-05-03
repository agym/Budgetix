package com.budgetix.user.dto;

import java.math.BigDecimal;

public record UpdateSettingsRequest(
    String currency,
    BigDecimal monthlyIncome,
    String financialGoalType,
    Boolean notifyBudgetAlerts,
    Boolean notifyGoalReminders,
    Boolean notifyWeeklySummary,
    String timezone
) {}
