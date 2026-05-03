package com.budgetix.transaction.service;

import com.budgetix.budget.entity.Budget;
import com.budgetix.budget.repository.BudgetRepository;
import com.budgetix.category.entity.Category;
import com.budgetix.common.enums.TransactionType;
import com.budgetix.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetUpdateService {

    private final BudgetRepository budgetRepository;

    /** Called when a new EXPENSE transaction is created. */
    public void updateOnNewTransaction(Transaction tx) {
        if (tx.getType() != TransactionType.EXPENSE) return;
        applyToBudgets(tx.getUser().getId(),
                tx.getCategory() != null ? tx.getCategory().getId() : null,
                tx.getDate().getMonthValue(), tx.getDate().getYear(),
                tx.getAmount());
    }

    /**
     * Called when a transaction is edited.
     * Reverses the old contribution then applies the new one.
     *
     * @param oldAmount   amount before edit
     * @param oldType     type before edit
     * @param oldCategory category before edit (may be null)
     * @param oldDate     date before edit (determines which month's budget to reverse)
     * @param newTx       the saved transaction after edit
     */
    public void updateOnTransactionEdit(BigDecimal oldAmount, TransactionType oldType,
                                        Category oldCategory, LocalDateTime oldDate,
                                        Transaction newTx) {
        // Reverse old contribution
        if (oldType == TransactionType.EXPENSE) {
            applyToBudgets(newTx.getUser().getId(),
                    oldCategory != null ? oldCategory.getId() : null,
                    oldDate.getMonthValue(), oldDate.getYear(),
                    oldAmount.negate());
        }
        // Apply new contribution
        if (newTx.getType() == TransactionType.EXPENSE) {
            applyToBudgets(newTx.getUser().getId(),
                    newTx.getCategory() != null ? newTx.getCategory().getId() : null,
                    newTx.getDate().getMonthValue(), newTx.getDate().getYear(),
                    newTx.getAmount());
        }
    }

    /** Called when a transaction is deleted. Reverses its budget contribution. */
    public void updateOnTransactionDelete(Transaction tx) {
        if (tx.getType() != TransactionType.EXPENSE) return;
        applyToBudgets(tx.getUser().getId(),
                tx.getCategory() != null ? tx.getCategory().getId() : null,
                tx.getDate().getMonthValue(), tx.getDate().getYear(),
                tx.getAmount().negate());
    }

    // -------------------------------------------------------------------------

    private void applyToBudgets(UUID userId, UUID categoryId, int month, int year, BigDecimal delta) {
        if (categoryId != null) {
            budgetRepository.findByUserAndCategoryAndPeriod(userId, categoryId, year, month)
                    .ifPresent(b -> adjustSpent(b, delta));
        }
        budgetRepository.findGlobalBudget(userId, year, month)
                .ifPresent(b -> adjustSpent(b, delta));
    }

    private void adjustSpent(Budget budget, BigDecimal delta) {
        BigDecimal newSpent = budget.getSpent().add(delta).max(BigDecimal.ZERO);
        budget.setSpent(newSpent);
        resetAlertsBelowThreshold(budget);
        budgetRepository.save(budget);
        log.debug("Budget {} spent adjusted by {} → {}", budget.getId(), delta, newSpent);
    }

    /**
     * Re-arms an alert when spending has recovered to 90 % of the threshold.
     * This allows the alert to fire again if spending crosses back up later.
     */
    private void resetAlertsBelowThreshold(Budget budget) {
        double usagePct = budget.getUsagePercent();
        budget.getAlerts().forEach(alert -> {
            double thresholdPct = alert.getThreshold().doubleValue() * 100;
            if (alert.isTriggered() && usagePct < thresholdPct * 0.9) {
                alert.setTriggered(false);
                log.debug("Budget alert re-armed for budget {} at threshold {}%", budget.getId(), thresholdPct);
            }
        });
    }
}
