package com.lms.filters;

import com.lms.dao.bankDao;
import com.lms.dao.daoDistributer;
import com.lms.models.Bank;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebFilter({"/managers", "/managers/*", "/underwriters", "/underwriters/*", "/users", "/users/*", "/loans", "/loans/*"})
public class bankIdFilter implements Filter {
    private bankDao bankDao = daoDistributer.getBankDao();
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        String bankIdParam = req.getParameter("bank_id");
        HttpServletResponse resp = (HttpServletResponse) res;
        if (bankIdParam == null || bankIdParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"message\":\"Bad Request: 'bank_id' is required\"}");
        } else {
            try {
                int bankId = Integer.parseInt(bankIdParam);
                Bank b = bankDao.getBankById(bankId);
                if(b == null){
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().println("{\"message\":\"Bank not found\"}");
                }else
                    chain.doFilter(req, res);
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println("{\"message\":\"Database error: " + e.getMessage() + "\"}");
                throw new RuntimeException(e);
            }
        }
    }
}
