# Technical Specification Document (TSD)
## Budgetix — Personal Finance Management Platform
**Version:** 1.0  
**Date:** May 2026

---

## 1. System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser (SPA)                         │
│              Angular 21 — standalone components              │
│        PrimeNG UI  │  ECharts  │  ngx-translate v17         │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS / REST JSON
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Boot 3.2 API                         │
│  Context path: /api   Port: 3000                            │
│  JWT filter → Controllers → Services → Repositories         │
└──────────┬────────────────────────┬────────────────────────┘
           │                        │
           ▼                        ▼
┌──────────────────┐     ┌─────────────────────┐
│  PostgreSQL 16   │     │     Redis 7          │
│  Port 5433       │     │  Port 6380           │
│  (Docker)        │     │  (session cache)     │
└──────────────────┘     └─────────────────────┘
                                    
           ┌─────────────────────┐
           │  MailHog (dev)      │
           │  SMTP :1025         │
           │  Web UI :8025       │
           └─────────────────────┘
```

---

## 2. Technology Stack

### 2.1 Backend

| Component | Technology | Version |
|---|---|---|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 3.2.x |
| Build tool | Maven | 3.9+ |
| ORM | Spring Data JPA / Hibernate | 6.x |
| Database migrations | Flyway | 9.x |
| Security | Spring Security + JWT | 6.x |
| API docs | SpringDoc OpenAPI (Swagger UI) | 2.x |
| Password hashing | BCrypt | cost factor 12 |
| HTTP client | Spring Web (RestTemplate / WebClient) | — |
| Email | Spring Mail (JavaMailSender) | — |
| Scheduler | Spring `@Scheduled` | — |
| Validation | Jakarta Validation | 3.x |
| Boilerplate reduction | Lombok | 1.18.x |

### 2.2 Frontend

| Component | Technology | Version |
|---|---|---|
| Language | TypeScript | 5.x |
| Framework | Angular | 21 |
| UI library | PrimeNG | 17.x |
| Charts | Apache ECharts (ngx-echarts) | — |
| Internationalisation | @ngx-translate/core + http-loader | 17.x |
| HTTP | Angular HttpClient | — |
| Routing | Angular Router (lazy-loaded) | — |
| State | Angular Signals | built-in |

### 2.3 Infrastructure

| Component | Technology |
|---|---|
| Database | PostgreSQL 16 Alpine (Docker) |
| Cache / session | Redis 7 Alpine (Docker) |
| Dev email | MailHog |
| Container runtime | Docker Compose 3.9 |
| Reverse proxy (prod) | Nginx |

---

## 3. Backend Package Structure

```
com.budgetix
├── BudgetixApplication.java
├── auth
│   ├── controller/AuthController.java
│   ├── service/AuthService.java  EmailService.java  OtpService.java
│   └── dto/  (RegisterRequest, LoginRequest, TokenResponse, ...)
├── user
│   ├── controller/UserController.java
│   ├── service/UserService.java  UserDetailsServiceImpl.java
│   ├── entity/User.java  UserProfile.java  RefreshToken.java  OtpCode.java
│   ├── repository/  (UserRepository, UserProfileRepository, ...)
│   └── dto/  (UserResponse, UpdateProfileRequest, ChangePasswordRequest, ...)
├── account
│   ├── controller/AccountController.java
│   ├── service/AccountService.java
│   ├── entity/Account.java
│   ├── repository/AccountRepository.java
│   └── dto/  (AccountRequest, AccountResponse)
├── category
│   ├── controller/CategoryController.java
│   ├── service/CategoryService.java
│   ├── entity/Category.java  AutoCategorizationRule.java
│   └── repository/  (CategoryRepository, AutoCategorizationRuleRepository)
├── transaction
│   ├── service/TransactionService.java  AutoCategorizationService.java
│   │         BudgetUpdateService.java   CsvImportService.java
│   ├── entity/Transaction.java
│   ├── repository/TransactionRepository.java
│   └── dto/  (TransactionRequest, TransactionResponse, TransactionFilterRequest)
├── budget
│   ├── controller/BudgetController.java
│   ├── service/BudgetService.java
│   ├── entity/Budget.java  BudgetAlert.java
│   ├── repository/BudgetRepository.java
│   └── dto/  (BudgetRequest, BudgetResponse)
├── goal
│   ├── controller/GoalController.java
│   ├── service/GoalService.java
│   ├── entity/SavingsGoal.java  GoalContribution.java
│   └── repository/  (SavingsGoalRepository, GoalContributionRepository)
├── recurring
│   ├── controller/RecurringController.java
│   ├── service/RecurringService.java  RecurringScheduler.java
│   ├── entity/RecurringTransaction.java
│   └── repository/RecurringTransactionRepository.java
├── notification
│   ├── controller/NotificationController.java
│   ├── service/NotificationService.java
│   ├── entity/Notification.java
│   └── repository/NotificationRepository.java
├── insight
│   ├── controller/InsightController.java
│   ├── entity/Insight.java
│   └── repository/InsightRepository.java
├── dashboard
│   ├── controller/DashboardController.java
│   └── service/DashboardService.java
├── common
│   ├── dto/ApiResponse.java  PageResponse.java
│   ├── enums/  (TransactionType, AccountType, CategoryType, BudgetPeriod,
│   │            GoalStatus, Frequency, NotificationType, InsightType, OtpType)
│   ├── exception/AppException.java  ErrorCode.java  GlobalExceptionHandler.java
│   └── util/JwtUtil.java
└── config
    ├── SecurityConfig.java
    ├── JwtAuthenticationFilter.java
    ├── WebConfig.java  (CORS)
    └── OpenApiConfig.java
```

---

## 4. Database Schema

### 4.1 Entity-Relationship Summary

```
users ──1──< user_profiles
users ──1──< refresh_tokens
users ──1──< otp_codes
users ──1──< accounts ──1──< transactions
users ──1──< categories (parent_id self-ref) ──1──< auto_categorization_rules
users ──1──< budgets ──1──< budget_alerts
users ──1──< savings_goals ──1──< goal_contributions
users ──1──< recurring_transactions
users ──1──< notifications
users ──1──< insights
transactions ──< transaction_tags  (element collection)
```

### 4.2 Key Constraints

| Table | Constraint |
|---|---|
| `budgets` | UNIQUE (user_id, category_id, year, month) — one budget per category per month |
| `transactions` | ON DELETE RESTRICT on account_id — prevents orphan balances |
| `accounts` | ON DELETE CASCADE from users |
| `refresh_tokens` | ON DELETE CASCADE from users |
| `goal_contributions` | UNIQUE transaction_id — one contribution per transaction |

### 4.3 Performance Indexes

```sql
idx_transactions_user_date     (user_id, date DESC)   -- primary list query
idx_transactions_category      (category_id)
idx_transactions_account       (account_id)
idx_transactions_type          (user_id, type)
idx_budgets_user_period        (user_id, year, month)
idx_notifications_user_read    (user_id, read)
idx_insights_user_dismissed    (user_id, dismissed)
idx_recurring_next_run         (next_run, is_active)  -- scheduler query
idx_categories_user_parent     (user_id, parent_id)
idx_refresh_tokens_user        (user_id)
idx_otp_codes_user_type        (user_id, type)
```

---

## 5. Security Design

### 5.1 Authentication Flow

```
1. POST /api/auth/login  { email, password }
   └─ AuthService validates credentials via BCrypt
   └─ Issues accessToken (JWT, 15 min) + refreshToken (UUID, 7 days, stored in DB)
   └─ Returns { accessToken, refreshToken, user }

2. Every protected request:
   └─ Authorization: Bearer <accessToken>
   └─ JwtAuthenticationFilter validates signature + expiry
   └─ Sets SecurityContext with userId as principal username

3. POST /api/auth/refresh  { refreshToken }
   └─ Validates token exists in DB and not expired
   └─ Issues new accessToken + rotates refreshToken (old deleted)

4. POST /api/auth/logout  { refreshToken }
   └─ Deletes refreshToken from DB
```

### 5.2 JWT Claims

```json
{
  "sub": "<userId UUID>",
  "iat": 1746000000,
  "exp": 1746000900
}
```

### 5.3 Public Endpoints

```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
POST /api/auth/verify-email
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET  /api/actuator/health
GET  /api/swagger-ui/**
GET  /api/v3/api-docs/**
```

All other endpoints require a valid Bearer token.

### 5.4 Password Policy

- Minimum 8 characters (enforced by Jakarta `@Size` on DTO)
- BCrypt cost factor 12 (~300 ms hash time, brute-force resistant)
- Original password never stored or logged

### 5.5 CORS Configuration

Configured in `WebConfig` — allowed origin defaults to `${FRONTEND_URL}` (env-configurable). All methods and standard headers permitted.

---

## 6. Frontend Architecture

### 6.1 Module / Component Map

```
AppComponent
└── RouterOutlet
    ├── LandingComponent          /
    ├── Auth shell (guestGuard)   /auth
    │   ├── LoginComponent        /auth/login
    │   ├── RegisterComponent     /auth/register
    │   ├── ForgotPasswordComponent /auth/forgot-password
    │   ├── ResetPasswordComponent  /auth/reset-password
    │   └── VerifyEmailComponent    /auth/verify-email
    └── ShellComponent (authGuard)
        ├── TopbarComponent
        │   ├── LanguageSwitcherComponent
        │   └── Notification bell + User chip
        ├── SidebarComponent
        ├── SessionTimeoutComponent
        └── RouterOutlet (children)
            ├── DashboardComponent     /dashboard
            ├── TransactionsComponent  /transactions
            ├── AccountsComponent      /accounts
            ├── BudgetsComponent       /budgets
            ├── GoalsComponent         /goals
            ├── InsightsComponent      /insights
            ├── ReportsComponent       /reports
            └── SettingsComponent      /settings
```

### 6.2 Guards

| Guard | Behaviour |
|---|---|
| `authGuard` | Redirects to `/auth/login` if no valid access token in localStorage |
| `guestGuard` | Redirects to `/dashboard` if already authenticated |

### 6.3 Interceptors

| Interceptor | Behaviour |
|---|---|
| `authInterceptor` | Attaches `Authorization: Bearer <token>` to every outbound request |

### 6.4 Core Services

| Service | Responsibility |
|---|---|
| `AuthService` | Login, register, token storage, logout, `currentUser` signal |
| `ApiService` | Base HTTP wrapper (base URL, error normalisation) |
| `TransactionService` | CRUD + filter + receipt upload |
| `AccountService` | CRUD accounts |
| `BudgetService` | CRUD budgets |
| `GoalService` | CRUD goals + contributions |
| `CategoryService` | CRUD categories |
| `DashboardService` | Overview + chart data |
| `NotificationService` | Unread count + mark read |
| `UserService` | Profile + settings + password |
| `InactivityService` | Session timeout (warn at 13 min, logout at 15 min) |

### 6.5 State Management

Angular Signals are used for all reactive state. No NgRx or external state library. Key patterns:

```typescript
// AuthService
currentUser = signal<User | null>(null);

// Component-local
open = signal(false);
countdown = signal(120);
```

### 6.6 Internationalisation

- Provider: `provideTranslateService({ fallbackLang: 'en' })` + `provideTranslateHttpLoader({ prefix: './assets/i18n/', suffix: '.json' })`
- Translation files: `src/assets/i18n/en.json`, `fr.json`, `ar.json`
- All UI strings referenced via `{{ 'KEY.SUBKEY' | translate }}`
- Language persisted in `localStorage` under key `budgetix_lang`
- Arabic triggers `document.documentElement.dir = 'rtl'`

---

## 7. API Response Envelope

All API responses use a consistent wrapper:

```json
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```

Error responses:

```json
{
  "success": false,
  "message": "TRANSACTION_NOT_FOUND",
  "data": null
}
```

Paginated responses use `PageResponse<T>`:

```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 134,
    "totalPages": 7,
    "last": false
  }
}
```

---

## 8. Recurring Scheduler

```
@Scheduled(fixedDelay = 3_600_000)  // every 60 minutes
RecurringScheduler.processRecurring()
  ├── Query: SELECT * FROM recurring_transactions WHERE next_run <= NOW() AND is_active = true
  ├── For each due record:
  │   ├── INSERT transaction (copy of template)
  │   ├── UPDATE account.balance
  │   ├── UPDATE budget.spent via BudgetUpdateService
  │   ├── SET last_run = NOW()
  │   ├── SET next_run = computeNextRun(frequency, NOW())
  │   └── INSERT notification for user
  └── Errors are caught per-record (one failure doesn't block others)
```

Frequency → next_run computation:

| Frequency | Advance by |
|---|---|
| DAILY | +1 day |
| WEEKLY | +1 week |
| BIWEEKLY | +2 weeks |
| MONTHLY | +1 month |
| QUARTERLY | +3 months |
| YEARLY | +1 year |

---

## 9. Budget Recalculation

`BudgetUpdateService` is called by `TransactionService` on every create, update, and delete:

```
On CREATE:
  └─ Find budget for (userId, categoryId, month, year)
  └─ If EXPENSE: budget.spent += amount
  └─ Check thresholds → fire alert notification if needed

On EDIT:
  └─ Reverse old contribution (subtract old amount from old budget)
  └─ Apply new contribution (add new amount to new budget)

On DELETE:
  └─ budget.spent -= transaction.amount (if EXPENSE)
```

---

## 10. File Upload

- Max file size: `${MAX_FILE_SIZE:5MB}` (configurable via env)
- Accepted types: `image/*`, `application/pdf`
- Storage path: `{UPLOAD_DIR}/receipts/{userId}/{uuid}_{originalFilename}`
- Returned URL: `/uploads/receipts/{userId}/{filename}` (served as static resource)

---

## 11. Error Codes

| Code | Meaning |
|---|---|
| `USER_NOT_FOUND` | No user with the given ID/email |
| `EMAIL_ALREADY_EXISTS` | Registration with duplicate email |
| `INVALID_CREDENTIALS` | Wrong email or password |
| `EMAIL_NOT_VERIFIED` | Login before email verification |
| `INVALID_OTP` | Wrong or expired OTP code |
| `TOKEN_EXPIRED` | JWT or refresh token expired |
| `ACCOUNT_NOT_FOUND` | Account doesn't exist or doesn't belong to user |
| `TRANSACTION_NOT_FOUND` | Transaction doesn't exist or doesn't belong to user |
| `CATEGORY_NOT_FOUND` | Category not found |
| `BUDGET_ALREADY_EXISTS` | Duplicate budget for same category/period |
| `GOAL_NOT_FOUND` | Goal not found |
| `UNSUPPORTED_FILE_TYPE` | Receipt upload with invalid MIME type |

---

## 12. Configuration Reference

All settings in `backend/src/main/resources/application.yml` with environment variable overrides:

| Variable | Default | Description |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5433/budgetix` | JDBC connection URL |
| `POSTGRES_USER` | `budgetix` | DB username |
| `POSTGRES_PASSWORD` | `budgetix_pass` | DB password |
| `JWT_ACCESS_SECRET` | `budgetix_access_secret_min_32_chars_long` | HMAC signing key for access tokens |
| `JWT_REFRESH_SECRET` | `budgetix_refresh_secret_min_32_chars_long` | HMAC signing key for refresh tokens |
| `PORT` | `3000` | Server port |
| `FRONTEND_URL` | `http://localhost:4200` | CORS allowed origin + email link base |
| `MAIL_HOST` | `localhost` | SMTP host |
| `MAIL_PORT` | `1025` | SMTP port |
| `MAIL_FROM` | `noreply@budgetix.app` | Sender address |
| `UPLOAD_DIR` | `uploads` | Root directory for receipt storage |
| `MAX_FILE_SIZE` | `5MB` | Maximum receipt file size |
