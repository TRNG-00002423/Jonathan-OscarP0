package com.rev.dao.DAO;

import java.util.List;
import java.util.Optional;

import com.rev.dao.model.Expense;

public interface ExpenseDAO {

    void createExpense(Expense expense);

    Optional<Expense> findExpenseById(int id);

    List<Expense> getPendingExpenses();

    List<Expense> getExpensesByEmployee(int userId);

    List<Expense> getExpensesByCategory(String category);
    
    List<Expense> getExpensesByDate(String date);

    void updateExpense(Expense expense);
    
    void deleteExpense(int id);
}
