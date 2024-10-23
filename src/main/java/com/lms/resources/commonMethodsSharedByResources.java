package com.lms.resources;

import com.lms.dao.bankDao;
import com.lms.dao.daoDistributer;
import com.lms.models.Bank;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class commonMethodsSharedByResources {

    public static int validateAndGetBankId(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String bankIdParam = req.getParameter("bank_id");

        bankDao bankDao = daoDistributer.getBankDao();
        int bankId;
        try {
            bankId = Integer.parseInt(bankIdParam);
            Bank bank = bankDao.getBankById(bankId);
            if (bank == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().println("{\"message\":\"Bank not found\"}");
                return -1;
            }else{
                return bankId;
            }
        } catch (NumberFormatException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().println("{\"message\":\"Bad Request: 'bank_id' must be a valid integer\"}");
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Integer getIdFromURI(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String[] pathParts = req.getRequestURI().split("/");
        if (pathParts.length == 4) {
            try {
                return Integer.parseInt(pathParts[3]);
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().println("{\"message\":\"Bad Request: 'id' must be a valid integer\"}");
                return null;
            }
        }else{
            return null;
        }
    }

    public static boolean checkRole(HttpServletResponse res, String performedOn, String performedBy) throws IOException {
        if (performedOn.equals("underwriter") && (performedBy == null || !performedBy.equals("manager"))) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"message\":\"Unauthorized: Only managers are allowed to perform this operation\"}");
            return false;
        }else{
            return true;
        }
    }
}
