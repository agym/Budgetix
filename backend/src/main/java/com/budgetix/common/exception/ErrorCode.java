package com.budgetix.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    OAUTH_ACCOUNT(HttpStatus.BAD_REQUEST, "This account uses Google Sign-In. Please continue with Google."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email already registered"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Email not verified"),
    INVALID_OTP(HttpStatus.BAD_REQUEST, "Invalid or expired OTP code"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or expired token"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh token expired"),
    TWO_FACTOR_REQUIRED(HttpStatus.UNAUTHORIZED, "Two-factor authentication required"),
    INVALID_2FA_CODE(HttpStatus.BAD_REQUEST, "Invalid 2FA code"),

    // Resources
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account not found"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Transaction not found"),
    BUDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "Budget not found"),
    GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Savings goal not found"),
    RECURRING_NOT_FOUND(HttpStatus.NOT_FOUND, "Recurring transaction not found"),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification not found"),
    INSIGHT_NOT_FOUND(HttpStatus.NOT_FOUND, "Insight not found"),

    // Business
    BUDGET_ALREADY_EXISTS(HttpStatus.CONFLICT, "Budget already exists for this category and period"),
    INSUFFICIENT_GOAL_BALANCE(HttpStatus.BAD_REQUEST, "Contribution exceeds goal target"),
    CANNOT_DELETE_SYSTEM_CATEGORY(HttpStatus.FORBIDDEN, "Cannot delete system category"),
    ACCOUNT_HAS_TRANSACTIONS(HttpStatus.CONFLICT, "Cannot delete account with existing transactions"),
    INVALID_CSV_FORMAT(HttpStatus.BAD_REQUEST, "Invalid CSV format"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds limit"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "Unsupported file type"),

    // Generic
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
