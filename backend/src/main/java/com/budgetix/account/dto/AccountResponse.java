package com.budgetix.account.dto;

import com.budgetix.account.entity.Account;
import com.budgetix.common.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    String name,
    AccountType type,
    BigDecimal balance,
    String currency,
    String color,
    String icon,
    boolean isDefault,
    LocalDateTime createdAt
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getType(), a.getBalance(),
            a.getCurrency(), a.getColor(), a.getIcon(), a.isDefault(), a.getCreatedAt());
    }
}
