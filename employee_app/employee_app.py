import os
from output_utils import clear_console
from Employee import Employee
from Expense import Expense
from datetime import datetime
from Approval import Approval


def main():
    global employees
    employees = []
    global expenses
    expenses = []
    global approvals
    approvals = []
    global logged_in_as 
    
    
    print("*" * 20)
    print("Welcome to Revature Expense Manager!")
    print("Please login")
    print("*" * 20)

    logged_in_as = login(employees)
    clear_console()


    print("*" * 20)
    print("Welcome to the Menu")
    print("*" * 20)

    while True:
        print("1. Expense Manager")
        print("5. Quit")

        user_input = input("Please enter a menu number")
        if not any(char.isdigit() for char in user_input):
            user_selection = ""
        else:
            user_selection = int(user_input)


        match user_selection:
            case 1:  
                expense_manager()
                clear_console()
            case 5:
                break
            case _:
                print("Not a valid menu item")

def login(employees):
    while True:
        print("Please enter your username: ")
        username = input()
        if username not in employees:
            print("User does not exist, Press 1 to create new employee, press 2 to try again")
            user_input = int(input())
            if user_input == 1:
                new_employee = add_employee()
                print("Logged in now!")
                return new_employee
            else:
                continue

        print("Please enter your password: ")
        input_password = input()
        password = employees[username]
        if input_password != password:
            print("Password is incorrect")

        print("Logged in now!")
        for employee in employees:
            if employee.username == username:
                return employee
        
    
            
        
def add_employee():
    print("Enter username")
    username = input()
    print("Enter password")
    password = input()
    print("Enter 1 for Employee, Enter 2 for Manager")
    role = int(input())
    new_employee = Employee(username, password, role)
    employees.append(new_employee)
    return new_employee

def expense_manager():
    while True:
        print("Expense Menu:")
        print("Enter 1 to add an expense: ")
        print("Enter 2 to check status of an expense:")
        user_input = int(input())
        match user_input:
            case 1:
                add_expense()
            case 2: 
                check_expense_status()


def add_expense():
    amount = float(input("Enter expense amount:"))
    description = input("Enter description: ")
    user_input = input("Enter a date (DD/MM/YYYY): ")    
    date_object = datetime.strptime(user_input, "%d/%m/%Y") 
    formatted_date = date_object.strftime("%B %d, %Y") 
    new_expense = Expense(logged_in_as.id, amount, description, formatted_date)
    expenses.append(new_expense)
    print(f"Expense added: {new_expense}")

def check_expense_status():
    #add later once we complete java
    print(approvals)

if __name__ == "__main__":
    main()
