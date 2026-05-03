package com.budgetix.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    UUID categoryId,
    @NotNull @Min(1) @Max(12) int month,
    @NotNull int year,
    boolean rollover
) {}
