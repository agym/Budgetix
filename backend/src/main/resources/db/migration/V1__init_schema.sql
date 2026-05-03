-- ============================================================
--  Budgetix – Initial Schema
-- ============================================================

-- Users
CREATE TABLE users (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    avatar          VARCHAR(500),
    email_verified  BOOLEAN      DEFAULT FALSE,
    two_factor_enabled BOOLEAN   DEFAULT FALSE,
    two_factor_secret  VARCHAR(255),
    role            VARCHAR(50)  DEFAULT 'USER',
    created_at      TIMESTAMP    DEFAULT NOW(),
    updated_at      TIMESTAMP    DEFAULT NOW()
);

-- User Profiles
CREATE TABLE user_profiles (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID        UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    currency                VARCHAR(10) DEFAULT 'USD',
    monthly_income          DECIMAL(15,2),
    financial_goal_type     VARCHAR(50),
    notify_budget_alerts    BOOLEAN     DEFAULT TRUE,
    notify_goal_reminders   BOOLEAN     DEFAULT TRUE,
    notify_weekly_summary   BOOLEAN     DEFAULT TRUE,
    timezone                VARCHAR(100) DEFAULT 'UTC',
    created_at              TIMESTAMP   DEFAULT NOW(),
    updated_at              TIMESTAMP   DEFAULT NOW()
);

-- Refresh Tokens
CREATE TABLE refresh_tokens (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(512) UNIQUE NOT NULL,
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- OTP Codes (email verification, password reset, 2FA)
CREATE TABLE otp_codes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code        VARCHAR(10) NOT NULL,
    type        VARCHAR(50) NOT NULL,
    expires_at  TIMESTAMP   NOT NULL,
    used        BOOLEAN     DEFAULT FALSE,
    created_at  TIMESTAMP   DEFAULT NOW()
);

-- Accounts
CREATE TABLE accounts (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    balance     DECIMAL(15,2) DEFAULT 0,
    currency    VARCHAR(10)  DEFAULT 'USD',
    color       VARCHAR(7),
    icon        VARCHAR(50),
    is_default  BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    DEFAULT NOW(),
    updated_at  TIMESTAMP    DEFAULT NOW()
);

-- Categories (supports parent-child hierarchy)
CREATE TABLE categories (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         REFERENCES users(id) ON DELETE CASCADE,
    parent_id   UUID         REFERENCES categories(id) ON DELETE SET NULL,
    name        VARCHAR(255) NOT NULL,
    icon        VARCHAR(50),
    color       VARCHAR(7),
    type        VARCHAR(50)  NOT NULL,
    is_system   BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    DEFAULT NOW(),
    updated_at  TIMESTAMP    DEFAULT NOW()
);

-- Auto-Categorization Rules
CREATE TABLE auto_categorization_rules (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID         NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    keyword     VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- Transactions
CREATE TABLE transactions (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id      UUID          NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    category_id     UUID          REFERENCES categories(id) ON DELETE SET NULL,
    amount          DECIMAL(15,2) NOT NULL,
    type            VARCHAR(50)   NOT NULL,
    description     VARCHAR(500),
    notes           TEXT,
    date            TIMESTAMP     NOT NULL,
    receipt         VARCHAR(500),
    is_recurring    BOOLEAN       DEFAULT FALSE,
    recurring_id    UUID,
    created_at      TIMESTAMP     DEFAULT NOW(),
    updated_at      TIMESTAMP     DEFAULT NOW()
);

-- Transaction Tags (many-to-one via element collection)
CREATE TABLE transaction_tags (
    transaction_id  UUID        NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    tag             VARCHAR(100) NOT NULL,
    PRIMARY KEY (transaction_id, tag)
);

-- Budgets
CREATE TABLE budgets (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     UUID          REFERENCES categories(id) ON DELETE SET NULL,
    amount          DECIMAL(15,2) NOT NULL,
    spent           DECIMAL(15,2) DEFAULT 0,
    period          VARCHAR(20)   DEFAULT 'MONTHLY',
    month           INTEGER,
    year            INTEGER,
    rollover        BOOLEAN       DEFAULT FALSE,
    rolled_amount   DECIMAL(15,2) DEFAULT 0,
    created_at      TIMESTAMP     DEFAULT NOW(),
    updated_at      TIMESTAMP     DEFAULT NOW(),
    CONSTRAINT uq_budget_user_category_period UNIQUE (user_id, category_id, year, month)
);

-- Budget Alerts (80% / 100% thresholds)
CREATE TABLE budget_alerts (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    budget_id       UUID          NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    threshold       DECIMAL(5,2)  NOT NULL,
    triggered       BOOLEAN       DEFAULT FALSE,
    triggered_at    TIMESTAMP,
    created_at      TIMESTAMP     DEFAULT NOW()
);

-- Savings Goals
CREATE TABLE savings_goals (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(255)  NOT NULL,
    target_amount   DECIMAL(15,2) NOT NULL,
    current_amount  DECIMAL(15,2) DEFAULT 0,
    deadline        TIMESTAMP,
    icon            VARCHAR(50),
    color           VARCHAR(7),
    status          VARCHAR(20)   DEFAULT 'ACTIVE',
    created_at      TIMESTAMP     DEFAULT NOW(),
    updated_at      TIMESTAMP     DEFAULT NOW()
);

-- Goal Contributions
CREATE TABLE goal_contributions (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id         UUID          NOT NULL REFERENCES savings_goals(id) ON DELETE CASCADE,
    transaction_id  UUID          UNIQUE REFERENCES transactions(id) ON DELETE SET NULL,
    amount          DECIMAL(15,2) NOT NULL,
    note            TEXT,
    date            TIMESTAMP     DEFAULT NOW()
);

-- Recurring Transactions
CREATE TABLE recurring_transactions (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id      UUID          NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    category_id     UUID          REFERENCES categories(id) ON DELETE SET NULL,
    amount          DECIMAL(15,2) NOT NULL,
    type            VARCHAR(50)   NOT NULL,
    description     VARCHAR(500)  NOT NULL,
    frequency       VARCHAR(20)   NOT NULL,
    start_date      TIMESTAMP     NOT NULL,
    end_date        TIMESTAMP,
    last_run        TIMESTAMP,
    next_run        TIMESTAMP     NOT NULL,
    is_active       BOOLEAN       DEFAULT TRUE,
    created_at      TIMESTAMP     DEFAULT NOW(),
    updated_at      TIMESTAMP     DEFAULT NOW()
);

-- Notifications
CREATE TABLE notifications (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    message     TEXT         NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    read        BOOLEAN      DEFAULT FALSE,
    data        JSONB,
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- Insights
CREATE TABLE insights (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(50)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    message     TEXT         NOT NULL,
    data        JSONB,
    period      VARCHAR(20),
    dismissed   BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- ============================================================
--  Indexes
-- ============================================================
CREATE INDEX idx_transactions_user_date     ON transactions(user_id, date DESC);
CREATE INDEX idx_transactions_category      ON transactions(category_id);
CREATE INDEX idx_transactions_account       ON transactions(account_id);
CREATE INDEX idx_transactions_type          ON transactions(user_id, type);
CREATE INDEX idx_budgets_user_period        ON budgets(user_id, year, month);
CREATE INDEX idx_notifications_user_read    ON notifications(user_id, read);
CREATE INDEX idx_insights_user_dismissed    ON insights(user_id, dismissed);
CREATE INDEX idx_recurring_next_run         ON recurring_transactions(next_run, is_active);
CREATE INDEX idx_categories_user_parent     ON categories(user_id, parent_id);
CREATE INDEX idx_refresh_tokens_user        ON refresh_tokens(user_id);
CREATE INDEX idx_otp_codes_user_type        ON otp_codes(user_id, type);
