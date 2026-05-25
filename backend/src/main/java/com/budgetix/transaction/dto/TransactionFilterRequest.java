package com.budgetix.transaction.dto;

import com.budgetix.common.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionFilterRequest(
    LocalDateTime startDate,
    LocalDateTime endDate,
    UUID categoryId,
    UUID accountId,
    TransactionType type,
    String search,
    int page,
    int size
) {
    public TransactionFilterRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 1000) size = 20;
    }
}
