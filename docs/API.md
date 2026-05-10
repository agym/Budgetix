# API Reference
## Budgetix REST API  
**Base URL:** `http://localhost:3000/api`  
**Swagger UI:** `http://localhost:3000/api/swagger-ui/index.html`

All protected endpoints require:
```
Authorization: Bearer <accessToken>
Content-Type: application/json
```

---

## Authentication

### POST `/auth/register`
Register a new user account.

**Request**
```json
{
  "name": "Ali Agyn",
  "email": "ali@example.com",
  "password": "MySecurePass1!"
}
```

**Response** `201 Created`
```json
{ "success": true, "message": "Registration successful. Please verify your email.", "data": null }
```

---

### POST `/auth/verify-email`
Verify email with the OTP sent during registration.

**Request**
```json
{ "email": "ali@example.com", "code": "847291" }
```

**Response** `200 OK`
```json
{ "success": true, "message": "Email verified successfully", "data": null }
```

---

### POST `/auth/login`
Authenticate and receive tokens.

**Request**
```json
{ "email": "ali@example.com", "password": "MySecurePass1!" }
```

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "550e8400-e29b-...",
    "user": {
      "id": "550e8400-...",
      "name": "Ali Agyn",
      "email": "ali@example.com",
      "role": "USER"
    }
  }
}
```

---

### POST `/auth/refresh`
Rotate tokens using a valid refresh token.

**Request**
```json
{ "refreshToken": "550e8400-e29b-..." }
```

**Response** `200 OK` — same shape as login response.

---

### POST `/auth/logout`
Invalidate the refresh token.

**Request**
```json
{ "refreshToken": "550e8400-e29b-..." }
```

---

### POST `/auth/resend-verification`
Resend the email-verification OTP. Rate-limited to **3 requests per 24 hours** per account.

**Request**
```json
{ "email": "ali@example.com" }
```

**Response** `200 OK`
```json
{ "success": true, "message": "Verification code resent", "data": null }
```

**Error responses**
| Status | Code | Meaning |
|--------|------|---------|
| 400 | `EMAIL_ALREADY_VERIFIED` | Email is already verified |
| 404 | `USER_NOT_FOUND` | No account with that email |
| 429 | `RESEND_LIMIT_EXCEEDED` | 3-attempt limit reached; retry after 24 h |

---

### POST `/auth/forgot-password`
Trigger a password-reset OTP email.

**Request**
```json
{ "email": "ali@example.com" }
```

---

### POST `/auth/reset-password`
Set a new password using the OTP.

**Request**
```json
{ "email": "ali@example.com", "code": "391847", "newPassword": "NewPass123!" }
```

---

## Users  *(requires auth)*

### GET `/users/me`
Return the authenticated user's profile.

**Response**
```json
{
  "data": {
    "id": "550e8400-...",
    "name": "Ali Agyn",
    "email": "ali@example.com",
    "avatar": null,
    "emailVerified": true,
    "profile": {
      "currency": "USD",
      "monthlyIncome": 5000.00,
      "timezone": "UTC",
      "notifyBudgetAlerts": true,
      "notifyGoalReminders": true,
      "notifyWeeklySummary": true
    }
  }
}
```

---

### PUT `/users/profile`
Update name and avatar.

**Request**
```json
{ "name": "Ali Agyn", "avatar": "https://..." }
```

---

### PUT `/users/settings`
Update financial preferences.

**Request**
```json
{
  "currency": "EUR",
  "monthlyIncome": 6000,
  "timezone": "Europe/Paris",
  "notifyBudgetAlerts": true,
  "notifyGoalReminders": false,
  "notifyWeeklySummary": true
}
```

---

### PUT `/users/password`
Change password (requires current password).

**Request**
```json
{ "currentPassword": "OldPass1!", "newPassword": "NewPass2!" }
```

---

## Accounts  *(requires auth)*

### GET `/accounts`
List all accounts for the authenticated user.

**Response**
```json
{
  "data": [
    {
      "id": "...", "name": "Main Checking", "type": "CHECKING",
      "balance": 3200.50, "currency": "USD", "color": "#2563eb",
      "icon": "pi-wallet", "isDefault": true
    }
  ]
}
```

---

### POST `/accounts`
Create a new account.

**Request**
```json
{
  "name": "Emergency Savings",
  "type": "SAVINGS",
  "balance": 1000.00,
  "currency": "USD",
  "color": "#10b981",
  "icon": "pi-shield",
  "isDefault": false
}
```

Account types: `CASH`, `BANK`, `CREDIT_CARD`, `SAVINGS`, `INVESTMENT`

---

### PUT `/accounts/{id}`
Update an account (same body as POST).

---

### DELETE `/accounts/{id}`
Delete an account. Returns `400` if transactions exist on it.

---

## Transactions  *(requires auth)*

### GET `/transactions`
Paginated, filtered transaction list.

**Query parameters**

| Param | Type | Description |
|---|---|---|
| `page` | int | Zero-based page (default 0) |
| `size` | int | Page size (default 20) |
| `startDate` | ISO datetime | Filter from date |
| `endDate` | ISO datetime | Filter to date |
| `type` | `INCOME` \| `EXPENSE` | Transaction type |
| `categoryId` | UUID | Filter by category |
| `accountId` | UUID | Filter by account |
| `search` | string | Full-text on description |

**Response**
```json
{
  "data": {
    "content": [ { "id": "...", "amount": 45.00, "type": "EXPENSE", ... } ],
    "page": 0, "size": 20, "totalElements": 134, "totalPages": 7
  }
}
```

---

### POST `/transactions`
Create a transaction.

**Request**
```json
{
  "accountId": "...",
  "categoryId": "...",
  "amount": 45.00,
  "type": "EXPENSE",
  "description": "Lunch at Café",
  "notes": "Business lunch",
  "date": "2026-05-01T12:30:00",
  "tags": ["food", "work"]
}
```

`categoryId` is optional — if omitted the system attempts auto-categorization from `description` keyword rules.

---

### PUT `/transactions/{id}`
Update a transaction (same body as POST).

---

### DELETE `/transactions/{id}`
Delete a single transaction and reverse the account balance.

---

### POST `/transactions/import`
Import transactions from a CSV file. `multipart/form-data` with fields `accountId` (UUID) and `file` (CSV).

Expected CSV format (header row required):
```
date,description,amount,type
2026-04-01,Salary,3000,INCOME
2026-04-03,Supermarket,45.50,EXPENSE
```

**Response**
```json
{ "data": { "imported": 42 } }
```

---

### POST `/transactions/bulk-delete`
Delete multiple transactions.

**Request**
```json
{ "ids": ["uuid1", "uuid2", "uuid3"] }
```

---

### POST `/transactions/{id}/receipt`
Upload a receipt image or PDF. `multipart/form-data`, field name `file`.

**Response**
```json
{ "data": "/uploads/receipts/{userId}/{filename}" }
```

---

## Budgets  *(requires auth)*

### GET `/budgets?month=5&year=2026`
Get all budgets for a given month/year (defaults to current month).

**Response**
```json
{
  "data": [
    {
      "id": "...", "categoryId": "...", "categoryName": "Dining",
      "amount": 500.00, "spent": 210.50, "period": "MONTHLY",
      "month": 5, "year": 2026, "rollover": false
    }
  ]
}
```

---

### POST `/budgets`
Create a budget.

**Request**
```json
{
  "categoryId": "...",
  "amount": 500.00,
  "period": "MONTHLY",
  "month": 5,
  "year": 2026,
  "rollover": false
}
```

---

### PUT `/budgets/{id}` / DELETE `/budgets/{id}`
Update or delete a budget.

---

## Savings Goals  *(requires auth)*

### GET `/goals`
List all goals.

---

### POST `/goals`
Create a savings goal.

**Request**
```json
{
  "name": "Emergency Fund",
  "targetAmount": 10000.00,
  "deadline": "2026-12-31T00:00:00",
  "icon": "pi-shield",
  "color": "#10b981"
}
```

---

### POST `/goals/{id}/contribute`
Add a contribution to a goal.

**Request**
```json
{ "amount": 200.00, "note": "Monthly top-up" }
```

---

### PATCH `/goals/{id}/status`
Change goal status.

**Request**
```json
{ "status": "COMPLETED" }
```

Statuses: `ACTIVE`, `COMPLETED`, `PAUSED`

---

## Recurring Transactions  *(requires auth)*

### GET `/recurring`
List all recurring transaction rules.

---

### POST `/recurring`
Create a recurring rule.

**Request**
```json
{
  "accountId": "...",
  "categoryId": "...",
  "amount": 15.99,
  "type": "EXPENSE",
  "description": "Netflix",
  "frequency": "MONTHLY",
  "startDate": "2026-01-01T00:00:00"
}
```

Frequencies: `DAILY`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY`

---

### POST `/recurring/{id}/toggle`
Pause or resume a recurring rule.

---

### DELETE `/recurring/{id}`
Delete a recurring rule (does not delete already-posted transactions).

---

## Dashboard  *(requires auth)*

### GET `/dashboard/overview`
KPI summary for the current month.

**Response**
```json
{
  "data": {
    "totalBalance": 8500.00,
    "monthlyIncome": 5000.00,
    "monthlyExpenses": 2300.00,
    "netSavings": 2700.00,
    "topCategory": "Dining",
    "recentTransactions": [...]
  }
}
```

---

### GET `/dashboard/charts/spending-by-category?from=...&to=...`
Pie/donut chart data — spending per category in the given range.

**Response**
```json
{
  "data": [
    { "category": "Dining", "amount": 210.50, "color": "#ef4444" },
    { "category": "Transport", "amount": 85.00, "color": "#f59e0b" }
  ]
}
```

---

### GET `/dashboard/charts/income-vs-expenses?months=6`
Bar chart data — monthly income vs expenses for the last N months.

---

### GET `/dashboard/charts/daily-trend?month=5&year=2026`
Line chart data — daily spend for a given month.

---

## Notifications  *(requires auth)*

### GET `/notifications`
List all notifications (most recent first).

---

### GET `/notifications/unread-count`
**Response** `{ "data": { "count": 3 } }`

---

### PATCH `/notifications/{id}/read`
Mark a single notification as read.

---

### PATCH `/notifications/read-all`
Mark all notifications as read.

---

## Insights  *(requires auth)*

### GET `/insights`
List active (non-dismissed) insights.

---

### POST `/insights/generate`
Trigger on-demand insight generation for the authenticated user. Analyses current month's transactions, budgets, and goals and creates new `Insight` records.

**Response** `200 OK`
```json
{ "success": true, "message": "Insights generated successfully", "data": null }
```

---

### PATCH `/insights/{id}/dismiss`
Dismiss an insight permanently.

---

## Categories  *(requires auth)*

### GET `/categories`
List all categories (system + user-created) with their parent relationships.

---

### POST `/categories`
Create a custom category.

**Request**
```json
{
  "name": "Gym",
  "type": "EXPENSE",
  "icon": "pi-heart",
  "color": "#8b5cf6",
  "parentId": null
}
```

---

### PUT `/categories/{id}` / DELETE `/categories/{id}`
Update or delete a category.

---

## Health Check  *(public)*

### GET `/actuator/health`
```json
{ "status": "UP" }
```
