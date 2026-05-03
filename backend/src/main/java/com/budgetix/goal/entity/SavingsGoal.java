package com.budgetix.goal.entity;

import com.budgetix.common.enums.GoalStatus;
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
@Table(name = "savings_goals")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_amount", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "current_amount")
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    private LocalDateTime deadline;
    private String icon;
    private String color;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL)
    @Builder.Default
    private List<GoalContribution> contributions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public double getProgressPercent() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) return 0;
        return currentAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}
