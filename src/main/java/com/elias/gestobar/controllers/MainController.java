package com.elias.gestobar.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import java.awt.desktop.AppEvent;


public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() {
        AppEvent.clearAll();
    }
}
