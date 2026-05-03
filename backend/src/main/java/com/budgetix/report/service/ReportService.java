package com.budgetix.report.service;

import com.budgetix.budget.entity.Budget;
import com.budgetix.budget.repository.BudgetRepository;
import com.budgetix.category.entity.Category;
import com.budgetix.category.repository.CategoryRepository;
import com.budgetix.common.enums.TransactionType;
import com.budgetix.transaction.entity.Transaction;
import com.budgetix.transaction.repository.TransactionRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository    categoryRepository;
    private final BudgetRepository      budgetRepository;

    public Map<String, Object> getMonthlyReport(UUID userId, int month, int year) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end   = start.plusMonths(1);

        // Core aggregates
        BigDecimal income   = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.INCOME,  start, end);
        BigDecimal expenses = transactionRepository.sumByTypeAndPeriod(userId, TransactionType.EXPENSE, start, end);
        BigDecimal netSavings = income.subtract(expenses);

        // savingsRate = (income - expenses) / income × 100  (guard against zero income)
        double savingsRate = income.compareTo(BigDecimal.ZERO) > 0
            ? netSavings.divide(income, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        long txCount = transactionRepository.countByPeriod(userId, start, end);

        // Category breakdown: [categoryId, sum, count]
        List<Object[]> rawBreakdown = transactionRepository.groupByCategoryWithCount(userId, start, end);

        // Pre-fetch categories for name/color lookup
        Map<UUID, Category> catMap = new HashMap<>();
        for (Object[] row : rawBreakdown) {
            UUID catId = (UUID) row[0];
            if (catId != null) {
                categoryRepository.findById(catId).ifPresent(c -> catMap.put(catId, c));
            }
        }

        // Budget amounts for variance calculation
        List<Budget> budgets = budgetRepository.findByUserAndPeriod(userId, year, month);
        Map<UUID, BigDecimal> budgetAmountByCat = new HashMap<>();
        for (Budget b : budgets) {
            if (b.getCategory() != null) {
                budgetAmountByCat.put(b.getCategory().getId(), b.getAmount());
            }
        }

        List<Map<String, Object>> categoryBreakdown = new ArrayList<>();
        for (Object[] row : rawBreakdown) {
            UUID catId       = (UUID) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            long count        = ((Number) row[2]).longValue();

            // percentage of total expenses
            double pct = expenses.compareTo(BigDecimal.ZERO) > 0
                ? amount.divide(expenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

            Category cat = catId != null ? catMap.get(catId) : null;
            BigDecimal budgeted = catId != null ? budgetAmountByCat.get(catId) : null;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("categoryId",        catId);
            entry.put("categoryName",      cat != null ? cat.getName() : "Uncategorized");
            entry.put("categoryColor",     cat != null ? cat.getColor() : null);
            entry.put("amount",            amount);
            entry.put("percentage",        pct);
            entry.put("transactionCount",  count);
            if (budgeted != null) {
                entry.put("budgetAmount",  budgeted);
                // variance: positive = under budget, negative = over budget
                entry.put("budgetVariance", budgeted.subtract(amount));
                double utilisation = amount.divide(budgeted, 4, RoundingMode.HALF_UP)
                                           .multiply(BigDecimal.valueOf(100)).doubleValue();
                entry.put("budgetUtilisation", utilisation);
            }
            categoryBreakdown.add(entry);
        }

        // Global budget overview (if any)
        Optional<Budget> globalBudget = budgetRepository.findGlobalBudget(userId, year, month);
        Map<String, Object> globalBudgetSummary = globalBudget.map(b -> {
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("budgetAmount",    b.getAmount());
            g.put("spent",           b.getSpent());
            g.put("remaining",       b.getRemaining());
            g.put("usagePercent",    b.getUsagePercent());
            return g;
        }).orElse(null);

        // Top expense category
        String topExpenseCategory = categoryBreakdown.isEmpty() ? null
            : (String) categoryBreakdown.get(0).get("categoryName");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("month",              month);
        result.put("year",               year);
        result.put("totalIncome",        income);
        result.put("totalExpenses",      expenses);
        result.put("netSavings",         netSavings);
        result.put("savingsRate",        savingsRate);
        result.put("transactionCount",   txCount);
        result.put("topExpenseCategory", topExpenseCategory);
        result.put("categoryBreakdown",  categoryBreakdown);
        if (globalBudgetSummary != null) result.put("globalBudget", globalBudgetSummary);
        return result;
    }

    // -------------------------------------------------------------------------
    // CSV export
    // -------------------------------------------------------------------------

    public byte[] exportCsv(UUID userId, int month, int year) throws IOException {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end   = start.plusMonths(1);
        List<Transaction> transactions = transactionRepository.findByUserAndPeriod(userId, start, end);

        // Summary row values
        BigDecimal totalIncome   = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (Transaction tx : transactions) {
            if (tx.getType() == TransactionType.INCOME)  totalIncome   = totalIncome.add(tx.getAmount());
            if (tx.getType() == TransactionType.EXPENSE) totalExpenses = totalExpenses.add(tx.getAmount());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out),
                CSVFormat.DEFAULT.builder()
                    .setHeader("Date", "Description", "Type", "Amount", "Category", "Account", "Notes", "Tags")
                    .build())) {

            for (Transaction tx : transactions) {
                printer.printRecord(
                    tx.getDate().format(fmt),
                    tx.getDescription(),
                    tx.getType(),
                    tx.getAmount(),
                    tx.getCategory() != null ? tx.getCategory().getName() : "",
                    tx.getAccount()  != null ? tx.getAccount().getName()  : "",
                    tx.getNotes()    != null ? tx.getNotes() : "",
                    tx.getTags()     != null ? String.join("|", tx.getTags()) : ""
                );
            }

            // Summary footer
            printer.println();
            printer.printRecord("SUMMARY", "", "", "", "", "", "", "");
            printer.printRecord("Total Income",   "", "", totalIncome,   "", "", "", "");
            printer.printRecord("Total Expenses",  "", "", totalExpenses, "", "", "", "");
            printer.printRecord("Net Savings",    "", "", totalIncome.subtract(totalExpenses), "", "", "", "");
        }
        return out.toByteArray();
    }

    // -------------------------------------------------------------------------
    // PDF export
    // -------------------------------------------------------------------------

    public byte[] exportPdf(UUID userId, int month, int year) throws Exception {
        Map<String, Object> report = getMonthlyReport(userId, month, year);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Fonts
        Font titleFont   = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD,   new BaseColor(30, 27, 75));
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   new BaseColor(30, 27, 75));
        Font labelFont   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   BaseColor.DARK_GRAY);
        Font valueFont   = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        Font greenFont   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(21, 128, 61));
        Font redFont     = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(220, 38, 38));

        // Title
        String monthName = LocalDateTime.of(year, month, 1, 0, 0)
            .getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
        Paragraph title = new Paragraph("Budgetix – Monthly Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        Paragraph subtitle = new Paragraph(monthName + " " + year, labelFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        doc.add(subtitle);

        // Summary table
        doc.add(new Paragraph("Financial Summary", headingFont));
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(100);
        summary.setSpacingBefore(8);
        summary.setSpacingAfter(16);
        addSummaryRow(summary, "Total Income",   fmt((BigDecimal) report.get("totalIncome")),   greenFont, labelFont);
        addSummaryRow(summary, "Total Expenses", fmt((BigDecimal) report.get("totalExpenses")), redFont,   labelFont);
        addSummaryRow(summary, "Net Savings",    fmt((BigDecimal) report.get("netSavings")),    greenFont, labelFont);
        addSummaryRow(summary, "Savings Rate",   String.format("%.1f%%", report.get("savingsRate")), valueFont, labelFont);
        addSummaryRow(summary, "Transactions",   String.valueOf(report.get("transactionCount")), valueFont, labelFont);
        if (report.get("topExpenseCategory") != null) {
            addSummaryRow(summary, "Top Expense Category", (String) report.get("topExpenseCategory"), valueFont, labelFont);
        }
        doc.add(summary);

        // Category breakdown table
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) report.get("categoryBreakdown");
        if (breakdown != null && !breakdown.isEmpty()) {
            doc.add(new Paragraph("Spending by Category", headingFont));
            PdfPTable catTable = new PdfPTable(4);
            catTable.setWidthPercentage(100);
            catTable.setWidths(new float[]{3f, 2f, 1.5f, 1.5f});
            catTable.setSpacingBefore(8);
            catTable.setSpacingAfter(16);

            // Header
            for (String h : new String[]{"Category", "Amount", "% of Expenses", "Transactions"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, labelFont));
                cell.setBackgroundColor(new BaseColor(241, 245, 249));
                cell.setPadding(6);
                catTable.addCell(cell);
            }

            for (Map<String, Object> row : breakdown) {
                catTable.addCell(cell((String) row.get("categoryName"), valueFont));
                catTable.addCell(cell(fmt((BigDecimal) row.get("amount")), valueFont));
                catTable.addCell(cell(String.format("%.1f%%", row.get("percentage")), valueFont));
                catTable.addCell(cell(String.valueOf(row.get("transactionCount")), valueFont));
            }
            doc.add(catTable);
        }

        // Budget vs Actual section
        @SuppressWarnings("unchecked")
        Map<String, Object> globalBudget = (Map<String, Object>) report.get("globalBudget");
        if (globalBudget != null) {
            doc.add(new Paragraph("Budget Overview", headingFont));
            PdfPTable budgetTable = new PdfPTable(2);
            budgetTable.setWidthPercentage(60);
            budgetTable.setSpacingBefore(8);
            addSummaryRow(budgetTable, "Budget",     fmt((BigDecimal) globalBudget.get("budgetAmount")), valueFont, labelFont);
            addSummaryRow(budgetTable, "Spent",      fmt((BigDecimal) globalBudget.get("spent")),        redFont,   labelFont);
            addSummaryRow(budgetTable, "Remaining",  fmt((BigDecimal) globalBudget.get("remaining")),    greenFont, labelFont);
            addSummaryRow(budgetTable, "Utilisation", String.format("%.1f%%", globalBudget.get("usagePercent")), valueFont, labelFont);
            doc.add(budgetTable);
        }

        // Footer
        Paragraph footer = new Paragraph(
            "Generated by Budgetix on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(20);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font valueFont, Font labelFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setBorder(Rectangle.BOTTOM);
        lCell.setPadding(5);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value, valueFont));
        vCell.setBorder(Rectangle.BOTTOM);
        vCell.setPadding(5);
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(vCell);
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(5);
        c.setBorderColor(new BaseColor(226, 232, 240));
        return c;
    }

    private String fmt(BigDecimal val) {
        if (val == null) return "$0.00";
        return String.format("$%,.2f", val);
    }
}
