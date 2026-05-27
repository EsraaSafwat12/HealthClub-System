// AdminScreen.java
package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import com.mycompany.healthclubsystem.Coupon;
import com.mycompany.healthclubsystem.FrozenMembership;
import com.mycompany.healthclubsystem.FamilyPackage;
import com.mycompany.healthclubsystem.Referral;
import com.mycompany.healthclubsystem.SplitPayment;
import com.mycompany.healthclubsystem.DigitalContract;
import com.mycompany.healthclubsystem.BodyProgress;
import java.util.Arrays;
import java.util.List;

public class AdminScreen {

    private User currentUser;
    private ArrayList<User> users;
    private ArrayList<Bill> bills;
    private ArrayList<Message> messages;
    private ArrayList<Schedule>         schedules;
    private ArrayList<Coupon>           coupons      = new ArrayList<>();
    private ArrayList<FrozenMembership> freezes      = new ArrayList<>();
    private ArrayList<FamilyPackage>    families     = new ArrayList<>();
    private ArrayList<Referral>         referrals    = new ArrayList<>();
    private ArrayList<SplitPayment>     splits       = new ArrayList<>();
    private ArrayList<DigitalContract>  contracts    = new ArrayList<>();
    private ArrayList<BodyProgress>     bodyProgress = new ArrayList<>();

    private static Timeline activeAutoSave;
    private static Timeline activeSessionTimer;

    private static void stopActiveTimers() {
        if (activeAutoSave    != null) { activeAutoSave.stop();    activeAutoSave    = null; }
        if (activeSessionTimer != null) { activeSessionTimer.stop(); activeSessionTimer = null; }
    }
    public AdminScreen(User user, ArrayList<User> users, ArrayList<Bill> bills,
            ArrayList<Message> messages, ArrayList<Schedule> schedules) {
        this.currentUser = user;
        this.users = users;
        this.bills = bills;
        this.messages = messages;
        this.schedules   = schedules;
        coupons      = FileManager.loadCoupons();
        freezes      = FileManager.loadFrozen();
        families     = FileManager.loadFamilyPackages();
        referrals    = FileManager.loadReferrals();
        splits       = FileManager.loadSplitPayments();
        contracts    = FileManager.loadContracts();
        bodyProgress = FileManager.loadBodyProgress();
        FileManager.autoLockExpired(users, freezes);
    }

    private String convertDate(String input) {
        try {
            String[] parts = input.trim().split("-");
            if (parts.length == 3 && parts[2].length() == 4)
                return parts[2] + "-" + parts[1] + "-" + parts[0];
        } catch (Exception e) { e.printStackTrace(); }
        return input;
    }

    private boolean isValidDate(String input) {
        try {
            String[] parts = input.trim().split("-");
            if (parts.length != 3) return false;
            int day   = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year  = Integer.parseInt(parts[2]);
            return day >= 1 && day <= 31 && month >= 1 && month <= 12 && year >= 2020;
        } catch (Exception e) { return false; }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }

    public void show(Stage stage) {
        stage.setTitle("Power Gym - Admin");

        VBox sidebar = makeSidebar(currentUser.getName(), "ADMINISTRATOR", new String[]{
            "\uD83C\uDFE0  Dashboard",
            "\uD83D\uDC65  Manage Users",
            "\uD83D\uDD17  Assign Members",
            "\uD83D\uDCB0  Billing",
            "\uD83D\uDCCA  Reports",
            "\uD83D\uDD14  Notifications",
            "\uD83D\uDCCB  Attendance",
            "\uD83D\uDCE6  Inventory",
            "\uD83D\uDCB5  Salaries",
            "\u2699\uFE0F  Update Info",
            "Charts",
            "Coupons",
            "Freeze",
            "Family",
            "Referrals",
            "Split Pay",
            "Contracts",
            "WhatsApp",
            "Body Progress"
        });

        VBox contentArea = new VBox(15);
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle(LoginScreen.contentBg());

        Label msgLabel = makeMsg();
        showDashboard(contentArea, msgLabel);

        Button[] btns = (Button[]) sidebar.getUserData();
        btns[0].setOnAction(e -> showDashboard(contentArea, msgLabel));
        btns[1].setOnAction(e -> showManageUsers(contentArea, msgLabel));
        btns[2].setOnAction(e -> showAssign(contentArea, msgLabel));
        btns[3].setOnAction(e -> showBilling(contentArea, msgLabel));
        btns[4].setOnAction(e -> showReports(contentArea, msgLabel));
        btns[5].setOnAction(e -> showNotifications(contentArea, msgLabel));
        btns[6].setOnAction(e -> showAttendanceAdmin(contentArea, msgLabel));
        btns[7].setOnAction(e -> showInventory(contentArea, msgLabel));
        btns[8].setOnAction(e -> showSalaries(contentArea, msgLabel));
        btns[9].setOnAction(e -> showUpdateInfo(contentArea, msgLabel));
        btns[10].setOnAction(e -> NewFeaturesAdminTab.showCharts(contentArea, msgLabel, bills, new java.util.ArrayList<>(), bodyProgress, users));
        btns[11].setOnAction(e -> NewFeaturesAdminTab.showCoupons(contentArea, msgLabel, coupons));
        btns[12].setOnAction(e -> NewFeaturesAdminTab.showFreeze(contentArea, msgLabel, freezes, users));
        btns[13].setOnAction(e -> NewFeaturesAdminTab.showFamilyPackages(contentArea, msgLabel, families, users));
        btns[14].setOnAction(e -> NewFeaturesAdminTab.showReferrals(contentArea, msgLabel, referrals, users));
        btns[15].setOnAction(e -> NewFeaturesAdminTab.showSplitPayments(contentArea, msgLabel, splits, bills, users));
        btns[16].setOnAction(e -> NewFeaturesAdminTab.showContracts(contentArea, msgLabel, contracts, users, bills));
        btns[17].setOnAction(e -> NewFeaturesAdminTab.showWhatsAppSettings(contentArea, msgLabel));
        btns[18].setOnAction(e -> NewFeaturesAdminTab.showBodyProgressAdmin(contentArea, msgLabel, bodyProgress, users));

        Button logoutBtn = (Button) sidebar.getChildren().get(sidebar.getChildren().size() - 1);
        logoutBtn.setOnAction(e -> {
            FileManager.saveUsers(users);
            FileManager.saveBills(bills);
            FileManager.saveMessages(messages);
            FileManager.saveSchedules(schedules);
            FileManager.saveCoupons(coupons);
            FileManager.saveFrozen(freezes);
            FileManager.saveFamilyPackages(families);
            FileManager.saveReferrals(referrals);
            FileManager.saveSplitPayments(splits);
            FileManager.saveContracts(contracts);
            FileManager.saveBodyProgress(bodyProgress);
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

        Scene adminScene = new Scene(scroll, 950, 680);
        stage.setScene(adminScene);
        stage.show();
        refreshTheme(scroll, sidebar, contentArea);

        // ===== CLOSE HANDLER =====
        stage.setOnCloseRequest(e -> {
            FileManager.saveUsers(users);
            FileManager.saveBills(bills);
            FileManager.saveMessages(messages);
            FileManager.saveSchedules(schedules);
            FileManager.saveCoupons(coupons);
            FileManager.saveFrozen(freezes);
            FileManager.saveFamilyPackages(families);
            FileManager.saveReferrals(referrals);
            FileManager.saveSplitPayments(splits);
            FileManager.saveContracts(contracts);
            FileManager.saveBodyProgress(bodyProgress);
            System.exit(0);
        });

        stopActiveTimers();

        // ===== AUTO-SAVE every 5 minutes =====
        activeAutoSave = new Timeline(new KeyFrame(Duration.minutes(5), ev -> {
            FileManager.saveUsers(users);
            FileManager.saveBills(bills);
            FileManager.saveMessages(messages);
            FileManager.saveSchedules(schedules);
        }));
        activeAutoSave.setCycleCount(Timeline.INDEFINITE);
        activeAutoSave.play();

        // ===== DAILY BACKUP at startup =====
        new Thread(() -> FileManager.createBackup()).start();

        // ===== SUBSCRIPTION EXPIRY ALERTS =====
        Platform.runLater(() -> {
            java.util.List<String> alerts = FileManager.getExpiryAlerts(users);
            if (!alerts.isEmpty()) {
                StringBuilder alertMsg = new StringBuilder("Subscription Alerts:\n");
                for (String a : alerts) {
                    String[] parts = a.split("\\|");
                    if (parts[0].equals("EXPIRED"))
                        alertMsg.append("EXPIRED: ").append(parts[2]).append(" (").append(parts[3]).append(")\n");
                    else
                        alertMsg.append("EXPIRING in ").append(parts[4]).append(" days: ").append(parts[2]).append("\n");
                }
                javafx.scene.control.Alert al = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
                al.setTitle("Subscription Alerts");
                al.setHeaderText("Members needing attention:");
                al.setContentText(alertMsg.toString());
                al.show();
            }
        });

        // ===== SESSION TIMEOUT: 15 min inactivity =====
        final long[] lastActivity = {System.currentTimeMillis()};
        adminScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED,  ev2 -> lastActivity[0] = System.currentTimeMillis());
        adminScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,    ev2 -> lastActivity[0] = System.currentTimeMillis());
        activeSessionTimer = new Timeline(new KeyFrame(Duration.minutes(1), ev -> {
            if (System.currentTimeMillis() - lastActivity[0] > 15 * 60 * 1000L) {
                stopActiveTimers();
                FileManager.saveUsers(users); FileManager.saveBills(bills);
                FileManager.saveMessages(messages); FileManager.saveSchedules(schedules);
                LoginScreen.goToLogin(stage);
            }
        }));
        activeSessionTimer.setCycleCount(Timeline.INDEFINITE);
        activeSessionTimer.play();
    }

    private void refreshTheme(ScrollPane scroll, VBox sidebar, VBox contentArea) {
        String scrollBg = AppState.darkMode
            ? "-fx-background-color: #0d0d0d; -fx-background: #0d0d0d;"
            : "-fx-background-color: #F5F5F5; -fx-background: #F5F5F5;";
        scroll.setStyle(scrollBg);
        sidebar.setStyle(LoginScreen.sidebarBg());
        contentArea.setStyle(LoginScreen.contentBg());
    }

    // ===================== SCREENS =====================

    private void showDashboard(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83C\uDFE0  Dashboard");

        Text welcome = new Text("Welcome back, " + currentUser.getName() + "! \uD83D\uDCAA");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        welcome.setFill(LoginScreen.primaryTextColor());
        welcome.setOpacity(0);
        area.getChildren().add(welcome);
        FadeTransition wft = new FadeTransition(Duration.millis(500), welcome);
        wft.setFromValue(0); wft.setToValue(1);
        wft.setDelay(Duration.millis(200)); wft.play();

        long members = users.stream().filter(u -> u.getRole().equals("member")).count();
        long coaches = users.stream().filter(u -> u.getRole().equals("coach")).count();
        long unpaid  = bills.stream().filter(b -> !b.isPaid()).count();

        HBox stats = new HBox(15);
        VBox[] cards = {
            makeStatCard("\uD83D\uDC65  Total Members", String.valueOf(members), "#FF6B00"),
            makeStatCard("\uD83C\uDFCB\uFE0F  Total Coaches", String.valueOf(coaches), "#FF9A3C"),
            makeStatCard("\uD83D\uDCB0  Unpaid Bills", String.valueOf(unpaid), "#FF4444")
        };

        for (int i = 0; i < cards.length; i++) {
            cards[i].setOpacity(0);
            stats.getChildren().add(cards[i]);
            FadeTransition ft = new FadeTransition(Duration.millis(400), cards[i]);
            ft.setFromValue(0); ft.setToValue(1);
            ft.setDelay(Duration.millis(200 * i)); ft.play();
        }

        area.getChildren().addAll(stats, msg);

        // ===== CHARTS =====
        javafx.scene.chart.PieChart pieChart = new javafx.scene.chart.PieChart();
        pieChart.getData().addAll(
            new javafx.scene.chart.PieChart.Data("Members (" + members + ")", members > 0 ? members : 1),
            new javafx.scene.chart.PieChart.Data("Coaches (" + coaches + ")", coaches > 0 ? coaches : 1)
        );
        pieChart.setTitle("Users Distribution");
        pieChart.setPrefSize(320, 230);
        pieChart.setLabelsVisible(true);

        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        javafx.scene.chart.NumberAxis yAxis   = new javafx.scene.chart.NumberAxis();
        javafx.scene.chart.BarChart<String, Number> barChart =
            new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        barChart.setTitle("Bills Overview");
        barChart.setPrefSize(320, 230);
        barChart.setLegendVisible(false);

        javafx.scene.chart.XYChart.Series<String, Number> series =
            new javafx.scene.chart.XYChart.Series<>();
        double paid      = bills.stream().filter(Bill::isPaid).mapToDouble(Bill::getAmount).sum();
        double unpaidAmt = bills.stream().filter(b -> !b.isPaid()).mapToDouble(Bill::getAmount).sum();
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("Paid $" + String.format("%.0f", paid), paid));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("Unpaid $" + String.format("%.0f", unpaidAmt), unpaidAmt));
        barChart.getData().add(series);

        String chartBg = AppState.darkMode ? "#1a1a1a" : "#FFFFFF";
        String chartBorder = AppState.darkMode ? "#2a2a2a" : "#DDDDDD";
        pieChart.setStyle("-fx-background-color: " + chartBg + ";");
        barChart.setStyle("-fx-background-color: " + chartBg + ";");

        HBox charts = new HBox(20, pieChart, barChart);
        charts.setPadding(new Insets(15));
        charts.setStyle(
            "-fx-background-color: " + chartBg + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + chartBorder + ";" +
            "-fx-border-radius: 10; -fx-border-width: 1;"
        );
        charts.setOpacity(0);
        area.getChildren().add(charts);

        // Dark/Light CSS for charts
        charts.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null) Platform.runLater(() -> {
                String plotBg  = AppState.darkMode ? "#111111" : "#F8F8F8";
                String gridClr = AppState.darkMode ? "#333333" : "#DDDDDD";
                String txtClr  = AppState.darkMode ? "#CCCCCC" : "#333333";
                for (javafx.scene.Node n : pieChart.lookupAll(".chart-plot-background"))
                    n.setStyle("-fx-background-color: " + chartBg + ";");
                for (javafx.scene.Node n : pieChart.lookupAll(".chart-legend"))
                    n.setStyle("-fx-background-color: " + chartBg + ";");
                for (javafx.scene.Node n : pieChart.lookupAll(".chart-title"))
                    n.setStyle("-fx-text-fill: #FF6B00;");
                for (javafx.scene.Node n : pieChart.lookupAll(".label"))
                    n.setStyle("-fx-text-fill: " + txtClr + ";");
                for (javafx.scene.Node n : barChart.lookupAll(".chart-plot-background"))
                    n.setStyle("-fx-background-color: " + plotBg + ";");
                for (javafx.scene.Node n : barChart.lookupAll(".chart-horizontal-grid-lines"))
                    n.setStyle("-fx-stroke: " + gridClr + ";");
                for (javafx.scene.Node n : barChart.lookupAll(".chart-title"))
                    n.setStyle("-fx-text-fill: #FF6B00;");
                for (javafx.scene.Node n : barChart.lookupAll(".axis"))
                    n.setStyle("-fx-tick-label-fill: " + txtClr + ";");
                for (javafx.scene.Node n : barChart.lookupAll(".data0.chart-bar"))
                    n.setStyle("-fx-bar-fill: #00FF88;");
                for (javafx.scene.Node n : barChart.lookupAll(".data1.chart-bar"))
                    n.setStyle("-fx-bar-fill: #FF4444;");
            });
        });

        FadeTransition cft = new FadeTransition(Duration.millis(600), charts);
        cft.setToValue(1); cft.setDelay(Duration.millis(500)); cft.play();

        // ===== TRANSLATE DASHBOARD =====
        if (!AppState.currentLangCode.equals("en")) {
            TranslationService.translateBatch(
                Arrays.asList("Dashboard", "Welcome back,", "Users Distribution", "Bills Overview"),
                AppState.currentLangCode, t -> {
                    welcome.setText(t.getOrDefault("Welcome back,", "Welcome back,") +
                        " " + currentUser.getName() + "! \uD83D\uDCAA");
                });
        }
    }

    private void showManageUsers(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDC65  Manage Users");

        VBox addSection = makeSection("\u2795  Add New User");
        TextField idF    = LoginScreen.makeTextField("ID (numbers only)");
        TextField nameF  = LoginScreen.makeTextField("Full Name (letters only)");
        TextField unameF = LoginScreen.makeTextField("Username (lowercase only)");
        PasswordField passF = LoginScreen.makePasswordField("Password (min 4 chars)");
        ComboBox<String> roleBox = makeCombo("admin", "coach", "member");
        TextField coachF = LoginScreen.makeTextField("Coach Name");
        TextField subF   = LoginScreen.makeTextField("Subscription End (DD-MM-YYYY)");
        VBox memberExtra = new VBox(8,
            LoginScreen.makeLabel("COACH NAME"), coachF,
            LoginScreen.makeLabel("SUB END DATE (DD-MM-YYYY)"), subF
        );
        memberExtra.setVisible(true);
        roleBox.setOnAction(e -> memberExtra.setVisible(roleBox.getValue().equals("member")));

        Button addBtn = makeOrangeBtn("\u2795  ADD USER", 200);
        addBtn.setOnAction(e -> {
            String idTxt = idF.getText().trim();
            String name  = nameF.getText().trim();
            String uname = unameF.getText().trim();
            String pass  = passF.getText().trim();
            String role  = roleBox.getValue();
            if (!idTxt.matches("[0-9]+"))       { showError(msg, "ID must be numbers only!"); return; }
            if (!name.matches("[a-zA-Z ]+"))    { showError(msg, "Name must be letters only!"); return; }
            if (uname.isEmpty())                { showError(msg, "Username empty!"); return; }
            if (!uname.matches("[a-z0-9_]+"))   { showError(msg, "Username: lowercase only!"); return; }
            if (pass.length() < 4)              { showError(msg, "Password too short!"); return; }
            int id = Integer.parseInt(idTxt);
            if (FileManager.isIdExists(id, users))          { showError(msg, "ID already exists!"); return; }
            if (FileManager.isUsernameExists(uname, users)) { showError(msg, "Username taken!"); return; }
            if (role.equals("member")) {
                String subInput = subF.getText().trim();
                if (!isValidDate(subInput)) { showError(msg, "Invalid date! Use DD-MM-YYYY"); return; }
                users.add(new Member(id, name, uname, pass, coachF.getText(), convertDate(subInput)));
            } else if (role.equals("coach")) {
                users.add(new Coach(id, name, uname, pass));
            } else {
                users.add(new Admin(id, name, uname, pass));
            }
            FileManager.saveUsers(users);
            showSuccess(msg, "User Added Successfully!");
            idF.clear(); nameF.clear(); unameF.clear(); passF.clear();
            coachF.clear(); subF.clear();
        });

        addSection.getChildren().addAll(
            LoginScreen.makeLabel("ID"), idF,
            LoginScreen.makeLabel("NAME"), nameF,
            LoginScreen.makeLabel("USERNAME"), unameF,
            LoginScreen.makeLabel("PASSWORD"), passF,
            LoginScreen.makeLabel("ROLE"), roleBox,
            memberExtra, addBtn
        );

        VBox editSection = makeSection("\u270F\uFE0F  Edit Existing User");
        TextField editIdF    = LoginScreen.makeTextField("Enter User ID to edit");
        Button loadBtn       = makeOrangeBtn("\uD83D\uDD0D  LOAD USER", 150);
        TextField editNameF  = LoginScreen.makeTextField("New Name");
        TextField editUnameF = LoginScreen.makeTextField("New Username (lowercase only)");
        PasswordField editPassF = LoginScreen.makePasswordField("New Password");
        TextField editCoachF = LoginScreen.makeTextField("New Coach Name");
        TextField editSubF   = LoginScreen.makeTextField("New Sub End (DD-MM-YYYY)");
        VBox editMemberExtra = new VBox(8,
            LoginScreen.makeLabel("NEW COACH"), editCoachF,
            LoginScreen.makeLabel("NEW SUB END (DD-MM-YYYY)"), editSubF
        );
        editMemberExtra.setVisible(false);
        Button saveEditBtn = makeOrangeBtn("\uD83D\uDCBE  SAVE CHANGES", 200);

        loadBtn.setOnAction(e -> {
            String idTxt = editIdF.getText().trim();
            if (!idTxt.matches("[0-9]+")) { showError(msg, "ID must be numbers!"); return; }
            User found = FileManager.findUserById(Integer.parseInt(idTxt), users);
            if (found == null) { showError(msg, "User not found!"); return; }
            editNameF.setText(found.getName());
            editUnameF.setText(found.getUsername());
            editPassF.clear();
            editMemberExtra.setVisible(found instanceof Member);
            if (found instanceof Member) {
                editCoachF.setText(((Member) found).getAssignedCoachName());
                editSubF.setText(((Member) found).getSubscriptionEndDate());
            }
            showSuccess(msg, "User loaded — edit and save!");
        });

        saveEditBtn.setOnAction(e -> {
            String idTxt = editIdF.getText().trim();
            if (!idTxt.matches("[0-9]+")) { showError(msg, "ID must be numbers!"); return; }
            User found = FileManager.findUserById(Integer.parseInt(idTxt), users);
            if (found == null) { showError(msg, "User not found!"); return; }
            String nn = editNameF.getText().trim();
            String nu = editUnameF.getText().trim();
            String np = editPassF.getText().trim();
            if (!nn.isEmpty()) {
                if (!nn.matches("[a-zA-Z ]+")) { showError(msg, "Name must be letters only!"); return; }
                found.setName(nn);
            }
            if (!nu.isEmpty()) {
                if (!nu.matches("[a-z0-9_]+")) { showError(msg, "Username: lowercase only!"); return; }
                found.setUsername(nu);
            }
            if (np.length() >= 4) found.setPassword(np);
            if (found instanceof Member) {
                String subInput = editSubF.getText().trim();
                if (!subInput.isEmpty()) {
                    if (!isValidDate(subInput)) { showError(msg, "Invalid date!"); return; }
                    ((Member) found).setSubscriptionEndDate(convertDate(subInput));
                }
                if (!editCoachF.getText().trim().isEmpty())
                    ((Member) found).setAssignedCoachName(editCoachF.getText().trim());
            }
            FileManager.saveUsers(users);
            showSuccess(msg, "User Updated Successfully!");
        });

        editSection.getChildren().addAll(
            LoginScreen.makeLabel("USER ID"), editIdF, loadBtn,
            LoginScreen.makeLabel("NEW NAME"), editNameF,
            LoginScreen.makeLabel("NEW USERNAME"), editUnameF,
            LoginScreen.makeLabel("NEW PASSWORD"), editPassF,
            editMemberExtra, saveEditBtn
        );

        VBox listSection = makeSection("\uD83D\uDCCB  List / Search / Delete");
        TextArea usersList = makeTextArea(180);
        TextField searchF  = LoginScreen.makeTextField("Search by name or ID...");
        Button listBtn     = makeOrangeBtn("\uD83D\uDCCB  LIST ALL", 150);
        Button searchBtn   = makeOrangeBtn("\uD83D\uDD0D  SEARCH", 120);
        TextField deleteIdF = LoginScreen.makeTextField("ID to delete");
        Button deleteBtn   = makeRedBtn("\uD83D\uDDD1\uFE0F  DELETE", 130);
        Button exportBtn   = makeOrangeBtn("\uD83D\uDCC4  EXPORT PDF", 150);

        listBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder(
                String.format("%-5s | %-15s | %-10s%n", "ID", "Name", "Role"));
            sb.append("-".repeat(35)).append("\n");
            for (User u : users)
                sb.append(String.format("%-5d | %-15s | %-10s%n",
                    u.getId(), u.getName(), u.getRole()));
            usersList.setText(sb.toString());
        });

        searchBtn.setOnAction(e -> {
            String q = searchF.getText().trim();
            StringBuilder sb = new StringBuilder();
            for (User u : users)
                if (u.getName().toLowerCase().contains(q.toLowerCase()) ||
                    String.valueOf(u.getId()).equals(q))
                    sb.append(String.format("%-5d | %-15s | %-10s%n",
                        u.getId(), u.getName(), u.getRole()));
            usersList.setText(sb.length() > 0 ? sb.toString() : "No results found.");
        });

        deleteBtn.setOnAction(e -> {
            String idTxt = deleteIdF.getText().trim();
            if (!idTxt.matches("[0-9]+")) { showError(msg, "ID must be numbers!"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("\uD83D\uDDD1\uFE0F  Delete User ID: " + idTxt);
            confirm.setContentText("Are you sure?");
            ButtonType yesBtn = new ButtonType("\uD83D\uDDD1\uFE0F  Yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType noBtn  = new ButtonType("\u274C  Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(yesBtn, noBtn);
            confirm.showAndWait().ifPresent(r -> {
                if (r == yesBtn) {
                    boolean removed = users.removeIf(u -> u.getId() == Integer.parseInt(idTxt));
                    if (removed) { FileManager.saveUsers(users); showSuccess(msg, "User Deleted!"); }
                    else showError(msg, "Not found!");
                }
            });
        });

        exportBtn.setOnAction(e -> exportUsersPDF());

        listSection.getChildren().addAll(
            new HBox(10, listBtn, searchF, searchBtn),
            usersList,
            new HBox(10, deleteIdF, deleteBtn, exportBtn)
        );

        animateIn(area, addSection, editSection, listSection, msg);

        // Translate section titles
        if (!AppState.currentLangCode.equals("en")) {
            TranslationService.translateBatch(
                Arrays.asList("Add New User", "Edit Existing User", "List / Search / Delete"),
                AppState.currentLangCode, t -> {});
        }
    }

    private void showAssign(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDD17  Assign Member to Coach");

        VBox section = makeSection("\uD83D\uDD17  Assign");
        TextField midF   = LoginScreen.makeTextField("Member ID");
        TextField coachF = LoginScreen.makeTextField("Coach Name");
        Button assignBtn = makeOrangeBtn("\uD83D\uDD17  ASSIGN NOW", 200);

        assignBtn.setOnAction(e -> {
            String idTxt = midF.getText().trim();
            if (!idTxt.matches("[0-9]+")) { showError(msg, "ID must be numbers!"); return; }
            User m = FileManager.findUserById(Integer.parseInt(idTxt), users);
            if (m instanceof Member) {
                ((Member) m).setAssignedCoachName(coachF.getText().trim());
                FileManager.saveUsers(users);
                showSuccess(msg, "Member Assigned!");
            } else showError(msg, "Member not found!");
        });

        section.getChildren().addAll(
            LoginScreen.makeLabel("MEMBER ID"), midF,
            LoginScreen.makeLabel("COACH NAME"), coachF,
            assignBtn
        );

        animateIn(area, section, msg);
    }

    private void showBilling(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCB0  Manage Billing");

        VBox addSection = makeSection("\u2795  Add Bill");
        TextField bMidF  = LoginScreen.makeTextField("Member ID");
        TextField bAmtF  = LoginScreen.makeTextField("Amount");
        TextField bDescF = LoginScreen.makeTextField("Description");
        Button addBillBtn = makeOrangeBtn("\u2795  ADD BILL", 200);

        addBillBtn.setOnAction(e -> {
            try {
                int mid    = Integer.parseInt(bMidF.getText().trim());
                double amt = Double.parseDouble(bAmtF.getText().trim());
                bills.add(new Bill(bills.size() + 1, mid, amt,
                    bDescF.getText(), LocalDate.now().toString()));
                FileManager.saveBills(bills);
                showSuccess(msg, "Bill Added!");
                bMidF.clear(); bAmtF.clear(); bDescF.clear();
            } catch (Exception ex) { showError(msg, "Invalid input!"); }
        });

        addSection.getChildren().addAll(
            LoginScreen.makeLabel("MEMBER ID"), bMidF,
            LoginScreen.makeLabel("AMOUNT"), bAmtF,
            LoginScreen.makeLabel("DESCRIPTION"), bDescF,
            addBillBtn
        );

        VBox listSection = makeSection("\uD83D\uDCCB  List Bills and Mark Paid");
        TextArea billsList = makeTextArea(150);
        Button listBtn     = makeOrangeBtn("\uD83D\uDCCB  LIST ALL BILLS", 200);
        TextField markF    = LoginScreen.makeTextField("Bill ID to mark paid");
        Button markBtn     = makeOrangeBtn("\u2705  MARK PAID", 150);
        Button exportBtn   = makeOrangeBtn("\uD83D\uDCC4  EXPORT PDF", 150);
        TextField billSearchF = LoginScreen.makeTextField("Search by Member ID or description...");
        Button billSearchBtn  = makeOrangeBtn("\uD83D\uDD0D  SEARCH", 130);

        listBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder(
                String.format("%-4s | %-8s | %-8s | %-15s | %-8s%n",
                    "ID", "MemberID", "Amount", "Description", "Status"));
            sb.append("-".repeat(55)).append("\n");
            for (Bill b : bills)
                sb.append(String.format("%-4d | %-8d | %-8.2f | %-15s | %-8s%n",
                    b.getBillId(), b.getMemberId(), b.getAmount(),
                    b.getDescription(), b.isPaid() ? "PAID" : "UNPAID"));
            billsList.setText(sb.toString());
        });

        billSearchBtn.setOnAction(e -> {
            String q = billSearchF.getText().trim().toLowerCase();
            StringBuilder sb = new StringBuilder(
                String.format("%-4s | %-8s | %-8s | %-15s | %-8s%n",
                    "ID", "MemberID", "Amount", "Description", "Status"));
            sb.append("-".repeat(55)).append("\n");
            boolean found = false;
            for (Bill b : bills) {
                if (String.valueOf(b.getMemberId()).contains(q) ||
                    b.getDescription().toLowerCase().contains(q)) {
                    sb.append(String.format("%-4d | %-8d | %-8.2f | %-15s | %-8s%n",
                        b.getBillId(), b.getMemberId(), b.getAmount(),
                        b.getDescription(), b.isPaid() ? "PAID" : "UNPAID"));
                    found = true;
                }
            }
            billsList.setText(found ? sb.toString() : "No results found.");
        });

        markBtn.setOnAction(e -> {
            try {
                int bi = Integer.parseInt(markF.getText().trim());
                for (Bill b : bills)
                    if (b.getBillId() == bi) {
                        b.setPaid(true);
                        FileManager.saveBills(bills);
                        showSuccess(msg, "Bill Marked as Paid!");
                        return;
                    }
                showError(msg, "Bill not found!");
            } catch (Exception ex) { showError(msg, "Invalid ID!"); }
        });

        exportBtn.setOnAction(e -> exportBillsPDF());

        listSection.getChildren().addAll(
            new HBox(10, listBtn, billSearchF, billSearchBtn),
            billsList,
            new HBox(10, markF, markBtn, exportBtn)
        );

        animateIn(area, addSection, listSection, msg);
    }

    private void showReports(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCCA  Reports");

        String cardsBg = AppState.darkMode ? "#0d0d0d" : "#F5F5F5";

        VBox billsSection = makeSection("\uD83D\uDCB0  Bills Report");
        Button billsReportBtn = makeOrangeBtn("\uD83D\uDCCA  GENERATE BILLS REPORT", 240);
        Button exportBillsBtn = makeOrangeBtn("\uD83D\uDCC4  EXPORT PDF", 150);
        VBox billsCardsBox = new VBox(10);
        billsCardsBox.setPadding(new Insets(10, 0, 0, 0));
        billsCardsBox.setStyle("-fx-background-color: " + cardsBg + ";");

        billsReportBtn.setOnAction(e -> {
            billsCardsBox.getChildren().clear();
            double totalPaid = 0, totalUnpaid = 0;
            int i = 0;
            for (Bill b : bills) {
                String status = b.isPaid() ? "PAID" : "UNPAID";
                String color  = b.isPaid() ? "#00FF88" : "#FF4444";
                String bg     = b.isPaid() ? "#0a2a1a" : "#2a0a0a";
                if (!AppState.darkMode) bg = b.isPaid() ? "#e8f5e9" : "#ffebee";
                if (b.isPaid()) totalPaid += b.getAmount();
                else totalUnpaid += b.getAmount();

                HBox card = new HBox(15);
                card.setPadding(new Insets(12, 20, 12, 20));
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

                VBox info = new VBox(3);
                Text billT = new Text("\uD83D\uDCB3  Bill #" + b.getBillId() +
                    "  |  Member ID: " + b.getMemberId());
                billT.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                billT.setFill(LoginScreen.primaryTextColor());
                Text amtT = new Text("Amount: $" + String.format("%.2f", b.getAmount()) +
                    "  |  " + b.getDescription() + "  |  " + b.getDate());
                amtT.setFont(Font.font("Arial", 11));
                amtT.setFill(LoginScreen.secondaryTextColor());
                info.getChildren().addAll(billT, amtT);
                card.getChildren().addAll(tagLabel, info);
                card.setOpacity(0);
                billsCardsBox.getChildren().add(card);

                FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                ft.setToValue(1); ft.setDelay(Duration.millis(i * 100)); ft.play();
                i++;
            }

            HBox summary = new HBox(20);
            summary.setPadding(new Insets(10, 0, 5, 0));
            Text paidT = new Text("\uD83D\uDFE2  Total Paid: $" + String.format("%.2f", totalPaid));
            paidT.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            paidT.setFill(Color.web("#00FF88"));
            Text unpaidT = new Text("\uD83D\uDD34  Total Unpaid: $" + String.format("%.2f", totalUnpaid));
            unpaidT.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            unpaidT.setFill(Color.web("#FF4444"));
            summary.getChildren().addAll(paidT, unpaidT);
            billsCardsBox.getChildren().add(0, summary);

            if (bills.isEmpty()) {
                Text empty = new Text("No bills found.");
                empty.setFill(LoginScreen.secondaryTextColor());
                billsCardsBox.getChildren().add(empty);
            }
        });
        exportBillsBtn.setOnAction(e -> exportBillsPDF());

        ScrollPane billsScroll = new ScrollPane(billsCardsBox);
        billsScroll.setFitToWidth(true);
        billsScroll.setPrefHeight(220);
        billsScroll.setStyle("-fx-background-color: " + cardsBg + "; -fx-background: " + cardsBg + ";");
        billsSection.getChildren().addAll(new HBox(10, billsReportBtn, exportBillsBtn), billsScroll);

        Button reportBtn = makeOrangeBtn("\uD83D\uDC65  GENERATE MEMBERS REPORT", 260);
        Button exportBtn = makeOrangeBtn("\uD83D\uDCC4  EXPORT PDF", 150);
        VBox cardsBox = new VBox(12);
        cardsBox.setPadding(new Insets(10, 0, 0, 0));
        cardsBox.setStyle("-fx-background-color: " + cardsBg + ";");

        reportBtn.setOnAction(e -> {
            cardsBox.getChildren().clear();
            int i = 0;
            for (User u : users) {
                if (u instanceof Member) {
                    Member m = (Member) u;
                    String status, color, bg;
                    try {
                        boolean expired = LocalDate.now().isAfter(LocalDate.parse(m.getSubscriptionEndDate()));
                        status = expired ? "EXPIRED" : "ACTIVE";
                        color  = expired ? "#FF4444" : "#00FF88";
                        bg     = AppState.darkMode
                            ? (expired ? "#2a0a0a" : "#0a2a1a")
                            : (expired ? "#ffebee" : "#e8f5e9");
                    } catch (Exception ex) {
                        status = "UNKNOWN"; color = "#888888";
                        bg = AppState.darkMode ? "#1a1a1a" : "#f5f5f5";
                    }

                    HBox card = new HBox(15);
                    card.setPadding(new Insets(15, 20, 15, 20));
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle(
                        "-fx-background-color: " + bg + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-radius: 10; -fx-border-width: 1.5;"
                    );

                    StackPane avatar = new StackPane();
                    Circle avatarCircle = new Circle(25);
                    avatarCircle.setFill(Color.web(color, 0.2));
                    avatarCircle.setStroke(Color.web(color));
                    avatarCircle.setStrokeWidth(2);
                    Text avatarText = new Text(String.valueOf(m.getName().charAt(0)));
                    avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                    avatarText.setFill(Color.web(color));
                    avatar.getChildren().addAll(avatarCircle, avatarText);

                    VBox info = new VBox(4);
                    Text nameT = new Text(m.getName());
                    nameT.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    nameT.setFill(LoginScreen.primaryTextColor());
                    Text statusT = new Text("[ " + status + " ]");
                    statusT.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    statusT.setFill(Color.web(color));
                    Text detailT = new Text(
                        "ID: " + m.getId() +
                        "  |  Coach: " + m.getAssignedCoachName() +
                        "  |  Until: " + m.getSubscriptionEndDate()
                    );
                    detailT.setFont(Font.font("Arial", 11));
                    detailT.setFill(LoginScreen.secondaryTextColor());
                    info.getChildren().addAll(nameT, statusT, detailT);
                    card.getChildren().addAll(avatar, info);
                    card.setOpacity(0);
                    cardsBox.getChildren().add(card);

                    FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                    ft.setToValue(1); ft.setDelay(Duration.millis(i * 120)); ft.play();
                    TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
                    tt.setFromX(-20); tt.setToX(0); tt.setDelay(Duration.millis(i * 120)); tt.play();
                    i++;
                }
            }
            if (cardsBox.getChildren().isEmpty()) {
                Text empty = new Text("No members found.");
                empty.setFill(LoginScreen.secondaryTextColor());
                cardsBox.getChildren().add(empty);
            }
        });

        exportBtn.setOnAction(e -> exportReportPDF());

        ScrollPane scrollCards = new ScrollPane(cardsBox);
        scrollCards.setFitToWidth(true);
        scrollCards.setPrefHeight(380);
        scrollCards.setStyle("-fx-background-color: " + cardsBg + "; -fx-background: " + cardsBg + ";");

        VBox section = makeSection("\uD83D\uDC65  Members Overview");
        section.getChildren().addAll(new HBox(10, reportBtn, exportBtn), scrollCards);
        area.getChildren().addAll(billsSection, section, msg);
    }

    private void showNotifications(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDD14  Subscription Notifications");

        Button checkBtn = makeOrangeBtn("\uD83D\uDD14  CHECK NOW", 200);
        String cardsBg = AppState.darkMode ? "#0d0d0d" : "#F5F5F5";
        VBox cardsBox = new VBox(12);
        cardsBox.setPadding(new Insets(10, 0, 0, 0));
        cardsBox.setStyle("-fx-background-color: " + cardsBg + ";");

        checkBtn.setOnAction(e -> {
            cardsBox.getChildren().clear();
            int i = 0, expiredCount = 0, warningCount = 0, activeCount = 0;

            for (User u : users) {
                if (u instanceof Member) {
                    Member m = (Member) u;
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate end = LocalDate.parse(m.getSubscriptionEndDate(), formatter);
                        long days = ChronoUnit.DAYS.between(LocalDate.now(), end);

                        String status, color, bg, tag;
                        if (days < 0) {
                            tag = "EXPIRED"; status = "Subscription has expired";
                            color = "#FF4444";
                            bg = AppState.darkMode ? "#2a0a0a" : "#ffebee";
                            expiredCount++;
                        } else if (days <= 7) {
                            tag = "WARNING"; status = days + " days remaining — renew soon!";
                            color = "#FFB300";
                            bg = AppState.darkMode ? "#2a1f00" : "#fff8e1";
                            warningCount++;
                        } else {
                            tag = "ACTIVE"; status = days + " days remaining";
                            color = "#00FF88";
                            bg = AppState.darkMode ? "#0a2a1a" : "#e8f5e9";
                            activeCount++;
                        }

                        HBox card = new HBox(15);
                        card.setPadding(new Insets(15, 20, 15, 20));
                        card.setAlignment(Pos.CENTER_LEFT);
                        card.setStyle(
                            "-fx-background-color: " + bg + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-radius: 10; -fx-border-width: 1.5;"
                        );

                        Label tagLabel = new Label(tag);
                        tagLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 11));
                        tagLabel.setTextFill(Color.web(color));
                        tagLabel.setPadding(new Insets(4, 10, 4, 10));
                        tagLabel.setStyle(
                            "-fx-background-color: " + color + "22;" +
                            "-fx-background-radius: 6;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-radius: 6; -fx-border-width: 1;"
                        );
                        tagLabel.setMinWidth(70); tagLabel.setAlignment(Pos.CENTER);

                        VBox info = new VBox(4);
                        Text nameT = new Text(m.getName());
                        nameT.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                        nameT.setFill(LoginScreen.primaryTextColor());
                        Text statusT = new Text(status);
                        statusT.setFont(Font.font("Arial", 12));
                        statusT.setFill(Color.web(color));
                        Text subT = new Text(
                            "Sub ends: " + m.getSubscriptionEndDate() +
                            "  |  Coach: " + m.getAssignedCoachName()
                        );
                        subT.setFont(Font.font("Arial", 11));
                        subT.setFill(LoginScreen.secondaryTextColor());

                        info.getChildren().addAll(nameT, statusT, subT);
                        card.getChildren().addAll(tagLabel, info);
                        card.setOpacity(0);
                        cardsBox.getChildren().add(card);

                        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                        ft.setToValue(1); ft.setDelay(Duration.millis(i * 120)); ft.play();
                        TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
                        tt.setFromX(-20); tt.setToX(0); tt.setDelay(Duration.millis(i * 120)); tt.play();
                        i++;
                    } catch (Exception ex) {}
                }
            }

            if (i > 0) {
                HBox summary = new HBox(20);
                summary.setPadding(new Insets(5, 0, 10, 0));
                Text expT  = new Text("\uD83D\uDD34  Expired: " + expiredCount);
                expT.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                expT.setFill(Color.web("#FF4444"));
                Text warnT = new Text("\uD83D\uDFE1  Warning: " + warningCount);
                warnT.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                warnT.setFill(Color.web("#FFB300"));
                Text actT  = new Text("\uD83D\uDFE2  Active: " + activeCount);
                actT.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                actT.setFill(Color.web("#00FF88"));
                summary.getChildren().addAll(expT, warnT, actT);
                cardsBox.getChildren().add(0, summary);
            }

            if (i == 0) {
                Text empty = new Text("No members found.");
                empty.setFill(LoginScreen.secondaryTextColor());
                cardsBox.getChildren().add(empty);
            }

            // Translate notification cards
            if (!AppState.currentLangCode.equals("en")) {
                TranslationService.translateBatch(
                    Arrays.asList("Subscription has expired",
                        "days remaining — renew soon!", "days remaining"),
                    AppState.currentLangCode, t -> {});
            }
        });

        ScrollPane scrollCards = new ScrollPane(cardsBox);
        scrollCards.setFitToWidth(true);
        scrollCards.setPrefHeight(400);
        scrollCards.setStyle("-fx-background-color: " + cardsBg + "; -fx-background: " + cardsBg + ";");

        VBox section = makeSection("\uD83D\uDD14  Member Status");
        section.getChildren().addAll(checkBtn, scrollCards);
        checkBtn.fire();
        animateIn(area, section, msg);
    }


    // ==================== ATTENDANCE ====================
    private void showAttendanceAdmin(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCCB  Attendance");
        AttendanceManager am = new AttendanceManager(users);
        animateIn(area, am.buildPanel(msg));
    }

    // ==================== INVENTORY ====================
    private void showInventory(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCE6  Inventory");
        InventoryScreen inv = new InventoryScreen();
        animateIn(area, inv.buildPanel(msg));
    }

    // ==================== SALARIES ====================
    private void showSalaries(VBox area, Label msg) {
        area.getChildren().clear();
        area.setStyle(LoginScreen.contentBg());
        animateTitle(area, "\uD83D\uDCB0  Salaries");
        SalaryScreen sal = new SalaryScreen(users);
        animateIn(area, sal.buildPanel(msg));
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
            if (!oldPassF.getText().equals(currentUser.getPassword())) {
                showError(msg, "Wrong password!"); return;
            }
            String nn = newNameF.getText().trim();
            String nu = newUnameF.getText().trim();
            String np = newPassF.getText().trim();
            if (!nn.isEmpty()) {
                if (!nn.matches("[a-zA-Z ]+")) { showError(msg, "Name: letters only!"); return; }
                currentUser.setName(nn);
            }
            if (!nu.isEmpty()) {
                if (!nu.matches("[a-z0-9_]+")) { showError(msg, "Username: lowercase only!"); return; }
                currentUser.setUsername(nu);
            }
            if (np.length() >= 4) currentUser.setPassword(np);
            FileManager.saveUsers(users);
            showSuccess(msg, "Info Updated Successfully!");
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

    // ===================== PDF EXPORTS =====================

    private void exportUsersPDF() {
        try {
            PDDocument doc = new PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 40, y = 750, tableW = 515, rowH = 22;

            cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            cs.addRect(0, 780, 612, 62); cs.fill();
            cs.setNonStrokingColor(1f, 0.42f, 0f);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
            cs.beginText(); cs.newLineAtOffset(margin, 800);
            cs.showText("POWER GYM  —  Users Report"); cs.endText();
            cs.setNonStrokingColor(0.6f, 0.6f, 0.6f);
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.beginText(); cs.newLineAtOffset(margin, 785);
            cs.showText("Generated: " + LocalDate.now()); cs.endText();
            cs.setStrokingColor(1f, 0.42f, 0f);
            cs.setLineWidth(2f);
            cs.moveTo(margin, 778); cs.lineTo(margin + tableW, 778); cs.stroke();

            y = 755;
            float[] colW = {40, 160, 160, 115};
            String[] headers = {"ID", "Name", "Username", "Role"};

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
                float[] bg = alt ? new float[]{0.1f, 0.1f, 0.1f} : new float[]{0.07f, 0.07f, 0.07f};
                cs.setNonStrokingColor(bg[0], bg[1], bg[2]);
                cs.addRect(margin, y - 5, tableW, rowH); cs.fill();
                float[] stripe = u.getRole().equals("admin") ? new float[]{1f, 0.42f, 0f}
                    : u.getRole().equals("coach") ? new float[]{0f, 0.6f, 1f}
                    : new float[]{0f, 0.8f, 0.3f};
                cs.setNonStrokingColor(stripe[0], stripe[1], stripe[2]);
                cs.addRect(margin, y - 5, 4, rowH); cs.fill();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(0.9f, 0.9f, 0.9f);
                xPos = margin + 8;
                cs.beginText(); cs.newLineAtOffset(xPos, y + 5);
                cs.showText(String.valueOf(u.getId())); cs.newLineAtOffset(colW[0], 0);
                cs.showText(truncate(u.getName(), 20)); cs.newLineAtOffset(colW[1], 0);
                cs.showText(truncate(u.getUsername(), 20)); cs.newLineAtOffset(colW[2], 0);
                cs.showText(u.getRole()); cs.endText();
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
            cs.showText("Power Gym  |  Users Report  |  " + LocalDate.now()); cs.endText();

            cs.close();
            String path = System.getProperty("user.dir") + File.separator + "users_report.pdf";
            doc.save(path); doc.close();
            showAlertSuccess("PDF saved!\n" + path);
        } catch (Exception ex) { showAlertError("Failed: " + ex.getMessage()); }
    }

    private void exportBillsPDF() {
        try {
            PDDocument doc = new PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 40, y = 750, tableW = 515, rowH = 22;

            cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            cs.addRect(0, 780, 612, 62); cs.fill();
            cs.setNonStrokingColor(1f, 0.42f, 0f);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
            cs.beginText(); cs.newLineAtOffset(margin, 800);
            cs.showText("POWER GYM  —  Bills Report"); cs.endText();
            cs.setNonStrokingColor(0.6f, 0.6f, 0.6f);
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.beginText(); cs.newLineAtOffset(margin, 785);
            cs.showText("Generated: " + LocalDate.now()); cs.endText();
            cs.setStrokingColor(1f, 0.42f, 0f);
            cs.setLineWidth(2f);
            cs.moveTo(margin, 778); cs.lineTo(margin + tableW, 778); cs.stroke();

            y = 755;
            float[] colW = {35, 60, 70, 165, 75, 70};
            String[] headers = {"ID", "MemberID", "Amount", "Description", "Date", "Status"};

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
            double totalPaid = 0, totalUnpaid = 0;
            for (Bill b : bills) {
                if (b.isPaid()) totalPaid += b.getAmount();
                else totalUnpaid += b.getAmount();
                float[] bg = alt ? new float[]{0.1f, 0.1f, 0.1f} : new float[]{0.07f, 0.07f, 0.07f};
                cs.setNonStrokingColor(bg[0], bg[1], bg[2]);
                cs.addRect(margin, y - 5, tableW, rowH); cs.fill();
                float[] stripe = b.isPaid() ? new float[]{0f, 0.8f, 0.3f} : new float[]{1f, 0.2f, 0.2f};
                cs.setNonStrokingColor(stripe[0], stripe[1], stripe[2]);
                cs.addRect(margin, y - 5, 4, rowH); cs.fill();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(0.9f, 0.9f, 0.9f);
                xPos = margin + 8;
                cs.beginText(); cs.newLineAtOffset(xPos, y + 5);
                cs.showText(String.valueOf(b.getBillId())); cs.newLineAtOffset(colW[0], 0);
                cs.showText(String.valueOf(b.getMemberId())); cs.newLineAtOffset(colW[1], 0);
                cs.showText("$" + String.format("%.2f", b.getAmount())); cs.newLineAtOffset(colW[2], 0);
                cs.showText(truncate(b.getDescription(), 22)); cs.newLineAtOffset(colW[3], 0);
                cs.showText(b.getDate()); cs.newLineAtOffset(colW[4], 0);
                cs.showText(b.isPaid() ? "PAID" : "UNPAID"); cs.endText();
                cs.setStrokingColor(0.25f, 0.25f, 0.25f);
                cs.setLineWidth(0.3f);
                cs.addRect(margin, y - 5, tableW, rowH); cs.stroke();
                y -= rowH + 1; alt = !alt;
                if (y < 80) break;
            }

            y -= 10;
            cs.setNonStrokingColor(0.12f, 0.12f, 0.12f);
            cs.addRect(margin, y - 5, tableW, 28); cs.fill();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.setNonStrokingColor(0f, 1f, 0.5f);
            cs.beginText(); cs.newLineAtOffset(margin + 8, y + 8);
            cs.showText("Total Paid: $" + String.format("%.2f", totalPaid));
            cs.newLineAtOffset(200, 0);
            cs.setNonStrokingColor(1f, 0.3f, 0.3f);
            cs.showText("Total Unpaid: $" + String.format("%.2f", totalUnpaid));
            cs.endText();

            cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            cs.addRect(0, 0, 612, 40); cs.fill();
            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.setNonStrokingColor(1f, 0.42f, 0f);
            cs.beginText(); cs.newLineAtOffset(margin, 15);
            cs.showText("Power Gym  |  Bills Report  |  " + LocalDate.now()); cs.endText();

            cs.close();
            String path = System.getProperty("user.dir") + File.separator + "bills_report.pdf";
            doc.save(path); doc.close();
            showAlertSuccess("PDF saved!\n" + path);
        } catch (Exception e) { showAlertError("Failed: " + e.getMessage()); }
    }

    private void exportReportPDF() {
        try {
            PDDocument doc = new PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 40, y = 750, tableW = 515, rowH = 22;

            cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            cs.addRect(0, 780, 612, 62); cs.fill();
            cs.setNonStrokingColor(1f, 0.42f, 0f);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
            cs.beginText(); cs.newLineAtOffset(margin, 800);
            cs.showText("POWER GYM  —  Members Report"); cs.endText();
            cs.setNonStrokingColor(0.6f, 0.6f, 0.6f);
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.beginText(); cs.newLineAtOffset(margin, 785);
            cs.showText("Generated: " + LocalDate.now()); cs.endText();
            cs.setStrokingColor(1f, 0.42f, 0f);
            cs.setLineWidth(2f);
            cs.moveTo(margin, 778); cs.lineTo(margin + tableW, 778); cs.stroke();

            y = 755;
            float[] colW = {35, 100, 100, 95, 80, 65};
            String[] headers = {"ID", "Name", "Coach", "Sub End", "Status", "Days Left"};

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
            long totalM = 0, activeM = 0;
            for (User u : users) {
                if (!(u instanceof Member)) continue;
                Member m = (Member) u; totalM++;
                String status; long daysLeft;
                try {
                    LocalDate end = LocalDate.parse(m.getSubscriptionEndDate());
                    daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), end);
                    if (daysLeft >= 0) activeM++;
                    status = daysLeft < 0 ? "EXPIRED" : daysLeft <= 7 ? "WARNING" : "ACTIVE";
                } catch (Exception ex) { status = "UNKNOWN"; daysLeft = 0; }

                final long dl = daysLeft; final String st = status;
                float[] rowBg = alt ? new float[]{0.1f,0.1f,0.1f} : new float[]{0.07f,0.07f,0.07f};
                cs.setNonStrokingColor(rowBg[0], rowBg[1], rowBg[2]);
                cs.addRect(margin, y - 5, tableW, rowH); cs.fill();
                float[] stripe = st.equals("ACTIVE") ? new float[]{0f,0.8f,0.3f}
                    : st.equals("EXPIRED") ? new float[]{1f,0.2f,0.2f} : new float[]{1f,0.7f,0f};
                cs.setNonStrokingColor(stripe[0], stripe[1], stripe[2]);
                cs.addRect(margin, y - 5, 4, rowH); cs.fill();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(0.9f, 0.9f, 0.9f);
                xPos = margin + 8;
                cs.beginText(); cs.newLineAtOffset(xPos, y + 5);
                cs.showText(String.valueOf(m.getId())); cs.newLineAtOffset(colW[0], 0);
                cs.showText(truncate(m.getName(), 13)); cs.newLineAtOffset(colW[1], 0);
                cs.showText(truncate(m.getAssignedCoachName(), 13)); cs.newLineAtOffset(colW[2], 0);
                cs.showText(m.getSubscriptionEndDate()); cs.newLineAtOffset(colW[3], 0);
                cs.showText(st); cs.newLineAtOffset(colW[4], 0);
                cs.showText(dl < 0 ? "Expired" : dl + " days"); cs.endText();
                cs.setStrokingColor(0.25f, 0.25f, 0.25f);
                cs.setLineWidth(0.3f);
                cs.addRect(margin, y - 5, tableW, rowH); cs.stroke();
                y -= rowH + 1; alt = !alt;
                if (y < 80) break;
            }

            y -= 10;
            cs.setNonStrokingColor(0.12f, 0.12f, 0.12f);
            cs.addRect(margin, y - 5, tableW, 28); cs.fill();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.setNonStrokingColor(1f, 0.42f, 0f);
            cs.beginText(); cs.newLineAtOffset(margin + 8, y + 8);
            cs.showText("Total: " + totalM); cs.newLineAtOffset(80, 0);
            cs.setNonStrokingColor(0f, 1f, 0.5f);
            cs.showText("Active: " + activeM); cs.newLineAtOffset(100, 0);
            cs.setNonStrokingColor(1f, 0.3f, 0.3f);
            cs.showText("Expired: " + (totalM - activeM)); cs.endText();

            cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            cs.addRect(0, 0, 612, 40); cs.fill();
            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.setNonStrokingColor(1f, 0.42f, 0f);
            cs.beginText(); cs.newLineAtOffset(margin, 15);
            cs.showText("Power Gym  |  Members Report  |  " + LocalDate.now()); cs.endText();

            cs.close();
            String path = System.getProperty("user.dir") + File.separator + "members_report.pdf";
            doc.save(path); doc.close();
            showAlertSuccess("PDF saved!\n" + path);
        } catch (Exception e) { showAlertError("Failed: " + e.getMessage()); }
    }

    // ===================== HELPERS =====================

    private VBox makeSidebar(String name, String role, String[] menuItems) {
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

        Text userName = new Text("\uD83D\uDC64  " + name);
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        userName.setFill(Color.WHITE);

        Text userRole = new Text(role);
        userRole.setFont(Font.font("Arial", 11));
        userRole.setFill(Color.web("#FF9A3C"));

        Separator sep = new Separator();
        Text menuTitle = new Text("MAIN MENU");
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuTitle.setFill(Color.web("#888888"));

        VBox header = new VBox(6, clubName, system, userName, userRole, sep, menuTitle);
        header.setOpacity(0);
        FadeTransition hf = new FadeTransition(Duration.millis(500), header);
        hf.setFromValue(0); hf.setToValue(1); hf.play();

        Button[] btns = new Button[menuItems.length];
        for (int i = 0; i < menuItems.length; i++) {
            btns[i] = makeSideBtn(menuItems[i]);
            btns[i].setOpacity(0);
            btns[i].setTranslateX(-30);
            FadeTransition ft = new FadeTransition(Duration.millis(400), btns[i]);
            ft.setFromValue(0); ft.setToValue(1);
            ft.setDelay(Duration.millis(300 + i * 150));
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), btns[i]);
            tt.setFromX(-30); tt.setToX(0);
            tt.setDelay(Duration.millis(300 + i * 150));
            new ParallelTransition(ft, tt).play();
        }

        // Translate sidebar buttons
        if (!AppState.currentLangCode.equals("en")) {
            java.util.Map<String, Button> map = new java.util.LinkedHashMap<>();
            for (int i = 0; i < menuItems.length; i++) {
                String cleanKey = menuItems[i].replaceAll("[^\\p{L}\\p{N} ]", "").trim();
                map.put(cleanKey, btns[i]);
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
        FadeTransition lf2 = new FadeTransition(Duration.millis(400), logoutBtn);
        lf2.setFromValue(0); lf2.setToValue(1);
        lf2.setDelay(Duration.millis(300 + menuItems.length * 150));
        lf2.play();

        // ===== DARK MODE TOGGLE =====
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
            "-fx-border-color: " + color + "; -fx-border-radius: 12; -fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,2);"
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

    private Button makeRedBtn(String text, int width) {
        Button btn = new Button(text);
        btn.setPrefWidth(width); btn.setPrefHeight(40);
        btn.setStyle(
            "-fx-background-color: #FF4444; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #FF6666; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #FF4444; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"
        ));
        return btn;
    }

    private ComboBox<String> makeCombo(String... items) {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(items);
        box.setValue(items[items.length - 1]);
        box.setPrefHeight(42);
        String comboBg = AppState.darkMode ? "#1a1a1a" : "#F5F5F5";
        String comboText = AppState.darkMode ? "white" : "#1a1a1a";
        box.setStyle(
            "-fx-background-color: " + comboBg + "; -fx-text-fill: " + comboText + ";" +
            "-fx-border-color: #333333; -fx-border-radius: 8; -fx-background-radius: 8;"
        );
        return box;
    }

    private Label makeMsg() {
        Label l = new Label("");
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return l;
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
            String cleanTitle = titleText.replaceAll("[^\\p{L}\\p{N} ]", "").trim();
            TranslationService.translateBatch(
                java.util.Arrays.asList(cleanTitle),
                AppState.currentLangCode,
                t -> title.setText(t.getOrDefault(cleanTitle, titleText))
            );
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

    private void showError(Label lbl, String text) {
        lbl.setTextFill(Color.web("#FF4444")); lbl.setText(text);
    }

    private void showSuccess(Label lbl, String text) {
        lbl.setTextFill(Color.web("#00FF88")); lbl.setText(text);
    }

    private void showAlertSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Success"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showAlertError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}