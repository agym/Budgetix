# Budgetix — Full Feature Documentation

> **Stack:** Spring Boot 3 (Java) · Angular 21 · PostgreSQL · PrimeNG · ngx-echarts · ngx-translate

---

## Table of Contents

1. [Authentication & Security](#1-authentication--security)
2. [Landing Page](#2-landing-page)
3. [Dashboard](#3-dashboard)
4. [Transactions](#4-transactions)
5. [Accounts](#5-accounts)
6. [Budgets](#6-budgets)
7. [Savings Goals](#7-savings-goals)
8. [Reports](#8-reports)
9. [Insights](#9-insights)
10. [Settings](#10-settings)
11. [Navigation & Shell](#11-navigation--shell)
12. [Internationalisation](#12-internationalisation)
13. [Categories & Auto-Categorization](#13-categories--auto-categorization)
14. [Recurring Transactions](#14-recurring-transactions)
15. [Backend API](#15-backend-api)
16. [Data Models](#16-data-models)
17. [Scheduled Jobs](#17-scheduled-jobs)

---

## 1. Authentication & Security

### Flows

| Flow | Route | Description |
|------|-------|-------------|
| Register | `/auth/register` | Name, email, password, confirm password. Sends a 6-digit OTP to email; redirects directly to the verify-email page. |
| Email Verification | `/auth/verify-email?email=` | Enter 6-digit OTP. Resend link available with a 60-second cooldown; rate-limited to 3 resends per 24 hours on the backend. Displays inline success/error feedback and "already verified" detection. |
| Login | `/auth/login` | Email + password. If 2FA enabled, a second step asks for a 6-digit code. |
| Forgot Password | `/auth/forgot-password` | Enter email → OTP sent. |
| Reset Password | `/auth/reset-password?email=` | Enter OTP + new password + confirm. |

### Token Strategy
- **Access token** — Short-lived JWT, sent as `Authorization: Bearer`.
- **Refresh token** — Long-lived, stored in DB (`RefreshToken` table). Used to issue a new access token.
- Tokens stored in `localStorage` on the frontend.

### Two-Factor Authentication (2FA)
- Optional per user, toggled in **Settings → Security**.
- On login, if enabled, backend sends a 6-digit code to the user's email.
- Frontend shows a second input step.

### Password Requirements
- Minimum 8 characters (validated on both frontend and backend).
- Hashed with BCrypt.

### Session Timeout
- Frontend inactivity timer (default 120 seconds warning).
- Warning dialog appears with countdown and progress bar.
- **Continue Session** resets the timer; **Sign Out** forces logout.

### Route Guards
- `AuthGuard` — redirects unauthenticated users to `/auth/login`.
- `GuestGuard` — redirects authenticated users away from `/auth/*`.

---

## 2. Landing Page

### Sections

| Section | Content |
|---------|---------|
| **Hero** | Animated budget-bar mockup (Food 72%, Housing 55%, Transport 88%, Entertainment 30%). "Get Started" and "Sign In" open a modal (not a full navigation). |
| **Stats** | 50K+ Transactions · $2B+ Managed · 99.9% Uptime · 4.9★ Rating |
| **Features** | 6 cards: Budget Management, Savings Goals, Financial Insights, Reports & Export, Multi-Account Support, Security & Encryption |
| **How It Works** | 3-step process: Sign Up → Link Accounts → Track & Optimize |
| **CTA** | Bottom call-to-action with modal triggers |

### Behaviour
- Header becomes sticky after scrolling.
- Intersection Observer adds reveal animations on scroll.
- Modal (login / register) has spring animation (`cubic-bezier(0.34,1.56,0.64,1)`).
- `Escape` key closes the modal.
- `document.body.overflow` is locked while modal is open.

---

## 3. Dashboard

**Route:** `/dashboard`

### KPI Cards (current month)

| Card | Details |
|------|---------|
| Total Income | Sum of income transactions + % change vs last month |
| Total Expenses | Sum of expense transactions + % change vs last month |
| Net Savings | Income − Expenses |
| Net Worth | Sum of all account balances |

### Projection Strip

| Metric | Description |
|--------|-------------|
| Avg Daily Spend | Total expenses ÷ days elapsed |
| Projected Month Expense | Avg daily spend × days in month |
| Projected End Balance | Current net worth + (projected income − projected expense) |
| Burn Rate Warning | Only shown when trend is negative — "~N days until $0" |

### Charts

| Chart | Type | Description |
|-------|------|-------------|
| Income vs Expenses | Grouped Bar (6 months) | Monthly income (green) vs expenses (red) for the last 6 months |
| Spending by Category | Donut Pie | Current month expenses split by category, colour-coded |
| Daily Trend | Combo Bar + Line | Daily income/expense bars + running net line with average marker |

### Data API
```
GET /dashboard/overview
GET /dashboard/charts/income-vs-expenses?months=6
GET /dashboard/charts/spending-by-category?from={iso}&to={iso}
GET /dashboard/charts/daily-trend?month={m}&year={y}
```

---

## 4. Transactions

**Route:** `/transactions`

### List View
- Lazy-loaded paginated table (20 rows per page).
- Columns: Date · Description · Category · Account · Amount · Type · Actions.
- Row checkboxes for bulk selection.

### Filters & Search

| Filter | Control |
|--------|---------|
| Search | Text input (searches description) |
| Type | Dropdown — All / Income / Expense / Transfer |
| Account | Dropdown — All / specific account |

### CRUD

**Create / Edit (modal form)**

| Field | Required | Notes |
|-------|----------|-------|
| Type | Yes | Income / Expense / Transfer |
| Amount | Yes | min 0.01 |
| Account | Yes | From user's accounts |
| Category | No | Flat list of user + system categories |
| Description | No | Free text |
| Date | Yes | Defaults to today |

**Delete**
- Single: confirmation dialog.
- Bulk: select rows → "Delete Selected" with count confirmation.

### CSV Import
1. Open Import CSV dialog.
2. Select account.
3. Upload `.csv` file (format: `date,description,amount,type`).
4. Backend parses and creates transactions in batch.
5. Toast shows count of imported records.

### Receipt Upload
- Per transaction; multipart file upload to `POST /transactions/{id}/receipt`.

---

## 5. Accounts

**Route:** `/accounts`

### Account Types
`CASH` · `BANK` · `CREDIT_CARD` · `SAVINGS` · `INVESTMENT`

### Fields

| Field | Required | Notes |
|-------|----------|-------|
| Name | Yes | Free text |
| Type | Yes | Dropdown |
| Initial Balance | No | Only on create; defaults to 0 |
| Currency | No | 3-letter code, defaults to USD |
| Color | No | Color picker, defaults to #6366f1 |

### Display
- Card grid with left-colored border.
- Balance shown in green (positive) or red (negative).
- Account type shown as subtitle.

### CRUD
- **Create / Edit** — modal form.
- **Delete** — confirmation dialog (backend prevents deletion if transactions exist).

---

## 6. Budgets

**Route:** `/budgets`

### Period Selection
- Month dropdown (January – December) + Year dropdown (2023–2026).
- Defaults to current month/year.
- Changing either reloads the budget list.

### Budget Fields

| Field | Required | Notes |
|-------|----------|-------|
| Category | No | Leave empty for a global budget covering all categories |
| Amount | Yes | min 0.01 |
| Rollover | No | Carry unspent balance to next month |

### Display Per Budget Card
- Category icon + name (or "Global Budget").
- **Spent / Limit** amounts.
- Progress bar — turns warning at 80%, danger at 100%.
- **% used** and **remaining** balance.
- Rolled-over amount note (if applicable).

### CRUD
- **Create** — modal form.
- **Delete** — icon button with confirmation.
- Edit not yet in UI (backend endpoint exists).

---

## 7. Savings Goals

**Route:** `/goals`

### Goal Fields

| Field | Required | Notes |
|-------|----------|-------|
| Name | Yes | Free text |
| Target Amount | Yes | min 0.01 |
| Deadline | No | Date picker |
| Color | No | Color picker, defaults to #6366f1 |
| Icon | No | PrimeNG pi-* icon name |

### Goal Statuses
`ACTIVE` · `COMPLETED` · `PAUSED` · `CANCELLED`

### Contribution System
- "Contribute" button on ACTIVE goals.
- Modal: Amount (required) + Note (optional).
- Each contribution is stored as a `GoalContribution` record.
- `currentAmount` is the sum of all contributions.

### Display Per Goal Card
- Coloured top border.
- Icon + Name + Deadline.
- `currentAmount / targetAmount`.
- Progress bar.
- `% complete` + `remaining to go`.
- Status badge.

### CRUD
- **Create** — modal form.
- **Contribute** — separate modal.
- **Delete** — trash icon with confirmation (ACTIVE goals only).

---

## 8. Reports

**Route:** `/reports`

### Period Selection
Month + Year dropdowns (same as Budgets).

### Summary KPIs
Total Income · Total Expenses · Net Savings · Savings Rate (%)

### Savings Rate Rating
| Rate | Label |
|------|-------|
| ≥ 20% | Great! (green) |
| 10–19% | Average |
| < 10% | Low (red) |

### Charts
| Chart | Description |
|-------|-------------|
| Spending by Category | Donut pie with legend |
| Savings Rate Gauge | Arc gauge 0–100% |

### Category Breakdown Table
Columns: Category · Amount · % of Expenses (with mini progress bar) · Transaction Count.
Sortable by any column.

### Exports
| Format | Endpoint | Filename |
|--------|----------|----------|
| CSV | `GET /reports/export/csv?month=&year=` | `report-{year}-{mm}.csv` |
| PDF | `GET /reports/export/pdf?month=&year=` | `report-{year}-{mm}.pdf` |

---

## 9. Insights

**Route:** `/insights`

### Insight Types

| Type | Category | Description |
|------|----------|-------------|
| `HIGH_SAVINGS_RATE` | Positive | Savings ≥ 30% |
| `LOW_SAVINGS_RATE` | Negative | Savings < 10% |
| `SPENDING_DECREASE` | Positive | MoM spend down |
| `SPENDING_INCREASE` | Negative | MoM spend up |
| `GOAL_MILESTONE` | Positive | Goal progress milestone |
| `BUDGET_ON_TRACK` | Positive | Budget within 90% |
| `BUDGET_EXCEEDED` | Negative | Budget over limit |
| `UNUSUAL_SPENDING` | Negative | Anomalous transaction |
| `SUBSCRIPTION_SUMMARY` | Info | Recurring subscriptions detected |
| `MONTHLY_SUMMARY` | Info | General monthly wrap-up |
| `INCOME_CHANGE` | Warning | Income variation |

### Actions
- **Generate Insights** button — calls `POST /insights/generate`.
- **Dismiss** (✕ button) — removes insight from list (`DELETE /insights/{id}/dismiss`).

### Auto-generation
- Runs automatically on the **1st of every month at 08:00 UTC** for all verified users.

---

## 10. Settings

**Route:** `/settings` — 3 tabs.

### Tab 1 — Profile
| Field | Notes |
|-------|-------|
| Full Name | Editable |
| Currency | 3-letter code (e.g. USD) |
| Monthly Income | Number |
| Timezone | Text (e.g. UTC, Europe/Paris) |

Save button calls `PUT /users/profile` + `PUT /users/settings`.

### Tab 2 — Security
**Change Password**
- Current password · New password (min 8) · Confirm password.
- Real-time password mismatch error.

**Two-Factor Authentication**
- Tag shows current state (Enabled / Disabled).
- Toggle button calls `PUT /users/settings` (twoFactorEnabled flag).

### Tab 3 — Notifications
**Preference Toggles**

| Preference | Default |
|------------|---------|
| Budget Alerts | On |
| Goal Milestones | On |
| Weekly Summary | Off |
| Large Transactions | On |

**Notification History Table**
- Columns: Icon + Title + Message · Date · Actions.
- **Mark as read** (single) · **Mark all as read** · **Delete**.
- Unread rows highlighted.
- Paginated (10 per page).

---

## 11. Navigation & Shell

### Sidebar Groups

| Group | Items |
|-------|-------|
| Overview | Dashboard · Transactions |
| Finance | Accounts · Budgets · Goals |
| Insights | Insights · Reports |
| Account | Settings |

- Collapsible (icon-only mode at 70px width).
- Active route highlighted with left accent bar.
- Collapse/expand toggle button.

### Topbar
- Hamburger menu toggle (mobile).
- Language switcher dropdown.
- Notification bell with unread count badge (fetched from `GET /notifications/unread-count`).
- User avatar chip (initials, links to Settings).
- Sign out button.

---

## 12. Internationalisation

| Language | Code | Direction | Flag |
|----------|------|-----------|------|
| English | `en` | LTR | 🇬🇧 |
| Français | `fr` | LTR | 🇫🇷 |
| العربية | `ar` | RTL | 🇸🇦 |

- `ngx-translate` with HTTP loader.
- Translation files: `public/assets/i18n/{en,fr,ar}.json`.
- On language switch: `document.documentElement.lang` and `.dir` are updated.
- Selection persisted in `localStorage` (`budgetix_lang`).

---

## 13. Categories & Auto-Categorization

> **Status:** Backend complete. Frontend UI not yet built — categories are consumed by Transactions and Budgets but are not directly managed through a dedicated screen.

### Category Fields

| Field | Required | Notes |
|-------|----------|-------|
| Name | Yes | Free text |
| Icon | No | PrimeNG `pi-*` icon name |
| Color | No | Hex color code |
| Type | Yes | `INCOME` / `EXPENSE` / `TRANSFER` |
| Parent | No | Reference to a parent category (enables hierarchy) |

### Hierarchy

- Categories support one level of nesting (parent → children).
- System categories ship pre-seeded and are shared across all users (`system = true`).
- User-created categories belong to a single user (`system = false`).

### Auto-Categorization Rules

Each category can have one or more keyword rules. When a new transaction is created without an explicit category, the backend's `AutoCategorizationService` scans the transaction's description against all active rules for that user and assigns the first matching category.

| Rule Field | Description |
|------------|-------------|
| Keyword | Text to match in transaction description (case-insensitive contains) |
| Category | The category to assign on match |

**API for rules:** `POST /categories/{id}/rules` — adds a rule to a category.

### Category API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/categories` | List all (system + user-created) |
| POST | `/categories` | Create a user category |
| PUT | `/categories/{id}` | Update a user category |
| DELETE | `/categories/{id}` | Delete (backend prevents deletion if transactions are linked) |
| POST | `/categories/{id}/rules` | Add an auto-categorization keyword rule |

---

## 14. Recurring Transactions

> **Status:** Backend complete with hourly scheduler. Frontend UI not yet built.

Recurring transactions automate the creation of predictable income or expense entries (e.g. salary, rent, subscriptions) so they appear in the transaction list and update account balances without manual entry.

### Fields

| Field | Required | Notes |
|-------|----------|-------|
| Amount | Yes | min 0.01 |
| Type | Yes | `INCOME` / `EXPENSE` / `TRANSFER` |
| Description | Yes | Free text |
| Account | Yes | Account to debit/credit |
| Category | No | Optional category assignment |
| Frequency | Yes | See table below |
| Start Date | Yes | Date of first occurrence |
| End Date | No | If omitted the recurrence runs indefinitely |

### Frequencies

| Value | Interval |
|-------|----------|
| `DAILY` | Every day |
| `WEEKLY` | Every 7 days |
| `BIWEEKLY` | Every 14 days |
| `MONTHLY` | Same day each month |
| `QUARTERLY` | Every 3 months |
| `YEARLY` | Once per year |

### Scheduler Behaviour

The `RecurringScheduler` runs **every hour**. For each active recurring transaction where `nextRun ≤ now`:

1. Creates a `Transaction` record.
2. Updates `Account.balance`.
3. Checks for matching budgets and updates `Budget.spent`.
4. Advances `nextRun` to the next occurrence date.
5. Marks `lastRun = now`.
6. Sends a `RECURRING_TX` in-app notification to the user.

If an `endDate` is set and the next occurrence would fall after it, the record is deactivated (`isActive = false`).

### Toggle

A single endpoint enables/disables a recurring transaction without deleting it:

`POST /recurring/{id}/toggle` — flips `isActive`.

### Recurring API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/recurring` | List all recurring transactions |
| POST | `/recurring` | Create a new recurring transaction |
| POST | `/recurring/{id}/toggle` | Enable / disable |
| DELETE | `/recurring/{id}` | Permanently delete |

---

## 15. Backend API

### Base URL: `/api`

#### Auth — `POST /auth/*`
| Endpoint | Body |
|----------|------|
| `/register` | `{ name, email, password }` |
| `/login` | `{ email, password, twoFactorCode? }` |
| `/refresh` | `{ refreshToken }` |
| `/logout` | `{ refreshToken }` |
| `/verify-email` | `{ email, code }` |
| `/forgot-password` | `{ email }` |
| `/reset-password` | `{ email, code, newPassword }` |

#### Transactions — `/transactions`
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Paginated list with filters |
| GET | `/{id}` | Single transaction |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| POST | `/bulk-delete` | `{ ids: string[] }` |
| POST | `/{id}/receipt` | Upload receipt (multipart) |
| POST | `/import` | CSV import `{ accountId, file }` |

#### Accounts — `/accounts`
`GET /` · `GET /{id}` · `POST /` · `PUT /{id}` · `DELETE /{id}`

#### Budgets — `/budgets`
`GET /?month=&year=` · `POST /` · `PUT /{id}` · `DELETE /{id}`

#### Goals — `/goals`
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | List all |
| GET | `/{id}` | Single |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| POST | `/{id}/contribute` | `{ amount, note? }` |
| PATCH | `/{id}/status` | `{ status }` |
| DELETE | `/{id}` | Delete |

#### Categories — `/categories`
`GET /` · `POST /` · `PUT /{id}` · `DELETE /{id}` · `POST /{id}/rules`

#### Reports — `/reports`
`GET /monthly?month=&year=` · `GET /export/csv?month=&year=` · `GET /export/pdf?month=&year=`

#### Insights — `/insights`
`GET /` · `POST /generate` · `DELETE /{id}/dismiss`

#### Notifications — `/notifications`
`GET /?unreadOnly=&page=&size=` · `GET /unread-count` · `PUT /{id}/read` · `PUT /read-all` · `DELETE /{id}`

#### Dashboard — `/dashboard`
`GET /overview` · `GET /charts/income-vs-expenses?months=` · `GET /charts/spending-by-category?from=&to=` · `GET /charts/daily-trend?month=&year=`

#### Users — `/users`
`GET /me` · `PUT /profile` · `PUT /settings` · `PUT /password`

#### Recurring Transactions — `/recurring`
`GET /` · `POST /` · `POST /{id}/toggle` · `DELETE /{id}`

---

## 16. Data Models

### User
```
id (UUID) · email · password (bcrypt) · name · avatar?
emailVerified · twoFactorEnabled · twoFactorSecret?
role · createdAt · updatedAt
→ UserProfile (1:1)
```

### UserProfile
```
currency (default USD) · monthlyIncome · timezone (default UTC)
notifyBudgetAlerts · notifyGoalReminders · notifyWeeklySummary · notifyLargeTransactions
financialGoalType?
```

### Account
```
id · name · type (CASH|BANK|CREDIT_CARD|SAVINGS|INVESTMENT)
balance · currency · color · icon · isDefault
→ User (many:1)
```

### Transaction
```
id · amount · type (INCOME|EXPENSE|TRANSFER)
description · notes · date · receipt? · recurring · tags[]
→ User (many:1) · Account (many:1) · Category (many:1, optional)
```

### Category
```
id · name · icon · color · type (INCOME|EXPENSE|TRANSFER) · system
→ User (many:1, null for system) · parent (many:1, optional) · children[] · rules[]
```

### Budget
```
id · amount · spent · period (MONTHLY) · month · year · rollover · rolledAmount
→ User (many:1) · Category (many:1, optional)
computed: usagePercent, remaining
```

### SavingsGoal
```
id · name · targetAmount · currentAmount · deadline? · icon · color
status (ACTIVE|COMPLETED|PAUSED|CANCELLED)
→ User (many:1) · contributions[]
computed: progressPercent, remaining
```

### GoalContribution
```
id · amount · note? · createdAt
→ Goal (many:1)
```

### RecurringTransaction
```
id · amount · type · description · frequency (DAILY|WEEKLY|BIWEEKLY|MONTHLY|QUARTERLY|YEARLY)
startDate · endDate? · lastRun? · nextRun · isActive
→ User (many:1) · Account (many:1) · Category (many:1, optional)
```

### Insight
```
id · type (InsightType) · title · message · data (JSON) · period · dismissed
→ User (many:1)
```

### Notification
```
id · type (NotificationType) · title · message · data (JSON) · read
→ User (many:1)
```

### OtpCode
```
id · code · type (EMAIL_VERIFICATION|PASSWORD_RESET|TWO_FACTOR) · expiresAt · used
→ User (many:1)
```

### RefreshToken
```
id · token · expiresAt
→ User (many:1)
```

---

## 17. Scheduled Jobs

### 1. Recurring Transactions Processor
- **Trigger:** Every hour (fixed delay).
- **Logic:** Find all `RecurringTransaction` records where `nextRun ≤ now` and `isActive = true`. For each:
  1. Create a `Transaction`.
  2. Update `Account.balance`.
  3. Update matching `Budget.spent`.
  4. Calculate and save next run date.
  5. Send a `RECURRING_TX` notification to the user.

### 2. Monthly Insight Generation
- **Trigger:** 1st of every month at 08:00 UTC (`cron: 0 0 8 1 * *`).
- **Logic:** For every verified user:
  1. Compute savings rate, MoM spending delta, income change.
  2. Check budget statuses.
  3. Detect goal milestones.
  4. Detect unusual spending (statistical anomaly).
  5. Summarise recurring subscriptions.
  6. Persist `Insight` records.
  7. Send `BUDGET_ALERT` / `GOAL_REMINDER` notifications for critical items.

---

## Feature Matrix

| Feature | Frontend | Backend | Notes |
|---------|----------|---------|-------|
| Register / Login | ✅ | ✅ | |
| Email Verification | ✅ | ✅ | 6-digit OTP |
| 2FA | ✅ | ✅ | Email-based TOTP |
| Password Reset | ✅ | ✅ | OTP-based |
| Session Timeout | ✅ | — | Frontend inactivity |
| Dashboard KPIs | ✅ | ✅ | 12+ metrics |
| Dashboard Charts | ✅ | ✅ | 3 chart types |
| Transactions CRUD | ✅ | ✅ | |
| Transaction Filters | ✅ | ✅ | Search, type, account |
| CSV Import | ✅ | ✅ | |
| Receipt Upload | — | ✅ | UI not yet wired |
| Accounts CRUD | ✅ | ✅ | |
| Budgets CRUD | ✅ | ✅ | |
| Budget Rollover | ✅ | ✅ | |
| Goals CRUD | ✅ | ✅ | |
| Goal Contributions | ✅ | ✅ | |
| Goal Status Update | — | ✅ | UI not yet wired |
| Reports (Monthly) | ✅ | ✅ | |
| CSV Export | ✅ | ✅ | |
| PDF Export | ✅ | ✅ | |
| Insights View | ✅ | ✅ | |
| Insights Generate | ✅ | ✅ | |
| Auto-categorisation | — | ✅ | Rule-based, UI not wired — see §13 |
| Recurring Transactions | — | ✅ | UI not yet built — see §14 |
| Notifications | ✅ (Settings) | ✅ | |
| Notification Bell | ✅ | ✅ | Unread count badge |
| Profile Settings | ✅ | ✅ | |
| Security Settings | ✅ | ✅ | |
| Notification Prefs | ✅ | ✅ | |
| Multi-language | ✅ | — | EN / FR / AR + RTL |
| Categories CRUD | — | ✅ | UI not yet built — see §13 |
| Dark Mode | ❌ | — | Not implemented |
| Mobile Responsive | Partial | — | Topbar adapts; sidebar collapses |
