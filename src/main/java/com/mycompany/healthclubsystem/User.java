/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.healthclubsystem;

/**
 * Abstract base class for all users in the system.
 * Implements Encapsulation and Abstraction.
 * Implements Reportable interface (Interface usage).
 */
public abstract class User implements Reportable {
    private int id;
    private String name;
    private String username;
    private String password;
    private String role; // admin, coach, member

    public User(int id, String name, String username, String password, String role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters & Setters (Encapsulation)
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }

    // Abstract methods to be implemented by subclasses (Polymorphism)
    public abstract String toCSV();

    // From Reportable interface - each subclass defines its own report (Polymorphism)
    @Override
    public abstract String generateReport();

    @Override
    public String toString() {
        return getId() + " - " + getName() + " (" + getRole() + ")";
    }

    public void setPasswordDirect(String password) {
        this.password = password;
    }
}
