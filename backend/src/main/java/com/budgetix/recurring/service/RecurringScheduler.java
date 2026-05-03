package com.budgetix.recurring.service;

import com.budgetix.account.entity.Account;
import com.budgetix.account.repository.AccountRepository;
import com.budgetix.common.enums.Frequency;
import com.budgetix.common.enums.NotificationType;
import com.budgetix.common.enums.TransactionType;
import com.budgetix.notification.service.NotificationService;
import com.budgetix.recurring.entity.RecurringTransaction;
import com.budgetix.recurring.repository.RecurringTransactionRepository;
import com.budgetix.transaction.entity.Transaction;
import com.budgetix.transaction.repository.TransactionRepository;
import com.budgetix.transaction.service.BudgetUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringScheduler {

    private final RecurringTransactionRepository recurringRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BudgetUpdateService budgetUpdateService;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 3600000) // every hour
    @Transactional
    public void processRecurring() {
        LocalDateTime now = LocalDateTime.now();
        List<RecurringTransaction> due = recurringRepository.findDueTransactions(now);

        log.info("Processing {} due recurring transactions", due.size());

        for (RecurringTransaction rt : due) {
            try {
                Transaction tx = Transaction.builder()
                    .user(rt.getUser())
                    .account(rt.getAccount())
                    .category(rt.getCategory())
                    .amount(rt.getAmount())
                    .type(rt.getType())
                    .description(rt.getDescription())
                    .date(now)
                    .recurring(true)
                    .recurringId(rt.getId())
                    .build();

                Transaction saved = transactionRepository.save(tx);

                // Update account balance: income increases, expense decreases
                Account account = rt.getAccount();
                BigDecimal delta = rt.getType() == TransactionType.INCOME
                        ? rt.getAmount()
                        : rt.getAmount().negate();
                account.setBalance(account.getBalance().add(delta));
                accountRepository.save(account);

                // Update budget spent tracking
                budgetUpdateService.updateOnNewTransaction(saved);

                rt.setLastRun(now);
                rt.setNextRun(computeNextRun(rt.getFrequency(), now));
                recurringRepository.save(rt);

                notificationService.send(rt.getUser(), NotificationType.RECURRING_TX,
                    "Recurring Transaction",
                    String.format("%s – %s %.2f processed", rt.getDescription(), rt.getType(), rt.getAmount()),
                    Map.of("recurringId", rt.getId().toString()));

            } catch (Exception e) {
                log.error("Failed to process recurring transaction {}: {}", rt.getId(), e.getMessage());
            }
        }
    }

    private LocalDateTime computeNextRun(Frequency frequency, LocalDateTime from) {
        return switch (frequency) {
            case DAILY     -> from.plusDays(1);
            case WEEKLY    -> from.plusWeeks(1);
            case BIWEEKLY  -> from.plusWeeks(2);
            case MONTHLY   -> from.plusMonths(1);
            case QUARTERLY -> from.plusMonths(3);
            case YEARLY    -> from.plusYears(1);
        };
    }
}
