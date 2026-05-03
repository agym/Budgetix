package com.budgetix.category.dto;

import com.budgetix.category.entity.Category;
import com.budgetix.common.enums.CategoryType;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
    UUID id,
    String name,
    String icon,
    String color,
    CategoryType type,
    boolean system,
    UUID parentId,
    List<CategoryResponse> children
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
            c.getId(), c.getName(), c.getIcon(), c.getColor(), c.getType(), c.isSystem(),
            c.getParent() != null ? c.getParent().getId() : null,
            c.getChildren() != null
                ? c.getChildren().stream().map(CategoryResponse::fromFlat).toList()
                : List.of()
        );
    }

    public static CategoryResponse fromFlat(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getIcon(), c.getColor(),
            c.getType(), c.isSystem(), c.getParent() != null ? c.getParent().getId() : null, List.of());
    }
}
