package com.budgetix.user.dto;

import com.budgetix.user.entity.User;
import com.budgetix.user.entity.UserProfile;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    String avatar,
    String role,
    boolean twoFactorEnabled,
    ProfileData profile
) {
    public record ProfileData(
        String currency,
        BigDecimal monthlyIncome,
        String financialGoalType,
        boolean notifyBudgetAlerts,
        boolean notifyGoalReminders,
        boolean notifyWeeklySummary,
        String timezone
    ) {
        public static ProfileData from(UserProfile p) {
            if (p == null) return null;
            return new ProfileData(p.getCurrency(), p.getMonthlyIncome(), p.getFinancialGoalType(),
                p.isNotifyBudgetAlerts(), p.isNotifyGoalReminders(), p.isNotifyWeeklySummary(), p.getTimezone());
        }
    }

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(), user.getName(), user.getEmail(), user.getAvatar(), user.getRole(),
            user.isTwoFactorEnabled(), ProfileData.from(user.getProfile())
        );
    }
}
