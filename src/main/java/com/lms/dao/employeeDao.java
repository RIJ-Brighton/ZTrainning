package com.lms.dao;

import com.lms.database.dbConnection;
import com.lms.models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class employeeDao {
    private final Connection connection = dbConnection.getConnection();

    private Employee format(ResultSet resultSet) throws SQLException {
        return new Employee(resultSet.getInt("emp_id"), resultSet.getString("FName"), resultSet.getString("LName"), resultSet.getString("role"), resultSet.getInt("bank_id"));
    }

    // Create employee
    public boolean createEmployee(Employee employee) throws SQLException {
        String query = "INSERT INTO Employees (FName, LName, role, bank_id) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, employee.getFName());
        statement.setString(2, employee.getLName());
        statement.setString(3, employee.getRole());
        statement.setInt(4, employee.getBankId());
        return statement.executeUpdate() > 0;
    }

    // Read employee by id
    public Employee getManagerByIdAndBankId(int empId, int bankId) throws SQLException {
        String query = "SELECT * FROM Employees WHERE role = 'manager' AND emp_id = ? AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, empId);
        statement.setInt(2, bankId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return format(resultSet);
        }
        return null;
    }
    public Employee getUnderwriterByIdAndBankId(int empId, int bankId) throws SQLException {
        String query = "SELECT * FROM Employees WHERE role = 'underwriter' AND emp_id = ? AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, empId);
        statement.setInt(2, bankId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return format(resultSet);
        }
        return null;
    }

    // Read all employees
    public List<Employee> getManagersByBankId(int bankId) throws SQLException {
        List<Employee> managers = new ArrayList<>();
        String query = "SELECT * FROM Employees WHERE role = 'manager' AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, bankId);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Employee manager = format(resultSet);
            managers.add(manager);
        }

        return managers;
    }
    public List<Employee> getUnderwritersByBankId(int bankId) throws SQLException {
        List<Employee> underwriters = new ArrayList<>();
        String query = "SELECT * FROM Employees WHERE role = 'underwriter' AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, bankId);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Employee underwriter = format(resultSet);
            underwriters.add(underwriter);
        }

        return underwriters;
    }

    // Update employee by id
    public boolean updateEmployeeById(Employee employee) throws SQLException {
        String query = "UPDATE Employees SET FName = ?, LName = ?, role = ?, bank_id = ? WHERE emp_id = ? AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, employee.getFName());
        statement.setString(2, employee.getLName());
        statement.setString(3, employee.getRole());
        statement.setInt(4, employee.getBankId());
        statement.setInt(5, employee.getId());
        statement.setInt(6, employee.getBankId());
        return statement.executeUpdate() > 0;
    }

    public boolean deleteEmployeeById(int empId, int bankId) throws SQLException {
        String query = "DELETE FROM Employees WHERE emp_id = ? AND bank_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, empId);
        statement.setInt(2, bankId);
        return statement.executeUpdate() > 0;
    }
}

//CREATE TABLE Employees (
//        emp_id INT AUTO_INCREMENT PRIMARY KEY,
//        FName VARCHAR(50) NOT NULL,
//        LName VARCHAR(50) NOT NULL,
//        role ENUM('manager', 'underwriter') NOT NULL,
//        bank_id INT,
//        FOREIGN KEY (bank_id) REFERENCES Banks(bank_id)
//        );
