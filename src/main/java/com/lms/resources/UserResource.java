package com.lms.resources;

import com.lms.dao.userDao;
import com.lms.dao.loanDao;
import com.lms.dao.daoDistributer;
import com.lms.models.Loan;
import com.lms.models.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;

public class UserResource extends HttpServlet {

    private PrintWriter out = null;
    private userDao userDao = daoDistributer.getUserDao();
    private loanDao loanDao = daoDistributer.getLoanDao();

    // get users
    //implement /users/id/loans also for underwriters
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        int bankId = Integer.parseInt(req.getParameter("bank_id"));
        String pathInfo = req.getPathInfo();

        if (pathInfo != null) {
            // /users/{user_id}
            String[] paths = pathInfo.split("/");
            if(paths.length == 0){
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Provide user ID\"}");
                return;
            } else if (paths.length == 3) {
                if(paths[2].equals("loans")){
                    //get users loans
                    String userIdString = paths[1];
                    try {
                        int userId = Integer.parseInt(userIdString);

                        if(userId != req.getIntHeader("user_id")){
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            out.println("{\"message\":\"Unauthorized : Cannot perform this action\"}");
                            return;
                        }

                        User user = userDao.getUserByIdAndBankId(userId, bankId);
                        if(user == null){
                            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.println("{\"message\":\"User not found\"}");
                            return;
                        }

                        List<Loan> loans = loanDao.getLoansByUserId(userId, bankId);
                        if (!loans.isEmpty()) {
                            Gson gson = new Gson();
                            out.println(gson.toJson(loans));
                        } else {
                            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.println("{\"message\":\"No loans found for the user.\"}");
                        }
                    }catch (NumberFormatException e) {
                        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.println("{\"message\":\"Invalid user ID\"}");
                    } catch (SQLException e) {
                        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
                    }

                }
                else{
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Invalid URI\"}");
                }
            } else if(paths.length == 2){
                String userIdString = pathInfo.substring(1);
                try {
                    int userId = Integer.parseInt(userIdString);

                    if(userId != req.getIntHeader("user_id")){
                        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        out.println("{\"message\":\"Unauthorized : Cannot perform this action\"}");
                        return;
                    }

                    User user = userDao.getUserByIdAndBankId(userId, bankId);
                    if (user != null) {
                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        out.println(json);
                    } else {
                        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.println("{\"message\":\"User not found\"}");
                    }
                } catch (NumberFormatException e) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"message\":\"Invalid user ID\"}");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else { // /users
            try {
                List<User> users = userDao.getUsersByBankId(bankId);
                Gson gson = new Gson();
                String json = gson.toJson(users);
                out.println(json);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // create user
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String jsonString = User.parseRequestBody(req);
        try {
            Gson gson = new Gson();
            User user = gson.fromJson(jsonString, User.class);

            if (user.getFName() == null || user.getFName().isEmpty() ||
                    user.getLName() == null || user.getLName().isEmpty() ||
                    user.getJob() == null || user.getJob().isEmpty() ||
                    user.getCredit_score() == 0) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out = res.getWriter();
                out.println("{\"message\":\"Bad Request: 'FName', 'LName', 'job' and 'credit score' fields are required\"}");
                return;
            }
            String bankIdParam = req.getParameter("bank_id");
            int bankId = Integer.parseInt(bankIdParam);
            user.setBank_id(bankId);

            boolean isCreated = userDao.createUser(user);
            out = res.getWriter();
            if (isCreated) {
                res.setStatus(HttpServletResponse.SC_CREATED);
                out.println("{\"message\":\"User created successfully\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Failed to create user\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    // update user
    public void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) { // /users/{user_id}
            String userIdString = pathInfo.substring(1);
            try {
                int userId = Integer.parseInt(userIdString);
                int bankId = Integer.parseInt(req.getParameter("bank_id"));

                String jsonString = User.parseRequestBody(req);

                Gson gson = new Gson();
                User user = gson.fromJson(jsonString, User.class);

                if (user.getFName() == null || user.getFName().isEmpty() ||
                        user.getLName() == null || user.getLName().isEmpty() ||
                        user.getJob() == null || user.getJob().isEmpty() ||
                        user.getCredit_score() == 0) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out = res.getWriter();
                    out.println("{\"message\":\"Bad Request: 'FName', 'LName', 'job' and 'credit score' fields are required\"}");
                    return;
                }

                user.setUser_id(userId);
                user.setBank_id(bankId);
                boolean isUpdated = userDao.updateUserById(user);

                if (isUpdated) {
                    res.setStatus(HttpServletResponse.SC_OK);
                    out.println("{\"message\":\"User updated successfully\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"message\":\"User not found\"}");
                }
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Invalid user ID\"}");
            } catch (SQLException e) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"message\":\"User ID is required\"}");
        }
    }

    // delete user
    public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        int bankId = Integer.parseInt(req.getParameter("bank_id"));
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) { // /users/{user_id}
            String userIdString = pathInfo.substring(1);
            try {
                int userId = Integer.parseInt(userIdString);
                boolean isDeleted = userDao.deleteUserById(userId, bankId);

                if (isDeleted) {
                    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"message\":\"User not found\"}");
                }
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"message\":\"Invalid user ID\"}");
            } catch (SQLException e) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"message\":\"User ID is required\"}");
        }
    }
}
