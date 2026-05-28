/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.healthclubsystem;

/**
 * Bill class handles billing records.
 * Required for Admin Module: Manage Billing.
 */
public class Bill {
    private int billId;
    private int memberId;
    private double amount;
    private String description;
    private String date;
    private boolean isPaid;

    public Bill(int billId, int memberId, double amount, String description, String date) {
        this.billId = billId;
        this.memberId = memberId;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.isPaid = false;
    }

    public int getBillId() { return billId; }
    public int getMemberId() { return memberId; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { this.isPaid = paid; }

    public String toCSV() {
        return billId + "," + memberId + "," + amount + "," + description + "," + date + "," + isPaid;
    }

    public static Bill fromCSV(String csv) {
        String[] parts = csv.split(",");
        Bill b = new Bill(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Double.parseDouble(parts[2]), parts[3], parts[4]);
        b.setPaid(Boolean.parseBoolean(parts[5]));
        return b;
    }
}