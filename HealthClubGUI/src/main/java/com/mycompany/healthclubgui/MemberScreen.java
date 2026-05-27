// MemberScreen.java
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

public class MemberScreen {

    private static Timeline activeAutoSave;
    private static Timeline activeSessionTimer;

    private static void stopActiveTimers() {
        if (activeAutoSave     != null) { activeAutoSave.stop();     activeAutoSave     = null; }
        if (activeSessionTimer != null) { activeSessionTimer.stop(); activeSessionTimer = null; }
    }

    private Member member;
    private ArrayList<Message> messages;
    private ArrayList<Schedule> schedules;
    private ArrayList<Bill> bills;

    public MemberScreen(Member member, ArrayList<Message> messages,
                        ArrayList<Schedule> schedules, ArrayList<Bill> bills) {
        this.member    = member;
        this.messages  = messages;
        this.schedules = schedules;
        this.bills     = bills;
    }

    public void show(Stage stage) {
        stage.setTitle("Power Gym - Member");

        VBox sidebar     = makeSidebar();
        VBox contentArea = new VBox(15);
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle(LoginScreen.contentBg());

        Label msgLabel = new Label("");
        msgLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        showDashboard(contentArea, msgLabel);

        Button[] btns = (Button[]) sidebar.getUserData();
        btns[0].setOnAction(e -> showDashboard(contentArea, msgLabel));
        btns[1].setOnAction(e -> showSubscription(contentArea, msgLabel));
        btns[2].setOnAction(e -> showSchedules(contentArea, msgLabel));
        btns[3].setOnAction(e -> showMessages(contentArea, msgLabel));
        btns[4].setOnAction(e -> showMyBills(contentArea, msgLabel));
        btns[5].setOnAction(e -> showMyPlan(contentArea, msgLabel));
        btns[6].setOnAction(e -> showUpdateInfo(contentArea, msgLabel));

        Button logoutBtn = (Button) sidebar.getChildren().get(sidebar.getChildren().size() - 1);
        logoutBtn.setOnAction(e -> {
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
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);

        Scene memberScene = new Scene(scroll, 950, 680);
        stage.setScene(memberScene);
        stage.show();
        String scrollBg = AppState.darkMode
            ? "-fx-background-color: #0d0d0d; -fx-background: #0d0d0d;"
            : "-fx-background-color: #F5F5F5; -fx-background: #F5F5F5;";
        scroll.setStyle(scrollBg);
        sidebar.setStyle(LoginScreen.sidebarBg());
        contentArea.setStyle(LoginScreen.contentBg());

        // ===== CLOSE HANDLER =====
        stage.setOnCloseRequest(e -> {
            FileManager.saveMessages(messages);
            FileManager.saveSchedules(schedules);
            System.exit(0);
        });

        stopActiveTimers();

        // ===== AUTO-SAVE every 5 minutes =====
        activeAutoSave = new Timeline(new KeyFrame(Duration.minutes(5), ev -> {
            FileManager.saveMessages(messages);
            FileManager.saveSchedules(schedules);
        }));
        activeAutoSave.setCycleCount(Timeline.INDEFINITE);
        activeAutoSave.play();

        // ===== SESSION TIMEOUT: 15 min =====
        final long[] lastActivity = {System.currentTimeMillis()};
        memberScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, ev2 -> lastActivity[0] = System.currentTimeMillis());
        memberScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,   ev2 -> lastActivity[0] = System.currentTimeMillis());
        activeSessionTimer = new Timeline(new KeyFrame(Duration.minutes(1), ev -> {
            if (System.currentTimeMillis() - lastActivity[0] > 15 * 60 * 1000L) {
                stopActiveTimers();
                FileManager.saveMessages(messages); FileManager.saveSchedules(schedules);
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
        animateTitle(area, "\uD83C\uDFE0  Member Dashboard");

        Text welcome = new Text("Welcome back, " + member.getName() + "! \uD83D\uDCAA");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        welcome.setFill(LoginScreen.primaryTextColor());
        welcome.setOpacity(0);
        area.getChildren().add(welcome);
        FadeTransition wft = new FadeTransition(Duration.millis(500), welcome);
        wft.setFromValue(0); wft.setToValue(1); wft.setDelay(Duration.millis(200)); wft.play();

        String status, color;
        try {
            boolean expired = LocalDate.now().isAfter(LocalDate.parse(member.getSubscriptionEndDate()));
            status = expired ? "EXPIRED" : "ACTIVE";
            color  = expired ? "#FF4444" : "#00FF88";
        } catch (Exception e) { status = "UNKNOWN"; color = "#888888"; }

        HBox stats = new HBox(15);
        VBox[] cards = {
            makeStatCard("\uD83D\uDCC5  Status",         status,                          color),
            makeStatCard("\uD83C\uDFCB\uFE0F  Coach",    member.getAssignedCoachName(),   "#FF9A3C"),
            makeStatCard("\u23F0  Sub End",               member.getSubscriptionEndDate(), "#FF6B00")
        };

        for (int i = 0; i < cards.length; i++) {
            cards[i].setOpacity(0);
            stats.getChildren().add(cards[i]);
            FadeTransition ft = new FadeTransition(Duration.millis(400), cards[i]);
            ft.setFromValue(0); ft.setToValue(1);
            ft.setDelay(Duration.millis(200 * i)); ft.play();
        }

        area.getChildren().addAll(stats, msg);

        if (!AppState.currentLangCode.equals("en")) {
            TranslationService.translateBatch(
                Arrays.asList("Welcome back,", "EXPIRED", "ACTIVE"),
                AppState.currentLangCode,
                t -> welcome.setText(t.getOrDefault("Welcome back,", "Welcome back,") +
                    " " + member.getName() + "! \uD83D\uDCAA")
            );
        }
    }

    private void showSubscription(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCC5  My Subscription");

        String endDate = member.getSubscriptionEndDate();
        String status, color, bg;
        try {
            LocalDate end = LocalDate.parse(endDate);
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), end);
            if (daysLeft < 0) {
                status = "\u274C  EXPIRED"; color = "#FF4444";
                bg = AppState.darkMode ? "#2a0a0a" : "#ffebee";
            } else if (daysLeft <= 7) {
                status = "\u26A0\uFE0F  WARNING — " + daysLeft + " days left";
                color = "#FFB300";
                bg = AppState.darkMode ? "#2a1f00" : "#fff8e1";
            } else {
                status = "\u2705  ACTIVE — " + daysLeft + " days left";
                color = "#00FF88";
                bg = AppState.darkMode ? "#0a2a1a" : "#e8f5e9";
            }
        } catch (Exception e) {
            status = "UNKNOWN"; color = "#888888";
            bg = AppState.darkMode ? "#1a1a1a" : "#f5f5f5";
        }

        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 12; -fx-border-width: 2;"
        );

        Label tagLabel = new Label(status);
        tagLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        tagLabel.setTextFill(Color.web(color));
        tagLabel.setPadding(new Insets(6, 16, 6, 16));
        tagLabel.setStyle(
            "-fx-background-color: " + color + "22;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 8; -fx-border-width: 1;"
        );

        Text endT = new Text("\uD83D\uDCC5  Subscription End Date:  " + endDate);
        endT.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        endT.setFill(LoginScreen.primaryTextColor());

        Text coachT = new Text("\uD83C\uDFCB\uFE0F  Assigned Coach:  " + member.getAssignedCoachName());
        coachT.setFont(Font.font("Arial", 13));
        coachT.setFill(LoginScreen.secondaryTextColor());

        card.getChildren().addAll(tagLabel, endT, coachT);
        animateIn(area, card, msg);
    }

    private void showSchedules(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCC5  My Training Schedules");

        Button viewBtn = makeOrangeBtn("\uD83D\uDCC5  VIEW MY SCHEDULES", 230);
        String cardsBg = AppState.darkMode ? "#0d0d0d" : "#F5F5F5";
        VBox cardsBox = new VBox(12);
        cardsBox.setPadding(new Insets(10, 0, 0, 0));
        cardsBox.setStyle("-fx-background-color: " + cardsBg + ";");

        viewBtn.setOnAction(e -> {
            cardsBox.getChildren().clear();
            int i = 0; boolean any = false;
            for (Schedule s : schedules) {
                if (s.getMemberId() == member.getId()) {
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
                    Text coachT = new Text("\uD83C\uDFCB\uFE0F  Coach: " + s.getCoachName());
                    coachT.setFont(Font.font("Arial", 11));
                    coachT.setFill(LoginScreen.secondaryTextColor());
                    info.getChildren().addAll(planT, coachT);
                    card.getChildren().addAll(dateBox, info);

                    card.setOpacity(0);
                    cardsBox.getChildren().add(card);
                    FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                    ft.setToValue(1); ft.setDelay(Duration.millis(i * 120)); ft.play();
                    TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
                    tt.setFromX(-20); tt.setToX(0); tt.setDelay(Duration.millis(i * 120)); tt.play();
                    i++;
                }
            }
            if (!any) {
                Text empty = new Text("No schedules assigned yet.");
                empty.setFont(Font.font("Arial", 13));
                empty.setFill(LoginScreen.secondaryTextColor());
                cardsBox.getChildren().add(empty);
            }
        });

        ScrollPane scrollCards = new ScrollPane(cardsBox);
        scrollCards.setFitToWidth(true); scrollCards.setPrefHeight(400);
        scrollCards.setStyle("-fx-background-color: " + cardsBg + "; -fx-background: " + cardsBg + ";");

        VBox section = makeSection("\uD83D\uDCC5  Training Schedules");
        section.getChildren().addAll(viewBtn, scrollCards);
        animateIn(area, section, msg);
    }

    private void showMessages(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCEC  My Messages");

        Button viewBtn = makeOrangeBtn("\uD83D\uDCEC  VIEW MY MESSAGES", 230);
        String cardsBg = AppState.darkMode ? "#0d0d0d" : "#F5F5F5";
        VBox cardsBox = new VBox(12);
        cardsBox.setPadding(new Insets(10, 0, 0, 0));
        cardsBox.setStyle("-fx-background-color: " + cardsBg + ";");

        viewBtn.setOnAction(e -> {
            cardsBox.getChildren().clear();
            int i = 0; boolean any = false;
            for (Message m : messages) {
                if (m.getToMemberId() == member.getId()) {
                    any = true;
                    String cardBg = AppState.darkMode ? "#1a1a1a" : "#FFFFFF";
                    VBox card = new VBox(8);
                    card.setPadding(new Insets(15, 20, 15, 20));
                    card.setStyle(
                        "-fx-background-color: " + cardBg + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #FF9A3C;" +
                        "-fx-border-radius: 10; -fx-border-width: 1.5;"
                    );

                    HBox header = new HBox(10);
                    header.setAlignment(Pos.CENTER_LEFT);

                    Label fromLabel = new Label("\uD83C\uDFCB\uFE0F  From: " + m.getFromCoach());
                    fromLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    fromLabel.setTextFill(Color.web("#FF9A3C"));
                    fromLabel.setPadding(new Insets(3, 10, 3, 10));
                    fromLabel.setStyle(
                        "-fx-background-color: #FF9A3C22; -fx-background-radius: 6;" +
                        "-fx-border-color: #FF9A3C; -fx-border-radius: 6; -fx-border-width: 1;"
                    );

                    Label dateLabel = new Label("\uD83D\uDCC5  " + m.getDate());
                    dateLabel.setFont(Font.font("Arial", 11));
                    dateLabel.setTextFill(LoginScreen.secondaryTextColor());

                    header.getChildren().addAll(fromLabel, dateLabel);

                    Text contentT = new Text("\uD83D\uDCAC  " + m.getContent());
                    contentT.setFont(Font.font("Arial", 13));
                    contentT.setFill(LoginScreen.primaryTextColor());
                    contentT.setWrappingWidth(500);

                    card.getChildren().addAll(header, contentT);
                    card.setOpacity(0);
                    cardsBox.getChildren().add(card);
                    FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                    ft.setToValue(1); ft.setDelay(Duration.millis(i * 120)); ft.play();
                    TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
                    tt.setFromX(-20); tt.setToX(0); tt.setDelay(Duration.millis(i * 120)); tt.play();
                    i++;
                }
            }
            if (!any) {
                Text empty = new Text("No messages yet.");
                empty.setFont(Font.font("Arial", 13));
                empty.setFill(LoginScreen.secondaryTextColor());
                cardsBox.getChildren().add(empty);
            }
        });

        ScrollPane scrollCards = new ScrollPane(cardsBox);
        scrollCards.setFitToWidth(true); scrollCards.setPrefHeight(400);
        scrollCards.setStyle("-fx-background-color: " + cardsBg + "; -fx-background: " + cardsBg + ";");

        VBox section = makeSection("\uD83D\uDCEC  Messages from Coach");
        section.getChildren().addAll(viewBtn, scrollCards);
        animateIn(area, section, msg);
    }

    private void showMyBills(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCB3  My Bills");

        String cardsBg = AppState.darkMode ? "#0d0d0d" : "#F5F5F5";
        VBox cardsBox = new VBox(12);
        cardsBox.setPadding(new Insets(10, 0, 0, 0));
        cardsBox.setStyle("-fx-background-color: " + cardsBg + ";");

        double totalPaid = 0, totalUnpaid = 0;
        boolean any = false; int i = 0;

        for (Bill b : bills) {
            if (b.getMemberId() == member.getId()) {
                any = true;
                String status = b.isPaid() ? "PAID" : "UNPAID";
                String color  = b.isPaid() ? "#00FF88" : "#FF4444";
                String bg     = AppState.darkMode
                    ? (b.isPaid() ? "#0a2a1a" : "#2a0a0a")
                    : (b.isPaid() ? "#e8f5e9" : "#ffebee");
                if (b.isPaid()) totalPaid += b.getAmount();
                else totalUnpaid += b.getAmount();

                HBox card = new HBox(15);
                card.setPadding(new Insets(15, 20, 15, 20));
                card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle(
                    "-fx-background-color: " + bg + ";" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: " + color + ";" +
                    "-fx-border-radius: 10; -fx-border-width: 1.5;"
                );

                Label tagLabel = new Label(status);
                tagLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 11));
                tagLabel.setTextFill(Color.web(color));
                tagLabel.setPadding(new Insets(4, 10, 4, 10));
                tagLabel.setStyle(
                    "-fx-background-color: " + color + "22;" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: " + color + ";" +
                    "-fx-border-radius: 6; -fx-border-width: 1;"
                );
                tagLabel.setMinWidth(65); tagLabel.setAlignment(Pos.CENTER);

                VBox info = new VBox(4);
                Text billT = new Text("\uD83D\uDCB3  Bill #" + b.getBillId());
                billT.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                billT.setFill(LoginScreen.primaryTextColor());
                Text amtT = new Text(
                    "Amount: $" + String.format("%.2f", b.getAmount()) +
                    "  |  " + b.getDescription() + "  |  " + b.getDate()
                );
                amtT.setFont(Font.font("Arial", 11));
                amtT.setFill(LoginScreen.secondaryTextColor());
                info.getChildren().addAll(billT, amtT);
                card.getChildren().addAll(tagLabel, info);
                card.setOpacity(0);
                cardsBox.getChildren().add(card);

                FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                ft.setToValue(1); ft.setDelay(Duration.millis(i * 100)); ft.play();
                i++;
            }
        }

        if (any) {
            HBox summary = new HBox(20);
            summary.setPadding(new Insets(10, 0, 5, 0));
            Text paidT = new Text("\uD83D\uDFE2  Paid: $" + String.format("%.2f", totalPaid));
            paidT.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            paidT.setFill(Color.web("#00FF88"));
            Text unpaidT = new Text("\uD83D\uDD34  Unpaid: $" + String.format("%.2f", totalUnpaid));
            unpaidT.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            unpaidT.setFill(Color.web("#FF4444"));
            summary.getChildren().addAll(paidT, unpaidT);
            cardsBox.getChildren().add(0, summary);
        } else {
            Text empty = new Text("No bills found.");
            empty.setFont(Font.font("Arial", 13));
            empty.setFill(LoginScreen.secondaryTextColor());
            cardsBox.getChildren().add(empty);
        }

        ScrollPane scrollCards = new ScrollPane(cardsBox);
        scrollCards.setFitToWidth(true); scrollCards.setPrefHeight(400);
        scrollCards.setStyle("-fx-background-color: " + cardsBg + "; -fx-background: " + cardsBg + ";");

        VBox section = makeSection("\uD83D\uDCB3  Billing History");
        section.getChildren().add(scrollCards);
        animateIn(area, section, msg);
    }


    private void showMyPlan(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83C\uDFCB\uFE0F  My Training Plan");

        VBox section = makeSection("My Training Plan");
        javafx.scene.control.TextArea planArea = new javafx.scene.control.TextArea();
        planArea.setEditable(false); planArea.setPrefHeight(300);
        planArea.setFont(javafx.scene.text.Font.font("Monospaced",11));
        planArea.setStyle("-fx-control-inner-background:#1a1a1a;-fx-text-fill:#e0e0e0;");

        javafx.scene.control.TextField phoneF = LoginScreen.makeTextField("Your WhatsApp number e.g. 201012345678");
        Button waBtn = new Button("\uD83D\uDCF1  Send to WhatsApp");
        waBtn.setPrefHeight(38); waBtn.setPrefWidth(220);
        waBtn.setStyle("-fx-background-color:#25D366;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;");

        java.util.ArrayList<WorkoutPlan> plans = FileManager.loadWorkoutPlans();
        StringBuilder sb = new StringBuilder();
        for (WorkoutPlan wp : plans) {
            if (wp.getMemberId() == member.getId()) {
                sb.append(wp.toReadableText()).append("\n\n");
            }
        }
        planArea.setText(sb.length() > 0 ? sb.toString() : "No training plans assigned yet.");

        waBtn.setOnAction(e -> {
            try {
                String phone = phoneF.getText().trim().replaceAll("[^0-9]","");
                String text  = java.net.URLEncoder.encode(sb.toString(), "UTF-8");
                String url   = "https://wa.me/" + phone + "?text=" + text;
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception ex) { showError(msg, "Could not open WhatsApp!"); }
        });

        section.getChildren().addAll(
            planArea, LoginScreen.makeLabel("WHATSAPP NUMBER"), phoneF, waBtn);
        animateIn(area, section, msg);
    }

    private void showUpdateInfo(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\u2699\uFE0F  Update My Info");

        VBox section = makeSection("\u270F\uFE0F  Update Info");
        PasswordField oldPassF = LoginScreen.makePasswordField("Current Password");
        TextField newNameF     = LoginScreen.makeTextField("New Name (letters only)");
        TextField newUnameF    = LoginScreen.makeTextField("New Username (lowercase only)");
        PasswordField newPassF = LoginScreen.makePasswordField("New Password (min 4 chars)");
        Button updateBtn       = makeOrangeBtn("\uD83D\uDCBE  UPDATE INFO", 200);

        updateBtn.setOnAction(e -> {
            if (!oldPassF.getText().equals(member.getPassword())) {
                showError(msg, "Wrong password!"); return;
            }
            String nn = newNameF.getText().trim();
            String nu = newUnameF.getText().trim();
            String np = newPassF.getText().trim();
            if (!nn.isEmpty() && nn.matches("[a-zA-Z ]+")) member.setName(nn);
            if (!nu.isEmpty()) {
                if (!nu.matches("[a-z0-9_]+")) { showError(msg, "Username: lowercase only!"); return; }
                member.setUsername(nu);
            }
            if (np.length() >= 4) member.setPassword(np);
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

        Text userName = new Text("\uD83D\uDC64  " + member.getName());
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        userName.setFill(Color.WHITE);

        Text userRole = new Text("MEMBER");
        userRole.setFont(Font.font("Arial", 11));
        userRole.setFill(Color.web("#FF9A3C"));

        Text menuTitle = new Text("MAIN MENU");
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuTitle.setFill(Color.web("#888888"));

        String[] items = {
            "\uD83C\uDFE0  Dashboard",
            "\uD83D\uDCC5  My Subscription",
            "\uD83C\uDFCB\uFE0F  My Schedules",
            "\uD83D\uDCEC  My Messages",
            "\uD83D\uDCB3  My Bills",
            "\uD83C\uDFCB\uFE0F  My Plan",
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
        val.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        val.setFill(Color.web(color));
        val.setWrappingWidth(150);
        val.setTextAlignment(TextAlignment.CENTER);
        Text lbl = new Text(label);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setFill(LoginScreen.secondaryTextColor());
        card.getChildren().addAll(val, lbl);
        return card;
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
}