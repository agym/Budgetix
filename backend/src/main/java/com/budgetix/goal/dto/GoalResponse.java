package com.budgetix.goal.dto;

import com.budgetix.common.enums.GoalStatus;
import com.budgetix.goal.entity.SavingsGoal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoalResponse(
    UUID id,
    String name,
    BigDecimal targetAmount,
    BigDecimal currentAmount,
    BigDecimal remaining,
    double progressPercent,
    LocalDateTime deadline,
    String icon,
    String color,
    GoalStatus status,
    LocalDateTime createdAt
) {
    public static GoalResponse from(SavingsGoal g) {
        return new GoalResponse(g.getId(), g.getName(), g.getTargetAmount(), g.getCurrentAmount(),
            g.getTargetAmount().subtract(g.getCurrentAmount()), g.getProgressPercent(),
            g.getDeadline(), g.getIcon(), g.getColor(), g.getStatus(), g.getCreatedAt());
    }
}
