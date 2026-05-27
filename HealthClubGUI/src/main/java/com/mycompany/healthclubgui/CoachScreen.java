// CoachScreen.java
package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import javafx.animation.*;
import javafx.animation.KeyFrame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

public class CoachScreen {

    private static Timeline activeAutoSave;
    private static Timeline activeSessionTimer;

    private static void stopActiveTimers() {
        if (activeAutoSave     != null) { activeAutoSave.stop();     activeAutoSave     = null; }
        if (activeSessionTimer != null) { activeSessionTimer.stop(); activeSessionTimer = null; }
    }

    private Coach coach;
    private ArrayList<User> users;
    private ArrayList<Message> messages;
    private ArrayList<Schedule> schedules;

    public CoachScreen(Coach coach, ArrayList<User> users,
                       ArrayList<Message> messages, ArrayList<Schedule> schedules) {
        this.coach     = coach;
        this.users     = users;
        this.messages  = messages;
        this.schedules = schedules;
    }

    public void show(Stage stage) {
        stage.setTitle("Power Gym - Coach");

        VBox sidebar     = makeSidebar();
        VBox contentArea = new VBox(15);
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle(LoginScreen.contentBg());

        Label msgLabel = new Label("");
        msgLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        showDashboard(contentArea, msgLabel);

        Button[] btns = (Button[]) sidebar.getUserData();
        btns[0].setOnAction(e -> showDashboard(contentArea, msgLabel));
        btns[1].setOnAction(e -> showMyMembers(contentArea, msgLabel));
        btns[2].setOnAction(e -> showAddSchedule(contentArea, msgLabel));
        btns[3].setOnAction(e -> showViewSchedules(contentArea, msgLabel));
        btns[4].setOnAction(e -> showSendMessage(contentArea, msgLabel));
        btns[5].setOnAction(e -> showWorkoutPlans(contentArea, msgLabel));
        btns[6].setOnAction(e -> showAttendanceCoach(contentArea, msgLabel));
        btns[7].setOnAction(e -> showUpdateInfo(contentArea, msgLabel));

        Button logoutBtn = (Button) sidebar.getChildren().get(sidebar.getChildren().size() - 1);
        logoutBtn.setOnAction(e -> {
            FileManager.saveUsers(users);
            FileManager.saveMessages(messages);
            FileManager.saveSchedules(schedules);
            LoginScreen.users     = FileManager.loadUsers();
            LoginScreen.bills     = FileManager.loadBills();
            LoginScreen.messages  = FileManager.loadMessages();
            LoginScreen.schedules = FileManager.loadSchedules();
            LoginScreen.goToLogin(stage);
        });

        HBox mainLayout = new HBox(sidebar, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        Scene coachScene = new Scene(scroll, 950, 680);
        stage.setScene(coachScene);
        stage.show();
        String scrollBg = AppState.darkMode
            ? "-fx-background-color: #0d0d0d; -fx-background: #0d0d0d;"
            : "-fx-background-color: #F5F5F5; -fx-background: #F5F5F5;";
        scroll.setStyle(scrollBg);
        sidebar.setStyle(LoginScreen.sidebarBg());
        contentArea.setStyle(LoginScreen.contentBg());

        // ===== CLOSE HANDLER =====
        stage.setOnCloseRequest(e -> {
            FileManager.saveUsers(users);
            FileManager.saveMessages(messages);
            FileManager.saveSchedules(schedules);
            System.exit(0);
        });

        stopActiveTimers();

        // ===== AUTO-SAVE every 5 minutes =====
        activeAutoSave = new Timeline(new KeyFrame(Duration.minutes(5), ev -> {
            FileManager.saveUsers(users);
            FileManager.saveMessages(messages);
            FileManager.saveSchedules(schedules);
        }));
        activeAutoSave.setCycleCount(Timeline.INDEFINITE);
        activeAutoSave.play();

        // ===== SESSION TIMEOUT: 15 min =====
        final long[] lastActivity = {System.currentTimeMillis()};
        coachScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, ev2 -> lastActivity[0] = System.currentTimeMillis());
        coachScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,   ev2 -> lastActivity[0] = System.currentTimeMillis());
        activeSessionTimer = new Timeline(new KeyFrame(Duration.minutes(1), ev -> {
            if (System.currentTimeMillis() - lastActivity[0] > 15 * 60 * 1000L) {
                stopActiveTimers();
                FileManager.saveUsers(users); FileManager.saveMessages(messages); FileManager.saveSchedules(schedules);
                LoginScreen.goToLogin(stage);
            }
        }));
        activeSessionTimer.setCycleCount(Timeline.INDEFINITE);
        activeSessionTimer.play();
    }

    // ===================== SCREENS =====================

    private void showDashboard(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83C\uDFE0  Coach Dashboard");

        Text welcome = new Text("Welcome back, " + coach.getName() + "! \uD83D\uDCAA");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        welcome.setFill(LoginScreen.primaryTextColor());
        welcome.setOpacity(0);
        area.getChildren().add(welcome);
        FadeTransition wft = new FadeTransition(Duration.millis(500), welcome);
        wft.setFromValue(0); wft.setToValue(1);
        wft.setDelay(Duration.millis(200)); wft.play();

        long myMembers = users.stream()
            .filter(u -> u instanceof Member &&
                ((Member) u).getAssignedCoachName().equalsIgnoreCase(coach.getName()))
            .count();
        long mySchedules = schedules.stream()
            .filter(s -> s.getCoachName().equalsIgnoreCase(coach.getName()))
            .count();

        HBox stats = new HBox(15);
        VBox[] cards = {
            makeStatCard("\uD83D\uDC65  My Members",   String.valueOf(myMembers),   "#FF6B00"),
            makeStatCard("\uD83D\uDCC5  My Schedules", String.valueOf(mySchedules), "#FF9A3C")
        };

        for (int i = 0; i < cards.length; i++) {
            cards[i].setOpacity(0);
            stats.getChildren().add(cards[i]);
            FadeTransition ft = new FadeTransition(Duration.millis(400), cards[i]);
            ft.setFromValue(0); ft.setToValue(1);
            ft.setDelay(Duration.millis(200 * i)); ft.play();
        }

        animateIn(area, stats, msg);

        if (!AppState.currentLangCode.equals("en")) {
            TranslationService.translateBatch(
                Arrays.asList("Welcome back,"),
                AppState.currentLangCode,
                t -> welcome.setText(t.getOrDefault("Welcome back,", "Welcome back,") +
                    " " + coach.getName() + "! \uD83D\uDCAA")
            );
        }
    }

    private void showMyMembers(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDC65  My Assigned Members");

        VBox section = makeSection("\uD83D\uDC65  Members List");
        TextArea membersArea = makeTextArea(250);
        Button viewBtn   = makeOrangeBtn("\uD83D\uDC65  VIEW MY MEMBERS", 220);
        Button exportBtn = makeOrangeBtn("\uD83D\uDCC4  EXPORT PDF", 150);

        viewBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder(
                String.format("%-5s | %-15s | %-15s%n", "ID", "Name", "Sub End"));
            sb.append("=".repeat(42)).append("\n");
            boolean any = false;
            for (User u : users) {
                if (u instanceof Member) {
                    Member m = (Member) u;
                    if (m.getAssignedCoachName().equalsIgnoreCase(coach.getName())) {
                        sb.append(String.format("%-5d | %-15s | %-15s%n",
                            m.getId(), m.getName(), m.getSubscriptionEndDate()));
                        any = true;
                    }
                }
            }
            membersArea.setText(any ? sb.toString() : "No members assigned yet.");
        });

        exportBtn.setOnAction(e -> exportMembersPDF());
        section.getChildren().addAll(new HBox(10, viewBtn, exportBtn), membersArea);
        animateIn(area, section, msg);
    }

    private void showAddSchedule(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCC5  Add Training Schedule");

        VBox section = makeSection("\uD83D\uDCC5  New Schedule");
        TextField midF  = LoginScreen.makeTextField("Member ID");
        TextField planF = LoginScreen.makeTextField("Plan Details");
        TextField dateF = LoginScreen.makeTextField("Date (YYYY-MM-DD)");
        Button addBtn   = makeOrangeBtn("\u2795  ADD SCHEDULE", 200);

        addBtn.setOnAction(e -> {
            String idTxt = midF.getText().trim();
            String plan  = planF.getText().trim();
            String date  = dateF.getText().trim();
            if (!idTxt.matches("[0-9]+")) { showError(msg, "ID must be numbers!"); return; }
            if (plan.isEmpty())           { showError(msg, "Plan cannot be empty!"); return; }
            try { LocalDate.parse(date); }
            catch (Exception ex)          { showError(msg, "Invalid date! Use YYYY-MM-DD"); return; }
            schedules.add(new Schedule(schedules.size() + 1,
                Integer.parseInt(idTxt), coach.getName(), plan, date));
            FileManager.saveSchedules(schedules);
            showSuccess(msg, "Schedule Added!");
            midF.clear(); planF.clear(); dateF.clear();
        });

        section.getChildren().addAll(
            LoginScreen.makeLabel("MEMBER ID"), midF,
            LoginScreen.makeLabel("PLAN DETAILS"), planF,
            LoginScreen.makeLabel("DATE (YYYY-MM-DD)"), dateF,
            addBtn
        );

        animateIn(area, section, msg);
    }

    private void showViewSchedules(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCC6  My Schedules");

        Button viewBtn = makeOrangeBtn("\uD83D\uDCC6  VIEW ALL SCHEDULES", 230);
        String cardsBg = AppState.darkMode ? "#0d0d0d" : "#F5F5F5";
        VBox cardsBox = new VBox(12);
        cardsBox.setPadding(new Insets(10, 0, 0, 0));
        cardsBox.setStyle("-fx-background-color: " + cardsBg + ";");

        viewBtn.setOnAction(e -> {
            cardsBox.getChildren().clear();
            int i = 0;
            boolean any = false;
            for (Schedule s : schedules) {
                if (s.getCoachName().equalsIgnoreCase(coach.getName())) {
                    any = true;
                    String cardBg = AppState.darkMode ? "#1a1a1a" : "#FFFFFF";
                    HBox card = new HBox(15);
                    card.setPadding(new Insets(15, 20, 15, 20));
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle(
                        "-fx-background-color: " + cardBg + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #FF6B00;" +
                        "-fx-border-radius: 10; -fx-border-width: 1.5;"
                    );

                    Label dateBox = new Label("\uD83D\uDCC5  " + s.getDate());
                    dateBox.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                    dateBox.setTextFill(Color.web("#FF6B00"));
                    dateBox.setPadding(new Insets(4, 10, 4, 10));
                    dateBox.setStyle(
                        "-fx-background-color: #FF6B0022; -fx-background-radius: 6;" +
                        "-fx-border-color: #FF6B00; -fx-border-radius: 6; -fx-border-width: 1;"
                    );
                    dateBox.setMinWidth(130); dateBox.setAlignment(Pos.CENTER);

                    VBox info = new VBox(4);
                    Text planT = new Text(s.getPlanDetails());
                    planT.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    planT.setFill(LoginScreen.primaryTextColor());
                    Text memberT = new Text("\uD83D\uDC65  Member ID: " + s.getMemberId());
                    memberT.setFont(Font.font("Arial", 11));
                    memberT.setFill(LoginScreen.secondaryTextColor());
                    info.getChildren().addAll(planT, memberT);
                    card.getChildren().addAll(dateBox, info);

                    card.setOpacity(0);
                    cardsBox.getChildren().add(card);
                    FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                    ft.setToValue(1); ft.setDelay(Duration.millis(i * 100)); ft.play();
                    TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
                    tt.setFromX(-20); tt.setToX(0); tt.setDelay(Duration.millis(i * 100)); tt.play();
                    i++;
                }
            }
            if (!any) {
                Text empty = new Text("No schedules added yet.");
                empty.setFont(Font.font("Arial", 13));
                empty.setFill(LoginScreen.secondaryTextColor());
                cardsBox.getChildren().add(empty);
            }
        });

        ScrollPane scrollCards = new ScrollPane(cardsBox);
        scrollCards.setFitToWidth(true); scrollCards.setPrefHeight(400);
        scrollCards.setStyle("-fx-background-color: " + cardsBg + "; -fx-background: " + cardsBg + ";");

        VBox section = makeSection("\uD83D\uDCC6  All My Schedules");
        section.getChildren().addAll(viewBtn, scrollCards);
        animateIn(area, section, msg);
    }

    private void showSendMessage(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCEC  Send Message to All Members");

        VBox section = makeSection("\uD83D\uDCEC  New Message");
        TextField msgF = LoginScreen.makeTextField("Type your message...");
        Button sendBtn = makeOrangeBtn("\uD83D\uDCEC  SEND TO ALL MEMBERS", 260);

        sendBtn.setOnAction(e -> {
            String content = msgF.getText().trim();
            if (content.isEmpty()) { showError(msg, "Message cannot be empty!"); return; }
            int count = 0;
            for (User u : users) {
                if (u instanceof Member &&
                    ((Member) u).getAssignedCoachName().equalsIgnoreCase(coach.getName())) {
                    messages.add(new Message(messages.size() + 1,
                        coach.getName(), u.getId(), content, LocalDate.now().toString()));
                    count++;
                }
            }
            FileManager.saveMessages(messages);
            showSuccess(msg, "Message sent to " + count + " member(s)!");
            msgF.clear();
        });

        section.getChildren().addAll(LoginScreen.makeLabel("MESSAGE"), msgF, sendBtn);
        animateIn(area, section, msg);
    }


    private void showWorkoutPlans(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83C\uDFCB\uFE0F  Workout Plans");
        WorkoutPlanScreen wps = new WorkoutPlanScreen(users, coach.getName());
        animateIn(area, wps.buildPanel(msg));
    }

    private void showAttendanceCoach(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCCB  Attendance");
        AttendanceManager am = new AttendanceManager(users);
        animateIn(area, am.buildPanel(msg));
    }

    private void showUpdateInfo(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\u2699\uFE0F  Update My Info");

        VBox section = makeSection("\u270F\uFE0F  Update Info");
        PasswordField oldPassF = LoginScreen.makePasswordField("Current Password");
        TextField newNameF     = LoginScreen.makeTextField("New Name");
        TextField newUnameF    = LoginScreen.makeTextField("New Username (lowercase only)");
        PasswordField newPassF = LoginScreen.makePasswordField("New Password");
        Button updateBtn       = makeOrangeBtn("\uD83D\uDCBE  UPDATE INFO", 200);

        updateBtn.setOnAction(e -> {
            if (!oldPassF.getText().equals(coach.getPassword())) {
                showError(msg, "Wrong password!"); return;
            }
            String nn = newNameF.getText().trim();
            String nu = newUnameF.getText().trim().toLowerCase();
            String np = newPassF.getText().trim();
            if (!nn.isEmpty() && nn.matches("[a-zA-Z ]+")) coach.setName(nn);
            if (!nu.isEmpty()) {
                if (!nu.matches("[a-z0-9_]+")) { showError(msg, "Username: lowercase only!"); return; }
                coach.setUsername(nu);
            }
            if (np.length() >= 4) coach.setPassword(np);
            FileManager.saveUsers(users);
            showSuccess(msg, "Info Updated!");
        });

        section.getChildren().addAll(
            LoginScreen.makeLabel("CURRENT PASSWORD"), oldPassF,
            LoginScreen.makeLabel("NEW NAME"), newNameF,
            LoginScreen.makeLabel("NEW USERNAME"), newUnameF,
            LoginScreen.makeLabel("NEW PASSWORD"), newPassF,
            updateBtn
        );

        animateIn(area, section, msg);
    }

    // ===================== PDF =====================

    private void exportMembersPDF() {
        try {
            PDDocument doc = new PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 40, y = 750, tableW = 515, rowH = 22;

            cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            cs.addRect(0, 780, 612, 62); cs.fill();
            cs.setNonStrokingColor(1f, 0.42f, 0f);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
            cs.beginText(); cs.newLineAtOffset(margin, 800);
            cs.showText("POWER GYM  —  Coach: " + coach.getName() + "  —  Members"); cs.endText();
            cs.setNonStrokingColor(0.6f, 0.6f, 0.6f);
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.beginText(); cs.newLineAtOffset(margin, 785);
            cs.showText("Generated: " + LocalDate.now()); cs.endText();
            cs.setStrokingColor(1f, 0.42f, 0f);
            cs.setLineWidth(2f);
            cs.moveTo(margin, 778); cs.lineTo(margin + tableW, 778); cs.stroke();

            y = 755;
            float[] colW = {40, 160, 140, 135};
            String[] headers = {"ID", "Name", "Sub End", "Status"};

            cs.setNonStrokingColor(0.15f, 0.15f, 0.15f);
            cs.addRect(margin, y - 5, tableW, rowH); cs.fill();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.setNonStrokingColor(1f, 0.6f, 0f);
            float xPos = margin + 5;
            cs.beginText(); cs.newLineAtOffset(xPos, y + 5);
            for (int h = 0; h < headers.length; h++) {
                cs.showText(headers[h]);
                if (h < headers.length - 1) cs.newLineAtOffset(colW[h], 0);
            }
            cs.endText();
            cs.setStrokingColor(1f, 0.42f, 0f);
            cs.setLineWidth(1.5f);
            cs.addRect(margin, y - 5, tableW, rowH); cs.stroke();

            y -= rowH + 2;
            boolean alt = false;
            for (User u : users) {
                if (!(u instanceof Member)) continue;
                Member m = (Member) u;
                if (!m.getAssignedCoachName().equalsIgnoreCase(coach.getName())) continue;

                String status;
                try {
                    long dl = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(), LocalDate.parse(m.getSubscriptionEndDate()));
                    status = dl < 0 ? "EXPIRED" : dl <= 7 ? "WARNING" : "ACTIVE";
                } catch (Exception ex) { status = "UNKNOWN"; }

                float[] bg = alt ? new float[]{0.1f,0.1f,0.1f} : new float[]{0.07f,0.07f,0.07f};
                cs.setNonStrokingColor(bg[0], bg[1], bg[2]);
                cs.addRect(margin, y - 5, tableW, rowH); cs.fill();

                float[] stripe = status.equals("ACTIVE") ? new float[]{0f,0.8f,0.3f}
                    : status.equals("EXPIRED") ? new float[]{1f,0.2f,0.2f} : new float[]{1f,0.7f,0f};
                cs.setNonStrokingColor(stripe[0], stripe[1], stripe[2]);
                cs.addRect(margin, y - 5, 4, rowH); cs.fill();

                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(0.9f, 0.9f, 0.9f);
                xPos = margin + 8;
                cs.beginText(); cs.newLineAtOffset(xPos, y + 5);
                cs.showText(String.valueOf(m.getId()));        cs.newLineAtOffset(colW[0], 0);
                cs.showText(m.getName());                      cs.newLineAtOffset(colW[1], 0);
                cs.showText(m.getSubscriptionEndDate());       cs.newLineAtOffset(colW[2], 0);
                cs.showText(status); cs.endText();

                cs.setStrokingColor(0.25f, 0.25f, 0.25f);
                cs.setLineWidth(0.3f);
                cs.addRect(margin, y - 5, tableW, rowH); cs.stroke();
                y -= rowH + 1; alt = !alt;
                if (y < 60) break;
            }

            cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            cs.addRect(0, 0, 612, 40); cs.fill();
            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.setNonStrokingColor(1f, 0.42f, 0f);
            cs.beginText(); cs.newLineAtOffset(margin, 15);
            cs.showText("Power Gym  |  Coach Members Report  |  " + LocalDate.now()); cs.endText();

            cs.close();
            String path = System.getProperty("user.dir") + File.separator + "coach_members_report.pdf";
            doc.save(path); doc.close();
            showAlert(Alert.AlertType.INFORMATION, "PDF saved!\n" + path);
        } catch (IOException e) { showAlert(Alert.AlertType.ERROR, "Failed: " + e.getMessage()); }
    }

    // ===================== HELPERS =====================

    private VBox makeSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setStyle(LoginScreen.sidebarBg());

        Text clubName = new Text("\uD83C\uDFCB\uFE0F  POWER GYM");
        clubName.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 15));
        clubName.setFill(Color.web("#FF6B00"));

        Text system = new Text("MANAGEMENT SYSTEM");
        system.setFont(Font.font("Arial", 10));
        system.setFill(Color.web("#FF9A3C"));

        Text userName = new Text("\uD83D\uDC64  " + coach.getName());
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        userName.setFill(Color.WHITE);

        Text userRole = new Text("COACH");
        userRole.setFont(Font.font("Arial", 11));
        userRole.setFill(Color.web("#FF9A3C"));

        Text menuTitle = new Text("MAIN MENU");
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuTitle.setFill(Color.web("#888888"));

        String[] items = {
            "\uD83C\uDFE0  Dashboard",
            "\uD83D\uDC65  My Members",
            "\uD83D\uDCC5  Add Schedule",
            "\uD83D\uDCC6  View Schedules",
            "\uD83D\uDCEC  Send Message",
            "\uD83C\uDFCB\uFE0F  Workout Plans",
            "\uD83D\uDCCB  Attendance",
            "\u2699\uFE0F  Update Info"
        };

        Button[] btns = new Button[items.length];
        for (int i = 0; i < items.length; i++) {
            btns[i] = makeSideBtn(items[i]);
            btns[i].setOpacity(0); btns[i].setTranslateX(-30);
            FadeTransition ft = new FadeTransition(Duration.millis(400), btns[i]);
            ft.setFromValue(0); ft.setToValue(1);
            ft.setDelay(Duration.millis(300 + i * 150));
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), btns[i]);
            tt.setFromX(-30); tt.setToX(0);
            tt.setDelay(Duration.millis(300 + i * 150));
            new ParallelTransition(ft, tt).play();
        }

        if (!AppState.currentLangCode.equals("en")) {
            java.util.Map<String, Button> map = new java.util.LinkedHashMap<>();
            for (int i = 0; i < items.length; i++) {
                String key = items[i].replaceAll("[^\\p{L}\\p{N} ]", "").trim();
                map.put(key, btns[i]);
            }
            ScreenTranslator.applyToButtons(map, AppState.currentLangCode, null);
        }

        Button logoutBtn = new Button("\uD83D\uDEAA  LOGOUT");
        logoutBtn.setPrefWidth(190); logoutBtn.setPrefHeight(40);
        logoutBtn.setStyle(
            "-fx-background-color: #FF4444; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        logoutBtn.setOpacity(0);
        FadeTransition lf = new FadeTransition(Duration.millis(400), logoutBtn);
        lf.setFromValue(0); lf.setToValue(1);
        lf.setDelay(Duration.millis(300 + items.length * 150)); lf.play();

        VBox header = new VBox(6, clubName, system, userName, userRole, new Separator(), menuTitle);
        header.setOpacity(0);
        FadeTransition hf = new FadeTransition(Duration.millis(500), header);
        hf.setFromValue(0); hf.setToValue(1); hf.play();

        Button themeToggle = new Button(AppState.darkMode ? "Dark Mode" : "Light Mode");
        themeToggle.setPrefWidth(190); themeToggle.setPrefHeight(36);
        themeToggle.setStyle(
            "-fx-background-color: #2a2a2a; -fx-text-fill: #CCCCCC;" +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;"
        );
        final VBox[] sidebarRef = {sidebar};
        themeToggle.setOnAction(ev -> {
            AppState.darkMode = !AppState.darkMode;
            themeToggle.setText(AppState.darkMode ? "Dark Mode" : "Light Mode");
            sidebarRef[0].setStyle(LoginScreen.sidebarBg());
            Button[] sbtns = (Button[]) sidebarRef[0].getUserData();
            if (sbtns != null && sbtns.length > 0) sbtns[0].fire();
        });

        sidebar.getChildren().add(header);
        sidebar.getChildren().addAll(btns);
        sidebar.getChildren().add(themeToggle);
        sidebar.getChildren().add(logoutBtn);
        sidebar.setUserData(btns);
        return sidebar;
    }

    private VBox makeSection(String title) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(20));
        section.setStyle(LoginScreen.sectionBg());
        Text t = new Text(title);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        t.setFill(LoginScreen.titleColor());
        section.getChildren().add(t);
        if (!AppState.currentLangCode.equals("en")) {
            String cleanKey = title.replaceAll("[^\\p{L}\\p{N} /]", "").trim();
            TranslationService.translateBatch(
                java.util.Arrays.asList(cleanKey),
                AppState.currentLangCode,
                tr -> t.setText(tr.getOrDefault(cleanKey, title))
            );
        }
        return section;
    }

    private VBox makeStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setPrefWidth(175); card.setPrefHeight(100);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        String cardBg = AppState.darkMode ? "#1a1a1a" : "#FFFFFF";
        card.setStyle(
            "-fx-background-color: " + cardBg + "; -fx-background-radius: 12;" +
            "-fx-border-color: " + color + "; -fx-border-radius: 12; -fx-border-width: 2;"
        );
        Text val = new Text(value);
        val.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 34));
        val.setFill(Color.web(color));
        Text lbl = new Text(label);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setFill(LoginScreen.secondaryTextColor());
        card.getChildren().addAll(val, lbl);
        return card;
    }

    private TextArea makeTextArea(int height) {
        TextArea ta = new TextArea();
        ta.setEditable(false); ta.setPrefHeight(height);
        ta.setStyle(LoginScreen.textAreaStyle());
        return ta;
    }

    private Button makeSideBtn(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(190); btn.setPrefHeight(38);
        btn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #CCCCCC;" +
            "-fx-font-size: 12px; -fx-background-radius: 8;" +
            "-fx-border-color: #333333; -fx-border-radius: 8; -fx-border-width: 1;" +
            "-fx-cursor: hand; -fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 10;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #FF6B00; -fx-text-fill: white;" +
            "-fx-font-size: 12px; -fx-background-radius: 8;" +
            "-fx-border-color: #FF6B00; -fx-border-radius: 8; -fx-border-width: 1;" +
            "-fx-cursor: hand; -fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 10;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #CCCCCC;" +
            "-fx-font-size: 12px; -fx-background-radius: 8;" +
            "-fx-border-color: #333333; -fx-border-radius: 8; -fx-border-width: 1;" +
            "-fx-cursor: hand; -fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 10;"
        ));
        return btn;
    }

    private Button makeOrangeBtn(String text, int width) {
        Button btn = new Button(text);
        btn.setPrefWidth(width); btn.setPrefHeight(40);
        btn.setStyle(LoginScreen.makeOrangeBtnStyle());
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: linear-gradient(to right, #FF9A3C, #FFB366);" +
            "-fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(LoginScreen.makeOrangeBtnStyle()));
        return btn;
    }

    private void animateTitle(VBox area, String titleText) {
        Text title = new Text(titleText);
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 24));
        title.setFill(LoginScreen.titleColor());
        title.setOpacity(0);
        area.getChildren().add(title);
        FadeTransition ft = new FadeTransition(Duration.millis(400), title);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        if (!AppState.currentLangCode.equals("en")) {
            String key = titleText.replaceAll("[^\\p{L}\\p{N} ]", "").trim();
            TranslationService.translateBatch(Arrays.asList(key),
                AppState.currentLangCode,
                t -> title.setText(t.getOrDefault(key, titleText)));
        }
    }

    private void animateIn(VBox area, javafx.scene.Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].setOpacity(0);
            area.getChildren().add(nodes[i]);
            FadeTransition ft = new FadeTransition(Duration.millis(400), nodes[i]);
            ft.setFromValue(0); ft.setToValue(1);
            ft.setDelay(Duration.millis(150 * i)); ft.play();
        }
    }

    private void showError(Label lbl, String t)   { lbl.setTextFill(Color.web("#FF4444")); lbl.setText(t); }
    private void showSuccess(Label lbl, String t) { lbl.setTextFill(Color.web("#00FF88")); lbl.setText(t); }
    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}