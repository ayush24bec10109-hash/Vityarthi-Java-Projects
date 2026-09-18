package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Transaction class
public class Transaction {
    private String id;
    private TransactionType type;
    private double amount;
    private String category;
    private String description;
    private LocalDate date;

    public Transaction(TransactionType type, double amount, String category, String description) {
        this.id = generateId();
        this.type = type;
        this.amount = amount;
        this.category = category.toUpperCase();
        this.description = description;
        this.date = LocalDate.now();
    }

    public Transaction(String id, TransactionType type, double amount, String category, String description, LocalDate date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    private String generateId() {
        return "TXN_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public String getId() { return id; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String typeSymbol = type == TransactionType.INCOME ? "+" : "-";
        return String.format("%s | %s | %-10s | %s$%-8.2f | %s", 
            date.format(formatter), id.substring(0, 8) + "...", category, typeSymbol, amount, description);
    }

    public String toFileString() {
        return String.format("%s,%s,%.2f,%s,%s,%s", 
            id, type, amount, category, description, date);
    }

    public static Transaction fromFileString(String fileString) {
        try {
            String[] parts = fileString.split(",");
            if (parts.length != 6) return null;
            
            String id = parts[0];
            TransactionType type = TransactionType.valueOf(parts[1]);
            double amount = Double.parseDouble(parts[2]);
            String category = parts[3];
            String description = parts[4];
            LocalDate date = LocalDate.parse(parts[5]);
            
            return new Transaction(id, type, amount, category, description, date);
        } catch (Exception e) {
            return null;
        }
    }
}
