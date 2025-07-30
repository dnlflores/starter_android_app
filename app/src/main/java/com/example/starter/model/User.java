package com.example.starter.model;

import com.google.gson.annotations.SerializedName;

public class User {
    private int id;
    private String username;
    
    // Default constructor for Gson
    public User() {}
    
    // Constructor
    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
} 