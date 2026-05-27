/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.healthclubsystem;

/**
 * Admin class inherits from User.
 */
public class Admin extends User {
    public Admin(int id, String name, String username, String password) {
        super(id, name, username, password, "admin");
    }

    @Override
    public String toCSV() {
        return getId() + "," + getName() + "," + getUsername() + "," + getPassword() + "," + getRole();
    }

    // Reportable interface - Admin report shows role info
    @Override
    public String generateReport() {
        return "Admin | ID: " + getId() + " | Name: " + getName() + " | Username: " + getUsername();
    }
}