package com.budgetix.budget.repository;

import com.budgetix.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    @Query("SELECT b FROM Budget b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.alerts WHERE b.user.id = :userId AND b.year = :year AND b.month = :month")
    List<Budget> findByUserAndPeriod(UUID userId, int year, int month);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId AND b.year = :year AND b.month = :month")
    Optional<Budget> findByUserAndCategoryAndPeriod(UUID userId, UUID categoryId, int year, int month);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category IS NULL AND b.year = :year AND b.month = :month")
    Optional<Budget> findGlobalBudget(UUID userId, int year, int month);

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT b FROM Budget b WHERE b.rollover = true AND b.year = :year AND b.month = :month")
    List<Budget> findRolloverBudgets(int year, int month);
}
