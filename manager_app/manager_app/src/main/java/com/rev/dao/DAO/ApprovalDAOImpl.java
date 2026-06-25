package com.rev.dao.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.rev.dao.model.Approval;

public class ApprovalDAOImpl implements ApprovalDAO{
    private final Connection conn;

    public ApprovalDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void approveExpense(int expenseId, int managerId, String comment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void denyExpense(int expenseId, int managerId, String comment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<Approval> getApprovalByExpenseId(int expenseId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Approval> getApprovalHistory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Approval> getApprovalHistoryByManager(int managerId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateApproval(int expenseId, int managerId, String status, String comment, String date) throws SQLException{
        String query = "UPDATE approvals SET status = ?, reviewer = ?, review_date = ?, comment = ? WHERE expense_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, status);
            stmt.setInt(2, managerId);
            stmt.setString(3, date);
            stmt.setString(4, comment);
            stmt.setInt(5, expenseId);

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected != 1) {
                throw new SQLException("No approval found for expense_id " + expenseId);
            }
        }
    }

}
