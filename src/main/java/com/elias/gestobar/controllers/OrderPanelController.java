package com.elias.gestobar.controllers;

import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.dto.TableDto;
import com.elias.gestobar.model.dto.TicketDetailDto;
import com.elias.gestobar.model.dto.TicketDto;
import com.elias.gestobar.service.TicketApiService;
import com.elias.gestobar.util.AlertHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class OrderPanelController {

    @FXML private Label  orderTitle;
    @FXML private VBox   orderItemsContainer;
    @FXML private Label  totalLabel;
    @FXML private Button closeBillButton;

    private final TicketApiService ticketService = new TicketApiService();

    private TablePanelController   tablePanelController;
    private NavbarController       navbarController;
    private ProductPanelController productPanelController;

    private TicketDto currentTicket;

    public void setTablePanelController(TablePanelController controller) {
        this.tablePanelController = controller;
    }

    public void setNavbarController(NavbarController controller) {
        this.navbarController = controller;
    }

    public void setProductPanelController(ProductPanelController controller) {
        this.productPanelController = controller;
    }

    public void onTableSelected(TableDto table) {
        Platform.runLater(() ->
                orderTitle.setText("Table " + table.number() + " — Order")
        );
    }

    public void renderTicket(TicketDto ticket) {
        Platform.runLater(() -> {
            currentTicket = ticket;
            orderItemsContainer.getChildren().clear();

            if (ticket == null || ticket.details() == null || ticket.details().isEmpty()) {
                totalLabel.setText("0.00 €");
                closeBillButton.setDisable(true);
                return;
            }

            for (TicketDetailDto item : ticket.details()) {
                orderItemsContainer.getChildren().add(createOrderItem(item));
            }

            totalLabel.setText(String.format("%.2f €", ticket.total()));
            closeBillButton.setDisable(false);
        });
    }

    public void clearOrder() {
        Platform.runLater(() -> {
            currentTicket = null;
            orderItemsContainer.getChildren().clear();
            orderTitle.setText("No table selected");
            totalLabel.setText("0.00 €");
            closeBillButton.setDisable(true);
        });
    }

    public void clearOrderKeepTitle() {
        Platform.runLater(() -> {
            currentTicket = null;
            orderItemsContainer.getChildren().clear();
            totalLabel.setText("0.00 €");
            closeBillButton.setDisable(true);
        });
    }

    private HBox createOrderItem(TicketDetailDto item) {
        Label name = new Label(item.productName());
        name.getStyleClass().add("order-item-name");

        Label sub = new Label(String.format("%.2f € x %d", item.unitPrice(), item.quantity()));
        sub.getStyleClass().add("order-item-sub");

        VBox info = new VBox(2, name, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        double subtotal = item.unitPrice().doubleValue() * item.quantity();
        Label price = new Label(String.format("%.2f €", subtotal));
        price.getStyleClass().add("order-item-price");

        Button minus = new Button("-");
        minus.getStyleClass().add("qty-btn-minus");
        minus.setOnAction(e -> handleQtyChange(item, item.quantity() - 1));

        Button plus = new Button("+");
        plus.getStyleClass().add("qty-btn-plus");
        plus.setOnAction(e -> handleQtyChange(item, item.quantity() + 1));

        HBox row = new HBox(6, info, spacer, price, minus, plus);
        row.getStyleClass().add("order-item-row");
        return row;
    }

    private void handleQtyChange(TicketDetailDto item, int newQty) {
        if (currentTicket == null) return;
        Integer ticketId = currentTicket.ticketId();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (newQty <= 0) {
                    ticketService.deleteProduct(ticketId, item.productId());
                } else {
                    ticketService.updateQuantity(ticketId, item.productId(), newQty);
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> reloadTicket(ticketId));
        task.setOnFailed(e -> AlertHelper.showError("Error", "No se pudo actualizar la cantidad."));
        new Thread(task).start();
    }

    private void reloadTicket(Integer ticketId) {
        Task<TicketDto> task = new Task<TicketDto>() {
            @Override
            protected TicketDto call() throws Exception {
                return ticketService.getTicket(ticketId);
            }
        };

        task.setOnSucceeded(e -> renderTicket(task.getValue()));
        task.setOnFailed(e -> AlertHelper.showError("Error", "No se pudo recargar el ticket."));
        new Thread(task).start();
    }

    @FXML
    private void handleCloseBill() {
        if (currentTicket == null) return;
        showConfirmationModal();
    }

    private void showConfirmationModal() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Cerrar cuenta");
        modal.setResizable(false);

        Label question = new Label("¿Desea cerrar la cuenta?");
        question.getStyleClass().add("modal-title");

        Button noBtn = new Button("No");
        noBtn.getStyleClass().add("modal-btn-cancel");
        noBtn.setOnAction(e -> modal.close());

        Button yesBtn = new Button("Sí, cobrar");
        yesBtn.getStyleClass().add("modal-btn-confirm");
        yesBtn.setOnAction(e -> {
            modal.close();
            Platform.runLater(this::showPaymentModal);
        });

        HBox buttons = new HBox(10, noBtn, yesBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(20, question, buttons);
        root.getStyleClass().add("modal-content");
        root.setPadding(new Insets(28));
        root.setMinWidth(480);
        root.setMinHeight(200);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/com/elias/gestobar/css/styles.css").toExternalForm()
        );
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void showPaymentModal() {
        BigDecimal total = currentTicket.total();

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Cobrar cuenta");
        modal.setResizable(false);

        // --- Título ---
        Label titleLbl = new Label("Cobrar cuenta");
        titleLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Separator sep1 = new Separator();

        // --- Input cantidad del cliente ---
        Label inputLbl = new Label("Cantidad entregada por el cliente:");
        inputLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #6B7280;");

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 15px; -fx-padding: 8 12 8 12; "
                + "-fx-border-color: #D1D5DB; -fx-border-width: 1; -fx-border-radius: 6; "
                + "-fx-background-radius: 6; -fx-background-color: #F9FAFB;");

        // --- Importe a cobrar ---
        Label totalLbl = new Label(String.format("Importe a cobrar:  %.2f €", total));
        totalLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #5B9BD5;");

        // --- Cambio a devolver ---
        Label changeLbl = new Label("Cambio a devolver:  —");
        changeLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #9CA3AF;");

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                BigDecimal given = new BigDecimal(newVal.replace(",", "."));
                BigDecimal change = given.subtract(total);
                String color = change.compareTo(BigDecimal.ZERO) >= 0 ? "#22C55E" : "#EF4444";
                changeLbl.setText(String.format("Cambio a devolver:  %.2f €", change));
                changeLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            } catch (NumberFormatException ex) {
                changeLbl.setText("Cambio a devolver:  —");
                changeLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #9CA3AF;");
            }
        });

        Separator sep2 = new Separator();

        // --- Botones ---
        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-family: 'Segoe UI'; "
                + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20 8 20; "
                + "-fx-background-radius: 6; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modal.close());

        Button confirmBtn = new Button("Cobrar");
        confirmBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; "
                + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20 8 20; "
                + "-fx-background-radius: 6; -fx-border-color: transparent; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> {
            modal.close();
            doCloseBill();
        });

        HBox buttons = new HBox(10, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(16, titleLbl, sep1, inputLbl, amountField, totalLbl, changeLbl, sep2, buttons);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: white;");

        modal.setScene(new Scene(root, 480, 340));
        modal.showAndWait();
    }

    private void doCloseBill() {
        Integer ticketId = currentTicket.ticketId();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ticketService.closeTicket(ticketId);
                return null;
            }
        };

        task.setOnSucceeded(e -> onBillClosed());
        task.setOnFailed(e -> AlertHelper.showError("Error", "No se pudo cerrar el ticket."));
        new Thread(task).start();
    }

    private void onBillClosed() {
        SessionManager.getInstance().clearActiveTicketId();
        clearOrder();

        if (tablePanelController  != null) tablePanelController.clearSelection();
        if (navbarController      != null) navbarController.clearActiveTable();
        if (productPanelController != null) {
            productPanelController.clearTable();
            productPanelController.setActiveTicketId(null);
        }
    }
}
