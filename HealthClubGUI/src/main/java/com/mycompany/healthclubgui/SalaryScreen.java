package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.time.LocalDate;
import java.util.ArrayList;

/** Salary management panel — used inside AdminScreen */
public class SalaryScreen {

    private final ArrayList<User>   users;
    private final ArrayList<Salary> salaries;

    public SalaryScreen(ArrayList<User> users) {
        this.users    = users;
        this.salaries = FileManager.loadSalaries();
    }

    public VBox buildPanel(Label msgLabel) {
        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setStyle(LoginScreen.contentBg());

        // ── Add salary record ──
        VBox addSection = LoginScreen.makeSectionBox("  Add Salary Record");
        TextField coachIdF  = LoginScreen.makeTextField("Coach ID");
        TextField baseF     = LoginScreen.makeTextField("Base Salary ($)");
        TextField bonusF    = LoginScreen.makeTextField("Bonus ($) — 0 if none");
        TextField dedF      = LoginScreen.makeTextField("Deductions ($) — 0 if none");
        TextField monthF    = LoginScreen.makeTextField("Month (YYYY-MM) e.g. 2025-06");

        Button addBtn = makeBtn("  ADD RECORD", "#27AE60");
        addBtn.setOnAction(e -> {
            try {
                int    cid  = Integer.parseInt(coachIdF.getText().trim());
                double base = Double.parseDouble(baseF.getText().trim());
                double bon  = Double.parseDouble(bonusF.getText().trim());
                double ded  = Double.parseDouble(dedF.getText().trim());
                String mon  = monthF.getText().trim();

                User u = FileManager.findUserById(cid, users);
                if (!(u instanceof Coach)) { showMsg(msgLabel,"Coach not found!",true); return; }
                if (!mon.matches("\\d{4}-\\d{2}")) { showMsg(msgLabel,"Month must be YYYY-MM",true); return; }

                int newId = salaries.stream().mapToInt(Salary::getSalaryId).max().orElse(0) + 1;
                salaries.add(new Salary(newId, cid, u.getName(), base, bon, ded, mon));
                FileManager.saveSalaries(salaries);
                showMsg(msgLabel,"Salary record added!",false);
                coachIdF.clear(); baseF.clear(); bonusF.clear(); dedF.clear(); monthF.clear();
            } catch (Exception ex) { showMsg(msgLabel,"Invalid input!",true); }
        });

        addSection.getChildren().addAll(
            LoginScreen.makeLabel("COACH ID"), coachIdF,
            LoginScreen.makeLabel("BASE SALARY"), baseF,
            LoginScreen.makeLabel("BONUS"), bonusF,
            LoginScreen.makeLabel("DEDUCTIONS"), dedF,
            LoginScreen.makeLabel("MONTH"), monthF,
            addBtn
        );

        // ── Mark as paid ──
        VBox paySection = LoginScreen.makeSectionBox("  Mark Salary Paid");
        TextField salIdF = LoginScreen.makeTextField("Salary Record ID");
        Button payBtn = makeBtn("  MARK PAID", "#8E44AD");
        payBtn.setOnAction(e -> {
            try {
                int sid = Integer.parseInt(salIdF.getText().trim());
                Salary s = salaries.stream().filter(x->x.getSalaryId()==sid).findFirst().orElse(null);
                if (s == null) { showMsg(msgLabel,"Record not found!",true); return; }
                if (s.isPaid())  { showMsg(msgLabel,"Already paid!",true); return; }
                s.markPaid(LocalDate.now().toString());
                FileManager.saveSalaries(salaries);
                showMsg(msgLabel,"Salary marked as paid!",false);
                salIdF.clear();
            } catch (Exception ex) { showMsg(msgLabel,"Invalid ID!",true); }
        });
        paySection.getChildren().addAll(LoginScreen.makeLabel("SALARY ID"), salIdF, payBtn);

        // ── List ──
        VBox listSection = LoginScreen.makeSectionBox("  Salary Records");
        TextArea listArea = makeTextArea(200);
        Button listAllBtn    = makeBtn("  ALL RECORDS", "#3498DB");
        Button unpaidBtn     = makeBtn("  UNPAID ONLY",  "#E67E22");
        Button summaryBtn    = makeBtn("  MONTHLY SUMMARY", "#2C3E50");

        listAllBtn.setOnAction(e -> {
            StringBuilder sb = buildHeader();
            for (Salary s : salaries) sb.append(s).append("\n");
            listArea.setText(sb.toString());
        });

        unpaidBtn.setOnAction(e -> {
            StringBuilder sb = buildHeader();
            for (Salary s : salaries) if (!s.isPaid()) sb.append(s).append("\n");
            listArea.setText(sb.toString());
        });

        summaryBtn.setOnAction(e -> {
            String thisMonth = LocalDate.now().toString().substring(0,7);
            double totalNet = 0;
            StringBuilder sb = new StringBuilder("Monthly Summary — " + thisMonth + "\n" + "─".repeat(50) + "\n");
            for (Salary s : salaries) {
                if (s.getMonth().equals(thisMonth)) {
                    sb.append(String.format("%-15s | Net: $%.0f | %s%n",
                        s.getCoachName(), s.getNet(), s.isPaid()?"PAID":"UNPAID"));
                    totalNet += s.getNet();
                }
            }
            sb.append("─".repeat(50)).append(String.format("%nTOTAL PAYROLL: $%.0f%n",totalNet));
            listArea.setText(sb.toString());
        });

        listSection.getChildren().addAll(new HBox(8,listAllBtn,unpaidBtn,summaryBtn), listArea);
        root.getChildren().addAll(addSection, paySection, listSection, msgLabel);
        return root;
    }

    private StringBuilder buildHeader() {
        StringBuilder sb = new StringBuilder(
            String.format("%-4s | %-15s | %-7s | %-8s | %-7s | %-6s | %-8s | %s%n",
                "ID","Coach","Month","Base","Bonus","Ded","Net","Status"));
        sb.append("─".repeat(85)).append("\n");
        return sb;
    }

    private void showMsg(Label l, String t, boolean err) {
        l.setText(t); l.setTextFill(err ? Color.RED : Color.LIMEGREEN);
    }

    private Button makeBtn(String t, String c) {
        Button b = new Button(t);
        b.setPrefHeight(38); b.setPrefWidth(160);
        b.setStyle("-fx-background-color:"+c+";-fx-text-fill:white;"
                 + "-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;");
        return b;
    }

    private TextArea makeTextArea(int h) {
        TextArea ta = new TextArea(); ta.setEditable(false); ta.setPrefHeight(h);
        ta.setFont(Font.font("Monospaced",11));
        ta.setStyle("-fx-control-inner-background:#1a1a1a;-fx-text-fill:#e0e0e0;");
        return ta;
    }
}
