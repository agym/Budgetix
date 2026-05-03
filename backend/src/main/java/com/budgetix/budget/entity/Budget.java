package com.budgetix.budget.entity;

import com.budgetix.category.entity.Category;
import com.budgetix.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "budgets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Builder.Default
    private BigDecimal spent = BigDecimal.ZERO;

    @Builder.Default
    private String period = "MONTHLY";

    private Integer month;
    private Integer year;

    @Builder.Default
    private boolean rollover = false;

    @Column(name = "rolled_amount")
    @Builder.Default
    private BigDecimal rolledAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BudgetAlert> alerts = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public double getUsagePercent() {
        if (amount.compareTo(BigDecimal.ZERO) == 0) return 0;
        return spent.divide(amount, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    public BigDecimal getRemaining() {
        return amount.add(rolledAmount).subtract(spent);
    }
}
