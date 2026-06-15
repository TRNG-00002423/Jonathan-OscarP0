
class Expense:
    id_counter = 0
    def __init__(self, employee_id, amount, description, date):
        self.employee_id = employee_id
        self.amount = amount
        self.description = description
        self.date = date
        self.id = self.id_counter
        self.id_counter += 1 
    
    def __str__(self):
        return f"Expsense: {self.id}, {self.description} for + ${self.amount:.2f} on {self.date}"
