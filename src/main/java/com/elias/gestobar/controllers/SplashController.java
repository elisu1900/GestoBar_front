package com.elias.gestobar.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private StackPane iconContainer;
    @FXML private Label labelGesto;
    @FXML private Label labelBarFX;
    @FXML private Label loadingLabel;
    @FXML private StackPane rootPane;
    @FXML private ImageView logoImage;
    @FXML private ProgressIndicator loadingSpinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> Router.goToLogin());
        pause.play();
    }
}