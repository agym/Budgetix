package com.budgetix.transaction.dto;

import com.budgetix.common.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record TransactionRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull TransactionType type,
    @NotNull UUID accountId,
    UUID categoryId,
    String description,
    String notes,
    @NotNull LocalDateTime date,
    Set<String> tags
) {}
