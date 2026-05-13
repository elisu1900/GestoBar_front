package com.elias.gestobar.controllers;

import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.dto.TableDto;
import com.elias.gestobar.model.dto.TicketDetailDto;
import com.elias.gestobar.model.dto.TicketDto;
import com.elias.gestobar.service.TicketApiService;
import com.elias.gestobar.util.AlertHelper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class OrderPanelController {

    @FXML private Label  orderTitle;
    @FXML private VBox   orderItemsContainer;
    @FXML private Label  totalLabel;
    @FXML private Button closeBillButton;

    private final TicketApiService ticketService = new TicketApiService();

    private TablePanelController  tablePanelController;
    private NavbarController      navbarController;
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
        orderTitle.setText("Table " + table.number() + " — Order");
    }


    public void renderTicket(TicketDto ticket) {
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
    }

    public void clearOrder() {
        currentTicket = null;
        orderItemsContainer.getChildren().clear();
        orderTitle.setText("No table selected");
        totalLabel.setText("0.00 €");
        closeBillButton.setDisable(true);
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

        if (tablePanelController != null)  tablePanelController.clearSelection();
        if (navbarController != null)      navbarController.clearActiveTable();
        if (productPanelController != null) productPanelController.clearTable();
        productPanelController.setActiveTicketId(null);

    }
}