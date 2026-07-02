package com.rev.manager_app;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
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
import com.rev.dao.model.Expense;
import com.rev.dao.model.User;
import com.rev.util.DatabaseConnectionUtil;
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
        } catch (SQLException ex) {
            System.out.println("Database error occurred.");
            logger.error("Database error occured", ex);
            ex.printStackTrace();
        }

        System.out.println("Welcome to the Menu!");

        while (true) {
            System.out.println("\n1. View pending expenses");
            System.out.println("2. Approve/Deny an expense");
            System.out.println("3. Generate a report");
            System.out.println("4. Exit the app");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1 and 4.");
                scanner.nextLine();
                continue;
            }

            int input = scanner.nextInt();
            scanner.nextLine();

            switch (input) {
                case 1:
                    try {
                        viewPendingExpenses(expenseDAO);
                    } catch (SQLException e) {
                        logger.error("Error viewing pending expenses", e);
                        System.out.println("Unable to Retrieve Pending Expenses.");
                    }
                    break;

                case 2:
                    try {
                        reviewExpense(scanner, user, expenseDAO, approvalDAO);
                    } catch (SQLException e) {
                        logger.error("Error reviewing expense", e);
                        System.out.println("Unable to Review Expense.");
                    }
                    break;

                case 3:
                    try {
                        generateReport(scanner, expenseDAO);
                    } catch (SQLException e) {
                        logger.error("Error generating report", e);
                        System.out.println("Unable to Generate Report.");
                    }
                    break;

                case 4:
                    System.out.println("Goodbye!");
                    return;

                default:
                    System.out.println("Please Choose a Number Between 1 and 4.");
            }
        }

    }

    public static User accessAccount(Scanner scanner, UserDAO userDAO) throws SQLException {
         while (true) {
            System.out.println("1. Log In");
            System.out.println("2. Create Account");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input!");
                scanner.nextLine();
                continue;
            }

            int input = scanner.nextInt();
            scanner.nextLine();

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

                default:
                    System.out.println("Please Enter 1 or 2.");
                    continue;
            }

            if (!user.getRole().equals("Manager")) {
                System.out.println("You do not have access to this application. Please use the Employee app.");
                logger.warn("Employee login attempted on manager app.");
                continue;
            }

            return user;
        }
    }

    public static User loginFlow(Scanner scanner, UserDAO userDAO) throws SQLException {
        System.out.println("Enter Username:");
        String username = scanner.next();
        System.out.println("Enter Password");
        String password = scanner.next();
        
        Optional<User> user = userDAO.login(username, password);

        if (user.isPresent()
                && PasswordUtil.verifyPassword(
                        password,
                        user.get().getPassword())) {
                            
            System.out.println("Login successful!");
            return user.get();
        } else {
            System.out.println("Invalid Credentials.");
            logger.warn("Login unsuccessful for username: {}", username);
            return null;
        }
    }

    public static User newUser(Scanner scanner, UserDAO userDAO) throws SQLException {
        System.out.println("Enter Username"); 
        String username = scanner.next();
        System.out.println("Enter Password");
        String password = scanner.next();

        String hashedPassword = PasswordUtil.hashPassword(password);
        
        User user = userDAO.createUser(username, hashedPassword);

        System.out.println("User Created Successfully!");
        logger.info("User created successfully: {}", username);
        return user; 
    }

    public static void viewPendingExpenses(ExpenseDAO expenseDAO) throws SQLException {

        List<ExpenseWithStatusDTO> expenseList = expenseDAO.getPendingExpenses();
        logger.info("Manager viewing pending expenses");

        if (!expenseList.isEmpty()){
            for (ExpenseWithStatusDTO expenseWithStatusDTO : expenseList) {
                System.out.println(expenseWithStatusDTO);
            }
        }
        else {
            logger.info("No pending expenses found");
        }
        TablePrinterUtil.printPendingExpenses(expenseList);
        
    }

    public static void reviewExpense(Scanner scanner, User user, ExpenseDAO expenseDAO, ApprovalDAO approvalDAO) throws SQLException {
        viewPendingExpenses(expenseDAO);
        System.out.println("Enter Expense ID:");
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
            System.out.println("Enter Comment for the Review:");
            userComment = scanner.nextLine();
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = today.format(formatter);

        approvalDAO.updateApproval(expenseId, user.getId(), approval, userComment, formattedDate);
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

    public static void generateReport(Scanner scanner, ExpenseDAO expenseDAO) throws SQLException {
        System.out.println("Generate Report By:");
        System.out.println("1. Employee");
        System.out.println("2. Category");
        System.out.println("3. Date");
        int user_input = scanner.nextInt();
        scanner.nextLine();
        String value = "";
        List<Expense> expenses = new ArrayList<>();
        

        switch (user_input) {
            case 1:
                System.out.println("1. By Employee Id");
                System.out.println("2. All Employees");
                int input = scanner.nextInt();
                if(input == 1){
                    System.out.println("Enter Employee Id:");
                    int employeeID = scanner.nextInt();
                    EmployeeReportDTO report = expenseDAO.getEmployeeReport(employeeID);
                    TablePrinterUtil.printEmployeeReport(report);
                } else if (input == 2) {
                    List<EmployeeReportDTO> reports = expenseDAO.getAllEmployeesReport();
                    TablePrinterUtil.printAllEmployeeReports(reports);
                }
                break;
            case 2:
                System.out.println("Enter Category:");
                value = scanner.nextLine();
                List<CategoryReportDTO> reports = expenseDAO.getExpensesByCategory(value);
                TablePrinterUtil.printCategoryReports(reports);
                break;
            case 3:
                System.out.println("Start Date (YYYY-MM-DD):");
                String input1 = scanner.nextLine();
                System.out.println("End Date (YYYY-MM-DD):");
                String input2 = scanner.nextLine();

                try {
                    DateTimeFormatter formatter =new DateTimeFormatterBuilder()
                                                    .appendPattern("uuuu-MM-dd")
                                                    .toFormatter()
                                                    .withResolverStyle(ResolverStyle.STRICT);

                    LocalDate startDate = LocalDate.parse(input1, formatter);
                    LocalDate endDate = LocalDate.parse(input2, formatter);

                    if (endDate.isBefore(startDate)) {
                        System.out.println("End Date Cannot be Before Start Date.");
                        return;
                    }
                    
                    List<DateReportDTO> reports1 = expenseDAO.getExpensesByDate(startDate.toString(), endDate.toString());
                    TablePrinterUtil.printDateReports(reports1);
                } catch (DateTimeParseException e) {
                    System.out.print("Invalid Date. Please Enter a Date in YYYY-MM-DD Format: ");
                }

                break;
            default:
                break;
        }
        logger.info("Report generated by filter: {}", value);
    }

}