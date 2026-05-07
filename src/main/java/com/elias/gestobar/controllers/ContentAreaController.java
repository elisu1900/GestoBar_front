package com.elias.gestobar.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ContentAreaController {

    @FXML private VBox productPanel;
    @FXML private VBox calculator;
    @FXML private VBox tablePanel;
    @FXML private HBox contentRoot;

    @FXML private TablePanelController tablePanelController;
    @FXML private ProductPanelController productPanelController;

    public TablePanelController getTablePanelController() {
        return tablePanelController;
    }

    public ProductPanelController getProductPanelController() {
        return productPanelController;
    }
}