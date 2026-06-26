package com.rev.manager_app;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.rev.dao.DAO.ApprovalDAO;
import com.rev.dao.DAO.ApprovalDAOImpl;
import com.rev.dao.DAO.ExpenseDAO;
import com.rev.dao.DAO.ExpenseDAOImpl;
import com.rev.dao.DAO.UserDAO;
import com.rev.dao.DAO.UserDAOImpl;
import com.rev.dao.dto.EmployeeReportDTO;
import com.rev.dao.dto.ExpenseWithStatusDTO;
import com.rev.dao.model.Expense;
import com.rev.dao.model.User;
import com.rev.util.DatabaseConnectionUtil;
import com.rev.util.TablePrinterUtil;

public class ManagerApp { 
    public static void main(String[] args) {
        Connection conn = DatabaseConnectionUtil.getConnection();
        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = new UserDAOImpl(conn);
        ExpenseDAO expenseDAO = new ExpenseDAOImpl(conn);
        ApprovalDAO approvalDAO = new ApprovalDAOImpl(conn);

        User user = null;
        try {
            user = accessAccount(scanner, userDAO);
        } catch (SQLException ex) {
            System.out.println("Database error occurred.");
            ex.printStackTrace();
        }

        System.out.println("Welcome to the Menu!");
        
        while(true){
            System.out.println("\n1. View pending expenses");
            System.out.println("2. Approve/deny an expense");
            System.out.println("3. Generate a report for an expense");
            System.out.println("4. Exit the app");
            int input = scanner.nextInt();

            switch (input){
                case 1:
                    try {
                        viewPendingExpenses(expenseDAO);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        reviewExpense(scanner, user, expenseDAO, approvalDAO);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    try {
                        generateReport(scanner, expenseDAO);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 4:
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
            }
        }

    }

    public static User accessAccount(Scanner scanner, UserDAO userDAO) throws SQLException {
        System.out.println("1. Log in with existing account");
        System.out.println("2. Create an account");
        int input = scanner.nextInt();
        User user = null;
        if (input == 1) {
            while (user == null) {
                user = loginFlow(scanner, userDAO);
            }
        } else if (input == 2) {
            while (user == null) {
                try {
                    user = newUser(scanner, userDAO);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!user.getRole().equals("Manager")) {
            System.out.println("You do not have access to this application. Please use the Employee app.");
            //TODO add break or exit here
        }
        return user;

    }

    public static User loginFlow(Scanner scanner, UserDAO userDAO) throws SQLException {
        System.out.println("Please enter a username");
        String username = scanner.next();
        System.out.println("Please enter a password");
        String password = scanner.next();
        
        Optional<User> user = userDAO.login(username, password);

        if (user.isPresent()) {
            System.out.println("Login successful!");
            return user.get();
        } else {
            System.out.println("Invalid credentials.");
            return null;
        }
    }

    public static User newUser(Scanner scanner, UserDAO userDAO) throws SQLException {
        System.out.println("Please enter a username");
        String username = scanner.next();
        System.out.println("Please enter a password");
        String password = scanner.next();

        User user = userDAO.createUser(username, password);

        System.out.println("User created successfully!");
        return user; 
    }

    public static void viewPendingExpenses(ExpenseDAO expenseDAO) throws SQLException {

        List<ExpenseWithStatusDTO> expenseList = expenseDAO.getPendingExpenses();

        TablePrinterUtil.printPendingExpenses(expenseList);
        
    }

    public static void reviewExpense(Scanner scanner, User user, ExpenseDAO expenseDAO, ApprovalDAO approvalDAO) throws SQLException {
        viewPendingExpenses(expenseDAO);
        System.out.println("Please enter expense ID:");
        int expenseId = scanner.nextInt();
        System.out.println("1. Approve");
        System.out.println("2. Deny");
        int user_option = scanner.nextInt();
        String approval = (user_option == 1) ? "approved" : "denied";
        System.out.println("1. Leave a comment with the review");
        System.out.println("2. Continue");            
        user_option = scanner.nextInt();
        scanner.nextLine();
        String userComment = null;
        if(user_option == 1){
            System.out.println("Enter comment for the review:");
            userComment = scanner.nextLine();
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = today.format(formatter);

        approvalDAO.updateApproval(expenseId, user.getId(), approval, userComment, formattedDate);
    }

    // Add most common category
    public static void generateReport(Scanner scanner, ExpenseDAO expenseDAO) throws SQLException {
        System.out.println("Generate report by:");
        System.out.println("1. Employee");
        System.out.println("2. Category");
        System.out.println("3. Date");
        int user_input = scanner.nextInt();
        scanner.nextLine();
        String value = "";
        List<Expense> expenses = new ArrayList<>();
        EmployeeReportDTO report = null;
        

        switch (user_input) {
            case 1:
                System.out.println("1. By employee id");
                System.out.println("2. All employees");
                int input = scanner.nextInt();
                if(input == 1){
                    System.out.println("Enter employee id:");
                    int employeeID = scanner.nextInt();
                    report = expenseDAO.getEmployeeReport(employeeID);
                    TablePrinterUtil.printEmployeeReport(report);
                } else if (input == 2) {
                    List<EmployeeReportDTO> reports = expenseDAO.getAllEmployeesReport();
                    TablePrinterUtil.printAllEmployeeReports(reports);
                }

                break;
            case 2:
                System.out.println("Enter Category:");
                value = scanner.nextLine();
                expenses = expenseDAO.getExpensesByCategory(value);
                break;
            case 3:
                System.out.println("Enter Date (DD/MM/YYYY):");
                String inputDate = scanner.nextLine();

                try {
                    DateTimeFormatter inputFormatter =
                        DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    LocalDate dateObject =
                        LocalDate.parse(inputDate, inputFormatter);

                    DateTimeFormatter outputFormatter =
                        DateTimeFormatter.ofPattern("MMM dd, yyyy");

                    value = dateObject.format(outputFormatter);

                    expenses = expenseDAO.getExpensesByDate(value);

                } catch (DateTimeParseException e) {
                    System.out.println("Not a valid date. Please try again!");
                    return;
                }
                break;

            default:
                break;
        }
    }

    public static EmployeeReportDTO generateEmployeeReport(List<Expense> expenses, int employeeId) {

        double total = 0;
        int count = expenses.size();

        for (Expense e : expenses) {
            total += e.getAmount();
        }

        double avg = count > 0 ? total / count : 0;

        return new EmployeeReportDTO(employeeId, total, avg, count, expenses);
    }
}