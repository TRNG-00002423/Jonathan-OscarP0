package com.rev.dao.DAO;

import java.util.List;

import com.rev.dao.dto.ExpenseWithStatusDTO;
import com.rev.dao.model.Expense;


public interface ExpenseDAO {
    //void createExpense(Expense expense);

    //Optional<Expense> findExpenseById(int id);

    List<ExpenseWithStatusDTO> getPendingExpenses();

    List<ExpenseWithStatusDTO> getExpensesByEmployee(int userId);

    List<ExpenseWithStatusDTO> getExpensesByCategory(String category);
    
    List<ExpenseWithStatusDTO> getExpensesByDate(String date);

    void updateExpense(Expense expense);
    
    void deleteExpense(int id);
}
