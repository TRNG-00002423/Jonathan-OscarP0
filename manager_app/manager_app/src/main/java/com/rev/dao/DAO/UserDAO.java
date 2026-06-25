package com.rev.dao.DAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.rev.dao.model.User;

public interface UserDAO {

    // CREATE
    User createUser(String username, String password, String role) throws SQLException;

    // READ
    Optional<User> findUserByid (int id) throws Exception;
    Optional<User> login(String username, String password) throws SQLException;

    //UPDATE
    void updateUser(User user) throws SQLException;

    //DELETE
    void deleteUserById(int id) throws SQLException;

    // READ ALL
    List<User> findAll() throws SQLException;
}
