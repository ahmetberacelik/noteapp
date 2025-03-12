package com.noteapp;

import com.noteapp.auth.KeycloakAuthService;
import com.noteapp.client.NoteClient;
import com.noteapp.model.Note;
import com.noteapp.view.LoginView;
import com.noteapp.viewmodel.NoteViewModel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoteApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(NoteApp.class);

    private NoteViewModel viewModel;
    private ListView<Note> noteListView;
    private KeycloakAuthService authService;
    private NoteClient noteClient;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Kimlik doğrulama ve API istemcisi oluşturma
        authService = new KeycloakAuthService();
        noteClient = new NoteClient();

        // LoginView göster
        LoginView loginView = new LoginView(primaryStage, authService, this::showMainView);
        loginView.show();
    }

    private void showMainView() {
        // Token'ı API istemcisine gönder
        noteClient.setAccessToken(authService.getAccessToken());

        // ViewModel oluştur
        viewModel = new NoteViewModel(noteClient);

        // Notları yükle
        try {
            viewModel.loadNotes();
        } catch (Exception e) {
            logger.error("Notlar yüklenirken hata oluştu", e);
            showError("Notlar yüklenirken hata oluştu", e.getMessage());
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Sol panel - Not listesi
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));

        Label notesLabel = new Label("Notlar");
        noteListView = new ListView<>();
        noteListView.setItems(viewModel.getNotes());
        noteListView.setPrefWidth(200);

        Button newNoteBtn = new Button("Yeni Not");
        newNoteBtn.setMaxWidth(Double.MAX_VALUE);

        Button logoutBtn = new Button("Çıkış");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);

        leftPanel.getChildren().addAll(notesLabel, noteListView, newNoteBtn, logoutBtn);

        // Sağ panel - Not detayları
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));

        Label titleLabel = new Label("Başlık");
        TextField titleField = new TextField();
        titleField.textProperty().bindBidirectional(viewModel.currentTitleProperty());

        Label contentLabel = new Label("İçerik");
        TextArea contentArea = new TextArea();
        contentArea.textProperty().bindBidirectional(viewModel.currentContentProperty());
        contentArea.setPrefHeight(300);

        HBox buttonBox = new HBox(10);
        Button saveBtn = new Button("Kaydet");
        Button deleteBtn = new Button("Sil");
        buttonBox.getChildren().addAll(saveBtn, deleteBtn);

        rightPanel.getChildren().addAll(titleLabel, titleField, contentLabel, contentArea, buttonBox);

        // Ana yerleşim
        root.setLeft(leftPanel);
        root.setCenter(rightPanel);

        // Olaylar
        noteListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.selectNote(newVal);
            }
        });

        newNoteBtn.setOnAction(e -> {
            viewModel.clearFields();
            noteListView.getSelectionModel().clearSelection();
        });

        saveBtn.setOnAction(e -> {
            try {
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    viewModel.updateNote(selectedNote);
                } else {
                    viewModel.addNote();
                }
            } catch (Exception ex) {
                logger.error("Not kaydedilirken hata oluştu", ex);
                showError("Not kaydedilirken hata oluştu", ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            try {
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    viewModel.deleteNote(selectedNote);
                }
            } catch (Exception ex) {
                logger.error("Not silinirken hata oluştu", ex);
                showError("Not silinirken hata oluştu", ex.getMessage());
            }
        });

        logoutBtn.setOnAction(e -> {
            authService.logout();
            LoginView loginView = new LoginView(primaryStage, authService, this::showMainView);
            loginView.show();
        });

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("Not Alma Uygulaması");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}