/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.healthclubsystem;

/**
 * Coach class inherits from User.
 */
public class Coach extends User {
    public Coach(int id, String name, String username, String password) {
        super(id, name, username, password, "coach");
    }

    @Override
    public String toCSV() {
        return getId() + "," + getName() + "," + getUsername() + "," + getPassword() + "," + getRole();
    }

    // Reportable interface - Coach report
    @Override
    public String generateReport() {
        return "Coach | ID: " + getId() + " | Name: " + getName() + " | Username: " + getUsername();
    }
}