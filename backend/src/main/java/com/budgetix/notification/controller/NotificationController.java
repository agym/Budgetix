package com.budgetix.notification.controller;

import com.budgetix.common.dto.ApiResponse;
import com.budgetix.common.dto.PageResponse;
import com.budgetix.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Notifications")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationService.NotificationDto>>> getAll(
        @AuthenticationPrincipal UserDetails p,
        @RequestParam(defaultValue = "false") boolean unreadOnly,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getAll(uid(p), unreadOnly, page, size)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", notificationService.countUnread(uid(p)))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        notificationService.markRead(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal UserDetails p) {
        notificationService.markAllRead(uid(p));
        return ResponseEntity.ok(ApiResponse.ok("All marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        notificationService.delete(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted"));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
