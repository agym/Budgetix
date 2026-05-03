package com.budgetix.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    String error,
    Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .timestamp(Instant.now())
            .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(Instant.now())
            .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ok(data);
    }

    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder()
            .success(true)
            .message(message)
            .timestamp(Instant.now())
            .build();
    }

    public static ApiResponse<Void> error(String error) {
        return ApiResponse.<Void>builder()
            .success(false)
            .error(error)
            .timestamp(Instant.now())
            .build();
    }
}
