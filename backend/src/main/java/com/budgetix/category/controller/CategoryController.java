package com.budgetix.category.controller;

import com.budgetix.category.dto.CategoryRequest;
import com.budgetix.category.dto.CategoryResponse;
import com.budgetix.category.service.CategoryService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Categories")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAll(uid(p))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@AuthenticationPrincipal UserDetails p,
                                                                @Valid @RequestBody CategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(categoryService.create(uid(p), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@AuthenticationPrincipal UserDetails p,
                                                                @PathVariable UUID id,
                                                                @Valid @RequestBody CategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.update(uid(p), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails p, @PathVariable UUID id) {
        categoryService.delete(uid(p), id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted"));
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<ApiResponse<Void>> addRule(@AuthenticationPrincipal UserDetails p,
                                                     @PathVariable UUID id,
                                                     @RequestBody Map<String, String> body) {
        categoryService.addRule(uid(p), id, body.get("keyword"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Rule added"));
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
