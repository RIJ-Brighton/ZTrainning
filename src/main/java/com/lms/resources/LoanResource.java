package com.lms.resources;

import com.google.gson.Gson;
import com.lms.dao.daoDistributer;
import com.lms.dao.loanDao;
import com.lms.dao.userDao;
import com.lms.dao.employeeDao;
import com.lms.dao.underwrittenByDao;
import com.lms.models.Loan;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class LoanResource extends HttpServlet {
    private PrintWriter out = null;
    private loanDao loanDao = daoDistributer.getLoanDao();
    private userDao userDao = daoDistributer.getUserDao();
    private employeeDao employeeDao = daoDistributer.getEmployeeDao();
    private underwrittenByDao underwrittenByDao = daoDistributer.getUnderwrittenByDao();

    //read
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        String bankIdParam = req.getParameter("bank_id");
        String loanStatus = req.getParameter("loan_status");
        int bankId = Integer.parseInt(bankIdParam);

        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                // GET loan by loan_id
                String loanIdString = pathInfo.substring(1);
                try {
                    int loanId = Integer.parseInt(loanIdString);
                    Loan loan = loanDao.getLoanById(loanId, bankId);
                    if (loan != null) {
                        Gson gson = new Gson();
                        String json = gson.toJson(loan);
                        out.println(json);
                    } else {
                        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.println("{\"message\":\"Loan not found\"}");
                    }
                } catch (NumberFormatException e) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Invalid loan ID\"}");
                }
            } else if (loanStatus != null) {
                //check status
                if(!(loanStatus.equals("approved") || loanStatus.equals("declined") || loanStatus.equals("paid") || loanStatus.equals("unpaid") || loanStatus.equals("partially_paid") || loanStatus.equals("pending"))) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Invalid status value\"}");
                    return;
                }
                //GET loan by status
                List<Loan> loans = loanDao.getAllLoansByStatusAndBankId(loanStatus, bankId);
                Gson gson = new Gson();
                String json = gson.toJson(loans);
                out.println(json);

            } else {
                // GET loans by bank_id
                try {
                    List<Loan> loans = loanDao.getAllLoansByBankId(bankId);
                    Gson gson = new Gson();
                    String json = gson.toJson(loans);
                    out.println(json);
                } catch (NumberFormatException e) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Invalid bank ID\"}");
                }
            }
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    // create
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        String jsonString = Loan.parseRequestBody(req);

        try {
            Gson gson = new Gson();
            Loan loan = gson.fromJson(jsonString, Loan.class);
            int bankId = Integer.parseInt(req.getParameter("bank_id"));
            loan.setBankId(bankId);
            int userId = loan.getUserId();
            if (loan.getAmount() <= 0 || loan.getLoanType() == null || loan.getUserId() <= 0) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Bad Request: All fields are required and must be valid\"}");
                return;
            }
            //check loan type
            if(!(loan.getLoanType().equals("Home_loan") || loan.getLoanType().equals("Gold_loan") ||loan.getLoanType().equals("Vehicle_loan") || loan.getLoanType().equals("Personal_loan") || loan.getLoanType().equals("Edu_loan"))){
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Bad Request: Invalid Loan type\"}");
                return;
            }

            //check if user_id is in bank
            if(userDao.getUserByIdAndBankId(userId, bankId) == null){
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"message\":\"User not found\"}");
                return;
            }

            boolean isCreated = loanDao.createLoan(loan);
            out = res.getWriter();
            if (isCreated) {
                res.setStatus(HttpServletResponse.SC_CREATED);
                out.println("{\"message\":\"Loan created successfully\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Failed to create loan\"}");
            }
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    // update
    public void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 2) {
            String[] pathParams = pathInfo.split("/");
            String loanIdString = pathParams[1];
            String loanStatus = pathParams[2];

            if(!(loanStatus.equals("approved") || loanStatus.equals("declined") || loanStatus.equals("paid") || loanStatus.equals("unpaid") || loanStatus.equals("partially_paid"))) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Invalid status value\"}");
                return;
            }

            try {
                int loanId = Integer.parseInt(loanIdString);
                int bankId = Integer.parseInt(req.getParameter("bank_id"));

                if (loanStatus == null) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Bad Request: 'loan_status' field is required\"}");
                    return;
                }
                if (loanStatus.equals("pending")) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Bad Request: 'loan_status' cannot be pending\"}");
                    return;
                }

                //check employee id from header
                int empId = req.getIntHeader("emp_id");
                if(employeeDao.getUnderwriterByIdAndBankId(empId, bankId) == null){
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.println("{\"message\":\"Only Underwriters from your bank can perform this operation\"}");
                    return;
                }

                boolean isUpdated = loanDao.updateLoanStatus(loanId, loanStatus, bankId);
                if (isUpdated) {
                    //update the Underwritten_by table
                    if(loanStatus.equals("approved") || loanStatus.equals("declined")){
                        if(underwrittenByDao.insertUnderwriter(empId, loanId, bankId)){
                            res.setStatus(HttpServletResponse.SC_OK);
                            out.println("{\"message\":\"Loan Underwritten successfully\"}");
                            return;
                        }
                    }
                    res.setStatus(HttpServletResponse.SC_OK);
                    out.println("{\"message\":\"Loan status updated successfully\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"message\":\"Loan not found\"}");
                }
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Invalid loan ID\"}");
            } catch (SQLException e) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"message\":\"Loan ID is required and status are required\"}");
        }
    }

    // delete
    public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String loanIdString = pathInfo.substring(1);
            try {
                int loanId = Integer.parseInt(loanIdString);
                boolean isDeleted = loanDao.deleteLoanById(loanId);

                if (isDeleted) {
                    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"message\":\"Loan not found\"}");
                }
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Invalid loan ID\"}");
            } catch (SQLException e) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"message\":\"Loan ID is required\"}");
        }
    }
}
