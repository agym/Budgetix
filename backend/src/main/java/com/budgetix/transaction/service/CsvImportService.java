package com.budgetix.transaction.service;

import com.budgetix.account.entity.Account;
import com.budgetix.account.repository.AccountRepository;
import com.budgetix.common.enums.TransactionType;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.transaction.dto.TransactionResponse;
import com.budgetix.transaction.entity.Transaction;
import com.budgetix.transaction.repository.TransactionRepository;
import com.budgetix.user.entity.User;
import com.budgetix.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserService userService;
    private final AutoCategorizationService autoCategorizationService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public List<TransactionResponse> importCsv(UUID userId, UUID accountId, MultipartFile file) {
        if (!file.getContentType().contains("csv") && !file.getOriginalFilename().endsWith(".csv")) {
            throw new AppException(ErrorCode.INVALID_CSV_FORMAT);
        }

        User user = userService.getEntity(userId);
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        List<Transaction> imported = new ArrayList<>();

        try (CSVParser parser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .build()
            .parse(new InputStreamReader(file.getInputStream()))) {

            for (var record : parser) {
                try {
                    String amountStr = record.get("amount");
                    String typeStr = record.isMapped("type") ? record.get("type") : "EXPENSE";
                    String description = record.isMapped("description") ? record.get("description") : "";
                    String dateStr = record.get("date");

                    BigDecimal amount = new BigDecimal(amountStr.replaceAll("[^0-9.]", ""));
                    TransactionType type = TransactionType.valueOf(typeStr.toUpperCase());
                    LocalDateTime date = parseDate(dateStr);

                    Transaction tx = Transaction.builder()
                        .user(user)
                        .account(account)
                        .amount(amount)
                        .type(type)
                        .description(description)
                        .date(date)
                        .category(autoCategorizationService.categorize(userId, description).orElse(null))
                        .build();

                    imported.add(tx);
                } catch (Exception e) {
                    log.warn("Skipping CSV row {}: {}", parser.getRecordNumber(), e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_CSV_FORMAT);
        }

        List<Transaction> saved = transactionRepository.saveAll(imported);
        return saved.stream().map(TransactionResponse::from).toList();
    }

    private LocalDateTime parseDate(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            return LocalDateTime.parse(dateStr + " 00:00:00", DATE_FORMAT);
        }
    }
}
