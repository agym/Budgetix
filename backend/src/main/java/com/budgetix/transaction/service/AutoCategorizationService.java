package com.budgetix.transaction.service;

import com.budgetix.category.entity.AutoCategorizationRule;
import com.budgetix.category.entity.Category;
import com.budgetix.category.repository.AutoCategorizationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutoCategorizationService {

    private final AutoCategorizationRuleRepository ruleRepository;

    public Optional<Category> categorize(UUID userId, String description) {
        if (description == null || description.isBlank()) return Optional.empty();

        String lower = description.toLowerCase();
        List<AutoCategorizationRule> rules = ruleRepository.findAllForUser(userId);

        return rules.stream()
            .filter(r -> lower.contains(r.getKeyword()))
            .map(AutoCategorizationRule::getCategory)
            .findFirst();
    }
}
