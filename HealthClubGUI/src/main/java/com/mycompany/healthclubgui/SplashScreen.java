package com.mycompany.healthclubgui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreen {

    public void show(Stage stage, Runnable onFinish) {

        // ===== ROOT =====
        StackPane root = new StackPane();

        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #050505, #111111);"
        );

        root.setPrefSize(700, 450);

        // ===== BACKGROUND GLOW =====
        Circle glow1 = new Circle(220);
        glow1.setFill(Color.web("#FF6B00", 0.08));
        glow1.setTranslateX(-180);
        glow1.setTranslateY(-120);

        Circle glow2 = new Circle(170);
        glow2.setFill(Color.web("#FF9A3C", 0.05));
        glow2.setTranslateX(220);
        glow2.setTranslateY(130);

        // ===== CONTENT =====
        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);

        // ===== ICON =====
        Text icon = new Text("🏋️");
        icon.setFont(Font.font(70));

        // ===== TITLE =====
        Text title = new Text("POWER GYM");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 48));
        title.setFill(Color.web("#FF6B00"));

        // ===== SUBTITLE =====
        Text sub = new Text("Transform Your Body • Transform Your Life");
        sub.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        sub.setFill(Color.web("#BBBBBB"));

        // ===== LINE =====
        Rectangle line = new Rectangle(260, 3);
        line.setArcWidth(10);
        line.setArcHeight(10);
        line.setFill(Color.web("#FF6B00"));

        // ===== LOADING =====
        Text loading = new Text("Loading System...");
        loading.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        loading.setFill(Color.web("#888888"));

        // ===== PROGRESS BAR =====
        Rectangle progressBg = new Rectangle(260, 8);
        progressBg.setArcWidth(20);
        progressBg.setArcHeight(20);
        progressBg.setFill(Color.web("#222222"));

        Rectangle progress = new Rectangle(0, 8);
        progress.setArcWidth(20);
        progress.setArcHeight(20);
        progress.setFill(Color.web("#FF6B00"));

        StackPane progressPane = new StackPane(progressBg, progress);

        content.getChildren().addAll(
            icon,
            title,
            sub,
            line,
            loading,
            progressPane
        );

        root.getChildren().addAll(glow1, glow2, content);

        // ===== SCENE =====
        Scene scene = new Scene(root);

        stage.setTitle("Power Gym");

        stage.setScene(scene);

        stage.setResizable(false);

        stage.show();

        // ===== CLOSE BUTTON FIX =====
        stage.setOnCloseRequest(e -> {
            System.exit(0);
        });

        // ===== ANIMATIONS =====

        // Fade animation
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), content);
        fade.setFromValue(0);
        fade.setToValue(1);

        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.seconds(1), icon);
        scale.setFromX(0.7);
        scale.setFromY(0.7);
        scale.setToX(1);
        scale.setToY(1);

        // Floating icon animation
        TranslateTransition floating = new TranslateTransition(Duration.seconds(1.8), icon);
        floating.setFromY(-6);
        floating.setToY(6);
        floating.setCycleCount(Animation.INDEFINITE);
        floating.setAutoReverse(true);

        // Progress animation
        Timeline progressAnim = new Timeline(
            new KeyFrame(Duration.seconds(0),
                new KeyValue(progress.widthProperty(), 0)
            ),
            new KeyFrame(Duration.seconds(3),
                new KeyValue(progress.widthProperty(), 260)
            )
        );

        fade.play();
        scale.play();
        floating.play();
        progressAnim.play();

        // ===== OPEN LOGIN =====
        PauseTransition wait = new PauseTransition(Duration.seconds(3.2));

        wait.setOnFinished(e -> {
            floating.stop();
            onFinish.run();
        });

        wait.play();
    }
}