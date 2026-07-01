from output_utils import clear_console
from tabulate import tabulate
from datetime import datetime
import requests
import questionary
import logging

logging.basicConfig(
    filename="employee.log",
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

def main():
    global logged_in_as 
    print("*" * 20)
    print("Welcome to Revature Expense Manager!")
    print("*" * 20)

    
    logged_in_as = login_menu()
    clear_console()
    if logged_in_as != None:
        expense_manager()


def login_menu():
        choice = questionary.select(
            "Employee Login",
            choices=[
                questionary.Choice("Login", "login"),
                questionary.Choice("Create Account", "create")
            ]
        ).ask()

        if choice == "login":
            return login()
        elif choice == "create":
            return add_employee()
        
    

def login():
    username = questionary.text("Enter username:").ask()
    logging.info(f"Login attempt for username: {username}")
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
            logging.info(f"User {username} logged in successfully")
            return result["user"]
        
        if response.status_code == 404 and result.get("success") == False:
            print(result["message"])
            logging.warning(f"Login failed: user {username} does not exist")
            return login_menu()

        tries -= 1
        logging.warning(f"Failed login for {username}. Attempts left: {tries}")
        print(f"{result['message']}. Attempts left: {tries}")

    print("You have run out of attempts.")
    return None
        
def add_employee():
    username = questionary.text(
            "Enter username:"
        ).ask()
    password = questionary.password(
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
        logging.info(f"New employee account created: {username}")
        return result["user"]
    
    if response.status_code == 400 and result.get("success") == False:
        print(result["message"])
        logging.warning(f"Account creation failed for {username}: Username taken")
        return login_menu()


def expense_manager():
    while True:
        user_input = questionary.select(
            "Main Menu",
            choices=[
                questionary.Choice("Add Expense", "add_expense"),
                questionary.Choice("View Expenses", "view_expenses"),
                questionary.Choice("Delete Expense", "delete_expenses"),
                questionary.Choice("Edit Expense", "edit_expense"),
                questionary.Choice("View History", "view_history"),
                questionary.Choice("Exit", "exit")
            ]
        ).ask()
        match user_input:
            case "add_expense":
                add_expense()
            case "view_expenses": 
                check_expense_status()
            case "delete_expenses": 
                delete_expense()
            case "edit_expense": 
                edit_expense()
            case "view_history": 
                view_expense_history()
            case "exit":
                return None
            
            

def validate_date(date_string):
    try:
        datetime.strptime(date_string, "%Y-%m-%d")
        return True
    except ValueError:
        return "Please enter a valid date in YYYY-MM-DD format."


def add_expense():
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
            default=datetime.today().strftime("%Y-%m-%d"),
            validate=validate_date
    ).ask()
  
    try:
        date_object = datetime.strptime(user_date, "%Y-%m-%d")    
    except ValueError as e:
        print("Not a valid date. Please try again!")
        return None        
    formatted_date = date_object.strftime("%B %d, %Y")

    logging.info(
            f"User {logged_in_as['username']} adding expense: "
            f"${amount}, category={category}, description={description}, date={formatted_date}"
        )

    response = requests.post(
        "http://127.0.0.1:5000/expense",
        json={
            "user_id": logged_in_as["id"],
            "amount": amount,
            "category": category,
            "description": description,
            "date": formatted_date
        }
    )
    if response.status_code == 201:
        print("Expense added!")
        logging.info("Expense added successfully")
    else:
        print("Failed to add expense.")
        logging.error("Expense failed to add")

def print_expenses(expenses):
    table = [
        [
            expense["id"],
            expense["amount"],
            expense["description"],
            expense["category"],
            expense["status"],
            expense["comment"]
        ]
        for expense in expenses
    ]

    print(tabulate(
        table,
        headers=["ID", "Amount", "Description", "Category", "Status", "Comment"],
        tablefmt="grid"
    ))

def check_expense_status():
    response = requests.get(f"http://127.0.0.1:5000/expenses/{logged_in_as['id']}")
    expenses = response.json() 
    if len(expenses) == 0:
        print("You have no expenses.")
    else: 
        print_expenses(expenses)


def delete_expense():
    expense_id =  choose_expense()
    if expense_id == "BACK":
        return
    logging.info(f"User {logged_in_as['username']} deleting expense ID {expense_id}")
    response = requests.delete(f"http://127.0.0.1:5000/expense/{expense_id}")
    print(response.json()["message"])


def choose_expense():
    response = requests.get(
        f"http://127.0.0.1:5000/expenses/{logged_in_as['id']}/pending"
    )
    expenses = response.json()

    if not expenses:
        print("You have no expenses to select.")
        return "BACK"

    header = (
        f"{'ID':<5}"
        f"{'Amount':<12}"
        f"{'Description':<20}"
        f"{'Category':<15}"
        f"{'Status':<12}"
    )

    expense_options = [
        questionary.Choice(
            title=(
                f"{expense['id']:<5}"
                f"${expense['amount']:<11.2f}"
                f"{expense['description']:<20}"
                f"{expense['category']:<15}"
                f"{expense['status']:<12}"
            ),
            value=expense["id"]
        )
        for expense in expenses
    ]

    expense_options.append(
        questionary.Choice("Go Back", "BACK")
    )

    selected_id = questionary.select(
        f"Choose an expense:\n\n  {header}\n{'-' * len(header)}",
        choices=expense_options
    ).ask()

    return selected_id
    

def edit_expense():
    expense_id =  choose_expense()
    if expense_id == "BACK":
        return

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
                        default=datetime.today().strftime("%Y-%m-%d"),
                        validate=validate_date
                ).ask()

        logging.info(
            f"User {logged_in_as['username']} updated expense "
            f"{expense_id}: {updated_field} -> {new_value}"
        )
        response = requests.put(f"http://127.0.0.1:5000/expense/{expense_id}",
                json={updated_field: new_value})
        print(response.json()["message"])
        print("Please edit another field or DONE")


def view_expense_history():
    response = requests.get(f"http://127.0.0.1:5000/expenses/{logged_in_as['id']}/history")
    expenses = response.json() 
    if len(expenses) == 0:
        print("You have no expense history.")
    else: 
        print_expenses(expenses)

if __name__ == "__main__":
    main()
