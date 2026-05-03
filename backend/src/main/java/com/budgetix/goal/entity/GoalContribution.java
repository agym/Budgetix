package com.budgetix.goal.entity;

import com.budgetix.transaction.entity.Transaction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "goal_contributions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GoalContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private SavingsGoal goal;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", unique = true)
    private Transaction transaction;

    @Column(nullable = false)
    private BigDecimal amount;

    private String note;

    @Builder.Default
    private LocalDateTime date = LocalDateTime.now();
}
