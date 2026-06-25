package com.rev.dao.DAO;

import java.util.List;
import java.util.Optional;

import com.rev.dao.model.Approval;

public interface ApprovalDAO {
    void approveExpense(int expenseId, int managerId, String comment);

    void denyExpense(int expenseId, int managerId, String comment);

    Optional<Approval> getApprovalByExpenseId(int expenseId);

    List<Approval> getApprovalHistory();

    List<Approval> getApprovalHistoryByManager(int managerId);
}
