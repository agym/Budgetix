package com.budgetix.recurring.repository;

import com.budgetix.recurring.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {
    List<RecurringTransaction> findByUserIdOrderByNextRunAsc(UUID userId);
    Optional<RecurringTransaction> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT r FROM RecurringTransaction r WHERE r.active = true AND r.nextRun <= :now AND (r.endDate IS NULL OR r.endDate > :now)")
    List<RecurringTransaction> findDueTransactions(LocalDateTime now);
}
