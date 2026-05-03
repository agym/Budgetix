package com.budgetix.recurring.dto;

import com.budgetix.common.enums.Frequency;
import com.budgetix.common.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull TransactionType type,
    @NotNull UUID accountId,
    UUID categoryId,
    @NotBlank String description,
    @NotNull Frequency frequency,
    @NotNull LocalDateTime startDate,
    LocalDateTime endDate
) {}
