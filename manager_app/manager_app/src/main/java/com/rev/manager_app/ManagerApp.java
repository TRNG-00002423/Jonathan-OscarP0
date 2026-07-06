package com.rev.manager_app;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rev.dao.DAO.ApprovalDAO;
import com.rev.dao.DAO.ApprovalDAOImpl;
import com.rev.dao.DAO.ExpenseDAO;
import com.rev.dao.DAO.ExpenseDAOImpl;
import com.rev.dao.DAO.UserDAO;
import com.rev.dao.DAO.UserDAOImpl;
import com.rev.dao.dto.CategoryReportDTO;
import com.rev.dao.dto.DateReportDTO;
import com.rev.dao.dto.EmployeeReportDTO;
import com.rev.dao.dto.ExpenseWithStatusDTO;
import com.rev.dao.model.User;
import com.rev.util.ConsoleUtil;
import com.rev.util.DatabaseConnectionUtil;
import com.rev.util.InputValidation;
import com.rev.util.PasswordUtil;
import com.rev.util.TablePrinterUtil;

public class ManagerApp { 
    private static final Logger logger =
        LoggerFactory.getLogger(ManagerApp.class);
    public static void main(String[] args) {
        logger.info("Manager app started");
        Connection conn = DatabaseConnectionUtil.getConnection();
        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = new UserDAOImpl(conn);
        ExpenseDAO expenseDAO = new ExpenseDAOImpl(conn);
        ApprovalDAO approvalDAO = new ApprovalDAOImpl(conn);

        User user = null;
        try {
            user = accessAccount(scanner, userDAO);
            ConsoleUtil.pause(scanner);
        } catch (SQLException ex) {
            ConsoleUtil.printError("Database error occurred.");
            logger.error("Database error occured", ex);
        }
        //I also don't think this is ever possible
        if (user == null) {
            System.out.println("Exiting application...");
            scanner.close();
            return;
        }

        while (true) {
            ConsoleUtil.clearScreen();
            ConsoleUtil.printHeader("Manager Main Menu");

            System.out.println("1) View Pending Expenses");
            System.out.println("2) Review Expense");
            System.out.println("3) Generate Report");
            System.out.println("4) Exit");
            System.out.println();
            int input =  InputValidation.getMenuChoice(scanner, 1, 4);

            switch (input) {
                case 1:
                    try {
                        viewPendingExpenses(scanner, expenseDAO);
                    } catch (SQLException e) {
                        logger.error("Error viewing pending expenses", e);
                        ConsoleUtil.printError("Unable to retrieve pending expenses.");
                    }
                    break;

                case 2:
                    try {
                        reviewExpense(scanner, user, expenseDAO, approvalDAO);
                    } catch (SQLException e) {
                        logger.error("Error reviewing expense", e);
                        ConsoleUtil.printError("Unable to Review Expense.");
                    }
                    break;

                case 3:
                    try {
                        generateReport(scanner, expenseDAO, userDAO);
                    } catch (SQLException e) {
                        logger.error("Error generating report", e);
                        ConsoleUtil.printError("Unable to generate report.");
                        ConsoleUtil.pause(scanner);
                    }
                    break;

                case 4:
                    System.out.println("Goodbye!");
                    return;

            }
        }

    }

    public static User accessAccount(Scanner scanner, UserDAO userDAO) throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("Manager Expense App");
            System.out.println("Welcome!");
            System.out.println();
            System.out.println("1) Log In");
            System.out.println("2) Create Account");
            System.out.println();
            int input = InputValidation.getMenuChoice(scanner, 1, 2);
            ConsoleUtil.clearScreen();

            User user = null;

            switch (input) {
                case 1:
                    while (user == null) {
                        user = loginFlow(scanner, userDAO);
                    }
                    break;

                case 2:
                    while (user == null) {
                        try {
                            user = newUser(scanner, userDAO);
                        } catch (SQLException e) {
                            logger.error("Error creating new user", e);
                        }
                    }
                    break;

            }
            //this never currently happens so we can delete I think
            if (user != null) {
                if (!user.getRole().equals("Manager")) {
                    System.out.println("You do not have access to this application. Please use the Employee app.");
                    logger.warn("Employee login on the manager app");
                    return null;
                }

                return user;
            }
        }
    }

    public static User loginFlow(Scanner scanner, UserDAO userDAO) throws SQLException {
        ConsoleUtil.printHeader("Manager Login");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        Optional<User> user = userDAO.login(username);

        if (user.isPresent()
                && PasswordUtil.verifyPassword(
                        password,
                        user.get().getPassword())) {

            ConsoleUtil.printSuccess("Login successful!");
            return user.get();
        } else {
            System.out.println("Invalid Credentials.");
            logger.warn("Login unsuccessful for username: {}", username);
            return null;
        }
    }

    public static User newUser(Scanner scanner, UserDAO userDAO) throws SQLException {
        ConsoleUtil.printHeader("Create Account");
        System.out.print("Enter Username: "); 
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String hashedPassword = PasswordUtil.hashPassword(password);
        
        User user = userDAO.createUser(username, hashedPassword);

        ConsoleUtil.printSuccess("User Created Successfully!");
        logger.info("User created successfully: {}", username);
        return user; 
    }

    public static void viewPendingExpenses(Scanner scanner, ExpenseDAO expenseDAO) throws SQLException {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("Pending Expenses");
        getPendingExpenses(expenseDAO);
        ConsoleUtil.pause(scanner);
    }

    public static List<ExpenseWithStatusDTO> getPendingExpenses(ExpenseDAO expenseDAO) throws SQLException {
        List<ExpenseWithStatusDTO> expenseList = expenseDAO.getPendingExpenses();
        logger.info("Manager viewing pending expenses");
        if (expenseList.isEmpty()){
            logger.info("No pending expenses found");
            ConsoleUtil.printInfo("No pending expenses were found.");
        }
        else {
            TablePrinterUtil.printPendingExpenses(expenseList);
        } 
        return expenseList;

    }


    

    public static void reviewExpense(Scanner scanner, User user, ExpenseDAO expenseDAO, ApprovalDAO approvalDAO) throws SQLException {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("Review Expense");
        List<ExpenseWithStatusDTO> pendingExpenses = getPendingExpenses(expenseDAO);
        if(pendingExpenses.isEmpty()){
            ConsoleUtil.pause(scanner);
            return;
        }
        System.out.println("1) Select an Expense");
        System.out.println("2) Back");
        int choice = InputValidation.getMenuChoice(scanner, 1, 2);
        if (choice == 2) {
            return;
        }
        int expenseId = InputValidation.getValidExpenseId(scanner, expenseDAO, "Please enter expense ID: ");  
        ExpenseWithStatusDTO expense = expenseDAO.getPendingExpenseById(expenseId);
        TablePrinterUtil.printPendingExpenses(List.of(expense));
        System.out.println("1) Approve");
        System.out.println("2) Deny");
        int user_option = InputValidation.getMenuChoice(scanner, 1, 2);
        String approval = (user_option == 1) ? "approved" : "denied";

        System.out.println("Enter a comment (press Enter to skip):");
        String userComment = scanner.nextLine().trim();
        if (userComment.isEmpty()) {
            userComment = null;
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = today.format(formatter);

        approvalDAO.updateApproval(expenseId, user.getId(), approval, userComment, formattedDate);
        ConsoleUtil.printSuccess(
            "Expense #" + expenseId + " was " + approval + "."
        );
        ConsoleUtil.pause(scanner);
        logger.info(
            "Manager {} {} expense {}",
            user.getUsername(),
            approval,
            expenseId
        );
        
        if (userComment != null) {
            logger.info(
                "Manager {} left comment on expense {}",
                user.getUsername(),
                expenseId
            );
        }
    }

    public static void generateReport(Scanner scanner, ExpenseDAO expenseDAO, UserDAO userDAO) throws SQLException {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("Generate Report");
        System.out.println("1) Employee");
        System.out.println("2) Category");
        System.out.println("3) Date");
        System.out.println("4) Back");
        System.out.println();
        int user_input = InputValidation.getMenuChoice(scanner, 1, 4);
        String value = "";

        switch (user_input) {
            case 1:
                ConsoleUtil.printHeader("Employee Report");
                System.out.println("1) By Employee Id");
                System.out.println("2) All Employees");
                System.out.println();
                int input = InputValidation.getMenuChoice(scanner, 1, 2);
                if(input == 1){
                    int employeeID = InputValidation.getValidEmployeeId(scanner, userDAO, "Enter Employee Id: ");
                    EmployeeReportDTO report = expenseDAO.getEmployeeReport(employeeID);
                    TablePrinterUtil.printEmployeeReport(report);
                } else if (input == 2) {
                    List<EmployeeReportDTO> employeeReports = expenseDAO.getAllEmployeesReport();
                    TablePrinterUtil.printAllEmployeeReports(employeeReports);
                }
                ConsoleUtil.pause(scanner);
                break;
            case 2:
                ConsoleUtil.printHeader("Category Report");
                System.out.print("Enter Category: ");
                value = scanner.nextLine();
                List<CategoryReportDTO> categoryReports = expenseDAO.getExpensesByCategory(value);
                TablePrinterUtil.printCategoryReports(categoryReports);
                ConsoleUtil.pause(scanner);
                break;
            case 3:
                ConsoleUtil.printHeader("Date Report");
                LocalDate startDate = InputValidation.getValidDate(scanner, "Start Date (YYYY-MM-DD): ");

                LocalDate endDate = InputValidation.getValidDate(scanner, "End Date (YYYY-MM-DD): ");

                while (endDate.isBefore(startDate)) {
                    ConsoleUtil.printError("End date cannot be before the start date.");
                    endDate = InputValidation.getValidDate(
                        scanner,
                        "End Date (YYYY-MM-DD): "
                    );
                }
                List<DateReportDTO> dateReports =
                    expenseDAO.getExpensesByDate(
                        startDate.toString(),
                        endDate.toString()
                    );

                TablePrinterUtil.printDateReports(dateReports);
                ConsoleUtil.pause(scanner);
                break;
            case 4:
                return;
            default:
                break;
        }
        logger.info("Report generated by filter: {}", value);
    }

}