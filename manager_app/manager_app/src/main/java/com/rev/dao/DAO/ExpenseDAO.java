package com.rev.dao.DAO;

import java.util.List;

import com.rev.dao.dto.CategoryReportDTO;
import com.rev.dao.dto.DateReportDTO;
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

    List<CategoryReportDTO> getExpensesByCategory(String category);
    
    List<DateReportDTO> getExpensesByDate(String startDate, String endDate);

    void updateExpense(Expense expense);
    
    void deleteExpense(int id);

    boolean expenseExists(int expenseId);
}
