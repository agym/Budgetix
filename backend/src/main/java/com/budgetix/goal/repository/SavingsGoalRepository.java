package com.budgetix.goal.repository;

import com.budgetix.goal.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {
    List<SavingsGoal> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<SavingsGoal> findByIdAndUserId(UUID id, UUID userId);
}
