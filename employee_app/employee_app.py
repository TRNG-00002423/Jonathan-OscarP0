import os
import sqlite3
from output_utils import clear_console
from Employee import Employee
from Expense import Expense
from datetime import datetime
from Approval import Approval
import requests

#adding this import, you may have to "pip install questionary"
import questionary

def main():
    global expenses
    expenses = []
    global approvals
    approvals = []
    global logged_in_as 
    
    
    print("*" * 20)
    print("Welcome to Revature Expense Manager!")
    print("*" * 20)

    conn = sqlite3.connect("../database/expense_manager.db")
    conn.row_factory = sqlite3.Row
    logged_in_as = login_menu(conn)
    clear_console()
    if logged_in_as != None:
        expense_manager(conn)


def login_menu(conn):
        choice = questionary.select(
            "Employee Login",
            choices=[
                questionary.Choice("Login", "login"),
                questionary.Choice("Create Account", "create")
            ]
        ).ask()

        if choice == "login":
            return login(conn)
        elif choice == "create":
            return add_employee(conn)
        
    

def login(conn):
    username = questionary.text("Enter username:").ask()

    tries = 5

    while tries > 0:
        password = questionary.password("Enter password:").ask()

        response = requests.post(
            "http://127.0.0.1:5000/login",
            json={
                "username": username,
                "password": password
            }
        )

        result = response.json()

        if response.status_code == 200 and result.get("success"):
            print("Logged in successfully!")
            return result["user"]
        
        if response.status_code == 404 and result.get("success") == False:
            print(result["message"])
            return login_menu(conn)


        tries -= 1
        print(f"{result['message']}. Attempts left: {tries}")

    print("You have run out of attempts.")
    return None
        
def add_employee(conn):
    username = questionary.text(
            "Enter username:"
        ).ask()
    password = questionary.text(
            "Enter password:"
        ).ask()
    
    role = "Employee"

    response = requests.post(
        "http://127.0.0.1:5000/user",
        json={
            "username": username,
            "password": password,
            "role": role
        }
    )

    result = response.json()

    if response.status_code == 201 and result.get("success"):
        print("User added successfully!")
        return result["user"]
    
    if response.status_code == 400 and result.get("success") == False:
        print(result["message"])
        return login_menu(conn)


def expense_manager(conn):
    while True:
        user_input = questionary.select(
            "Main Menu",
            choices=[
                questionary.Choice("Add expense", "add_expense"),
                questionary.Choice("View expenses", "view_expenses"),
                questionary.Choice("Delete expense", "delete_expenses"),
                questionary.Choice("Edit expense", "edit_expense"),
                questionary.Choice("View history", "view_history"),
                questionary.Choice("Exit", "exit")
            ]
        ).ask()
        match user_input:
            case "add_expense":
                add_expense(conn)
            case "view_expenses": 
                check_expense_status(conn)
            case "delete_expenses": 
                delete_expense(conn)
            case "edit_expense": 
                edit_expense(conn)
            case "view_history": 
                view_expense_history(conn)
            case "exit":
                conn.close()
                return None
            
            

def validate_date(date_string):
    try:
        datetime.strptime(date_string, "%Y-%m-%d")
        return True
    except ValueError:
        return "Please enter a valid date in YYYY-MM-DD format."


def add_expense(conn):
    cursor = conn.cursor()
    amount = questionary.text(
        "Enter expense amount:",
        validate=lambda x: x.replace(".", "", 1).isdigit() and float(x) > 0
    ).ask()
    amount = float(amount)
    description = questionary.text(
            "Enter description (optional):"
        ).ask()
    category = questionary.text(
        "Enter category (optional):"
    ).ask()
    user_date = questionary.text(
            "Enter a date (YYYY-MM-DD):",
            validate=validate_date
    ).ask()
  
    try:
        date_object = datetime.strptime(user_date, "%Y-%m-%d")    
    except ValueError as e:
        print("Not a valid date. Please try again!")
        return None        
    formatted_date = date_object.strftime("%B %d, %Y")
    
    cursor.execute(
        "INSERT INTO expenses(user_id, amount, category, description, date) VALUES (?, ?, ?, ?, ?)",
        (logged_in_as["id"], amount, category, description, formatted_date)
    )

    expense_id = cursor.lastrowid

    cursor.execute(
        "INSERT INTO approvals(expense_id, status) VALUES (?, ?)",
        (expense_id, "pending")
    )
    conn.commit()
    print(f"Expense added!")

def print_expenses(expenses):
    for expense in expenses:
        print(
            f"Expense ID: {expense['id']} | "
            f"Amount: {expense['amount']} | "
            f"Description: {expense['description']} | "
            f"Category: {expense['category']} | "
            f"Status: {expense['status']}"
        )

def check_expense_status(conn):
    cursor = conn.cursor()
    cursor.execute("""SELECT * FROM expenses JOIN approvals ON 
                               approvals.expense_id = expenses.id WHERE expenses.user_id = ?""", 
                               (logged_in_as["id"],))
    
    expenses = cursor.fetchall()
    if len(expenses) == 0:
        print("You have no expenses.")
    else: 
        print_expenses(expenses)

def view_pending_expenses(conn):
    cursor = conn.cursor()
    cursor.execute("""SELECT * FROM expenses JOIN approvals ON 
                               approvals.expense_id = expenses.id WHERE expenses.user_id = ? AND approvals.status = ? """, 
                               (logged_in_as["id"], "pending"))
    
    expenses = cursor.fetchall()
    if len(expenses) == 0:
        print("You have no pendings expenses.")
    else: 
        print_expenses(expenses)
    

def delete_expense(conn):
    expense_id =  choose_expense(conn)
    if expense_id == "BACK":
        return
    cursor =  conn.cursor()
    cursor.execute(
            "DELETE FROM approvals WHERE expense_id = ?",
            (expense_id,)
        )
    cursor.execute(
        "DELETE FROM expenses WHERE id = ?",
        (expense_id,)
    )

    conn.commit()
    print(f"Expense {expense_id} was deleted")



def choose_expense(conn):
    cursor = conn.cursor()
    cursor.execute("""SELECT * FROM expenses JOIN approvals ON 
                               approvals.expense_id = expenses.id WHERE expenses.user_id = ? AND approvals.status = ? """, 
                               (logged_in_as["id"], "pending"))
    
    expenses = cursor.fetchall()
    if len(expenses) == 0:
        print("You have no expenses to select.")
        return "BACK"
           

    expense_options = [questionary.Choice(
            title=f"Expense ID: {row['id']} | Amount: ${row['amount']} | Description: {row['description']} | Category: {row['category']} | Status: {row['status']}", 
            value=row['id']
        )
        for row in expenses
    ]
    expense_options.append(
        questionary.Choice("Go Back", "BACK")
    )

    selected_id = questionary.select(
        "What expense would you like to select?",
        choices=expense_options
    ).ask()

    return selected_id
    

def edit_expense(conn):
    expense_id =  choose_expense(conn)
    if expense_id == "BACK":
        return
    cursor =  conn.cursor()
    cursor.execute("SELECT * FROM expenses WHERE expenses.id = ?", (expense_id,))
    expense = cursor.fetchone()

    while True:
        updated_field = questionary.select(
            "What would you like to edit?",
            choices=["amount", "description", "category", "date", "DONE"]
        ).ask()


        match updated_field:
            case "DONE":
                break
            case "amount":
                new_value = questionary.text(
                    "Enter expense amount:",
                    validate=lambda x: x.replace(".", "", 1).isdigit() and float(x) > 0
                ).ask()
                new_value = float(new_value)
            case "description":
                new_value = questionary.text(
                        "Enter description (optional):"
                    ).ask()
            case "category":
                new_value = questionary.text(
                    "Enter category (optional):"
                ).ask()
            case "date":
                new_value = questionary.text(
                        "Enter a date (YYYY-MM-DD):",
                        validate=validate_date
                ).ask()

        cursor.execute(
            f"UPDATE expenses SET {updated_field} = ? WHERE id = ?",
            (new_value, expense_id)
        )

        conn.commit()
        print("Expense updated successfully.")
        print("Please edit another field or DONE")

    


def view_expense_history(conn):
    cursor = conn.cursor()
    cursor.execute("""SELECT * FROM expenses JOIN approvals ON 
                        approvals.expense_id = expenses.id WHERE expenses.user_id = ? 
                        AND (approvals.status = ? OR approvals.status = ?)""", 
                    (logged_in_as["id"], "approved", "denied"))
    
    expenses = cursor.fetchall()
    if len(expenses) == 0:
        print("You have no expense history.")
    else: 
        print_expenses(expenses)

if __name__ == "__main__":
    main()
