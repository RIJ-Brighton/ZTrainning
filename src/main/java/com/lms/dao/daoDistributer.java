package com.lms.dao;

public class daoDistributer {
    private static bankDao bankDao;
    private static employeeDao employeeDao;
    private static userDao userDao;
    private static loanDao loanDao;
    private static underwrittenByDao underwrittenByDao;

    public static bankDao getBankDao() {
        if(bankDao == null){
            bankDao = new bankDao();
        }
        return bankDao;
    }

    public static employeeDao getEmployeeDao() {
        if(employeeDao == null){
            employeeDao = new employeeDao();
        }
        return employeeDao;
    }

    public static userDao getUserDao() {
        if(userDao == null){
            userDao = new userDao();
        }
        return userDao;
    }

    public static loanDao getLoanDao() {
        if(loanDao == null){
            loanDao = new loanDao();
        }
        return loanDao;
    }

    public static underwrittenByDao getUnderwrittenByDao() {
        if(underwrittenByDao == null){
            underwrittenByDao = new underwrittenByDao();
        }
        return underwrittenByDao;
    }
}
