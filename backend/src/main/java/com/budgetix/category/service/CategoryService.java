package com.budgetix.category.service;

import com.budgetix.category.dto.CategoryRequest;
import com.budgetix.category.dto.CategoryResponse;
import com.budgetix.category.entity.AutoCategorizationRule;
import com.budgetix.category.entity.Category;
import com.budgetix.category.repository.AutoCategorizationRuleRepository;
import com.budgetix.category.repository.CategoryRepository;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.user.entity.User;
import com.budgetix.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AutoCategorizationRuleRepository ruleRepository;
    private final UserService userService;

    public List<CategoryResponse> getAll(UUID userId) {
        return categoryRepository.findRootCategoriesForUser(userId)
            .stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(UUID userId, CategoryRequest req) {
        User user = userService.getEntity(userId);

        Category parent = null;
        if (req.parentId() != null) {
            parent = categoryRepository.findById(req.parentId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        Category category = Category.builder()
            .user(user)
            .name(req.name())
            .type(req.type())
            .icon(req.icon())
            .color(req.color())
            .parent(parent)
            .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID userId, UUID id, CategoryRequest req) {
        Category category = findUserOwned(userId, id);

        if (req.name() != null) category.setName(req.name());
        if (req.icon() != null) category.setIcon(req.icon());
        if (req.color() != null) category.setColor(req.color());
        if (req.parentId() != null) {
            Category parent = categoryRepository.findById(req.parentId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            category.setParent(parent);
        }

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Category category = findUserOwned(userId, id);
        if (category.isSystem()) throw new AppException(ErrorCode.CANNOT_DELETE_SYSTEM_CATEGORY);
        categoryRepository.delete(category);
    }

    @Transactional
    public void addRule(UUID userId, UUID categoryId, String keyword) {
        Category category = findUserOwned(userId, categoryId);
        AutoCategorizationRule rule = AutoCategorizationRule.builder()
            .category(category)
            .keyword(keyword.toLowerCase())
            .build();
        ruleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(UUID ruleId) {
        ruleRepository.deleteById(ruleId);
    }

    public Category findOwned(UUID userId, UUID categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseGet(() -> categoryRepository.findByIdAndUserIsNull(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND)));
    }

    private Category findUserOwned(UUID userId, UUID categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
