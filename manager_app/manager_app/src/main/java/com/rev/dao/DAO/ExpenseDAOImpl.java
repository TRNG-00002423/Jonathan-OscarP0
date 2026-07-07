package com.rev.dao.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rev.dao.dto.CategoryReportDTO;
import com.rev.dao.dto.DateReportDTO;
import com.rev.dao.dto.EmployeeReportDTO;
import com.rev.dao.dto.ExpenseWithStatusDTO;
import com.rev.dao.model.Expense;

public class ExpenseDAOImpl implements ExpenseDAO{

    private final Connection conn;

    public ExpenseDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<ExpenseWithStatusDTO> getPendingExpenses() {
        String query = 
        """
        SELECT e.*, 
        a.expense_id, a.status, a.reviewer, a.comment, a.review_date 
        FROM expenses e JOIN approvals a ON e.id = a.expense_id 
        WHERE a.status = ?""";

        String status = "pending";
        List<ExpenseWithStatusDTO> expenseList = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(query)){
            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                expenseList.add(mapRowExpenseWithStatus(mapRowExpense(rs),rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        return expenseList;
    }

    @Override
    public List<Expense> getExpensesByEmployee(int userId) {
        String query = "SELECT * FROM expenses WHERE user_id = ?";
        List<Expense> expenses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                expenses.add(mapRowExpense(rs));

        } catch (SQLException e) {
            e.printStackTrace();

        }

        return expenses;
    }
    
    @Override
    public EmployeeReportDTO getEmployeeReport(int employeeId) {

        String query =
            "SELECT user_id, SUM(amount) AS total, AVG(amount) AS average, COUNT(*) AS count " +
            "FROM expenses WHERE user_id = ? GROUP BY user_id";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                return mapRowEmployeeReportDTO(rs);


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new EmployeeReportDTO(employeeId, 0, 0, 0, new ArrayList<>());
    }

    
    @Override
    public List<EmployeeReportDTO> getAllEmployeesReport() {
        String query =
            "SELECT user_id, SUM(amount) AS total, AVG(amount) AS average, COUNT(*) AS count " +
            "FROM expenses GROUP BY user_id";

        List<EmployeeReportDTO> reports = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();

            while(rs.next())
                reports.add(mapRowEmployeeReportDTO(rs));


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }

    @Override
    public List<CategoryReportDTO> getExpensesByCategory(String category) {
        String query =
            "SELECT user_id, category, SUM(amount) AS total, AVG(amount) AS average, COUNT(*) AS count " +
            "FROM expenses WHERE category = ? GROUP BY user_id";
        
        List<CategoryReportDTO> reports = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, category.toLowerCase());
            ResultSet rs = ps.executeQuery();

            while(rs.next())
                reports.add(mapRowCategoryReportDTO(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }

    @Override
    public List<DateReportDTO> getExpensesByDate(String startDate, String endDate) {
        String query = "SELECT user_id, date, SUM(amount) AS total, AVG(amount) AS average, COUNT(*) AS count " +
                       "FROM expenses WHERE date BETWEEN ? AND ? GROUP BY user_id, date";
        List<DateReportDTO> reports = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(query)){
            ps.setString(1, startDate);
            ps.setString(2, endDate);

            ResultSet rs = ps.executeQuery();

            while (rs.next())
                reports.add(mapRowDateReportDTO(rs));

        } catch (SQLException e) {
            e.printStackTrace();

        }

        return reports;
    }

    @Override
    public void updateExpense(Expense expense) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteExpense(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   private Expense mapRowExpense(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getDouble("amount"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("date")
        );
    }

    private ExpenseWithStatusDTO mapRowExpenseWithStatus(Expense expense, ResultSet rs) throws SQLException {
        return new ExpenseWithStatusDTO(
                    expense,
                    rs.getString("status"),
                    rs.getString("comment")
        );
    }

    private EmployeeReportDTO mapRowEmployeeReportDTO(ResultSet rs) throws SQLException{
        return new EmployeeReportDTO(
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        rs.getDouble("average"),
                        rs.getInt("count"),
                        new ArrayList<>());
    }

    private CategoryReportDTO mapRowCategoryReportDTO(ResultSet rs) throws SQLException{
        return new CategoryReportDTO(
                        rs.getInt("user_id"),
                        rs.getString("category"),
                        rs.getDouble("total"),
                        rs.getDouble("average"),
                        rs.getInt("count"),
                        new ArrayList<>());
    }

    private DateReportDTO mapRowDateReportDTO(ResultSet rs) throws SQLException{
        return new DateReportDTO(
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        rs.getDouble("average"),
                        rs.getInt("count"),
                        rs.getString("date"),
                        new ArrayList<>());
    }

    public boolean expenseExists(int expenseId) {
        String query = "SELECT 1 FROM expenses JOIN approvals ON expenses.id = approvals.expense_id WHERE expenses.id = ? AND status = 'pending'";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, expenseId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ExpenseWithStatusDTO getPendingExpenseById(int expenseId) {
        String query = 
        """
        SELECT e.*, a.status, a.comment
        FROM expenses e 
        JOIN approvals a ON e.id = a.expense_id
        WHERE e.id = ? AND a.status = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, expenseId);
            ps.setString(2, "pending");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowExpenseWithStatus(mapRowExpense(rs), rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    

}
