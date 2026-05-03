package com.budgetix.goal.repository;

import com.budgetix.goal.entity.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, UUID> {}
