package com.noteapp.view;

import com.noteapp.auth.KeycloakAuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginView {

    private KeycloakAuthService authService;
    private Stage primaryStage;
    private Runnable onLoginSuccess;

    public LoginView(Stage primaryStage, KeycloakAuthService authService, Runnable onLoginSuccess) {
        this.primaryStage = primaryStage;
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
    }

    public void show() {
        primaryStage.setTitle("Not Uygulaması - Giriş");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Not Uygulaması Giriş");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label userName = new Label("Kullanıcı Adı:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Label pw = new Label("Şifre:");
        grid.add(pw, 0, 2);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);

        Button btn = new Button("Giriş");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 6);

        btn.setOnAction(e -> {
            actionTarget.setText("Giriş yapılıyor...");

            // Arka planda giriş işlemini yap
            new Thread(() -> {
                try {
                    boolean success = authService.login(userTextField.getText(), pwBox.getText());

                    // UI thread'de sonucu işle
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            actionTarget.setText("Giriş başarılı!");
                            onLoginSuccess.run();
                        } else {
                            actionTarget.setText("Giriş başarısız. Kullanıcı adı veya şifre hatalı.");
                        }
                    });

                } catch (IOException | InterruptedException ex) {
                    javafx.application.Platform.runLater(() ->
                            actionTarget.setText("Hata: " + ex.getMessage())
                    );
                }
            }).start();
        });

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}