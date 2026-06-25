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
        sb.append("Expense{");
        sb.append("id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", amount=").append(amount);
        sb.append(", category=").append(category);
        sb.append(", description=").append(description);
        sb.append(", date=").append(date);
        sb.append('}');
        return sb.toString();
    }



}
