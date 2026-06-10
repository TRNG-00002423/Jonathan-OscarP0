# Adding Employee
import os
from output_utils import clear_console

def main():
    employees = []
    logged_in_as = ""
    print("*" * 20)
    print("Welcome to the Menu")
    print("*" * 20)

    
    while True:
        # Change later once user is logged in
        print("1. Login")
        print("2. Expenses")
        print("5. Quit")

        user_input = input("Please enter a menu number")
        if not any(char.isdigit() for char in user_input):
            user_selection = ""
        else:
            user_selection = int(user_input)


        match user_selection:
            case 1:
                logged_in_as = login(employees)
                clear_console()
            case 2:  
                expenses()
            case 5:
                break
            case _:
                print("Not a valid menu item")

def login(employees):

    while True:
        print("Please enter your username: ")
        username = input()
        if username not in employees:
            print("User does not exist")
            continue

        print("Please enter your password: ")
        input_password = input()
        password = employees[username]
        if input_password != password:
            print("Password is incorrect")

        print("Logged in now!")
        return username
        
def add_employee(employee):

def expenses():
    while True:
        print("Enter Expenses: ")      


if __name__ == "__main__":
    main()
