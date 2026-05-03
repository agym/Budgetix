package com.budgetix.budget.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "budget_alerts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(nullable = false)
    private BigDecimal threshold;

    @Builder.Default
    private boolean triggered = false;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
