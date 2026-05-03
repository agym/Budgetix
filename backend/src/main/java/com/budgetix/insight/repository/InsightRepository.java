package com.budgetix.insight.repository;

import com.budgetix.insight.entity.Insight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsightRepository extends JpaRepository<Insight, UUID> {
    List<Insight> findByUserIdAndDismissedFalseOrderByCreatedAtDesc(UUID userId);
    Optional<Insight> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COUNT(i) FROM Insight i WHERE i.user.id = :userId AND i.period = :period AND i.type = :type AND i.dismissed = false")
    long countByPeriodAndType(UUID userId, String period, com.budgetix.common.enums.InsightType type);
}
