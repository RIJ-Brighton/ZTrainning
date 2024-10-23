package com.lms.dao;

import com.lms.database.dbConnection;
import com.lms.models.Loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class loanDao {
    private final Connection connection = dbConnection.getConnection();

    private Loan format(ResultSet resultSet) throws SQLException {
        return new Loan(
                resultSet.getInt("loan_id"),
                resultSet.getInt("amount"),
                resultSet.getString("loan_type"),
                resultSet.getString("loan_status"),
                resultSet.getInt("user_id"),
                resultSet.getInt("bank_id")
        );
    }

    // Create
    public boolean createLoan(Loan loan) throws SQLException {
        String query = "INSERT INTO Loans (amount, loan_type, loan_status, user_id, bank_id) VALUES (?, ?, 'pending', ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, loan.getAmount());
            statement.setString(2, loan.getLoanType());
            statement.setInt(3, loan.getUserId());
            statement.setInt(4, loan.getBankId());
            return statement.executeUpdate() > 0;
        }
    }

    // Read all loans
    public List<Loan> getAllLoansByBankId(int bankId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM Loans WHERE bank_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bankId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(format(resultSet));
                }
            }
        }
        return loans;
    }

    // Read all loans by status
    public List<Loan> getAllLoansByStatusAndBankId(String status, int bankId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM Loans WHERE bank_id = ? AND loan_status = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bankId);
            statement.setString(2, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(format(resultSet));
                }
            }
        }
        return loans;
    }

    // Read loan by ID
    public Loan getLoanById(int loanId, int bankId) throws SQLException {
        String query = "SELECT * FROM Loans WHERE loan_id = ? AND bank_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, loanId);
            statement.setInt(2, bankId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return format(resultSet);
                }
            }
        }
        return null;
    }

    //Read loans by user id
    public List<Loan> getLoansByUserId(int userId, int bankId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM Loans WHERE user_id = ? AND bank_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, bankId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(format(resultSet));
                }
            }
        }
        return loans;
    }

    //Read loans join underwritten
    public List<Loan> getLoansJoinUnderwrittenAndEmp(int empId, int bankId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT l.* FROM Loans l " +
                "JOIN Underwritten_by u ON l.loan_id = u.loan_id " +
                "WHERE u.emp_id = ? and u.bank_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, empId);
            statement.setInt(2, bankId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(format(resultSet));
                }
            }
        }
        return loans;
    }


    // Update loan status by ID
    public boolean updateLoanStatus(int loanId, String newStatus, int bankID) throws SQLException {
        String query = "UPDATE Loans SET loan_status = ? WHERE loan_id = ? AND bank_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newStatus);
            statement.setInt(2, loanId);
            statement.setInt(3, bankID);
            return statement.executeUpdate() > 0;
        }
    }

    // Delete loan by ID
    public boolean deleteLoanById(int loanId) throws SQLException {
        String query = "DELETE FROM Loans WHERE loan_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, loanId);
            return statement.executeUpdate() > 0;
        }
    }
}


