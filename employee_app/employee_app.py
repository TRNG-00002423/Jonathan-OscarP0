import os
import sqlite3
from output_utils import clear_console
from Employee import Employee
from Expense import Expense
from datetime import datetime
from Approval import Approval


def main():
    global expenses
    expenses = []
    global approvals
    approvals = []
    global logged_in_as 
    
    
    print("*" * 20)
    print("Welcome to Revature Expense Manager!")
    print("Please login")
    print("*" * 20)

    conn = sqlite3.connect("../database/expense_manager.db")
    conn.row_factory = sqlite3.Row
    logged_in_as = login(conn)
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
                expense_manager(conn)
                clear_console()
            case 5:
                conn.close()
                break
            case _:
                print("Not a valid menu item")

def login(conn):
    while True:
        print("Please enter your username: ")
        username = input()  
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM users WHERE username = ?",(username,))

        result = cursor.fetchone()

        if result is None:
            print("User does not exist, Press 1 to create new employee, press 2 to try again")
            user_input = int(input())
            if user_input == 1:
                new_employee = add_employee(conn)
                print("Logged in now!")
                return new_employee
            else:
                continue

        print("Please enter your password: ")
        input_password = input()
        
        # give max amount of tries
        if input_password != result["password"]:
            print("Password is incorrect")
            continue
        

        print("Logged in now!")
        return result
        
def add_employee(conn):
    print("Enter username")
    username = input()
    print("Enter password")
    password = input()
    print("Enter 1 for Employee, Enter 2 for Manager")
    role_input = int(input())
    role = "Manager" if (role_input == 2) else "Employee"

    cursor = conn.cursor()
    try:
        user = cursor.execute("INSERT INTO users(username,password,role) VALUES(?,?,?)",(username,password,role))
        conn.commit()
    except sqlite3.IntegrityError as e:
        print("Username is taken!")
        return add_employee(conn)

    return user

def expense_manager(conn):
    while True:
        print("Expense Menu:")
        print("Enter 1 to add an expense: ")
        print("Enter 2 to check status of an expense:")
        user_input = int(input())
        match user_input:
            case 1:
                add_expense(conn)
            case 2: 
                check_expense_status()


def add_expense(conn):
    cursor = conn.cursor()
    amount = float(input("Enter expense amount:"))
    description = input("Enter description: ")
    user_input = input("Enter a date (DD/MM/YYYY): ")  
  
    try:
        date_object = datetime.strptime(user_input, "%d/%m/%Y") 
    except ValueError as e:
        print("Not a valid date. Please try again!")
        return None        
    formatted_date = date_object.strftime("%B %d, %Y")

    cursor.execute("INSERT INTO expenses(employee_id,amount,description,date) VALUES(?,?,?,?)",(logged_in_as["id"],amount, description, user_input))
    conn.commit()
    print(f"Expense added: ")

def check_expense_status():
    #add later once we complete java
    print(approvals)

if __name__ == "__main__":
    main()
