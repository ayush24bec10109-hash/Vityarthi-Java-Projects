package model;

// Budget class
public class Budget {
    private String category;
    private double monthlyLimit;
    private double currentSpending;

    public Budget(String category, double monthlyLimit) {
        this.category = category.toUpperCase();
        this.monthlyLimit = monthlyLimit;
        this.currentSpending = 0.0;
    }

    public String getCategory() { return category; }
    public double getMonthlyLimit() { return monthlyLimit; }
    public double getCurrentSpending() { return currentSpending; }
    public void setMonthlyLimit(double limit) { this.monthlyLimit = limit; }
    public void addSpending(double amount) { this.currentSpending += amount; }
    public void resetSpending() { this.currentSpending = 0.0; }

    public double getRemainingBudget() {
        return monthlyLimit - currentSpending;
    }

    public boolean isOverBudget() {
        return currentSpending > monthlyLimit;
    }

    public double getUsagePercentage() {
        if (monthlyLimit == 0) return 0;
        return (currentSpending / monthlyLimit) * 100;
    }

    @Override
    public String toString() {
        String status = isOverBudget() ? "OVER BUDGET" : "WITHIN BUDGET";
        return String.format("%-15s | $%-8.2f / $%-8.2f | $%-8.2f remaining | %s", 
            category, currentSpending, monthlyLimit, getRemainingBudget(), status);
    }

    public String toFileString() {
        return String.format("%s,%.2f,%.2f", category, monthlyLimit, currentSpending);
    }

    public static Budget fromFileString(String fileString) {
        try {
            String[] parts = fileString.split(",");
            if (parts.length != 3) return null;
            
            String category = parts[0];
            double limit = Double.parseDouble(parts[1]);
            double spending = Double.parseDouble(parts[2]);
            
            Budget budget = new Budget(category, limit);
            budget.currentSpending = spending;
            return budget;
        } catch (Exception e) {
            return null;
        }
    }
}
