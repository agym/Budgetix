package com.budgetix.transaction.controller;

import com.budgetix.common.dto.ApiResponse;
import com.budgetix.common.dto.PageResponse;
import com.budgetix.transaction.dto.TransactionFilterRequest;
import com.budgetix.transaction.dto.TransactionRequest;
import com.budgetix.transaction.dto.TransactionResponse;
import com.budgetix.transaction.service.CsvImportService;
import com.budgetix.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Transactions")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CsvImportService csvImportService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getAll(
        @AuthenticationPrincipal UserDetails p,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) UUID accountId,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        var filter = new TransactionFilterRequest(startDate, endDate, categoryId, accountId,
            type != null ? com.budgetix.common.enums.TransactionType.valueOf(type) : null, search, page, size);
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getAll(uid(p), filter)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(@AuthenticationPrincipal UserDetails p,
                                                                     @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getById(uid(p), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(@AuthenticationPrincipal UserDetails p,
                                                                    @Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(transactionService.create(uid(p), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(@AuthenticationPrincipal UserDetails p,
                                                                    @PathVariable UUID id,
                                                                    @Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.update(uid(p), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        transactionService.delete(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Transaction deleted"));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@AuthenticationPrincipal UserDetails p,
                                                         @RequestBody Map<String, List<UUID>> body) {
        transactionService.bulkDelete(uid(p), body.get("ids"));
        return ResponseEntity.ok(ApiResponse.ok("Transactions deleted"));
    }

    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadReceipt(@AuthenticationPrincipal UserDetails p,
                                                              @PathVariable UUID id,
                                                              @RequestParam("file") MultipartFile file) throws IOException {
        String url = transactionService.uploadReceipt(uid(p), id, file);
        return ResponseEntity.ok(ApiResponse.ok("Receipt uploaded", url));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> importCsv(
        @AuthenticationPrincipal UserDetails p,
        @RequestParam UUID accountId,
        @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(csvImportService.importCsv(uid(p), accountId, file)));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
