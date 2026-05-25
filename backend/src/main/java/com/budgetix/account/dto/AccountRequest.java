package com.budgetix.account.dto;

import com.budgetix.common.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequest(
    @NotBlank String name,
    @NotNull AccountType type,
    BigDecimal initialBalance,
    String currency,
    String color,
    String icon,
    boolean isDefault,
    String institutionName,
    String lastFour
) {}
