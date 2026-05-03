package com.budgetix.category.dto;

import com.budgetix.common.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CategoryRequest(
    @NotBlank String name,
    @NotNull CategoryType type,
    String icon,
    String color,
    UUID parentId
) {}
