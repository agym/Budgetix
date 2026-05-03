package com.budgetix.account.repository;

import com.budgetix.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserIdOrderByIsDefaultDescNameAsc(UUID userId);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId")
    long countTransactions(UUID accountId);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) FROM Transaction t WHERE t.account.id = :accountId")
    BigDecimal calculateBalance(UUID accountId);
}
