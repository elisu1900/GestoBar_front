package com.elias.gestobar;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.controllers.Router;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    // MainApp.java

    @Override
    public void start(Stage stage) {
        Router.init(stage);

        stage.setTitle(AppConfig.getWindowTitle());
        stage.setWidth(AppConfig.getWindowWidth());
        stage.setHeight(AppConfig.getWindowHeight());
        stage.setMaximized(AppConfig.isMaximized());
        stage.setResizable(true);

        Router.goTo("splash");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}