package com.budgetix.budget.controller;

import com.budgetix.budget.dto.BudgetRequest;
import com.budgetix.budget.dto.BudgetResponse;
import com.budgetix.budget.service.BudgetService;
import com.budgetix.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Budgets")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getByPeriod(
        @AuthenticationPrincipal UserDetails p,
        @RequestParam(defaultValue = "0") int month,
        @RequestParam(defaultValue = "0") int year) {
        LocalDate now = LocalDate.now();
        int m = month > 0 ? month : now.getMonthValue();
        int y = year > 0 ? year : now.getYear();
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getByPeriod(uid(p), m, y)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> create(@AuthenticationPrincipal UserDetails p,
                                                               @Valid @RequestBody BudgetRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(budgetService.create(uid(p), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(@AuthenticationPrincipal UserDetails p,
                                                               @PathVariable UUID id,
                                                               @Valid @RequestBody BudgetRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(budgetService.update(uid(p), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        budgetService.delete(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Budget deleted"));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
