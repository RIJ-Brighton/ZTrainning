package com.lms.models;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

public class Loan {
    private int Id;
    private int amount;
    private String loan_type;
    private String loan_status;
    private int user_id;
    private int bank_id;

    public Loan() {}
    public Loan(int loanId, int amount, String loanType, String loanStatus, int userId, int bankId) {
        this.Id = loanId;
        this.amount = amount;
        this.loan_type = loanType;
        this.loan_status = loanStatus;
        this.user_id = userId;
        this.bank_id = bankId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getLoanStatus() {
        return loan_status;
    }

    public void setLoanStatus(String loanStatus) {
        this.loan_status = loanStatus;
    }

    public String getLoanType() {
        return loan_type;
    }

    public void setLoanType(String loanType) {
        this.loan_type = loanType;
    }

    public int getBankId() {
        return bank_id;
    }

    public void setBankId(int bankId) {
        this.bank_id = bankId;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int userId) {
        this.user_id = userId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int loanId) {
        this.Id = loanId;
    }

    public static String parseRequestBody(HttpServletRequest req) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        return requestBody.toString();
    }
}
