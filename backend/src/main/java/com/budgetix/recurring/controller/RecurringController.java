package com.budgetix.recurring.controller;

import com.budgetix.common.dto.ApiResponse;
import com.budgetix.recurring.dto.RecurringRequest;
import com.budgetix.recurring.dto.RecurringResponse;
import com.budgetix.recurring.service.RecurringService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Recurring Transactions")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/recurring")
@RequiredArgsConstructor
public class RecurringController {

    private final RecurringService recurringService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringResponse>>> getAll(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(recurringService.getAll(uid(p))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringResponse>> create(@AuthenticationPrincipal UserDetails p,
                                                                  @Valid @RequestBody RecurringRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(recurringService.create(uid(p), req)));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggle(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        recurringService.toggle(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Status toggled"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        recurringService.delete(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Recurring transaction deleted"));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
