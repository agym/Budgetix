package com.budgetix.goal.controller;

import com.budgetix.common.dto.ApiResponse;
import com.budgetix.common.enums.GoalStatus;
import com.budgetix.goal.dto.GoalRequest;
import com.budgetix.goal.dto.GoalResponse;
import com.budgetix.goal.service.GoalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Savings Goals")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getAll(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.getAll(uid(p))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> getById(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.getById(uid(p), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> create(@AuthenticationPrincipal UserDetails p,
                                                             @Valid @RequestBody GoalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(goalService.create(uid(p), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> update(@AuthenticationPrincipal UserDetails p,
                                                             @PathVariable UUID id,
                                                             @Valid @RequestBody GoalRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.update(uid(p), id, req)));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<ApiResponse<GoalResponse>> contribute(@AuthenticationPrincipal UserDetails p,
                                                                 @PathVariable UUID id,
                                                                 @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String note = body.get("note") != null ? body.get("note").toString() : null;
        return ResponseEntity.ok(ApiResponse.ok(goalService.contribute(uid(p), id, amount, note)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@AuthenticationPrincipal UserDetails p,
                                                           @PathVariable UUID id,
                                                           @RequestBody Map<String, String> body) {
        goalService.updateStatus(uid(p), id, GoalStatus.valueOf(body.get("status")));
        return ResponseEntity.ok(ApiResponse.ok("Status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        goalService.delete(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Goal deleted"));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
