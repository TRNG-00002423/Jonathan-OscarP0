package com.rev.dao.dto;

import com.rev.dao.model.Expense;

public class ExpenseWithStatusDTO {

    private final Expense expense;
    private final String status;
    private final String comment;

    public ExpenseWithStatusDTO(
            Expense expense,
            String status,
            String comment) {

        this.expense = expense;
        this.status = status;
        this.comment = comment;
    }

    public Expense getExpense() {
        return expense;
    }

    public String getStatus() {
        return status;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
  
        sb.append("").append(expense);
        sb.append(" | Status: ").append(status);
        sb.append(" | Comment: ").append(comment);
        return sb.toString();
    }

}
