package com.lms.dao;

import com.lms.database.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class underwrittenByDao {
    private final Connection connection = dbConnection.getConnection();

    public boolean insertUnderwriter(int empId, int loanId, int bankId) throws SQLException {
        String checkQuery = "SELECT loan_id FROM Underwritten_by WHERE loan_id = ?";
        PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
        checkStatement.setInt(1, loanId);
        ResultSet resultSet = checkStatement.executeQuery();

        if (resultSet.next()) {
            return false;
        }

        String insertQuery = "INSERT INTO Underwritten_by (emp_id, loan_id, bank_id) VALUES (?, ?, ?)";
        PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
        insertStatement.setInt(1, empId);
        insertStatement.setInt(2, loanId);
        insertStatement.setInt(3, bankId);

        return insertStatement.executeUpdate() > 0;
    }

}
