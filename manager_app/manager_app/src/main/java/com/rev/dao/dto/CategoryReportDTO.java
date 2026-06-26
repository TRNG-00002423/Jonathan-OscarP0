package com.rev.dao.dto;

import java.util.List;

import com.rev.dao.model.Expense;

public class CategoryReportDTO {
    private final int employeeId;
    private final String category;
    private final double total;
    private final double average;
    private final int count;
    private final List<Expense> expenses;

    public CategoryReportDTO(int employeeId, String category, double total, double average, int count,
            List<Expense> expenses) {
        this.employeeId = employeeId;
        this.category = category;
        this.total = total;
        this.average = average;
        this.count = count;
        this.expenses = expenses;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getCategory() {
        return category;
    }

    public double getTotal() {
        return total;
    }

    public double getAverage() {
        return average;
    }

    public int getCount() {
        return count;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    

    
}
