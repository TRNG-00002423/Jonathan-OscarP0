package com.rev.dao.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.rev.dao.model.User;

public class UserDAOImpl implements UserDAO{
    private final Connection conn;


    public UserDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public User createUser(String username, String password, String role) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<User> findUserByid(int id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<User> login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                // System.out.println("Logged in as " + user);
                return Optional.of(mapRow(result));
            } else {
                System.out.println("Username or password is incorrect.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void updateUser(User user) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteUserById(int id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<User> findAll() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")
        );
    }

}
