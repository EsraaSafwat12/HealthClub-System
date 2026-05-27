package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.awt.Desktop;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * WorkoutPlanScreen — coach builds a training plan for a member.
 * Can export to TXT and open WhatsApp link.
 */
public class WorkoutPlanScreen {

    private final ArrayList<User>        users;
    private final ArrayList<WorkoutPlan> plans;
    private final String                 coachName;

    public WorkoutPlanScreen(ArrayList<User> users, String coachName) {
        this.users     = users;
        this.coachName = coachName;
        this.plans     = FileManager.loadWorkoutPlans();
    }

    public VBox buildPanel(Label msgLabel) {
        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setStyle(LoginScreen.contentBg());

        // ── Create plan ──
        VBox createSection = LoginScreen.makeSectionBox("  Create Workout Plan");
        TextField memberIdF = LoginScreen.makeTextField("Member ID");
        ComboBox<String> goalBox = new ComboBox<>();
        goalBox.getItems().addAll("Weight Loss","Muscle Gain","Endurance","Flexibility","General Fitness");
        goalBox.setValue("General Fitness"); goalBox.setPrefHeight(40); goalBox.setPrefWidth(280);
        TextField notesF = LoginScreen.makeTextField("Notes (optional)");

        // exercise builder
        VBox exerciseSection = LoginScreen.makeSectionBox("  Add Exercises to Plan");
        TextField exNameF  = LoginScreen.makeTextField("Exercise name");
        TextField setsF    = LoginScreen.makeTextField("Sets (0 if time-based)");
        TextField repsF    = LoginScreen.makeTextField("Reps (0 if time-based)");
        TextField durF     = LoginScreen.makeTextField("Duration e.g. 30 sec (or leave empty)");
        TextField muscleF  = LoginScreen.makeTextField("Muscle group e.g. Chest");
        TextField exNotesF = LoginScreen.makeTextField("Exercise notes (optional)");
        TextArea  previewA = makeTextArea(160);

        final ArrayList<WorkoutExercise> tempExercises = new ArrayList<>();

        Button addExBtn = makeBtn("  ADD EXERCISE", "#27AE60");
        addExBtn.setOnAction(e -> {
            String exName = exNameF.getText().trim();
            if (exName.isEmpty()) { showMsg(msgLabel,"Exercise name required!",true); return; }
            try {
                int sets = Integer.parseInt(setsF.getText().trim());
                int reps = Integer.parseInt(repsF.getText().trim());
                tempExercises.add(new WorkoutExercise(
                    exName, sets, reps,
                    durF.getText().trim(), muscleF.getText().trim(), exNotesF.getText().trim()
                ));
                refreshPreview(previewA, tempExercises);
                exNameF.clear(); setsF.clear(); repsF.clear(); durF.clear();
                muscleF.clear(); exNotesF.clear();
                showMsg(msgLabel,"Exercise added!",false);
            } catch (Exception ex) { showMsg(msgLabel,"Sets and reps must be numbers!",true); }
        });

        Button clearExBtn = makeBtn("  CLEAR", "#E74C3C");
        clearExBtn.setOnAction(e -> { tempExercises.clear(); previewA.clear(); });

        exerciseSection.getChildren().addAll(
            LoginScreen.makeLabel("EXERCISE NAME"), exNameF,
            LoginScreen.makeLabel("SETS"), setsF,
            LoginScreen.makeLabel("REPS"), repsF,
            LoginScreen.makeLabel("DURATION"), durF,
            LoginScreen.makeLabel("MUSCLE GROUP"), muscleF,
            LoginScreen.makeLabel("NOTES"), exNotesF,
            new HBox(10, addExBtn, clearExBtn),
            LoginScreen.makeLabel("PREVIEW"), previewA
        );

        Button savePlanBtn  = makeBtn("  SAVE PLAN", "#2980B9");
        savePlanBtn.setOnAction(e -> {
            try {
                int mid = Integer.parseInt(memberIdF.getText().trim());
                User u  = FileManager.findUserById(mid, users);
                if (!(u instanceof Member)) { showMsg(msgLabel,"Member not found!",true); return; }
                if (tempExercises.isEmpty()) { showMsg(msgLabel,"Add at least one exercise!",true); return; }

                int newId = plans.stream().mapToInt(WorkoutPlan::getPlanId).max().orElse(0)+1;
                WorkoutPlan wp = new WorkoutPlan(newId, mid, u.getName(), coachName,
                    goalBox.getValue(), LocalDate.now().toString(), notesF.getText().trim());
                for (WorkoutExercise ex : tempExercises) wp.addExercise(ex);
                plans.add(wp);
                FileManager.saveWorkoutPlans(plans);
                tempExercises.clear(); previewA.clear();
                memberIdF.clear(); notesF.clear();
                showMsg(msgLabel,"Plan saved for " + u.getName() + "!",false);
            } catch (Exception ex) { showMsg(msgLabel,"Invalid Member ID!",true); }
        });

        createSection.getChildren().addAll(
            LoginScreen.makeLabel("MEMBER ID"), memberIdF,
            LoginScreen.makeLabel("GOAL"), goalBox,
            LoginScreen.makeLabel("NOTES"), notesF
        );

        // ── Send / Export ──
        VBox sendSection = LoginScreen.makeSectionBox("  Export & Share Plan");
        TextField planIdF = LoginScreen.makeTextField("Plan ID to export");
        TextField phoneF  = LoginScreen.makeTextField("WhatsApp number e.g. 201012345678");
        Button exportBtn  = makeBtn("  EXPORT TXT", "#8E44AD");
        Button waBtn      = makeBtn("  OPEN WHATSAPP", "#25D366");
        TextArea exportA  = makeTextArea(180);

        exportBtn.setOnAction(e -> {
            try {
                int pid = Integer.parseInt(planIdF.getText().trim());
                WorkoutPlan wp = findPlanById(pid);
                if (wp == null) { showMsg(msgLabel,"Plan not found!",true); return; }
                String text = wp.toReadableText();
                exportA.setText(text);
                // save to file
                String fname = "workout_plan_" + pid + ".txt";
                try (PrintWriter pw = new PrintWriter(new FileWriter(fname))) { pw.print(text); }
                showMsg(msgLabel,"Exported to " + fname,false);
            } catch (Exception ex) { showMsg(msgLabel,"Invalid Plan ID!",true); }
        });

        waBtn.setOnAction(e -> {
            try {
                int pid = Integer.parseInt(planIdF.getText().trim());
                WorkoutPlan wp = findPlanById(pid);
                if (wp == null) { showMsg(msgLabel,"Plan not found!",true); return; }
                String phone = phoneF.getText().trim().replaceAll("[^0-9]","");
                String msg = java.net.URLEncoder.encode(wp.toReadableText(), "UTF-8");
                String url  = "https://wa.me/" + phone + "?text=" + msg;
                Desktop.getDesktop().browse(new java.net.URI(url));
                showMsg(msgLabel,"Opening WhatsApp...",false);
            } catch (Exception ex) { showMsg(msgLabel,"Could not open WhatsApp: " + ex.getMessage(),true); }
        });

        sendSection.getChildren().addAll(
            LoginScreen.makeLabel("PLAN ID"), planIdF,
            LoginScreen.makeLabel("PHONE (for WhatsApp)"), phoneF,
            new HBox(10, exportBtn, waBtn),
            exportA
        );

        // ── List plans ──
        VBox listSection = LoginScreen.makeSectionBox("  My Plans");
        TextArea listArea = makeTextArea(180);
        Button listBtn = makeBtn("  LIST MY PLANS", "#3498DB");
        listBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder(
                String.format("%-4s | %-15s | %-20s | %-12s | Exercises%n","ID","Member","Goal","Date"));
            sb.append("─".repeat(70)).append("\n");
            for (WorkoutPlan wp : plans) {
                if (wp.getCoachName().equalsIgnoreCase(coachName))
                    sb.append(String.format("%-4d | %-15s | %-20s | %-12s | %d%n",
                        wp.getPlanId(), wp.getMemberName(), wp.getGoal(),
                        wp.getCreatedDate(), wp.getExercises().size()));
            }
            listArea.setText(sb.toString());
        });
        listSection.getChildren().addAll(listBtn, listArea);

        root.getChildren().addAll(createSection, exerciseSection, savePlanBtn,
                                   sendSection, listSection, msgLabel);
        return root;
    }

    private WorkoutPlan findPlanById(int id) {
        return plans.stream().filter(p->p.getPlanId()==id).findFirst().orElse(null);
    }

    private void refreshPreview(TextArea area, ArrayList<WorkoutExercise> exs) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (WorkoutExercise ex : exs) sb.append(i++).append(". ").append(ex).append("\n");
        area.setText(sb.toString());
    }

    private void showMsg(Label l, String t, boolean err) {
        l.setText(t); l.setTextFill(err ? Color.RED : Color.LIMEGREEN);
    }

    private Button makeBtn(String t, String c) {
        Button b = new Button(t);
        b.setPrefHeight(38); b.setPrefWidth(175);
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
