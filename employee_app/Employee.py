class Employee:
    id_counter = 0
    def __init__(self, username, password, role):
        self.username = username
        self.password = password
        self.role = role
        self.id = self.id_counter
        self.id_counter += 1