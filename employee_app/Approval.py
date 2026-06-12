class Approval:
    id_counter = 0
    def __init__(self, expense_id, comment, review_date, status="pending", reviewer_id = None):
        self.expense_id = expense_id
        self.reviewer = reviewer_id
        self.comment = comment
        self.review_date = review_date
        self.status = status
        self.id = self.id_counter
        self.id_counter += 1

        