package com.budgetix.dashboard.controller;

import com.budgetix.common.dto.ApiResponse;
import com.budgetix.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Dashboard")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview(uid(p))));
    }

    @GetMapping("/charts/spending-by-category")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> spendingByCategory(
        @AuthenticationPrincipal UserDetails p,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSpendingByCategory(uid(p), from, to)));
    }

    @GetMapping("/charts/income-vs-expenses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> incomeVsExpenses(
        @AuthenticationPrincipal UserDetails p,
        @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getIncomeVsExpenses(uid(p), months)));
    }

    @GetMapping("/charts/daily-trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> dailyTrend(
        @AuthenticationPrincipal UserDetails p,
        @RequestParam int month,
        @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDailyTrend(uid(p), month, year)));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
