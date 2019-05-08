package com.example.newapp;


public class User {
    private String name;
    public int state;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getState() { return state; }
    public void setState(int st) {
        this.state= st;
    }
    @Override
    public String toString() {
        return "User [name=" + name +state+"]";
    }
}