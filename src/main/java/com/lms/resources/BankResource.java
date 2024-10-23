package com.lms.resources;

import com.lms.dao.bankDao;
import com.lms.dao.daoDistributer;
import com.lms.models.Bank;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import com.google.gson.Gson;

public class BankResource extends HttpServlet {

    private PrintWriter out = null;
    private bankDao bankDao = daoDistributer.getBankDao();

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) { // /banks/{bank_id}
            String bankIdString = pathInfo.substring(1);
            try {
                int bankId = Integer.parseInt(bankIdString);
                Bank bank = bankDao.getBankById(bankId);
                if (bank != null) {
                    out = res.getWriter();
                    out.println("{\"id\":" + bank.getId() + ", \"name\":\"" + bank.getName() + "\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"message\":\"Bank not found\"}");
                }
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Invalid bank ID\"}");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else { // /banks
            try {
                List<Bank> banks = bankDao.getAllBanks();
                Gson gson = new Gson();
                String json = gson.toJson(banks);
                out.println(json);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        String jsonString = requestBody.toString();
        try {
            Gson gson = new Gson();
            Bank bank = gson.fromJson(jsonString, Bank.class);

            if (bank.getName() == null || bank.getName().isEmpty()) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out = res.getWriter();
                out.println("{\"message\":\"Bad Request: 'name' field is required\"}");
                return;
            }
            boolean isCreated = bankDao.createBank(bank);
            out = res.getWriter();
            if (isCreated) {
                res.setStatus(HttpServletResponse.SC_CREATED);
                out.println("{\"message\":\"Bank inserted successfully\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Failed to insert bank\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
    public void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String bankIdString = pathInfo.substring(1);
            try {
                int bankId = Integer.parseInt(bankIdString);

                BufferedReader reader = req.getReader();
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
                String jsonString = requestBody.toString();
                String updatedName = null;
                Gson gson = new Gson();
                Bank bank = gson.fromJson(jsonString, Bank.class);
                if (bank.getName() == null || bank.getName().isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out = res.getWriter();
                    out.println("{\"message\":\"Bad Request: 'name' field is required\"}");
                    return;
                }
                updatedName = bank.getName();
                Bank bankToBeUpdated = new Bank(bankId, updatedName);

                boolean isUpdated = bankDao.updateBankById(bankToBeUpdated);
                out = res.getWriter();

                if (isUpdated) {
                    res.setStatus(HttpServletResponse.SC_OK);
                    out.println("{\"message\":\"Bank updated successfully\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"message\":\"Bank not found\"}");
                }
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out = res.getWriter();
                out.println("{\"message\":\"Invalid bank ID\"}");
            } catch (SQLException e) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out = res.getWriter();
                out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out = res.getWriter();
            out.println("{\"message\":\"Bank ID is required\"}");
        }

    }
    public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String bankIdString = pathInfo.substring(1);

            try {
                int bankId = Integer.parseInt(bankIdString);
                boolean isDeleted = bankDao.deleteBankById(bankId);

                if (isDeleted) {
                    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"message\":\"Bank not found\"}");
                }
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Invalid bank ID\"}");
            } catch (SQLException e) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"message\":\"Bank ID is required\"}");
        }
    }
}
