package com.budgetix.auth.dto;

import com.budgetix.user.entity.User;

import java.util.UUID;

public record UserAuthResponse(
    UUID id,
    String name,
    String email,
    String avatar,
    String role,
    boolean twoFactorEnabled
) {
    public static UserAuthResponse from(User user) {
        return new UserAuthResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAvatar(),
            user.getRole(),
            user.isTwoFactorEnabled()
        );
    }
}
