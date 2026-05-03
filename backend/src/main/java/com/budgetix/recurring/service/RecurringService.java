package com.budgetix.recurring.service;

import com.budgetix.account.repository.AccountRepository;
import com.budgetix.category.repository.CategoryRepository;
import com.budgetix.common.enums.Frequency;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.recurring.dto.RecurringRequest;
import com.budgetix.recurring.dto.RecurringResponse;
import com.budgetix.recurring.entity.RecurringTransaction;
import com.budgetix.recurring.repository.RecurringTransactionRepository;
import com.budgetix.user.entity.User;
import com.budgetix.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringService {

    private final RecurringTransactionRepository recurringRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public List<RecurringResponse> getAll(UUID userId) {
        return recurringRepository.findByUserIdOrderByNextRunAsc(userId)
            .stream().map(RecurringResponse::from).toList();
    }

    @Transactional
    public RecurringResponse create(UUID userId, RecurringRequest req) {
        User user = userService.getEntity(userId);
        var account = accountRepository.findByIdAndUserId(req.accountId(), userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        var category = req.categoryId() != null
            ? categoryRepository.findById(req.categoryId()).orElse(null) : null;

        RecurringTransaction rt = RecurringTransaction.builder()
            .user(user)
            .account(account)
            .category(category)
            .amount(req.amount())
            .type(req.type())
            .description(req.description())
            .frequency(req.frequency())
            .startDate(req.startDate())
            .endDate(req.endDate())
            .nextRun(req.startDate())
            .build();

        return RecurringResponse.from(recurringRepository.save(rt));
    }

    @Transactional
    public void toggle(UUID userId, UUID id) {
        RecurringTransaction rt = findOwned(userId, id);
        rt.setActive(!rt.isActive());
        recurringRepository.save(rt);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        recurringRepository.delete(findOwned(userId, id));
    }

    private RecurringTransaction findOwned(UUID userId, UUID id) {
        return recurringRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AppException(ErrorCode.RECURRING_NOT_FOUND));
    }
}
