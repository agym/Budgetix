package com.budgetix.auth.controller;

import com.budgetix.auth.dto.*;
import com.budgetix.auth.service.AuthService;
import com.budgetix.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Registration successful. Please verify your email."));
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(req)));
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(req)));
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    @Operation(summary = "Verify email")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        authService.verifyEmail(req);
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully"));
    }

    @Operation(summary = "Request password reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(ApiResponse.ok("If the email exists, a reset code has been sent"));
    }

    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully"));
    }
}
