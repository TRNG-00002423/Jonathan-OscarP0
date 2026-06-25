package com.rev.dao.model;

public class Expense {
    private final int id;
    private final int userId;
    private final double amount;
    private final String category;
    private final String description;
    private final String date;

    public Expense(int id, int userId, double amount, String category,  String description, String date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Expense ");
        sb.append(" ID: ").append(id);
        sb.append(" | UserID: ").append(userId);
        sb.append(" | Amount: ").append(amount);
        sb.append(" | Category: ").append(category);
        sb.append(" | Description: ").append(description);
        sb.append(" | Date: ").append(date);
        return sb.toString();
    }



}
