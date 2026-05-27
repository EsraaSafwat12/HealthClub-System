package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginScreen extends Application {

    static ArrayList<User>     users;
    static ArrayList<Bill>     bills;
    static ArrayList<Message>  messages;
    static ArrayList<Schedule> schedules;

    private Text          welcomeText;
    private Text          subText;
    private Label         userLabel;
    private Label         passLabel;
    private TextField     userField;
    private PasswordField passField;
    private Button        loginBtn;
    private Button        registerBtn;
    private Label         msgLabel;
    private VBox          rightPanel;
    private HBox          root;

    @Override
    public void start(Stage stage) {
        // load data once at startup
        users     = FileManager.loadUsers();
        bills     = FileManager.loadBills();
        messages  = FileManager.loadMessages();
        schedules = FileManager.loadSchedules();

        new SplashScreen().show(stage, () -> {
            buildLoginScreen(stage);
            // first run: no users yet → open register automatically
            if (users.isEmpty()) openRegisterScreen(stage);
        });
    }

    void buildLoginScreen(Stage stage) {
        stage.setTitle("Power Gym");

        // ===== LEFT PANEL =====
        VBox leftPanel = new VBox(20);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPrefWidth(380);
        leftPanel.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1a0a00, #3d1800);");
        Text gymTitle = new Text("🏋️ POWER GYM");
        gymTitle.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 32));
        gymTitle.setFill(Color.web("#FF6B00"));
        Text slogan = new Text("Transform Your Body\nTransform Your Life");
        slogan.setFill(Color.web("#DDDDDD"));
        slogan.setTextAlignment(TextAlignment.CENTER);
        VBox features = new VBox(12);
        features.setAlignment(Pos.CENTER_LEFT);
        features.setPadding(new Insets(0, 0, 0, 40));
        for (String item : new String[]{
            "✔  Smart Member Management",
            "✔  Coach Scheduling",
            "✔  Billing System",
            "✔  Notifications",
            "✔  PDF Reports"
        }) {
            Label lbl = new Label(item);
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            features.getChildren().add(lbl);
        }
        leftPanel.getChildren().addAll(gymTitle, slogan, features);

        // ===== RIGHT PANEL =====
        rightPanel = new VBox(14);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(40, 50, 40, 50));
        rightPanel.setPrefWidth(470);
        rightPanel.setStyle("-fx-background-color: #111111;");

        // ===== LANGUAGE COMBOBOX =====
        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().addAll(TranslationService.LANGUAGES.keySet());
        langBox.setValue(AppState.currentLangName);
        langBox.setPrefWidth(200);
        langBox.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: white;" +
            "-fx-border-color: #FF6B00; -fx-border-radius: 6; -fx-background-radius: 6;");

        // ===== THEME BUTTON =====
        Button themeBtn = new Button(AppState.darkMode ? "🌙" : "☀");
        themeBtn.setPrefWidth(44); themeBtn.setPrefHeight(34);
        themeBtn.setStyle(
            "-fx-background-color: #2a2a2a; -fx-text-fill: white;" +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 16px;");
        themeBtn.setOnAction(e -> {
            AppState.darkMode = !AppState.darkMode;
            themeBtn.setText(AppState.darkMode ? "🌙" : "☀");
            applyTheme();
        });

        HBox topBar = new HBox(10, langBox, themeBtn);
        topBar.setAlignment(Pos.TOP_RIGHT);

        // ===== TEXTS =====
        welcomeText = new Text("Welcome Back!");
        welcomeText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        welcomeText.setFill(Color.WHITE);
        subText = new Text("Sign in to continue");
        subText.setFill(Color.GRAY);

        userLabel = makeLabel("USERNAME");
        passLabel = makeLabel("PASSWORD");
        userField = makeTextField("Username");
        passField = makePasswordField("Password");

        msgLabel = new Label();
        msgLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Label loadingLabel = new Label("🌐  Translating...");
        loadingLabel.setTextFill(Color.web("#FF9A3C"));
        loadingLabel.setVisible(false);

        // ===== BUTTONS =====
        loginBtn = new Button("LOGIN");
        loginBtn.setPrefWidth(350); loginBtn.setPrefHeight(45);
        loginBtn.setStyle(makeOrangeBtnStyle());

        registerBtn = new Button("REGISTER");
        registerBtn.setPrefWidth(350); registerBtn.setPrefHeight(45);
        registerBtn.setStyle(makeOutlineBtnStyle());

        // ===== LANGUAGE CHANGE =====
        langBox.setOnAction(e -> {
            String selected = langBox.getValue();
            if (selected == null || selected.equals(AppState.currentLangName)) return;
            AppState.setLanguage(selected);
            if (AppState.isRTL())
                rightPanel.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            else
                rightPanel.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            loadingLabel.setVisible(true);
            loginBtn.setDisable(true); registerBtn.setDisable(true);
            TranslationService.translateBatch(Arrays.asList(
                "Welcome Back!", "Sign in to continue",
                "USERNAME", "PASSWORD", "Username", "Password", "LOGIN", "REGISTER"
            ), AppState.currentLangCode, t -> {
                welcomeText.setText(t.getOrDefault("Welcome Back!", "Welcome Back!"));
                subText.setText(t.getOrDefault("Sign in to continue", "Sign in to continue"));
                userLabel.setText(t.getOrDefault("USERNAME", "USERNAME"));
                passLabel.setText(t.getOrDefault("PASSWORD", "PASSWORD"));
                userField.setPromptText(t.getOrDefault("Username", "Username"));
                passField.setPromptText(t.getOrDefault("Password", "Password"));
                loginBtn.setText(t.getOrDefault("LOGIN", "LOGIN"));
                registerBtn.setText(t.getOrDefault("REGISTER", "REGISTER"));
                loadingLabel.setVisible(false);
                loginBtn.setDisable(false); registerBtn.setDisable(false);
            });
        });

        // ===== LOGIN ACTION =====
        loginBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                msgLabel.setText("⚠  Please fill all fields!");
                msgLabel.setTextFill(Color.web("#FF9A3C"));
                return;
            }

            // reload fresh from file every login attempt
            users = FileManager.loadUsers();
            bills = FileManager.loadBills();
            messages  = FileManager.loadMessages();
            schedules = FileManager.loadSchedules();

            User found = null;
            for (User usr : users)
                if (usr.getUsername().equalsIgnoreCase(u) && usr.getPassword().equals(p))
                { found = usr; break; }

            if (found == null) {
                msgLabel.setText("✖  Invalid username or password!");
                msgLabel.setTextFill(Color.RED);
                TranslateTransition shake = new TranslateTransition(Duration.millis(70), rightPanel);
                shake.setFromX(-10); shake.setToX(10);
                shake.setCycleCount(6); shake.setAutoReverse(true); shake.play();
            } else {
                msgLabel.setText("✔"); msgLabel.setTextFill(Color.LIMEGREEN);
                // navigate based on role — found is already the correct subtype from parseUser
                String role = found.getRole();
                if (role.equals("admin")) {
                    new AdminScreen(found, users, bills, messages, schedules).show(stage);
                } else if (role.equals("coach")) {
                    new CoachScreen((Coach) found, users, messages, schedules).show(stage);
                } else if (role.equals("member")) {
                    new MemberScreen((Member) found, messages, schedules, bills).show(stage);
                } else {
                    msgLabel.setText("✖  Unknown role: " + role);
                    msgLabel.setTextFill(Color.RED);
                }
            }
        });

        registerBtn.setOnAction(e -> openRegisterScreen(stage));

        // ===== ASSEMBLE =====
        rightPanel.getChildren().addAll(
            topBar, welcomeText, subText,
            userLabel, userField,
            passLabel, passField,
            msgLabel, loadingLabel,
            loginBtn, registerBtn
        );

        root = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        Scene scene = new Scene(root, 870, 560);
        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), root);
        fade.setFromValue(0); fade.setToValue(1); fade.play();

        stage.setScene(scene);
        stage.setResizable(false);
        // save only if there is real data
        stage.setOnCloseRequest(e -> {
            if (users     != null && !users.isEmpty())     FileManager.saveUsers(users);
            if (bills     != null && !bills.isEmpty())     FileManager.saveBills(bills);
            if (messages  != null && !messages.isEmpty())  FileManager.saveMessages(messages);
            if (schedules != null && !schedules.isEmpty()) FileManager.saveSchedules(schedules);
            System.exit(0);
        });
        stage.show();
    }

    // ===== THEME =====
    private void applyTheme() {
        if (AppState.darkMode) {
            root.setStyle("-fx-background-color: #0d0d0d;");
            rightPanel.setStyle("-fx-background-color: #111111;");
            welcomeText.setFill(Color.WHITE);
            subText.setFill(Color.GRAY);
        } else {
            root.setStyle("-fx-background-color: #F0F2F5;");
            rightPanel.setStyle("-fx-background-color: #FFFFFF;");
            welcomeText.setFill(Color.web("#1a1a1a"));
            subText.setFill(Color.web("#555555"));
        }
    }

    // ===== REGISTER SCREEN =====
    void openRegisterScreen(Stage stage) {
        Stage regStage = new Stage();
        regStage.setTitle("Create New Account - Power Gym");

        VBox layout = new VBox(14);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setStyle(AppState.darkMode
            ? "-fx-background-color: #0a0a0a;"
            : "-fx-background-color: #F5F5F5;");

        Text title = new Text("🏋️ CREATE ACCOUNT");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 24));
        title.setFill(Color.web("#FF6B00"));

        TextField  idField          = makeTextField("ID  e.g. 101");
        TextField  nameField        = makeTextField("Full Name");
        TextField  uField           = makeTextField("Username");
        PasswordField pField        = makePasswordField("Min 4 characters");
        PasswordField confirmPField = makePasswordField("Confirm Password");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("admin", "coach", "member");
        roleBox.setValue("member");
        roleBox.setPrefHeight(44); roleBox.setPrefWidth(350);
        roleBox.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: white;" +
            "-fx-border-color: #333333; -fx-border-radius: 8; -fx-background-radius: 8;");

        TextField     coachField = makeTextField("Assigned Coach Name");
        TextField     subField   = makeTextField("Subscription End  DD-MM-YYYY");

        VBox memberFields = new VBox(8,
            makeLabel("COACH NAME"), coachField,
            makeLabel("SUBSCRIPTION END  (DD-MM-YYYY)"), subField);
        memberFields.setVisible(true);
        roleBox.setOnAction(e ->
            memberFields.setVisible(roleBox.getValue().equals("member")));

        Label msgLbl = new Label("");
        msgLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Button regBtn = new Button("✅  CREATE ACCOUNT");
        regBtn.setPrefWidth(350); regBtn.setPrefHeight(48);
        regBtn.setStyle(makeOrangeBtnStyle());

        regBtn.setOnAction(e -> {
            try {
                String idTxt = idField.getText().trim();
                String name  = nameField.getText().trim();
                String uname = uField.getText().trim();
                String pass  = pField.getText().trim();
                String conf  = confirmPField.getText().trim();
                String role  = roleBox.getValue();

                // validations
                if (!idTxt.matches("[0-9]+")) {
                    msgLbl.setTextFill(Color.RED);
                    msgLbl.setText("❌  ID must be numbers only!"); return;
                }
                if (name.isEmpty()) {
                    msgLbl.setTextFill(Color.RED);
                    msgLbl.setText("❌  Name cannot be empty!"); return;
                }
                if (uname.isEmpty() || !uname.matches("[a-z0-9_]+")) {
                    msgLbl.setTextFill(Color.RED);
                    msgLbl.setText("❌  Username: lowercase letters/numbers only!"); return;
                }
                if (pass.length() < 4) {
                    msgLbl.setTextFill(Color.RED);
                    msgLbl.setText("❌  Password too short! (min 4)"); return;
                }
                if (!pass.equals(conf)) {
                    msgLbl.setTextFill(Color.RED);
                    msgLbl.setText("❌  Passwords do not match!"); return;
                }
                int id = Integer.parseInt(idTxt);
                if (FileManager.isIdExists(id, users)) {
                    msgLbl.setTextFill(Color.RED);
                    msgLbl.setText("❌  ID already exists!"); return;
                }
                if (FileManager.isUsernameExists(uname, users)) {
                    msgLbl.setTextFill(Color.RED);
                    msgLbl.setText("❌  Username already taken!"); return;
                }

                // create correct subtype
                if (role.equals("member")) {
                    String subInput = subField.getText().trim();
                    if (coachField.getText().trim().isEmpty()) {
                        msgLbl.setTextFill(Color.RED);
                        msgLbl.setText("❌  Coach name cannot be empty!"); return;
                    }
                    // convert DD-MM-YYYY → YYYY-MM-DD
                    String[] parts = subInput.split("-");
                    if (parts.length != 3 || parts[2].length() != 4) {
                        msgLbl.setTextFill(Color.RED);
                        msgLbl.setText("❌  Date must be DD-MM-YYYY"); return;
                    }
                    String subDate = parts[2] + "-" + parts[1] + "-" + parts[0];
                    users.add(new Member(id, name, uname, pass,
                        coachField.getText().trim(), subDate));

                } else if (role.equals("coach")) {
                    users.add(new Coach(id, name, uname, pass));

                } else {
                    users.add(new Admin(id, name, uname, pass));
                }

                // save to file
                FileManager.saveUsers(users);

                msgLbl.setTextFill(Color.LIMEGREEN);
                msgLbl.setText("✅  Account created! You can now login.");

                // clear fields
                idField.clear(); nameField.clear(); uField.clear();
                pField.clear(); confirmPField.clear();
                coachField.clear(); subField.clear();

            } catch (Exception ex) {
                msgLbl.setTextFill(Color.RED);
                msgLbl.setText("❌  Error: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(
            title, new Separator(),
            makeLabel("ID"),   idField,
            makeLabel("FULL NAME"), nameField,
            makeLabel("USERNAME"),  uField,
            makeLabel("PASSWORD"),  pField,
            makeLabel("CONFIRM PASSWORD"), confirmPField,
            makeLabel("ROLE"),  roleBox,
            memberFields,
            msgLbl, regBtn
        );

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        regStage.setScene(new Scene(scroll, 470, 650));
        regStage.show();
    }

    // ===== STATIC HELPERS =====
    public static Label makeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#FF6B00"));
        return lbl;
    }
    public static TextField makeTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(42); tf.setPrefWidth(350);
        tf.setStyle(fieldStyle());
        return tf;
    }
    public static PasswordField makePasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setPrefHeight(42); pf.setPrefWidth(350);
        pf.setStyle(fieldStyle());
        return pf;
    }
    private static String fieldStyle() {
        return AppState.darkMode
            ? "-fx-background-color: #1a1a1a; -fx-text-fill: white;" +
              "-fx-prompt-text-fill: #555; -fx-border-color: #333;" +
              "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px;"
            : "-fx-background-color: #F5F5F5; -fx-text-fill: #1a1a1a;" +
              "-fx-prompt-text-fill: #AAA; -fx-border-color: #CCC;" +
              "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px;";
    }
    public static String makeOrangeBtnStyle() {
        return "-fx-background-color: linear-gradient(to right, #FF6B00, #FF9A3C);" +
               "-fx-text-fill: white; -fx-font-weight: bold;" +
               "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;";
    }
    public static String makeOutlineBtnStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #FF6B00;" +
               "-fx-border-color: #FF6B00; -fx-border-radius: 8;" +
               "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
    }
    public static String contentBg() {
        return AppState.darkMode
            ? "-fx-background-color: #0d0d0d;"
            : "-fx-background-color: #F0F2F5;";
    }
    public static String sectionBg() {
        return AppState.darkMode
            ? "-fx-background-color: #1a1a1a; -fx-background-radius: 10;" +
              "-fx-border-color: #2a2a2a; -fx-border-radius: 10; -fx-border-width: 1;"
            : "-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
              "-fx-border-color: #E0E0E0; -fx-border-radius: 10; -fx-border-width: 1;";
    }
    public static String sidebarBg() {
        return AppState.darkMode
            ? "-fx-background-color: linear-gradient(to bottom, #1a0a00, #2d1200);"
            : "-fx-background-color: linear-gradient(to bottom, #2d1200, #5a2800);";
    }
    public static String textAreaStyle() {
        return AppState.darkMode
            ? "-fx-control-inner-background: #111111; -fx-text-fill: #FFFFFF;" +
              "-fx-font-family: monospace; -fx-font-size: 12px;" +
              "-fx-border-color: #333333; -fx-border-radius: 6;"
            : "-fx-control-inner-background: #FAFAFA; -fx-text-fill: #1a1a1a;" +
              "-fx-font-family: monospace; -fx-font-size: 12px;" +
              "-fx-border-color: #DDDDDD; -fx-border-radius: 6;";
    }
    public static Color primaryTextColor() {
        return AppState.darkMode ? Color.WHITE : Color.web("#1a1a1a");
    }
    public static Color secondaryTextColor() {
        return AppState.darkMode ? Color.web("#AAAAAA") : Color.web("#555555");
    }
    public static Color titleColor() { return Color.web("#FF6B00"); }

    public static void main(String[] args) { launch(args); }

    // يُستخدم من الـ screens التانية عشان يرجع للـ login بدون splash
    public static void goToLogin(Stage stage) {
        users     = FileManager.loadUsers();
        bills     = FileManager.loadBills();
        messages  = FileManager.loadMessages();
        schedules = FileManager.loadSchedules();
        LoginScreen ls = new LoginScreen();
        ls.buildLoginScreen(stage);
        if (users.isEmpty()) ls.openRegisterScreen(stage);
    }

    public static VBox makeSectionBox(String title) {
        VBox box = new VBox(10);
        box.setPadding(new javafx.geometry.Insets(20));
        box.setStyle(sectionBg());
        javafx.scene.text.Text t = new javafx.scene.text.Text(title);
        t.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 15));
        t.setFill(javafx.scene.paint.Color.web("#FF6B00"));
        box.getChildren().add(t);
        return box;
    }
}
