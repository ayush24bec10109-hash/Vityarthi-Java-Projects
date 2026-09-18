package app;

import java.util.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import model.Transaction;
import model.TransactionType;
import model.Budget;
import service.FileManager;

// Main PersonalFinanceManager class
public class PersonalFinanceManager {
    private List<Transaction> transactions;
    private List<Budget> budgets;
    private Scanner scanner;

    public PersonalFinanceManager() {
        this.transactions = FileManager.loadTransactions();
        this.budgets = FileManager.loadBudgets();
        this.scanner = new Scanner(System.in);
        updateBudgetSpending();
    }

    public void run() {
        System.out.println("Welcome to Personal Finance Manager!");
        System.out.println("Your data is securely loaded. " + transactions.size() + " transactions found.");

        while (true) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        transactionManagement();
                        break;
                    case "2":
                        financialAnalytics();
                        break;
                    case "3":
                        budgetPlanning();
                        break;
                    case "4":
                        System.out.println("Saving your data...");
                        FileManager.saveTransactions(transactions);
                        FileManager.saveBudgets(budgets);
                        System.out.println("Thank you for using Personal Finance Manager!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter 1-4.");
                }
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("            MAIN MENU");
        System.out.println("=".repeat(50));
        System.out.println("1. Transaction Management");
        System.out.println("2. Financial Analytics");
        System.out.println("3. Budget Planning");
        System.out.println("4. Exit");
        System.out.println("-".repeat(50));
        System.out.print("Choose option (1-4): ");
    }

    // Transaction Management Module
    private void transactionManagement() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("      TRANSACTION MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1. Add Transaction");
            System.out.println("2. View All Transactions");
            System.out.println("3. Search Transactions");
            System.out.println("4. Delete Transaction");
            System.out.println("5. View Current Balance");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose option (1-6): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    addTransaction();
                    break;
                case "2":
                    viewAllTransactions();
                    break;
                case "3":
                    searchTransactions();
                    break;
                case "4":
                    deleteTransaction();
                    break;
                case "5":
                    viewCurrentBalance();
                    break;
                case "6":
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1-6.");
            }
        }
    }

    private void addTransaction() {
        System.out.println("\n--- Add New Transaction ---");

        // Transaction Type
        TransactionType type = null;
        while (type == null) {
            System.out.print("Type (INCOME/EXPENSE): ");
            String typeInput = scanner.nextLine().trim().toUpperCase();
            try {
                type = TransactionType.valueOf(typeInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Please enter either INCOME or EXPENSE");
            }
        }

        // Amount
        double amount = 0;
        while (amount == 0) {
            System.out.print("Amount: $");
            String amountInput = scanner.nextLine().trim();
            if (validateAmount(amountInput)) {
                amount = Double.parseDouble(amountInput);
            }
        }

        // Category
        String category = null;
        while (category == null) {
            System.out.print("Category: ");
            category = scanner.nextLine().trim();
            if (!validateCategory(category)) {
                category = null;
            }
        }

        // Description
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            description = "No description";
        }

        // Create and add transaction
        Transaction transaction = new Transaction(type, amount, category, description);
        transactions.add(transaction);

        // Update budget spending if it's an expense
        if (type == TransactionType.EXPENSE) {
            updateBudgetForCategory(category, amount);
        }

        System.out.println("Transaction added successfully!");
        System.out.println("Details: " + transaction);
        
        // Auto-save
        FileManager.saveTransactions(transactions);
        FileManager.saveBudgets(budgets);
    }

    private void viewAllTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("\n--- All Transactions (" + transactions.size() + ") ---");
        System.out.printf("%-12s %-15s %-12s %-10s %s%n", 
            "Date", "ID", "Category", "Amount", "Description");
        System.out.println("-".repeat(80));

        double totalIncome = 0;
        double totalExpenses = 0;

        for (Transaction t : transactions) {
            String amountStr = (t.getType() == TransactionType.INCOME ? "+$" : "-$") + 
                             String.format("%.2f", t.getAmount());
            System.out.printf("%-12s %-15s %-12s %-10s %s%n",
                t.getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                t.getId().substring(0, 8) + "...",
                t.getCategory(),
                amountStr,
                t.getDescription());

            if (t.getType() == TransactionType.INCOME) {
                totalIncome += t.getAmount();
            } else {
                totalExpenses += t.getAmount();
            }
        }

        System.out.println("-".repeat(80));
        System.out.printf("Total Income: +$%.2f | Total Expenses: -$%.2f | Net: $%.2f%n",
            totalIncome, totalExpenses, (totalIncome - totalExpenses));
    }

    private void searchTransactions() {
        System.out.print("Enter search term (category/description): ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getCategory().toLowerCase().contains(searchTerm) ||
                t.getDescription().toLowerCase().contains(searchTerm)) {
                results.add(t);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No transactions found matching: " + searchTerm);
            return;
        }

        System.out.println("\n--- Search Results (" + results.size() + " transactions) ---");
        for (Transaction result : results) {
            System.out.println(result);
        }
    }

    private void deleteTransaction() {
        viewAllTransactions();
        if (transactions.isEmpty()) return;

        System.out.print("Enter transaction ID to delete: ");
        String id = scanner.nextLine().trim();

        Transaction toRemove = null;
        for (Transaction t : transactions) {
            if (t.getId().equals(id)) {
                toRemove = t;
                break;
            }
        }

        if (toRemove != null) {
            transactions.remove(toRemove);
            
            // Update budget if it was an expense
            if (toRemove.getType() == TransactionType.EXPENSE) {
                updateBudgetSpending();
            }
            
            FileManager.saveTransactions(transactions);
            FileManager.saveBudgets(budgets);
            System.out.println("Transaction deleted successfully!");
        } else {
            System.out.println("Transaction not found with ID: " + id);
        }
    }

    private void viewCurrentBalance() {
        double totalIncome = 0;
        double totalExpenses = 0;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome += t.getAmount();
            } else {
                totalExpenses += t.getAmount();
            }
        }

        double balance = totalIncome - totalExpenses;

        System.out.println("\n--- Current Financial Summary ---");
        System.out.printf("Total Income:  +$%.2f%n", totalIncome);
        System.out.printf("Total Expenses: -$%.2f%n", totalExpenses);
        System.out.printf("Current Balance: $%.2f%n", balance);
        
        if (balance < 0) {
            System.out.println("Warning: You have a negative balance!");
        }
    }

    // Financial Analytics Module
    private void financialAnalytics() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("        FINANCIAL ANALYTICS");
            System.out.println("=".repeat(50));
            System.out.println("1. Spending by Category");
            System.out.println("2. Monthly Summary");
            System.out.println("3. Cash Flow Analysis");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose option (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    spendingByCategory();
                    break;
                case "2":
                    monthlySummary();
                    break;
                case "3":
                    cashFlowAnalysis();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1-4.");
            }
        }
    }

    private void spendingByCategory() {
        Map<String, Double> categorySpending = new HashMap<>();
        Map<String, Double> categoryIncome = new HashMap<>();

        for (Transaction t : transactions) {
            String category = t.getCategory();
            if (t.getType() == TransactionType.EXPENSE) {
                categorySpending.put(category, 
                    categorySpending.getOrDefault(category, 0.0) + t.getAmount());
            } else {
                categoryIncome.put(category,
                    categoryIncome.getOrDefault(category, 0.0) + t.getAmount());
            }
        }

        System.out.println("\n--- Spending by Category ---");
        if (categorySpending.isEmpty()) {
            System.out.println("No expense data available.");
        } else {
            List<Map.Entry<String, Double>> sortedSpending = new ArrayList<>(categorySpending.entrySet());
            sortedSpending.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            
            for (Map.Entry<String, Double> entry : sortedSpending) {
                System.out.printf("%-15s: $%-8.2f%n", entry.getKey(), entry.getValue());
            }
        }

        System.out.println("\n--- Income by Category ---");
        if (categoryIncome.isEmpty()) {
            System.out.println("No income data available.");
        } else {
            List<Map.Entry<String, Double>> sortedIncome = new ArrayList<>(categoryIncome.entrySet());
            sortedIncome.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            
            for (Map.Entry<String, Double> entry : sortedIncome) {
                System.out.printf("%-15s: $%-8.2f%n", entry.getKey(), entry.getValue());
            }
        }
    }

    private void monthlySummary() {
        Map<YearMonth, Double> monthlyIncome = new HashMap<>();
        Map<YearMonth, Double> monthlyExpenses = new HashMap<>();

        for (Transaction t : transactions) {
            YearMonth month = YearMonth.from(t.getDate());
            if (t.getType() == TransactionType.INCOME) {
                monthlyIncome.put(month, 
                    monthlyIncome.getOrDefault(month, 0.0) + t.getAmount());
            } else {
                monthlyExpenses.put(month, 
                    monthlyExpenses.getOrDefault(month, 0.0) + t.getAmount());
            }
        }

        System.out.println("\n--- Monthly Summary ---");
        if (monthlyIncome.isEmpty() && monthlyExpenses.isEmpty()) {
            System.out.println("No transaction data available.");
            return;
        }

        Set<YearMonth> allMonths = new TreeSet<>(monthlyIncome.keySet());
        allMonths.addAll(monthlyExpenses.keySet());

        for (YearMonth month : allMonths) {
            double income = monthlyIncome.getOrDefault(month, 0.0);
            double expenses = monthlyExpenses.getOrDefault(month, 0.0);
            double net = income - expenses;
            
            System.out.printf("%s: Income: $%.2f | Expenses: $%.2f | Net: $%.2f%n",
                month, income, expenses, net);
        }
    }

    private void cashFlowAnalysis() {
        double totalIncome = 0;
        double totalExpenses = 0;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome += t.getAmount();
            } else {
                totalExpenses += t.getAmount();
            }
        }

        double netCashFlow = totalIncome - totalExpenses;
        double savingsRate = totalIncome > 0 ? (netCashFlow / totalIncome) * 100 : 0;

        System.out.println("\n--- Cash Flow Analysis ---");
        System.out.printf("Total Cash Inflow:  $%.2f%n", totalIncome);
        System.out.printf("Total Cash Outflow: $%.2f%n", totalExpenses);
        System.out.printf("Net Cash Flow:      $%.2f%n", netCashFlow);
        System.out.printf("Savings Rate:       %.1f%%%n", savingsRate);

        if (savingsRate > 20) {
            System.out.println("Excellent savings rate! Keep it up!");
        } else if (savingsRate > 0) {
            System.out.println("Positive savings rate. Good job!");
        } else {
            System.out.println("Negative savings rate. Consider reducing expenses.");
        }
    }

    // Budget Planning Module
    private void budgetPlanning() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("         BUDGET PLANNING");
            System.out.println("=".repeat(50));
            System.out.println("1. Set/Update Budget");
            System.out.println("2. View Budget Status");
            System.out.println("3. Budget vs Actual");
            System.out.println("4. Remove Budget");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choose option (1-5): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    setOrUpdateBudget();
                    break;
                case "2":
                    viewBudgetStatus();
                    break;
                case "3":
                    budgetVsActual();
                    break;
                case "4":
                    removeBudget();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1-5.");
            }
        }
    }

    private void setOrUpdateBudget() {
        System.out.print("Enter category for budget: ");
        String category = scanner.nextLine().trim().toUpperCase();

        if (!validateCategory(category)) {
            return;
        }

        System.out.print("Enter monthly budget limit: $");
        String limitInput = scanner.nextLine().trim();
        if (!validateBudgetLimit(limitInput)) {
            return;
        }

        double limit = Double.parseDouble(limitInput);

        // Check if budget already exists
        Budget existingBudget = null;
        for (Budget budget : budgets) {
            if (budget.getCategory().equals(category)) {
                existingBudget = budget;
                break;
            }
        }

        if (existingBudget != null) {
            existingBudget.setMonthlyLimit(limit);
            existingBudget.resetSpending();
            updateBudgetSpending();
            System.out.println("Budget updated for category: " + category);
        } else {
            Budget newBudget = new Budget(category, limit);
            updateBudgetForCategory(category, 0);
            budgets.add(newBudget);
            System.out.println("New budget created for category: " + category);
        }

        FileManager.saveBudgets(budgets);
    }

    private void viewBudgetStatus() {
        if (budgets.isEmpty()) {
            System.out.println("No budgets set. Create a budget first!");
            return;
        }

        System.out.println("\n--- Current Budget Status ---");
        for (Budget budget : budgets) {
            System.out.println(budget);
            if (budget.isOverBudget()) {
                System.out.println("   You've exceeded your budget by $" + 
                    String.format("%.2f", Math.abs(budget.getRemainingBudget())));
            }
        }
    }

    private void budgetVsActual() {
        if (budgets.isEmpty()) {
            System.out.println("No budgets set. Create a budget first!");
            return;
        }

        System.out.println("\n--- Budget vs Actual Spending ---");
        for (Budget budget : budgets) {
            double actual = budget.getCurrentSpending();
            double planned = budget.getMonthlyLimit();
            double variance = planned - actual;
            double percentage = budget.getUsagePercentage();

            System.out.printf("%s:%n", budget.getCategory());
            System.out.printf("   Planned: $%.2f | Actual: $%.2f | Variance: $%.2f%n",
                planned, actual, variance);
            System.out.printf("   Usage: %.1f%% %s%n", percentage,
                percentage > 100 ? "(OVER)" : percentage > 90 ? "(WARNING)" : "(GOOD)");
            System.out.println();
        }
    }

    private void removeBudget() {
        viewBudgetStatus();
        if (budgets.isEmpty()) return;

        System.out.print("Enter category to remove budget: ");
        String category = scanner.nextLine().trim().toUpperCase();

        Budget toRemove = null;
        for (Budget budget : budgets) {
            if (budget.getCategory().equals(category)) {
                toRemove = budget;
                break;
            }
        }

        if (toRemove != null) {
            budgets.remove(toRemove);
            FileManager.saveBudgets(budgets);
            System.out.println("Budget removed for category: " + category);
        } else {
            System.out.println("No budget found for category: " + category);
        }
    }

    // Validation Methods
    private boolean validateAmount(String input) {
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                System.out.println("Error: Amount must be positive");
                return false;
            }
            if (amount > 1000000) {
                System.out.println("Error: Amount too large (max: 1,000,000)");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number");
            return false;
        }
    }

    private boolean validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            System.out.println("Error: Category cannot be empty");
            return false;
        }
        if (category.length() > 20) {
            System.out.println("Error: Category too long (max: 20 characters)");
            return false;
        }
        if (category.contains(",") || category.contains(";")) {
            System.out.println("Error: Category cannot contain special characters");
            return false;
        }
        return true;
    }

    private boolean validateBudgetLimit(String input) {
        try {
            double limit = Double.parseDouble(input);
            if (limit < 0) {
                System.out.println("Error: Budget limit cannot be negative");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number for budget");
            return false;
        }
    }

    // Helper Methods
    private void updateBudgetSpending() {
        // Reset all budget spending
        for (Budget budget : budgets) {
            budget.resetSpending();
        }

        // Update spending from transactions
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.EXPENSE) {
                updateBudgetForCategory(transaction.getCategory(), transaction.getAmount());
            }
        }
    }

    private void updateBudgetForCategory(String category, double amount) {
        for (Budget budget : budgets) {
            if (budget.getCategory().equals(category)) {
                budget.addSpending(amount);
                break;
            }
        }
    }

    // Main Method
    public static void main(String[] args) {
        try {
            PersonalFinanceManager manager = new PersonalFinanceManager();
            manager.run();
        } catch (Exception e) {
            System.out.println("Critical error: " + e.getMessage());
            System.out.println("Please restart the application.");
        }
    }
}
