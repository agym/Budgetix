package com.budgetix.report.controller;

import com.budgetix.common.dto.ApiResponse;
import com.budgetix.report.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Reports")
@SecurityRequirement(name = "Bearer Auth")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> monthly(
            @AuthenticationPrincipal UserDetails p,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getMonthlyReport(uid(p), month, year)));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal UserDetails p,
            @RequestParam int month,
            @RequestParam int year) throws Exception {

        byte[] csv = reportService.exportCsv(uid(p), month, year);
        String filename = String.format("budgetix-report-%d-%02d.csv", year, month);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal UserDetails p,
            @RequestParam int month,
            @RequestParam int year) throws Exception {

        byte[] pdf = reportService.exportPdf(uid(p), month, year);
        String filename = String.format("budgetix-report-%d-%02d.pdf", year, month);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }
}
