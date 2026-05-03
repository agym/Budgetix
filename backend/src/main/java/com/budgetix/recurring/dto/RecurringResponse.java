package com.budgetix.recurring.dto;

import com.budgetix.common.enums.Frequency;
import com.budgetix.common.enums.TransactionType;
import com.budgetix.recurring.entity.RecurringTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringResponse(
    UUID id,
    BigDecimal amount,
    TransactionType type,
    String description,
    Frequency frequency,
    LocalDateTime startDate,
    LocalDateTime endDate,
    LocalDateTime nextRun,
    LocalDateTime lastRun,
    boolean active,
    String accountName,
    String categoryName
) {
    public static RecurringResponse from(RecurringTransaction r) {
        return new RecurringResponse(r.getId(), r.getAmount(), r.getType(), r.getDescription(),
            r.getFrequency(), r.getStartDate(), r.getEndDate(), r.getNextRun(), r.getLastRun(),
            r.isActive(),
            r.getAccount() != null ? r.getAccount().getName() : null,
            r.getCategory() != null ? r.getCategory().getName() : null);
    }
}
