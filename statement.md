# Problem Statement

Many individuals struggle with financial management due to disorganized tracking methods and a lack of proper budgeting tools. People often lose track of their spending patterns, fail to maintain budgets, and lack insight into their overall financial health. This leads to poor financial decisions, unnecessary debt, and an inability to achieve savings goals. There is a clear need for a simple, accessible tool that gives individuals visibility and control over their finances.

# Scope of the Project

The Personal Finance Manager is a Java-based console application that helps users manage their financial activities efficiently. It provides tools to:

- Record and manage income and expense transactions
- Analyze spending patterns and cash flow over time
- Set, track, and evaluate monthly budgets by category

The application uses local file-based storage with basic obfuscation for transaction and budget data, and does not require any external database or internet connection to run. It is scoped as a single-user, offline console tool — it does not cover multi-user accounts, bank integrations, or GUI-based interaction.

# Target Users

- Individuals who want a lightweight, no-setup tool to track personal income and expenses
- Students and early professionals managing a limited, simple budget
- Anyone who prefers a fast, offline console tool over a heavyweight finance app or spreadsheet

# High-Level Features

- **Transaction Management** — add, view, search, and delete income/expense transactions with category and description tagging
- **Financial Analytics** — spending-by-category breakdown, monthly income/expense summaries, and overall cash flow & savings rate analysis
- **Budget Planning** — set or update monthly budget limits per category, track actual vs. planned spending, and get over-budget warnings
- **Data Persistence** — transactions and budgets are automatically saved to local files and reloaded on the next run
- **Input Validation & Error Handling** — guards against invalid amounts, empty/malformed categories, and file I/O errors without crashing
