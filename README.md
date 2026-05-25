# Budgetix

Personal finance management application — Spring Boot 3 backend + Angular 21 frontend + PostgreSQL.

## Features

| # | Feature | Backend | Frontend |
|---|---------|---------|---------|
| 1 | **Dashboard** — Live KPIs (incl. Net Worth), 3 chart types (bar/donut/line), 6-month trends, projections | ✅ | ✅ |
| 2 | **Transactions** — Full CRUD, notes, tags, filtering, CSV import, bulk delete, auto-categorization | ✅ | ✅ |
| 3 | **Accounts** — 5 account types, auto balance tracking, account-to-account transfers | ✅ | ✅ |
| 4 | **Budgets** — Category limits, progress bars, rollover, 80%/100% alerts | ✅ | ✅ |
| 5 | **Savings Goals** — Targets with deadline, manual contributions, milestone insights | ✅ | ✅ |
| 6 | **Recurring Transactions** — 6 frequencies, hourly scheduler, pause/resume | ✅ | ✅ |
| 7 | **Categories** — Hierarchy, system + custom, icon/color picker, auto-categorization keyword rules | ✅ | ✅ |
| 8 | **Reports** — Monthly analysis, category breakdown, CSV & PDF export | ✅ | ✅ |
| 9 | **Insights** — 11 rule-based types, monthly scheduled + on-demand generation | ✅ | ✅ |
| 10 | **Settings** — Profile, password change, 2FA toggle, notification preferences | ✅ | ✅ |
| 11 | **Financial Calendar** — Monthly day grid with income/expense overlays per day | — | ✅ |

### Auth & Security
- JWT access token (15 min) + refresh token rotation (7 days)
- Email OTP verification on registration
- Two-Factor Authentication (email OTP) on login
- Forgot / reset password flow
- Google OAuth2 sign-in
- Session inactivity timeout (120 s) with countdown dialog
- Route guards (auth + guest)

## Documentation

| Document | Description |
|----------|-------------|
| [Features & User Flows](docs/FEATURES.md) | Complete guide to every feature and how-to flows |
| [Business Specification (BSD)](docs/BSD.md) | Functional requirements, business objectives, user stories |
| [Technical Specification (TSD)](docs/TSD.md) | Architecture, DB schema, security design, API contracts |
| [API Reference](docs/API.md) | Full REST endpoint documentation with request/response examples |
| [Production Guide](docs/PRODUCTION.md) | Docker, Nginx, SSL, backups, scaling, hardening checklist |
| [Bank Account Linking](docs/bank-account-linking.html) | Manual vs live-linking explained, Open Banking flow, Plaid/Tink integration roadmap |
| [User Guide & Source Map](docs/user-guide.html) | Full user guide for every feature, bank linking deep-dive, complete FE↔BE source code map |

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| Angular CLI | `npm i -g @angular/cli` |
| Docker Desktop | Latest |

---

## 1. Clone & configure

```bash
git clone <repo-url>
cd Budgetix
```

No `.env` file is needed for local development — defaults are baked into `application.yml` and `docker-compose.yml`.

---

## 2. Start infrastructure (PostgreSQL + Redis + MailHog)

```bash
docker-compose up -d
```

| Service | Host | Port |
|---------|------|------|
| PostgreSQL | localhost | **5433** |
| Redis | localhost | **6380** |
| MailHog SMTP | localhost | 1025 |
| MailHog Web UI | http://localhost:8025 | — |

Verify containers are healthy:

```bash
docker ps
```

All three should show `healthy` or `Up`.

---

## 3. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on **http://localhost:3000/api**.  
Swagger UI: http://localhost:3000/api/swagger-ui.html  
Health check: http://localhost:3000/api/actuator/health

> **Windows tip — if startup fails with `AccessDeniedException` on `.class` files:**  
> A previously killed JVM may have locked files. Fix:
>
> ```powershell
> Get-Process java | Stop-Process -Force
> Remove-Item -Path "backend\target" -Recurse -Force
> mvn spring-boot:run
> ```

---

## 4. Start the frontend

```bash
cd frontend
npm install        # first time only
ng serve
```

Opens on **http://localhost:4300**.

---

## 5. Connect a database GUI

Use **DBeaver**, **pgAdmin**, or any PostgreSQL client:

| Field | Value |
|-------|-------|
| Host | `localhost` |
| Port | `5433` |
| Database | `budgetix` |
| Username | `budgetix` |
| Password | `budgetix_pass` |

> Do **not** use `postgres` as the username — that superuser account does not exist in this container.

---

## 6. Default app credentials

Register a new account at http://localhost:4300.  
Check http://localhost:8025 (MailHog) to read the OTP verification email.

---

## Stopping everything

```bash
# Stop infrastructure
docker-compose down

# Stop backend / frontend
Ctrl+C in each terminal
```

To wipe the database volume entirely:

```bash
docker-compose down -v
```

---

## Project structure

```
Budgetix/
├── backend/               # Spring Boot 3 / Java 21
│   └── src/main/
│       ├── java/com/budgetix/
│       │   ├── auth/          # JWT, OTP, OAuth2
│       │   ├── user/          # Profile, settings
│       │   ├── account/       # Financial accounts
│       │   ├── transaction/   # CRUD, CSV import
│       │   ├── category/      # Hierarchy + rules
│       │   ├── budget/        # Limits + alerts
│       │   ├── goal/          # Savings goals
│       │   ├── recurring/     # Scheduled transactions
│       │   ├── insight/       # Auto-generated insights
│       │   ├── notification/  # In-app notifications
│       │   ├── report/        # CSV + PDF export
│       │   └── dashboard/     # Aggregated KPIs
│       └── resources/
│           ├── application.yml
│           └── db/migration/  # Flyway V1–V6
├── frontend/              # Angular 21 standalone
│   └── src/app/
│       ├── features/      # dashboard, transactions, accounts,
│       │                  # budgets, goals, recurring, categories,
│       │                  # calendar, reports, insights, settings
│       ├── layout/        # shell, sidebar, topbar
│       └── core/          # services, guards, interceptors, models
├── docs/                  # presentation.html + spec docs
├── docker-compose.yml     # PostgreSQL 16 + Redis 7 + MailHog
└── .env.example
```

---

## Environment variables (production)

Copy `.env.example` to `.env` and fill in real values before deploying.

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | Full JDBC URL |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | DB credentials |
| `REDIS_HOST` / `REDIS_PORT` | Redis connection |
| `JWT_ACCESS_SECRET` / `JWT_REFRESH_SECRET` | Must be ≥ 32 chars |
| `FRONTEND_URL` | Used for CORS + email links |
| `MAIL_HOST` / `MAIL_PORT` | SMTP server |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | OAuth2 (optional) |
