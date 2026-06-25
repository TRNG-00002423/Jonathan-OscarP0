package com.rev.dao.DAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.rev.dao.model.User;

public interface UserDAO {

    // CREATE
    int insert(User user) throws SQLException;

    // READ
    Optional<User> findUserByid (int id) throws Exception;

    //UPDATE
    void updateUser(User user) throws SQLException;

    //DELETE
    void deleteUserById(int id) throws SQLException;

    // READ ALL
    List<User> findAll() throws SQLException;
}
