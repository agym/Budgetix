package com.budgetix.account.service;

import com.budgetix.account.dto.AccountRequest;
import com.budgetix.account.dto.AccountResponse;
import com.budgetix.account.entity.Account;
import com.budgetix.account.repository.AccountRepository;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.user.entity.User;
import com.budgetix.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    public List<AccountResponse> getAll(UUID userId) {
        return accountRepository.findByUserIdOrderByIsDefaultDescNameAsc(userId)
            .stream().map(AccountResponse::from).toList();
    }

    public AccountResponse getById(UUID userId, UUID id) {
        return AccountResponse.from(findOwned(userId, id));
    }

    @Transactional
    public AccountResponse create(UUID userId, AccountRequest req) {
        User user = userService.getEntity(userId);

        Account account = Account.builder()
            .user(user)
            .name(req.name())
            .type(req.type())
            .balance(req.initialBalance() != null ? req.initialBalance() : BigDecimal.ZERO)
            .currency(req.currency() != null ? req.currency() : "USD")
            .color(req.color())
            .icon(req.icon())
            .isDefault(req.isDefault())
            .build();

        if (req.isDefault()) clearDefaultFlag(userId);

        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID id, AccountRequest req) {
        Account account = findOwned(userId, id);

        if (req.name() != null) account.setName(req.name());
        if (req.type() != null) account.setType(req.type());
        if (req.color() != null) account.setColor(req.color());
        if (req.icon() != null) account.setIcon(req.icon());
        if (req.currency() != null) account.setCurrency(req.currency());
        if (req.isDefault() && !account.isDefault()) {
            clearDefaultFlag(userId);
            account.setDefault(true);
        }

        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Account account = findOwned(userId, id);
        if (accountRepository.countTransactions(id) > 0) {
            throw new AppException(ErrorCode.ACCOUNT_HAS_TRANSACTIONS);
        }
        accountRepository.delete(account);
    }

    public Account findOwned(UUID userId, UUID accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private void clearDefaultFlag(UUID userId) {
        accountRepository.findByUserIdOrderByIsDefaultDescNameAsc(userId)
            .forEach(a -> {
                if (a.isDefault()) {
                    a.setDefault(false);
                    accountRepository.save(a);
                }
            });
    }
}
