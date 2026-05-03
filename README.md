# Budgetix

Personal finance management application — Spring Boot 3 backend + Angular 21 frontend + PostgreSQL.

## Documentation

| Document | Description |
|---|---|
| [Features & User Flows](docs/FEATURES.md) | Complete guide to every feature and how-to flows |
| [Business Specification (BSD)](docs/BSD.md) | Functional requirements, business objectives, user stories |
| [Technical Specification (TSD)](docs/TSD.md) | Architecture, DB schema, security design, API contracts |
| [API Reference](docs/API.md) | Full REST endpoint documentation with request/response examples |
| [Production Guide](docs/PRODUCTION.md) | Docker, Nginx, SSL, backups, scaling, hardening checklist |

---

## Prerequisites

| Tool | Version |
|---|---|
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
|---|---|---|
| PostgreSQL | localhost | **5433** |
| Redis | localhost | 6380 |
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
Health check: http://localhost:3000/api/actuator/health

> **Windows tip — if startup fails with `AccessDeniedException` on `.class` files:**  
> A previously killed JVM may have locked files. Fix:
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

Opens on **http://localhost:4200**.

---

## 5. Connect a database GUI

Use **DBeaver**, **pgAdmin**, or any PostgreSQL client with these credentials:

| Field | Value |
|---|---|
| Host | `localhost` |
| Port | `5433` |
| Database | `budgetix` |
| Username | `budgetix` |
| Password | `budgetix_pass` |

> Do **not** use `postgres` as the username — that superuser account does not exist in this container.

---

## 6. Default app credentials

Register a new account at http://localhost:4200, or use the seeded demo user if migrations include one.

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
├── backend/          # Spring Boot 3 / Java 21
│   └── src/main/resources/application.yml
├── frontend/         # Angular 21 standalone
│   └── src/
├── docker-compose.yml
└── .env.example      # reference for environment variables
```

---

## Environment variables (production)

Copy `.env.example` to `.env` and fill in real values before deploying.  
Key variables:

| Variable | Description |
|---|---|
| `DATABASE_URL` | Full JDBC URL |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | DB credentials |
| `JWT_ACCESS_SECRET` / `JWT_REFRESH_SECRET` | Must be ≥ 32 chars |
| `FRONTEND_URL` | Used for CORS + email links |
| `MAIL_HOST` / `MAIL_PORT` | SMTP server |
