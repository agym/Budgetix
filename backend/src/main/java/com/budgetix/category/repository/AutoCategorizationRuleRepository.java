package com.budgetix.category.repository;

import com.budgetix.category.entity.AutoCategorizationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AutoCategorizationRuleRepository extends JpaRepository<AutoCategorizationRule, UUID> {

    @Query("SELECT r FROM AutoCategorizationRule r JOIN FETCH r.category c WHERE (c.user.id = :userId OR c.user IS NULL)")
    List<AutoCategorizationRule> findAllForUser(UUID userId);
}
