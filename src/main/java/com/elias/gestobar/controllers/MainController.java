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
        TablePanelController   tablePanelController   = contentAreaController.getTablePanelController();
        ProductPanelController productPanelController = contentAreaController.getProductPanelController();

        tablePanelController.setOrderPanelController(orderPanelController);
        tablePanelController.setNavbarController(navbarController);

        tablePanelController.setProductPanelController(productPanelController);

        productPanelController.setOrderPanelController(orderPanelController);
    }
}
