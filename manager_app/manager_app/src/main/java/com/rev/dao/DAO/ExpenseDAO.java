package com.rev.dao.DAO;

import java.util.List;

import com.rev.dao.dto.EmployeeReportDTO;
import com.rev.dao.dto.ExpenseWithStatusDTO;
import com.rev.dao.model.Expense;


public interface ExpenseDAO {
    //void createExpense(Expense expense);

    //Optional<Expense> findExpenseById(int id);

    List<ExpenseWithStatusDTO> getPendingExpenses();

    List<Expense> getExpensesByEmployee(int userId);

    EmployeeReportDTO getEmployeeReport(int employeeId);

    List<EmployeeReportDTO> getAllEmployeesReport();

    List<Expense> getExpensesByCategory(String category);
    
    List<Expense> getExpensesByDate(String date);

    void updateExpense(Expense expense);
    
    void deleteExpense(int id);
}
