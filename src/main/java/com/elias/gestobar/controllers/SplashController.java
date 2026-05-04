package com.elias.gestobar.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private ImageView logoImage;
    @FXML private ProgressIndicator loadingSpinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Esperar 2 segundos y luego cargar la pantalla principal
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> loadMainScreen());
        pause.play();
    }

    private void loadMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}