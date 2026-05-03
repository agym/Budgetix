package com.budgetix.account.controller;

import com.budgetix.account.dto.AccountRequest;
import com.budgetix.account.dto.AccountResponse;
import com.budgetix.account.service.AccountService;
import com.budgetix.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Accounts")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAll(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAll(uid(p))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getById(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getById(uid(p), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> create(@AuthenticationPrincipal UserDetails p,
                                                               @Valid @RequestBody AccountRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(accountService.create(uid(p), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(@AuthenticationPrincipal UserDetails p,
                                                               @PathVariable UUID id,
                                                               @Valid @RequestBody AccountRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.update(uid(p), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        accountService.delete(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Account deleted"));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
