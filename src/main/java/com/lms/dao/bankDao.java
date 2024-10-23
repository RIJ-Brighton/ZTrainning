package com.lms.dao;

import com.lms.database.dbConnection;
import com.lms.models.Bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class bankDao {
    private final Connection connection = dbConnection.getConnection();

    private Bank format(ResultSet resultSet) throws SQLException {
        return new Bank(resultSet.getInt("bank_id"), resultSet.getString("bank_name"));
    }

    //create
    public boolean createBank(Bank bank) throws SQLException {
        String query = "INSERT INTO Banks (bank_name) VALUES(?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, bank.getName());
        return statement.executeUpdate() > 0;
    }

    //update

    //read all banks
    public List<Bank> getAllBanks() throws SQLException {
        List<Bank> banks = new ArrayList<>();
        String query = "SELECT * FROM Banks";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Bank bank = format(resultSet);
            banks.add(bank);
        }
        return banks;
    }
    //read bank by id
    public Bank getBankById(int bankId) throws SQLException {
        String query = "SELECT * FROM Banks WHERE bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, bankId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return format(resultSet);
        }
        return null;
    }

    //update bank by id
    public boolean updateBankById(Bank bank) throws SQLException {
        String query = "UPDATE Banks SET bank_name = ? WHERE bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, bank.getName());
        statement.setInt(2, bank.getId());
        return statement.executeUpdate() > 0;
    }

    //delete bank by id
    public boolean deleteBankById(int bankId) throws SQLException {
        String query = "DELETE FROM Banks WHERE bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, bankId);
        return statement.executeUpdate() > 0;
    }
}

//create table Banks (
//        bank_id INT Not null auto_increment,
//        bank_name varchar(50) Not null,
//        primary key (bank_id)
//);