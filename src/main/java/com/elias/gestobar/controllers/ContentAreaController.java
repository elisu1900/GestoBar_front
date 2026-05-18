package com.elias.gestobar.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ContentAreaController {

    @FXML private VBox productPanel;
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

    @FXML
    private void handleOpenCalculator() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/elias/gestobar/view/calculator.fxml")
            );
            VBox content = loader.load();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Calculadora");
            modal.setResizable(false);
            content.setMinWidth(360);
            content.setMinHeight(420);
            content.setPrefWidth(380);
            modal.setScene(new Scene(content, 380, 440));
            modal.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
