package com.rev.dao.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

        try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, status);

            ResultSet rs = stmt.executeQuery();
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

        try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
                expenses.add(mapRowExpense(rs));

        } catch (SQLException e) {
            e.printStackTrace();

        }

        return expenses;
    }

    @Override
    public List<Expense> getExpensesByCategory(String category) {
        String query = "SELECT * FROM expenses WHERE category = ?";
        List<Expense> expenses = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
                expenses.add(mapRowExpense(rs));

        } catch (SQLException e) {
            e.printStackTrace();

        }

        return expenses;
    }

    @Override
    public List<Expense> getExpensesByDate(String date) {
        String query = "SELECT * FROM expenses WHERE date = ?";
        List<Expense> expenses = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
                expenses.add(mapRowExpense(rs));

        } catch (SQLException e) {
            e.printStackTrace();

        }

        return expenses;
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

}
