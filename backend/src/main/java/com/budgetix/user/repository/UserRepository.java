package com.budgetix.user.repository;

import com.budgetix.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findByIdWithProfile(UUID id);

    @Query("SELECT u.id FROM User u WHERE u.emailVerified = true")
    List<UUID> findAllVerifiedUserIds();
}
