package com.mycompany.healthclubsystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Attendance — records a single check-in/check-out event for a member or coach.
 */
public class Attendance {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int    userId;
    private String userName;
    private String role;          // member | coach
    private String checkIn;
    private String checkOut;      // empty until checked out
    private String pin;           // 4-digit PIN used for check-in

    public Attendance(int userId, String userName, String role, String pin) {
        this.userId   = userId;
        this.userName = userName;
        this.role     = role;
        this.pin      = pin;
        this.checkIn  = LocalDateTime.now().format(FMT);
        this.checkOut = "";
    }

    // for loading from CSV
    private Attendance() {}

    public void checkOut() {
        this.checkOut = LocalDateTime.now().format(FMT);
    }

    public boolean isCheckedOut() { return !checkOut.isEmpty(); }

    public int    getUserId()   { return userId; }
    public String getUserName() { return userName; }
    public String getRole()     { return role; }
    public String getCheckIn()  { return checkIn; }
    public String getCheckOut() { return checkOut; }
    public String getPin()      { return pin; }

    /** Duration in minutes, -1 if not checked out yet */
    public long getDurationMinutes() {
        if (checkOut.isEmpty()) return -1;
        try {
            LocalDateTime in  = LocalDateTime.parse(checkIn,  FMT);
            LocalDateTime out = LocalDateTime.parse(checkOut, FMT);
            return java.time.Duration.between(in, out).toMinutes();
        } catch (Exception e) { return -1; }
    }

    public String toCSV() {
        return userId + "," + userName + "," + role + "," + pin
             + "," + checkIn + "," + checkOut;
    }

    public static Attendance fromCSV(String csv) {
        // format: userId,userName,role,pin,checkIn,checkOut
        // checkIn/Out contain spaces so we split carefully (first 4 commas only)
        String[] p = csv.split(",", 6);
        if (p.length < 6) return null;
        Attendance a = new Attendance();
        try {
            a.userId   = Integer.parseInt(p[0].trim());
            a.userName = p[1].trim();
            a.role     = p[2].trim();
            a.pin      = p[3].trim();
            a.checkIn  = p[4].trim();
            a.checkOut = p[5].trim();
        } catch (Exception e) { return null; }
        return a;
    }

    @Override
    public String toString() {
        String dur = isCheckedOut() ? getDurationMinutes() + " min" : "still in";
        return String.format("%-4d | %-15s | %-6s | %-16s | %-16s | %s",
            userId, userName, role, checkIn, checkOut.isEmpty() ? "---" : checkOut, dur);
    }
}
