package com.budgetix.transaction.dto;

import com.budgetix.common.enums.TransactionType;
import com.budgetix.transaction.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    BigDecimal amount,
    TransactionType type,
    String description,
    String notes,
    LocalDateTime date,
    String receipt,
    boolean recurring,
    Set<String> tags,
    AccountRef account,
    CategoryRef category,
    UUID transferPairId,
    boolean transferCredit,
    LocalDateTime createdAt
) {
    public record AccountRef(UUID id, String name, String icon, String color) {}
    public record CategoryRef(UUID id, String name, String icon, String color) {}

    public static TransactionResponse from(Transaction t) {
        AccountRef account = t.getAccount() != null
            ? new AccountRef(t.getAccount().getId(), t.getAccount().getName(),
                t.getAccount().getIcon(), t.getAccount().getColor())
            : null;

        CategoryRef category = t.getCategory() != null
            ? new CategoryRef(t.getCategory().getId(), t.getCategory().getName(),
                t.getCategory().getIcon(), t.getCategory().getColor())
            : null;

        return new TransactionResponse(t.getId(), t.getAmount(), t.getType(), t.getDescription(),
            t.getNotes(), t.getDate(), t.getReceipt(), t.isRecurring(), t.getTags(),
            account, category, t.getTransferPairId(), t.isTransferCredit(), t.getCreatedAt());
    }
}
