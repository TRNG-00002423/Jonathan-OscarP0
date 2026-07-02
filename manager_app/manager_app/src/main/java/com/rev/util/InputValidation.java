package com.rev.util;

import java.util.Scanner;

import com.rev.dao.DAO.ExpenseDAO;
import com.rev.dao.DAO.ExpenseDAOImpl;

public class InputValidation {
    public static int getMenuChoice(Scanner scanner, int min, int max) {
        while (true) {
            System.out.print("Choose an option: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();

                if (choice >= min && choice <= max) {
                    scanner.nextLine(); // clear newline
                    return choice;
                }
            } else {
                scanner.nextLine(); // clear invalid input
            }

            System.out.println("Invalid input. Please enter a number between " 
                                + min + " and " + max + ".");
        }
    }

    public static int getValidExpenseId(Scanner scanner, ExpenseDAO expenseDAO, String prompt) {
        while (true) {
            System.out.println(prompt);

            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();

                if (((ExpenseDAOImpl) expenseDAO).expenseExists(value)) {
                    return value;
                } else {
                    System.out.println("Expense ID does not exist.");
                }

            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }
}
