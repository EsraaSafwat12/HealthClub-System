package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import java.time.LocalDate;
import java.util.*;

/**
 * AttendanceManager — handles check-in/check-out via 4-digit PIN.
 * Used inside Admin and Coach screens.
 */
public class AttendanceManager {

    private final ArrayList<User>       users;
    private final ArrayList<Attendance> records;
    private final Map<Integer,String>   pins;

    public AttendanceManager(ArrayList<User> users) {
        this.users   = users;
        this.records = FileManager.loadAttendance();
        this.pins    = FileManager.loadPins();
        // auto-assign PINs for users who don't have one
        ensurePins();
    }

    // ── ensure every user has a PIN ──────────────────────────
    private void ensurePins() {
        boolean changed = false;
        for (User u : users) {
            if (!pins.containsKey(u.getId())) {
                pins.put(u.getId(), generatePin(u));
                changed = true;
            }
        }
        if (changed) FileManager.savePins(pins);
    }

    private String generatePin(User u) {
        // default PIN = last 4 digits of ID padded, e.g. ID=5 → "0005"
        return String.format("%04d", u.getId() % 10000);
    }

    // ── build the Attendance UI panel ────────────────────────
    public VBox buildPanel(Label msgLabel) {
        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setStyle(LoginScreen.contentBg());

        // ── Check-in section ──
        VBox checkinSection = LoginScreen.makeSectionBox("  Check-In / Check-Out via PIN");

        TextField pinField = LoginScreen.makeTextField("Enter 4-digit PIN");
        pinField.setMaxWidth(200);

        Button checkinBtn  = makeBtn("  CHECK IN",  "#27AE60");
        Button checkoutBtn = makeBtn("  CHECK OUT", "#E67E22");
        HBox btnRow = new HBox(10, checkinBtn, checkoutBtn);

        checkinBtn.setOnAction(e -> handleCheckIn(pinField.getText().trim(), msgLabel));
        checkoutBtn.setOnAction(e -> handleCheckOut(pinField.getText().trim(), msgLabel));

        checkinSection.getChildren().addAll(
            LoginScreen.makeLabel("PIN"), pinField, btnRow
        );

        // ── Today's log ──
        VBox logSection = LoginScreen.makeSectionBox("  Today's Attendance Log");
        TextArea logArea = makeTextArea(220);
        Button refreshBtn = makeBtn("  REFRESH", "#3498DB");
        refreshBtn.setOnAction(e -> refreshLog(logArea));
        refreshLog(logArea);

        logSection.getChildren().addAll(refreshBtn, logArea);

        // ── PIN management ──
        VBox pinSection = LoginScreen.makeSectionBox("  PIN Management");
        TextField uidField  = LoginScreen.makeTextField("User ID");
        TextField newPinF   = LoginScreen.makeTextField("New 4-digit PIN");
        Button setPinBtn    = makeBtn("  SET PIN", "#8E44AD");
        TextArea pinList    = makeTextArea(150);
        Button listPinsBtn  = makeBtn("  LIST ALL PINS", "#2C3E50");

        setPinBtn.setOnAction(e -> {
            try {
                int uid = Integer.parseInt(uidField.getText().trim());
                String np = newPinF.getText().trim();
                if (!np.matches("[0-9]{4}")) { showMsg(msgLabel, "PIN must be 4 digits!", true); return; }
                User u = FileManager.findUserById(uid, users);
                if (u == null) { showMsg(msgLabel, "User not found!", true); return; }
                pins.put(uid, np);
                FileManager.savePins(pins);
                showMsg(msgLabel, "PIN updated for " + u.getName(), false);
            } catch (Exception ex) { showMsg(msgLabel, "Invalid ID!", true); }
        });

        listPinsBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder(
                String.format("%-4s | %-15s | %-6s | %s%n", "ID","Name","Role","PIN"));
            sb.append("─".repeat(40)).append("\n");
            for (User u : users) {
                String pin = pins.getOrDefault(u.getId(), "----");
                sb.append(String.format("%-4d | %-15s | %-6s | %s%n",
                    u.getId(), u.getName(), u.getRole(), pin));
            }
            pinList.setText(sb.toString());
        });

        pinSection.getChildren().addAll(
            LoginScreen.makeLabel("USER ID"), uidField,
            LoginScreen.makeLabel("NEW PIN"), newPinF,
            setPinBtn, listPinsBtn, pinList
        );

        root.getChildren().addAll(checkinSection, logSection, pinSection, msgLabel);
        return root;
    }

    // ── Check-in logic ────────────────────────────────────────
    private void handleCheckIn(String pin, Label msg) {
        User u = findByPin(pin);
        if (u == null) { showMsg(msg, "Invalid PIN!", true); return; }
        // check not already checked in today without checkout
        for (Attendance a : records) {
            if (a.getUserId() == u.getId()
                    && a.getCheckIn().startsWith(LocalDate.now().toString())
                    && !a.isCheckedOut()) {
                showMsg(msg, u.getName() + " is already checked in!", true);
                return;
            }
        }
        Attendance att = new Attendance(u.getId(), u.getName(), u.getRole(), pin);
        records.add(att);
        FileManager.saveAttendance(records);
        showMsg(msg, "  " + u.getName() + " checked in at " + att.getCheckIn(), false);
    }

    private void handleCheckOut(String pin, Label msg) {
        User u = findByPin(pin);
        if (u == null) { showMsg(msg, "Invalid PIN!", true); return; }
        // find last open record
        Attendance open = null;
        for (int i = records.size()-1; i >= 0; i--) {
            Attendance a = records.get(i);
            if (a.getUserId() == u.getId() && !a.isCheckedOut()) { open = a; break; }
        }
        if (open == null) { showMsg(msg, u.getName() + " is not checked in!", true); return; }
        open.checkOut();
        FileManager.saveAttendance(records);
        showMsg(msg, "  " + u.getName() + " checked out. Duration: " + open.getDurationMinutes() + " min", false);
    }

    private User findByPin(String pin) {
        for (Map.Entry<Integer,String> e : pins.entrySet())
            if (e.getValue().equals(pin)) return FileManager.findUserById(e.getKey(), users);
        return null;
    }

    private void refreshLog(TextArea area) {
        String today = LocalDate.now().toString();
        StringBuilder sb = new StringBuilder(
            String.format("%-4s | %-15s | %-6s | %-16s | %-16s | Duration%n",
                "ID","Name","Role","Check-In","Check-Out"));
        sb.append("─".repeat(80)).append("\n");
        for (Attendance a : records)
            if (a.getCheckIn().startsWith(today)) sb.append(a).append("\n");
        area.setText(sb.toString());
    }

    // ── helpers ───────────────────────────────────────────────
    private void showMsg(Label lbl, String txt, boolean isError) {
        lbl.setText(txt);
        lbl.setTextFill(isError ? Color.RED : Color.LIMEGREEN);
    }

    private Button makeBtn(String text, String color) {
        Button b = new Button(text);
        b.setPrefHeight(38); b.setPrefWidth(160);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;"
                 + "-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;");
        return b;
    }

    private TextArea makeTextArea(int height) {
        TextArea ta = new TextArea();
        ta.setEditable(false); ta.setPrefHeight(height);
        ta.setFont(Font.font("Monospaced", 11));
        ta.setStyle("-fx-control-inner-background:#1a1a1a;-fx-text-fill:#e0e0e0;");
        return ta;
    }
}
