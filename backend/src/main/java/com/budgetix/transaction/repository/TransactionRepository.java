package com.budgetix.transaction.repository;

import com.budgetix.common.enums.TransactionType;
import com.budgetix.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>,
    JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.date BETWEEN :from AND :to")
    BigDecimal sumByTypeAndPeriod(UUID userId, TransactionType type, LocalDateTime from, LocalDateTime to);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.category JOIN FETCH t.account WHERE t.user.id = :userId AND t.date BETWEEN :from AND :to ORDER BY t.date DESC")
    List<Transaction> findByUserAndPeriod(UUID userId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT t.category.id, SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.date BETWEEN :from AND :to GROUP BY t.category.id")
    List<Object[]> groupByCategory(UUID userId, LocalDateTime from, LocalDateTime to);

    /** Returns [categoryId, totalAmount, transactionCount] for EXPENSE transactions. */
    @Query("SELECT t.category.id, SUM(t.amount), COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.date BETWEEN :from AND :to GROUP BY t.category.id ORDER BY SUM(t.amount) DESC")
    List<Object[]> groupByCategoryWithCount(UUID userId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.date BETWEEN :from AND :to")
    long countByPeriod(UUID userId, LocalDateTime from, LocalDateTime to);

    /** Average daily expense for a period: total expense / distinct date count. */
    @Query("SELECT COALESCE(SUM(t.amount), 0), COUNT(DISTINCT FUNCTION('DATE', t.date)) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.date BETWEEN :from AND :to")
    Object[] sumAndDayCountByExpense(UUID userId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT FUNCTION('DATE', t.date), SUM(CASE WHEN t.type='INCOME' THEN t.amount ELSE 0 END), SUM(CASE WHEN t.type='EXPENSE' THEN t.amount ELSE 0 END) FROM Transaction t WHERE t.user.id = :userId AND t.date BETWEEN :from AND :to GROUP BY FUNCTION('DATE', t.date) ORDER BY FUNCTION('DATE', t.date)")
    List<Object[]> getDailyTrend(UUID userId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.user.id = :userId ORDER BY t.date DESC")
    Page<Transaction> findByAccount(UUID accountId, UUID userId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.category.id = :categoryId AND t.user.id = :userId")
    long countByCategory(UUID categoryId, UUID userId);
}
