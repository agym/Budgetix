package com.budgetix.user.controller;

import com.budgetix.common.dto.ApiResponse;
import com.budgetix.user.dto.ChangePasswordRequest;
import com.budgetix.user.dto.UpdateProfileRequest;
import com.budgetix.user.dto.UpdateSettingsRequest;
import com.budgetix.user.dto.UserResponse;
import com.budgetix.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMe(userId(principal))));
    }

    @Operation(summary = "Update profile")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(userId(principal), req)));
    }

    @Operation(summary = "Update settings")
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<UserResponse>> updateSettings(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @RequestBody UpdateSettingsRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateSettings(userId(principal), req)));
    }

    @Operation(summary = "Change password")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(userId(principal), req);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
    }

    private UUID userId(UserDetails principal) {
        return UUID.fromString(principal.getUsername());
    }
}
