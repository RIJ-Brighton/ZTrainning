package com.lms.dao;

import com.lms.database.dbConnection;
import com.lms.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class userDao {
    private final Connection connection = dbConnection.getConnection();

    private User format(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("user_id"),
                resultSet.getString("FName"),
                resultSet.getString("LName"),
                resultSet.getInt("credit_score"),
                resultSet.getString("job"),
                resultSet.getInt("bank_id")
        );
    }

    // Create a new User
    public boolean createUser(User user) throws SQLException {
        String query = "INSERT INTO Users (FName, LName, credit_score, job, bank_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, user.getFName());
        statement.setString(2, user.getLName());
        statement.setInt(3, user.getCredit_score());
        statement.setString(4, user.getJob());
        statement.setInt(5, user.getBank_id());
        return statement.executeUpdate() > 0;
    }

    // Read all Users
    public List<User> getUsersByBankId(int bankId) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM Users WHERE bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, bankId);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            User user = format(resultSet); // Assuming format method is similar to the BankDao
            users.add(user);
        }
        return users;
    }


    // Read User by ID
    public User getUserByIdAndBankId(int userId, int bankId) throws SQLException {
        String query = "SELECT * FROM Users WHERE user_id = ? AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        statement.setInt(2, bankId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return format(resultSet);
        }
        return null;
    }

    // Update User by ID
    public boolean updateUserById(User user) throws SQLException {
        String query = "UPDATE Users SET FName = ?, LName = ?, credit_score = ?, job = ?, bank_id = ? WHERE user_id = ? AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, user.getFName());
        statement.setString(2, user.getLName());
        statement.setInt(3, user.getCredit_score());
        statement.setString(4, user.getJob());
        statement.setInt(5, user.getBank_id());
        statement.setInt(6, user.getUser_id());
        statement.setInt(7, user.getBank_id());
        return statement.executeUpdate() > 0;
    }

    // Delete User by ID
    public boolean deleteUserById(int userId, int bankId) throws SQLException {
        String query = "DELETE FROM Users WHERE user_id = ? AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        statement.setInt(2, bankId);
        return statement.executeUpdate() > 0;
    }
}


//CREATE TABLE Users (
//    user_id INT NOT NULL AUTO_INCREMENT,
//    FName VARCHAR(50) NOT NULL,
//    LName VARCHAR(50) NOT NULL,
//    credit_score INT NOT NULL,
//    job VARCHAR(100),
//    bank_id INT,
//    PRIMARY KEY (user_id),
//    FOREIGN KEY (bank_id)
//    REFERENCES Banks(bank_id)
//);

