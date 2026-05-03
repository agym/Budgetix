package com.budgetix.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    private String currency = "USD";

    @Column(name = "monthly_income")
    private BigDecimal monthlyIncome;

    @Column(name = "financial_goal_type")
    private String financialGoalType;

    @Column(name = "notify_budget_alerts")
    @Builder.Default
    private boolean notifyBudgetAlerts = true;

    @Column(name = "notify_goal_reminders")
    @Builder.Default
    private boolean notifyGoalReminders = true;

    @Column(name = "notify_weekly_summary")
    @Builder.Default
    private boolean notifyWeeklySummary = true;

    @Builder.Default
    private String timezone = "UTC";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
