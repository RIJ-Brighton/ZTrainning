package com.lms.resources;

import com.google.gson.Gson;
import com.lms.dao.daoDistributer;
import com.lms.dao.employeeDao;
import com.lms.dao.loanDao;
import com.lms.models.Employee;
import com.lms.models.Loan;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.lms.resources.commonMethodsSharedByResources.validateAndGetBankId;

public class EmployeeResources extends HttpServlet {
    private PrintWriter out = null;
    private employeeDao employeeDao = daoDistributer.getEmployeeDao();
    private loanDao loanDao = daoDistributer.getLoanDao();

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        int bankId = validateAndGetBankId(req, res);
        if(bankId == -1){return;}

        Integer empId = commonMethodsSharedByResources.getIdFromURI(req, res);

        //loan upd by emp
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            String[] paths = pathInfo.split("/");

            if (paths.length == 0) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Provide underwriter ID\"}");
                return;
            }

            else if(paths.length == 2){
                //get emp by id
                String role;
                if (req.getRequestURI().contains("/underwriters")) {
                    role = "underwriter";
                } else if (req.getRequestURI().contains("/managers")) {
                    role = "manager";
                } else {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Bad Request: Role in URI not recognized. Expected '/managers' or '/underwriters'\"}");
                    return;
                }

                try {
                    if (empId != null) {
                        Employee employee;
                        if (role.equals("manager")) {
                            employee = employeeDao.getManagerByIdAndBankId(empId, bankId);
                        } else {
                            employee = employeeDao.getUnderwriterByIdAndBankId(empId, bankId);
                        }

                        if (employee == null) {
                            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.println("{\"message\":\"No " + role + " found with emp_id: " + empId + " and bank_id: " + bankId + "\"}");
                            return;
                        }

                        Gson gson = new Gson();
                        String jsonResponse = gson.toJson(employee);
                        res.setStatus(HttpServletResponse.SC_OK);
                        out.println(jsonResponse);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
                }
            } else if (paths.length == 3) {
                //emp loans verified
                if(req.getRequestURI().contains("/managers")){
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Managers dont have this record\"}");
                    return;
                }
                try {
                    int emp_id = Integer.parseInt(paths[1]);
                    List<Loan> loans = loanDao.getLoansJoinUnderwrittenAndEmp(emp_id, bankId);
                    if (!loans.isEmpty()) {
                        Gson gson = new Gson();
                        out.println(gson.toJson(loans));
                    } else {
                        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.println("{\"message\":\"No loans found for this underwriter.\"}");
                    }
                } catch (SQLException e) {
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
                }
            }

        }
        else {
            String role;
            if (req.getRequestURI().contains("/underwriters")) {
                role = "underwriter";
            } else if (req.getRequestURI().contains("/managers")) {
                role = "manager";
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Bad Request: Role in URI not recognized. Expected '/managers' or '/underwriters'\"}");
                return;
            }

            List<Employee> employees;
            if (role.equals("manager")) {
                try {
                    employees = employeeDao.getManagersByBankId(bankId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    employees = employeeDao.getUnderwritersByBankId(bankId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            if (employees.isEmpty()) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"message\":\"No " + role + "s found for the given bank ID\"}");
                return;
            }

            Gson gson = new Gson();
            String jsonResponse = gson.toJson(employees);
            res.setStatus(HttpServletResponse.SC_OK);
            out.println(jsonResponse);
        }


    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        try {

            int bankId = validateAndGetBankId(req, res);
            if(bankId == -1) {return;}

            //parse body
            Gson gson = new Gson();
            Employee employee = gson.fromJson(Employee.parseRequestBody(req), Employee.class);

            if (employee.getFName() == null || employee.getFName().isEmpty() ||
                    employee.getLName() == null || employee.getLName().isEmpty() ||
                    employee.getRole() == null || employee.getRole().isEmpty()) {

                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().println("{\"message\":\"Bad Request: 'FName', 'LName', and 'Role' fields are required\"}");
                return;
            }

            //check role
            if(!commonMethodsSharedByResources.checkRole(res, employee.getRole(), req.getHeader("role"))){
                return;
            }

            employee.setBankId(bankId);
            boolean isCreated = employeeDao.createEmployee(employee);

            if (isCreated) {
                res.setStatus(HttpServletResponse.SC_CREATED);
                res.getWriter().println("{\"message\":\"Employee inserted successfully\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().println("{\"message\":\"Failed to insert employee\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    public void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        int bankId = validateAndGetBankId(req, res);
        if(bankId == -1) {return;}
        Integer empId = commonMethodsSharedByResources.getIdFromURI(req, res);

        if (empId == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"message\":\"Employee Id is required\"}");
            return;
        }
        Gson gson = new Gson();
        Employee employee = gson.fromJson(Employee.parseRequestBody(req), Employee.class);
        employee.setBankId(bankId);
        employee.setId(empId);

        if(!commonMethodsSharedByResources.checkRole(res, employee.getRole(), req.getHeader("role"))){
            return;
        }

        String performer_id = req.getHeader("emp_id");
        if(performer_id == null || performer_id.isEmpty()){
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"message\":\"Unauthorized: require manager's id\"}");
            return;
        }

        try {
            Employee e = employeeDao.getManagerByIdAndBankId(Integer.parseInt(performer_id), bankId);
            if(e == null){
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"message\":\"Manager not found\"}");
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if((employee.getRole().equals("manager")) & (!performer_id.equals(empId.toString()))){
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"message\":\"Unauthorized: Only the respective manager can update their details\"}");
            return;
        }

        try {
            boolean isUpdated = employeeDao.updateEmployeeById(employee);
            if(isUpdated) {
                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                res.getWriter().println("{\"message\":\"something\"}");
            }else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"message\":\"Employee not found\"}");
            }
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            throw new RuntimeException(e);
        }
    }
    public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        int bankId = validateAndGetBankId(req, res);
        if(bankId == -1) {return;}
        Integer empId = commonMethodsSharedByResources.getIdFromURI(req, res);
        if(empId == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().println("{\"message\":\"Employee Id is required\"}");
            return;
        }

        if(req.getHeader("role") == null || req.getHeader("role").isEmpty()){
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().println("{\"message\":\"Unauthorized: can only be performed by managers\"}");
            return;
        }

        if(!req.getHeader("role").equals("manager")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"message\":\"Unauthorized: Only managers are allowed to perform this operation\"}");
            return;
        }
        try {
            boolean isDeleted = employeeDao.deleteEmployeeById(empId, bankId);
            if (isDeleted) {
                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"message\":\"Bank not found\"}");
            }
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            throw new RuntimeException(e);
        }
    }
}
