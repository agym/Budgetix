package com.budgetix.goal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GoalRequest(
    @NotBlank String name,
    @NotNull @DecimalMin("0.01") BigDecimal targetAmount,
    LocalDateTime deadline,
    String icon,
    String color
) {}
