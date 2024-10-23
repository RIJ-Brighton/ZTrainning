package com.lms.models;

public class Bank {
    private String name;
    private int id;

    public Bank(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public Bank(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
