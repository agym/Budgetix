package com.budgetix.transaction.service;

import com.budgetix.account.entity.Account;
import com.budgetix.account.repository.AccountRepository;
import com.budgetix.category.entity.Category;
import com.budgetix.category.repository.CategoryRepository;
import com.budgetix.common.dto.PageResponse;
import com.budgetix.common.enums.TransactionType;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.transaction.dto.TransactionFilterRequest;
import com.budgetix.transaction.dto.TransactionRequest;
import com.budgetix.transaction.dto.TransactionResponse;
import com.budgetix.transaction.dto.TransferRequest;
import com.budgetix.transaction.entity.Transaction;
import com.budgetix.transaction.repository.TransactionRepository;
import com.budgetix.user.entity.User;
import com.budgetix.user.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final AutoCategorizationService autoCategorizationService;
    private final BudgetUpdateService budgetUpdateService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public PageResponse<TransactionResponse> getAll(UUID userId, TransactionFilterRequest filter) {
        Specification<Transaction> spec = buildSpec(userId, filter);
        PageRequest pageRequest = PageRequest.of(filter.page(), filter.size(),
            Sort.by(Sort.Direction.DESC, "date"));
        return PageResponse.from(
            transactionRepository.findAll(spec, pageRequest).map(TransactionResponse::from)
        );
    }

    public TransactionResponse getById(UUID userId, UUID id) {
        return TransactionResponse.from(findOwned(userId, id));
    }

    @Transactional
    public TransactionResponse create(UUID userId, TransactionRequest req) {
        User user = userService.getEntity(userId);
        Account account = accountRepository.findByIdAndUserId(req.accountId(), userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        Category category = resolveCategory(userId, req.categoryId(), req.description());

        Transaction tx = Transaction.builder()
            .user(user)
            .account(account)
            .category(category)
            .amount(req.amount())
            .type(req.type())
            .description(req.description())
            .notes(req.notes())
            .date(req.date())
            .tags(req.tags() != null ? req.tags() : new java.util.HashSet<>())
            .build();

        Transaction saved = transactionRepository.save(tx);
        updateAccountBalance(account, req.amount(), req.type(), true);
        budgetUpdateService.updateOnNewTransaction(saved);

        return TransactionResponse.from(saved);
    }

    @Transactional
    public TransactionResponse update(UUID userId, UUID id, TransactionRequest req) {
        Transaction tx = findOwned(userId, id);

        // Snapshot old state before any mutations (needed for budget reversal)
        BigDecimal oldAmount = tx.getAmount();
        TransactionType oldType = tx.getType();
        Category oldCategory = tx.getCategory();
        java.time.LocalDateTime oldDate = tx.getDate();

        if (req.accountId() != null && !req.accountId().equals(tx.getAccount().getId())) {
            updateAccountBalance(tx.getAccount(), oldAmount, oldType, false);
            Account newAccount = accountRepository.findByIdAndUserId(req.accountId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
            tx.setAccount(newAccount);
            updateAccountBalance(newAccount, req.amount(), req.type(), true);
        } else {
            updateAccountBalance(tx.getAccount(), oldAmount, oldType, false);
            updateAccountBalance(tx.getAccount(), req.amount(), req.type(), true);
        }

        if (req.categoryId() != null) {
            Category cat = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            tx.setCategory(cat);
        }

        tx.setAmount(req.amount());
        tx.setType(req.type());
        if (req.description() != null) tx.setDescription(req.description());
        if (req.notes() != null) tx.setNotes(req.notes());
        if (req.date() != null) tx.setDate(req.date());
        if (req.tags() != null) tx.setTags(req.tags());

        Transaction saved = transactionRepository.save(tx);
        budgetUpdateService.updateOnTransactionEdit(oldAmount, oldType, oldCategory, oldDate, saved);
        return TransactionResponse.from(saved);
    }

    @Transactional
    public List<TransactionResponse> transfer(UUID userId, TransferRequest req) {
        if (req.fromAccountId().equals(req.toAccountId()))
            throw new AppException(ErrorCode.INVALID_TRANSFER);

        User user = userService.getEntity(userId);
        Account from = accountRepository.findByIdAndUserId(req.fromAccountId(), userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account to = accountRepository.findByIdAndUserId(req.toAccountId(), userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        UUID pairId = UUID.randomUUID();
        String desc = req.description() != null && !req.description().isBlank()
            ? req.description() : null;

        Transaction debit = Transaction.builder()
            .user(user).account(from)
            .amount(req.amount()).type(TransactionType.TRANSFER)
            .description(desc != null ? desc : "Transfer to " + to.getName())
            .date(req.date()).transferPairId(pairId).transferCredit(false)
            .build();

        Transaction credit = Transaction.builder()
            .user(user).account(to)
            .amount(req.amount()).type(TransactionType.TRANSFER)
            .description(desc != null ? desc : "Transfer from " + from.getName())
            .date(req.date()).transferPairId(pairId).transferCredit(true)
            .build();

        from.setBalance(from.getBalance().subtract(req.amount()));
        to.setBalance(to.getBalance().add(req.amount()));
        accountRepository.save(from);
        accountRepository.save(to);

        return List.of(
            TransactionResponse.from(transactionRepository.save(debit)),
            TransactionResponse.from(transactionRepository.save(credit))
        );
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Transaction tx = findOwned(userId, id);

        if (tx.getTransferPairId() != null) {
            // Reverse this leg's balance effect
            if (tx.isTransferCredit()) {
                tx.getAccount().setBalance(tx.getAccount().getBalance().subtract(tx.getAmount()));
            } else {
                tx.getAccount().setBalance(tx.getAccount().getBalance().add(tx.getAmount()));
            }
            accountRepository.save(tx.getAccount());
            // Delete paired leg and reverse its balance
            transactionRepository.findByTransferPairIdAndIdNot(tx.getTransferPairId(), id)
                .ifPresent(pair -> {
                    if (pair.isTransferCredit()) {
                        pair.getAccount().setBalance(pair.getAccount().getBalance().subtract(pair.getAmount()));
                    } else {
                        pair.getAccount().setBalance(pair.getAccount().getBalance().add(pair.getAmount()));
                    }
                    accountRepository.save(pair.getAccount());
                    transactionRepository.delete(pair);
                });
            transactionRepository.delete(tx);
            return;
        }

        updateAccountBalance(tx.getAccount(), tx.getAmount(), tx.getType(), false);
        budgetUpdateService.updateOnTransactionDelete(tx);
        transactionRepository.delete(tx);
    }

    @Transactional
    public void bulkDelete(UUID userId, List<UUID> ids) {
        ids.forEach(id -> delete(userId, id));
    }

    @Transactional
    public String uploadReceipt(UUID userId, UUID txId, MultipartFile file) throws IOException {
        if (!file.getContentType().startsWith("image/") && !file.getContentType().equals("application/pdf")) {
            throw new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        Transaction tx = findOwned(userId, txId);

        Path uploadPath = Paths.get(uploadDir, "receipts", userId.toString());
        Files.createDirectories(uploadPath);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        String url = "/uploads/receipts/" + userId + "/" + filename;
        tx.setReceipt(url);
        transactionRepository.save(tx);

        return url;
    }

    private Category resolveCategory(UUID userId, UUID categoryId, String description) {
        if (categoryId != null) {
            return categoryRepository.findById(categoryId).orElse(null);
        }
        return autoCategorizationService.categorize(userId, description).orElse(null);
    }

    private void updateAccountBalance(Account account, BigDecimal amount, TransactionType type, boolean add) {
        BigDecimal delta = type == TransactionType.INCOME ? amount : amount.negate();
        if (!add) delta = delta.negate();
        account.setBalance(account.getBalance().add(delta));
        accountRepository.save(account);
    }

    private Transaction findOwned(UUID userId, UUID id) {
        return transactionRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    private Specification<Transaction> buildSpec(UUID userId, TransactionFilterRequest f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (f.startDate() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("date"), f.startDate()));
            if (f.endDate() != null) predicates.add(cb.lessThanOrEqualTo(root.get("date"), f.endDate()));
            if (f.type() != null) predicates.add(cb.equal(root.get("type"), f.type()));
            if (f.categoryId() != null) predicates.add(cb.equal(root.get("category").get("id"), f.categoryId()));
            if (f.accountId() != null) predicates.add(cb.equal(root.get("account").get("id"), f.accountId()));
            if (f.search() != null && !f.search().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + f.search().toLowerCase() + "%"));
            }
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
