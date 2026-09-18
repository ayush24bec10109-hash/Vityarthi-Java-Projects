package service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Base64;

import model.Budget;
import model.Transaction;

// FileManager class
public class FileManager {
    private static final String TRANSACTIONS_FILE = "transactions.dat";
    private static final String BUDGETS_FILE = "budgets.dat";
    private static final String ENCRYPTION_KEY = "MySuperSecretKey123";

    // NOTE: this is a simple XOR + Base64 obfuscation, not cryptographically
    // secure encryption. It deters casual viewing of the raw data file but
    // should not be relied on to protect sensitive financial data.
    private static String simpleEncrypt(String data) {
        try {
            byte[] keyBytes = ENCRYPTION_KEY.getBytes();
            byte[] dataBytes = data.getBytes();
            byte[] encrypted = new byte[dataBytes.length];
            
            for (int i = 0; i < dataBytes.length; i++) {
                encrypted[i] = (byte) (dataBytes[i] ^ keyBytes[i % keyBytes.length]);
            }
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.out.println("Warning: Encryption failed, saving as plain text");
            return data;
        }
    }

    private static String simpleDecrypt(String encryptedData) {
        try {
            byte[] keyBytes = ENCRYPTION_KEY.getBytes();
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = new byte[encryptedBytes.length];
            
            for (int i = 0; i < encryptedBytes.length; i++) {
                decrypted[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
            }
            return new String(decrypted);
        } catch (Exception e) {
            System.out.println("Warning: Decryption failed, reading as plain text");
            return encryptedData;
        }
    }

    public static void saveTransactions(List<Transaction> transactions) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            for (Transaction transaction : transactions) {
                String encryptedLine = simpleEncrypt(transaction.toFileString());
                writer.println(encryptedLine);
            }
        } catch (IOException e) {
            System.out.println("Error: Could not save transactions to file");
        }
    }

    public static List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(TRANSACTIONS_FILE))) {
                return transactions;
            }
            
            List<String> lines = Files.readAllLines(Paths.get(TRANSACTIONS_FILE));
            for (String encryptedLine : lines) {
                String decryptedLine = simpleDecrypt(encryptedLine);
                Transaction transaction = Transaction.fromFileString(decryptedLine);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not load transactions file");
        }
        return transactions;
    }

    public static void saveBudgets(List<Budget> budgets) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BUDGETS_FILE))) {
            for (Budget budget : budgets) {
                String encryptedLine = simpleEncrypt(budget.toFileString());
                writer.println(encryptedLine);
            }
        } catch (IOException e) {
            System.out.println("Error: Could not save budgets to file");
        }
    }

    public static List<Budget> loadBudgets() {
        List<Budget> budgets = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(BUDGETS_FILE))) {
                return budgets;
            }
            
            List<String> lines = Files.readAllLines(Paths.get(BUDGETS_FILE));
            for (String encryptedLine : lines) {
                String decryptedLine = simpleDecrypt(encryptedLine);
                Budget budget = Budget.fromFileString(decryptedLine);
                if (budget != null) {
                    budgets.add(budget);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not load budgets file");
        }
        return budgets;
    }
}
