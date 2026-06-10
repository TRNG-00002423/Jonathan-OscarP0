def __main__():
    employees = {"oscarkardon": "password"}
    login(employees)
    while True:
        #menu stuff

def login(employees):
    print("Please enter your username: ")
    username = input()
    if username not in employees.keys:
        print("User does not exist")
        create_employee()
    print("Please enter your password: ")
    password = input()

