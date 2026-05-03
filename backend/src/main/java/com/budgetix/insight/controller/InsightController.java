package com.budgetix.insight.controller;

import com.budgetix.common.dto.ApiResponse;
import com.budgetix.insight.service.InsightService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Insights")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InsightService.InsightResponse>>> getAll(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(insightService.getAll(uid(p))));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<InsightService.InsightResponse>>> generate(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(insightService.generateForUser(uid(p))));
    }

    @DeleteMapping("/{id}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismiss(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        insightService.dismiss(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Insight dismissed"));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
