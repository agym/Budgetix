package com.budgetix.category.repository;

import com.budgetix.category.entity.Category;
import com.budgetix.common.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE (c.user.id = :userId OR c.user IS NULL) AND c.parent IS NULL ORDER BY c.name")
    List<Category> findRootCategoriesForUser(UUID userId);

    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.user IS NULL) AND c.type = :type ORDER BY c.name")
    List<Category> findByUserAndType(UUID userId, CategoryType type);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.rules WHERE c.user IS NULL")
    List<Category> findAllSystemWithRules();

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    Optional<Category> findByIdAndUserIsNull(UUID id);
}
