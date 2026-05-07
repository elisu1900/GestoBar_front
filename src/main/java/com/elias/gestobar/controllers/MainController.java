package com.elias.gestobar.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML private BorderPane rootPane;

    @FXML private NavbarController navbarController;
    @FXML private ContentAreaController contentAreaController;
    @FXML private OrderPanelController orderPanelController;

    @FXML
    public void initialize() {
        contentAreaController.getTablePanelController()
                .setOrderPanelController(orderPanelController);
        contentAreaController.getTablePanelController()
                .setNavbarController(navbarController);

        contentAreaController.getProductPanelController()
                .setOrderPanelController(orderPanelController);
    }
}
