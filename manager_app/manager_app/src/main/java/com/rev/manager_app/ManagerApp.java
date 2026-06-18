package com.rev.manager_app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import com.rev.util.DatabaseConnectionUtil;

public class ManagerApp { 
    public static void main(String[] args) {
        Connection conn = DatabaseConnectionUtil.getConnection();
        Scanner scanner = new Scanner(System.in);
        User user = accessAccount(scanner, conn);

        System.out.println("Welcome to the Menu!");
        
        while(true){
            System.out.println("\nPlease enter 1 to view pending expenses");
            System.out.println("Please enter 2 to approve/deny an expense");
            System.out.println("Please enter 3 to generate a report for an expense");
            System.out.println("Please enter 4 to exit the app");
            int input = scanner.nextInt();

            switch (input){
                case 1:
                    try {
                        viewPendingExpenses(conn);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        reviewExpense(scanner, conn, user);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    try {
                        generateReport(scanner, conn);
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

    public static User accessAccount(Scanner scanner, Connection conn) {
        System.out.println("Please enter 1 if you have an existing account");
        System.out.println("Please enter 2 if you would like to create an account");
        int input = scanner.nextInt();
        User user = null;
        if (input == 1) {
            while (user == null) {
                user = login(scanner, conn);
            }
        } else if (input == 2) {
            while (user == null) {
                try {
                    user = createUser(scanner, conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!user.getRole().equals("Manager")) {
            System.out.println("You do not have access to this application. Please use the Employee app.");
        }
        return user;

    }

    public static User login(Scanner scanner, Connection conn) {
        System.out.println("Please enter a username");
        String username = scanner.next();
        System.out.println("Please enter a password");
        String password = scanner.next();
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        User user = null;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                user = new User(result.getInt("id"), result.getString("username"),
                        result.getString("password"), result.getString("role"));
                System.out.println("Logged in as " + user);
            } else {
                System.out.println("Username or password is incorrect.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static User createUser(Scanner scanner, Connection conn) throws SQLException {
        System.out.println("Please enter a username");
        String username = scanner.next();
        System.out.println("Please enter a password");
        String password = scanner.next();
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        User user = null;
        try (PreparedStatement stmt = conn.prepareStatement(
                query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, "Manager");
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                user = new User(id, username, password, "Manager");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return user;

        }
        System.out.println("User created successfully: " + user);
        return user;
    }

    public static void viewPendingExpenses(Connection conn) throws SQLException {
        String query = "SELECT expenses.user_id as user_id, expenses.amount, expenses.description, expenses.date, approvals.expense_id, approvals.status, approvals.reviewer, approvals.comment, approvals.review_date FROM expenses JOIN approvals ON expenses.id = approvals.expense_id WHERE approvals.status = ?";
        String status = "pending";

        try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, status);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("[Expense ID: " + rs.getInt("expense_id") + 
                "| Amount: " + rs.getDouble("amount") +
                "| Description: " + rs.getString("description") +
                "| Status: " + rs.getString("status") + "]");
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public static void reviewExpense(Scanner scanner, Connection conn, User user) throws SQLException {
        viewPendingExpenses(conn);
        System.out.println("Please enter expense ID:");
        int expenseId = scanner.nextInt();
        System.out.println("Enter 1 to approve or 2 to deny");
        int user_option = scanner.nextInt();
        String approval = (user_option == 1) ? "approved" : "denied";
        System.out.println("Enter 1 to leave a comment with the review or 2 to continue");          
        user_option = scanner.nextInt();
        String userComment = null;
        if(user_option == 1){
            System.out.println("Please enter comment for the review");
            userComment = scanner.nextLine();
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = today.format(formatter);


        String query = "UPDATE approvals SET status = ?, reviewer = ?, review_date = ?, comment = ? WHERE expense_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, approval);
            stmt.setInt(2, user.getId());
            stmt.setString(3, formattedDate);
            stmt.setString(4, userComment);
            stmt.setInt(5, expenseId);

            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected != 1)
                throw new SQLException("Expense id does not exist!");
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    // Add total amount, most common category
    public static void generateReport(Scanner scanner, Connection conn) throws SQLException {
        System.out.println("Generate report by:");
        System.out.println("1. Employee");
        System.out.println("2. Category");
        System.out.println("3. Date");
        int user_input = scanner.nextInt();
        String query = "";
        String value = "";

        switch (user_input) {
            case 1:
                System.out.println("Enter employee id:");
                query = "SELECT * FROM expenses WHERE user_id = ?";
                value = String.valueOf(scanner.nextInt());
                break;
            case 2:
                System.out.println("Enter Category:");
                query = "SELECT * FROM expenses WHERE category = ?";
                value = scanner.nextLine();
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
                        DateTimeFormatter.ofPattern("MMMM dd, yyyy");

                    value = dateObject.format(outputFormatter);

                    query = "SELECT * FROM expenses WHERE date = ?";

                } catch (DateTimeParseException e) {
                    System.out.println("Not a valid date. Please try again!");
                    return;
                }
                break;

            default:
                break;
        }

       try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, value);
            ResultSet rs = stmt.executeQuery();
            double totalExpenseAmount = 0.0;
            int expenseCount = 0;
            while (rs.next()) {
                System.out.println(
                    "Expense ID: " + rs.getInt("id") +
                    " Amount: " + rs.getDouble("amount") +
                    " Description: " + rs.getString("description")
                );

                totalExpenseAmount += rs.getDouble("amount");
                expenseCount++;
            }
            System.out.println("Report Aggregates: ");
            System.out.println("Expense Count: " + expenseCount);
            System.out.println("Total Amount: " + totalExpenseAmount);
            System.out.println("Average Expense Cost:" + (totalExpenseAmount / expenseCount));

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }
}