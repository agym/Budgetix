# Business Specification Document (BSD)
## Budgetix — Personal Finance Management Platform
**Version:** 1.0  
**Date:** May 2026  
**Status:** Active Development

---

## 1. Executive Summary

Budgetix is a personal finance management web application that enables individuals to track income and expenses, manage multiple bank accounts, set monthly budgets, define savings goals, and gain actionable insights into their spending behaviour — all from a single, secure dashboard.

The product targets financially aware individuals who want a self-hosted or privately deployed alternative to third-party money management tools, without sharing sensitive financial data with external aggregators.

---

## 2. Business Objectives

| # | Objective | Success Metric |
|---|---|---|
| 1 | Enable users to centralise all financial accounts | ≥ 3 accounts linked per active user |
| 2 | Reduce overspending via budget alerts | Budget alert open-rate ≥ 60% |
| 3 | Increase savings rate through goal tracking | ≥ 40% of users set at least one savings goal |
| 4 | Automate repetitive data entry | ≥ 70% of recurring transactions processed without manual input |
| 5 | Retain users through actionable intelligence | Insight dismissal rate < 30% |

---

## 3. Stakeholders

| Role | Responsibilities |
|---|---|
| End User | Register, manage finances, consume insights |
| System Administrator | Deploy, maintain, monitor infrastructure |
| Developer | Build and extend features |

---

## 4. Scope

### 4.1 In Scope

- User registration, email verification, login, password reset
- Multi-account financial tracking (cash, bank, credit card, savings, investment)
- Full transaction lifecycle: create, edit, delete, bulk delete, filter, search
- Receipt attachment per transaction (image / PDF upload)
- Monthly budget management with automated spend tracking and threshold alerts
- Savings goals with contribution history and deadline tracking
- Recurring transaction scheduling (daily → yearly)
- Dashboard with real-time KPIs and three chart types
- AI-style spending insights
- In-app notification centre
- Financial reports (category breakdown, trend analysis)
- Multi-language UI (English, French, Arabic with full RTL support)
- Session inactivity timeout with user warning

### 4.2 Out of Scope (v1.0)

- Real bank account integration / Open Banking APIs
- Mobile native applications
- Shared household / multi-user budgets
- Investment portfolio tracking
- Tax reporting

---

## 5. Functional Requirements

### 5.1 Authentication & Identity

| ID | Requirement |
|---|---|
| AUTH-01 | Users must register with name, email, and password |
| AUTH-02 | Email must be verified via a 6-digit OTP before dashboard access |
| AUTH-03 | Login returns a short-lived access token (15 min) and a refresh token (7 days) |
| AUTH-04 | Refresh token rotation must invalidate the previous token on use |
| AUTH-05 | Forgotten passwords must be reset via an email OTP code |
| AUTH-06 | Sessions must expire after 15 minutes of inactivity with a 2-minute warning |
| AUTH-07 | The system must support two-factor authentication via email OTP (enabled/disabled per user in Settings → Security) |

### 5.2 Account Management

| ID | Requirement |
|---|---|
| ACC-01 | A user may create unlimited accounts of types: CASH, BANK, CREDIT_CARD, SAVINGS, INVESTMENT |
| ACC-02 | Each account has a name, balance, currency, icon, and colour |
| ACC-03 | One account may be designated as default |
| ACC-04 | Deleting an account with linked transactions must be prevented (RESTRICT) |
| ACC-05 | Account balance must be recalculated automatically on every transaction create, update, or delete |

### 5.3 Transactions

| ID | Requirement |
|---|---|
| TX-01 | Transactions must be typed as INCOME or EXPENSE |
| TX-02 | Every transaction must be linked to an account |
| TX-03 | Category assignment may be manual or automatic via keyword rules |
| TX-04 | Transactions may carry free-text tags for ad-hoc grouping |
| TX-05 | Users may attach a receipt (image or PDF, max 5 MB) to any transaction |
| TX-06 | The transaction list must support filtering by date range, type, category, account, and free-text search |
| TX-07 | Paginated results must be returned (default 20 per page, sortable by date descending) |
| TX-08 | Bulk delete must support removing multiple transactions in one request |

### 5.4 Budgets

| ID | Requirement |
|---|---|
| BUD-01 | A budget is defined per category per month/year |
| BUD-02 | Only one budget per (user, category, year, month) combination is permitted |
| BUD-03 | Budget spent amount must update automatically when transactions are created, edited, or deleted |
| BUD-04 | An alert notification must fire when spent reaches 80% of the budget limit |
| BUD-05 | A second alert must fire when spent reaches 100% (budget exceeded) |
| BUD-06 | Budgets may optionally roll over unspent amounts to the following month |

### 5.5 Savings Goals

| ID | Requirement |
|---|---|
| GOAL-01 | A goal requires a name, target amount, and optional deadline |
| GOAL-02 | Manual contributions are recorded with an optional note |
| GOAL-03 | A contribution may be linked to a transaction |
| GOAL-04 | Goal status transitions: ACTIVE → COMPLETED or ACTIVE → PAUSED |
| GOAL-05 | Progress percentage must be computed and returned by the API |

### 5.6 Recurring Transactions

| ID | Requirement |
|---|---|
| REC-01 | Supported frequencies: DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY |
| REC-02 | The scheduler runs every hour and processes all transactions whose `next_run ≤ now` |
| REC-03 | Each processed run advances `next_run` to the next schedule date |
| REC-04 | A notification is sent to the user after each automatic processing |
| REC-05 | Users may pause (toggle) or delete recurring rules |

### 5.7 Dashboard

| ID | Requirement |
|---|---|
| DASH-01 | Overview must show total balance, monthly income, monthly expenses, and net savings |
| DASH-02 | Spending-by-category chart data must accept a configurable date range |
| DASH-03 | Income vs Expenses chart must support 1–12 trailing months |
| DASH-04 | Daily trend chart must show spend per day for a given month/year |

### 5.8 Insights

| ID | Requirement |
|---|---|
| INS-01 | Insights are categorised by type (e.g. spending anomaly, goal progress, budget warning) |
| INS-02 | Users may dismiss individual insights |
| INS-03 | Dismissed insights are excluded from the active feed |

### 5.9 Notifications

| ID | Requirement |
|---|---|
| NOT-01 | Notifications are stored in-database and surfaced via the topbar bell icon |
| NOT-02 | The unread count must be fetched on each app load |
| NOT-03 | Notification types: BUDGET_ALERT, GOAL_REACHED, RECURRING_TX, GENERAL |

### 5.10 Internationalisation

| ID | Requirement |
|---|---|
| I18N-01 | Default language is English |
| I18N-02 | French and Arabic must be fully translated |
| I18N-03 | Switching to Arabic must set `dir="rtl"` on the document root |
| I18N-04 | Language preference must persist across sessions via localStorage |

---

## 6. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | Passwords hashed with BCrypt (cost 12). JWT secrets ≥ 32 characters. No plain-text credentials stored. |
| Performance | API p95 latency < 300 ms on standard hardware. Frontend first-load < 3 s on broadband. |
| Availability | 99.5% uptime target for self-hosted deployments with Docker |
| Scalability | Stateless API — horizontally scalable behind a load balancer |
| Data integrity | All financial mutations (balance updates, budget recalculation) wrapped in database transactions |
| Accessibility | WCAG 2.1 AA — keyboard navigable, sufficient colour contrast, ARIA labels on interactive elements |
| Internationalisation | Full RTL layout support for Arabic |
| Audit | All DB writes timestamped with `created_at` / `updated_at` |

---

## 7. User Flows

### 7.1 New User Onboarding

```
Landing page → "Get started free"
  → Register (name, email, password)
    → Email OTP sent
      → Verify email (/auth/verify-email)
        → Redirected to Dashboard
          → Prompted to add first account
            → Add transactions
              → Set budgets & goals
```

### 7.2 Daily Use

```
Login → Dashboard overview
  → Review KPI cards (balance, income, expenses)
  → Check budget progress bars
  → Add new transaction (manual or via recurring auto-post)
  → Review notifications (budget alert / recurring processed)
```

### 7.3 Password Recovery

```
Login page → "Forgot password"
  → Enter email
    → OTP sent to inbox
      → Enter OTP + new password on reset page
        → Redirected to login
```

### 7.4 Budget Alert Flow

```
User creates budget (e.g. $500 Dining / month)
  → Transactions posted to Dining category
    → BudgetUpdateService recalculates spent
      → At 80%: notification "You've used 80% of your Dining budget"
      → At 100%: notification "Dining budget exceeded"
        → User adjusts spending or increases budget limit
```

### 7.5 Recurring Transaction Flow

```
User defines: "Netflix $15.99 EXPENSE MONTHLY from 2026-01-01"
  → Scheduler runs every hour
    → Finds next_run ≤ now
      → Creates transaction automatically
      → Updates account balance
      → Updates relevant budget spent
      → Sends notification to user
      → Advances next_run by 1 month
```

---

## 8. Assumptions & Constraints

- The platform is single-tenant per deployment (one user base per instance).
- Currency conversion is not performed; amounts are stored as entered.
- Email delivery depends on a configured SMTP server (MailHog in development).
- File receipts are stored on the local filesystem; cloud storage is a future enhancement.
- The "AI insights" in v1.0 are rule-based pattern analysis, not a machine-learning model.
