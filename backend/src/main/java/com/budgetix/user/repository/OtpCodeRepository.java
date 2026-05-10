package com.budgetix.user.repository;

import com.budgetix.common.enums.OtpType;
import com.budgetix.user.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    @Query("SELECT o FROM OtpCode o WHERE o.user.id = :userId AND o.type = :type AND o.used = false ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpCode> findLatestValid(UUID userId, OtpType type);

    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.user.id = :userId AND o.type = :type")
    void invalidateAll(UUID userId, OtpType type);

    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.user.id = :userId AND o.type = :type AND o.createdAt >= :since")
    long countRecentAttempts(@Param("userId") UUID userId, @Param("type") OtpType type, @Param("since") LocalDateTime since);
}
