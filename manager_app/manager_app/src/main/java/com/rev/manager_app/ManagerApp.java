package com.rev.manager_app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import com.rev.util.DatabaseConnectionUtil;

public class ManagerApp {
    public static void main(String[] args) {
        Connection conn = DatabaseConnectionUtil.getConnection();
        Scanner scanner = new Scanner(System.in);
        User user = accessAccount(scanner, conn);
        
    }

    public static User accessAccount(Scanner scanner, Connection conn){
        System.out.println("Please enter 1 if you have an existing account");
        System.out.println("Please enter 2 if you would like to create an account");
        int input = scanner.nextInt();
        User user = null;
        if(input == 1){
            while(user == null){
                user = login(scanner, conn);
            }
        }
        else if(input == 2){
            while(user == null){
                try {
                    user = createUser(scanner, conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!user.getRole().equals("Manager")){
            System.out.println("You do not have access to this application. Please use the Employee app.");
        }
        return user;

    }
    public static User login(Scanner scanner, Connection conn){
        System.out.println("Please enter a username");
        String username = scanner.next();
        System.out.println("Please enter a password");
        String password = scanner.next();
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        User user = null;
        try (PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet result = stmt.executeQuery();
            if(result.next()){
                user = new User(result.getInt("id"), result.getString("username"), 
                result.getString("password"), result.getString("role"));
                System.out.println("Logged in as " + user);
            }
            else{
                System.out.println("Username or password is incorrect.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
    public static User createUser(Scanner scanner, Connection conn) throws SQLException{
        System.out.println("Please enter a username");
        String username = scanner.next();
        System.out.println("Please enter a password");
        String password = scanner.next();
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        User user = null;
        try (PreparedStatement stmt = conn.prepareStatement(
            query, Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, "Manager");
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                user = new User(id, username, password, "Manager");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return user;

        }
        System.out.println("User created successfully: " + user);
        return user;
    }
}