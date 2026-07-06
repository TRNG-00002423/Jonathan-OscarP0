package com.rev.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

import com.rev.dao.DAO.ExpenseDAO;
import com.rev.dao.DAO.UserDAO;

public class InputValidation {
    public static int getMenuChoice(Scanner scanner, int min, int max) {
        while (true) {
            System.out.print("Choose an option: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();

                if (choice >= min && choice <= max) {
                    scanner.nextLine(); 
                    return choice;
                }
            } else {
                scanner.nextLine();
            }

            System.out.println();
            System.out.println("Invalid input.");
            System.out.println("Please enter a number between " + min + " and " + max + ".");
            System.out.println();
        }
    }

    public static int getValidExpenseId(Scanner scanner, ExpenseDAO expenseDAO, String prompt) {
        while (true) {
            System.out.print(prompt);

            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();

                if ((expenseDAO).expenseExists(value)) {
                    return value;
                } else {
                    System.out.println("Expense " + value + " is not pending review.");
                }

            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    public static int getValidEmployeeId(Scanner scanner, UserDAO userDAO, String prompt){
        while (true) {
            System.out.print(prompt);

            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();

                if ((userDAO).userExists(value)) {
                    return value;
                } else {
                    System.out.println("Employee " + value + " does not exist.");
                }

            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    public static LocalDate getValidDate(Scanner scanner, String prompt) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("uuuu-MM-dd")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        while (true) {
            System.out.print(prompt);

            String input = scanner.nextLine();

            try {
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                ConsoleUtil.printError("Invalid date. Please use YYYY-MM-DD.");
            }
        }
    }
    
}
