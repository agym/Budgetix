# Budgetix — User Guide

> Your personal finance companion. Track spending, set budgets, reach savings goals, and understand your money.

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Dashboard](#2-dashboard)
3. [Transactions](#3-transactions)
4. [Accounts](#4-accounts)
5. [Budgets](#5-budgets)
6. [Savings Goals](#6-savings-goals)
7. [Reports](#7-reports)
8. [Insights](#8-insights)
9. [Settings](#9-settings)
10. [Tips & Tricks](#10-tips--tricks)

---

## 1. Getting Started

### Creating your account

1. Open Budgetix and click **Get Started** on the home page.
2. Fill in your **name**, **email address**, and a **password** (at least 8 characters).
3. Click **Create Account**.
4. Check your inbox — you'll receive a **6-digit verification code**.
5. Enter the code on the verification screen and click **Verify**.
6. You're in! You'll be taken straight to your dashboard.

> **Didn't get the email?** Click **Resend Code** on the verification screen and check your spam folder.

---

### Signing in

1. Click **Sign In** on the home page.
2. Enter your email and password.
3. If you have **Two-Factor Authentication** enabled, you'll be asked for a second 6-digit code sent to your email.

---

### Forgot your password?

1. On the sign-in screen, click **Forgot password?**
2. Enter your email address and click **Send Reset Code**.
3. Check your inbox for a 6-digit code.
4. Enter the code, your new password, and confirm it.
5. Click **Reset Password** — you can now sign in with the new password.

---

### Staying signed in / Session timeout

Budgetix will warn you if you've been inactive for a while. A dialog will appear with a countdown timer.

- Click **Continue Session** to keep working.
- Click **Sign Out** if you're done.

If you don't respond in time, you'll be signed out automatically for security.

---

## 2. Dashboard

The dashboard is your financial command centre. It updates automatically every time you open it.

### What you'll see

**KPI Cards (top row)**

| Card | What it means |
|------|---------------|
| Total Income | All money you received this month |
| Total Expenses | All money you spent this month |
| Net Savings | Income minus Expenses |
| Net Worth | Combined balance across all your accounts |

Each card shows an arrow and percentage indicating how you're doing compared to last month.

---

**Projection Strip (second row)**

| Metric | What it means |
|--------|---------------|
| Avg Daily Spend | How much you're spending per day on average |
| Projected Month Expense | Estimated total spending by end of month |
| Projected End Balance | Estimated net worth at month end |
| Burn Rate Warning | Appears if your spending trend would bring your balance to $0 — shows how many days away that is |

---

**Charts (bottom section)**

- **Income vs Expenses** — A 6-month bar chart so you can spot spending trends at a glance. Green = income, red = expenses.
- **Spending by Category** — A donut chart showing where your money went this month (Food, Transport, Housing, etc.).
- **Daily Trend** — A day-by-day view of income, expenses, and your running net balance for the current month.

---

## 3. Transactions

**Route:** sidebar → *Transactions*

This is the full log of every financial movement — income, expenses, and transfers.

### Browsing your transactions

- Transactions are shown 20 at a time, newest first.
- Use the **search bar** to find a transaction by description.
- Use the **Type** dropdown to show only Income, Expense, or Transfer.
- Use the **Account** dropdown to filter by a specific account.
- Click a column header to sort.

---

### Adding a transaction

1. Click **Add Transaction** (top right).
2. Fill in the form:

| Field | Required? | Notes |
|-------|-----------|-------|
| Type | Yes | Income, Expense, or Transfer |
| Amount | Yes | Must be greater than 0 |
| Account | Yes | Which account the money came from/went to |
| Category | No | Helps with budgets and reports |
| Description | No | e.g. "Groceries at Lidl" |
| Date | Yes | Defaults to today |

3. Click **Create**.

---

### Editing a transaction

Click the **pencil icon** on any row, update the fields, and click **Save**.

---

### Deleting a transaction

- **Single:** Click the **trash icon** on the row and confirm.
- **Multiple:** Tick the checkboxes on the left, then click **Delete Selected** at the bottom of the table.

> Deleting a transaction automatically adjusts your account balance and updates any linked budget.

---

### Importing from a CSV file

If your bank lets you export transactions as a CSV:

1. Click **Import CSV**.
2. Select the **account** these transactions belong to.
3. Click **Choose CSV File** and select your file.

   Expected format (one row per transaction):
   ```
   date,description,amount,type
   2025-04-01,Salary,3000,INCOME
   2025-04-03,Supermarket,45.50,EXPENSE
   ```

4. Click **Import**. A toast message will confirm how many transactions were added.

---

## 4. Accounts

**Route:** sidebar → *Accounts*

Accounts represent real-world places your money lives — bank accounts, cash wallets, credit cards, and so on.

### Account types

| Type | Use it for |
|------|-----------|
| Bank | Checking / current accounts |
| Cash | Physical cash on hand |
| Credit Card | Cards you pay off monthly |
| Savings | Savings / deposit accounts |
| Investment | Stocks, crypto, pension funds |

---

### Adding an account

1. Click **Add Account**.
2. Fill in:
   - **Name** — e.g. "Barclays Current"
   - **Type** — pick from the list above
   - **Initial Balance** — your current balance in that account
   - **Currency** — 3-letter code (USD, EUR, GBP…)
   - **Color** — helps visually identify accounts in lists and charts
3. Click **Create**.

---

### Editing an account

Click the **pencil icon** on the account card to update its name, type, currency, or colour.

> You cannot edit the balance directly — it changes automatically when you add or delete transactions.

---

### Deleting an account

Click the **trash icon** and confirm. You cannot delete an account that has transactions linked to it — remove or reassign those transactions first.

---

## 5. Budgets

**Route:** sidebar → *Budgets*

Budgets let you set spending limits for categories each month and track how close you are to the limit.

### Selecting a period

Use the **Month** and **Year** dropdowns at the top to view any month's budgets. Defaults to the current month.

---

### Understanding the budget card

Each budget card shows:
- The **category** it covers (or "Global Budget" if it covers everything).
- **Spent / Limit** — how much you've used vs how much you allowed.
- A **progress bar** that changes colour:
  - Blue/primary — under 80%
  - Orange — 80–99%
  - Red — 100% or over
- **% used** and **amount remaining**.
- A note if any amount was **rolled over** from last month.

---

### Creating a budget

1. Click **Add Budget**.
2. Choose a **Category** (or leave it blank for a budget that covers all spending).
3. Enter the **Amount** you want to allow for the month.
4. Toggle **Enable Rollover** if you want any unspent balance to carry over to next month.
5. Click **Create**.

---

### Deleting a budget

Click the **trash icon** on the card and confirm. This only removes the budget rule — your transactions are not affected.

---

### Rollover explained

If you budget $500 for Groceries and only spend $420, with rollover enabled next month's budget starts at **$580** ($500 + $80 unspent).

---

## 6. Savings Goals

**Route:** sidebar → *Goals*

Goals let you save towards something specific — a holiday, an emergency fund, a new laptop — and track your progress.

### Creating a goal

1. Click **New Goal**.
2. Fill in:
   - **Goal Name** — e.g. "Holiday to Japan"
   - **Target Amount** — how much you need to save
   - **Deadline** — optional target date
   - **Color** — pick a colour for the card accent
3. Click **Create Goal**.

---

### Adding a contribution

When you've saved some money towards a goal:

1. Click **Contribute** on the goal card.
2. Enter the **Amount** you're putting in.
3. Optionally add a **Note** (e.g. "Monthly transfer").
4. Click **Add**.

The progress bar and remaining amount update immediately.

---

### Goal statuses

| Status | Meaning |
|--------|---------|
| Active | In progress |
| Completed | You've reached the target amount |
| Paused | Temporarily on hold |
| Cancelled | No longer pursuing this goal |

---

### Deleting a goal

Click the **trash icon** on an Active goal card and confirm.

---

## 7. Reports

**Route:** sidebar → *Reports*

Reports give you a detailed financial summary for any month you choose.

### Selecting a period

Use the **Month** and **Year** dropdowns at the top. The report loads automatically when you change either.

---

### What the report shows

**Summary row**
Total Income · Total Expenses · Net Savings · Savings Rate

Your savings rate is rated:
- **Great!** (green) — 20% or above
- **Average** — 10–19%
- **Low** (red) — below 10%

**Charts**
- Spending by Category donut chart.
- Savings Rate gauge (0–100%).

**Category Breakdown Table**
A sortable table showing every spending category: amount spent, percentage of total expenses, and number of transactions. Click any column header to sort.

---

### Exporting your report

| Button | What you get |
|--------|-------------|
| **Export CSV** | A spreadsheet you can open in Excel or Google Sheets |
| **Export PDF** | A formatted PDF you can save or share |

Both files are named `report-{year}-{month}` and download automatically.

---

## 8. Insights

**Route:** sidebar → *Insights*

Insights are smart observations that Budgetix generates by analysing your transactions, budgets, and goals.

### Types of insights

| Colour | Type | Example |
|--------|------|---------|
| Green | Positive | "Your savings rate of 32% is excellent!" |
| Red | Negative | "Spending increased 24% vs last month" |
| Blue | Info | "You have 4 recurring subscriptions totalling $87/month" |
| Orange | Warning | "Your income varied significantly this month" |

---

### Generating insights

Click **Generate Insights** at the top right. Budgetix will analyse your recent finances and add new observations to the list.

> Insights are also generated automatically on the **1st of every month**.

---

### Dismissing an insight

Click the **✕ button** on any insight card to remove it from your list. Dismissed insights won't come back.

---

## 9. Settings

**Route:** sidebar → *Settings* (or click your name in the topbar)

### Profile tab

Update your display name, default currency, monthly income, and timezone. Click **Save Profile** when done.

---

### Security tab

**Change your password**
1. Enter your **current password**.
2. Enter and confirm your **new password** (min 8 characters).
3. Click **Change Password**.

**Two-Factor Authentication (2FA)**
- When enabled, every sign-in requires a second 6-digit code sent to your email.
- Click **Enable 2FA** / **Disable 2FA** to toggle. A confirmation tag shows the current state.

---

### Notifications tab

**Preference toggles** — choose which types of alerts you want to receive:

| Toggle | What triggers it |
|--------|-----------------|
| Budget Alerts | When you're approaching or exceeding a budget limit |
| Goal Milestones | When you hit a savings milestone |
| Weekly Summary | A weekly email digest |
| Large Transactions | When a single transaction is unusually large |

Click **Save Preferences** after changing any toggle.

**Notification history** — a table of past notifications. You can:
- Mark individual notifications as read (✓ button).
- Click **Mark all read** to clear the badge at once.
- Delete notifications you no longer need (trash icon).

---

## 10. Tips & Tricks

**Set up accounts first**
Before adding transactions, create at least one account so you have somewhere to assign transactions.

**Use categories consistently**
Assigning a category to every transaction makes the Spending by Category chart and Report breakdown much more useful.

**Let budgets guide you**
Create a budget for your biggest spending categories (Food, Transport, Housing). The progress bar turning orange is an early warning that you're approaching the limit.

**Check the Dashboard projection strip**
If "Projected End Balance" is lower than expected, review your Daily Trend chart to spot what's driving the increase.

**Generate insights after a big month**
After a month with unusual spending or income, click Generate Insights to get a personalised analysis.

**Use the CSV import for past data**
If you're starting mid-year, export transactions from your bank's online portal and import them to catch up your history instantly.

**Switch language anytime**
Click the language flag in the top bar to switch between English, French, and Arabic (RTL layout switches automatically).

**Keyboard shortcut**
Press `Escape` to close any open modal or dialog.

---

*For technical details or API documentation, see [FEATURES.md](FEATURES.md).*
