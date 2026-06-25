package com.rev.dao.model;

public class Approval {
    private final int id;
    private final int expenseId;
    private final int managerId;
    private final String status;
    private final int reviewer;
    private final String comment;
    private final String reviewDate;

    public Approval(int id, int expenseId,  int managerId, String status, int reviewer, String comment, String reviewDate ) {
        this.id = id;
        this.expenseId = expenseId;
        this.managerId = managerId;
        this.status = status;
        this.reviewer = reviewer;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    public int getId() {
        return id;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public int getManagerId() {
        return managerId;
    }

    public String getStatus() {
        return status;
    }

    public int getReviewer() {
        return reviewer;
    }

    public String getComment() {
        return comment;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Approval{");
        sb.append("id=").append(id);
        sb.append(", expenseId=").append(expenseId);
        sb.append(", managerId=").append(managerId);
        sb.append(", status=").append(status);
        sb.append(", reviewer=").append(reviewer);
        sb.append(", comment=").append(comment);
        sb.append(", reviewDate=").append(reviewDate);
        sb.append('}');
        return sb.toString();
    }


}
