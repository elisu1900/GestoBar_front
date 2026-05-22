package com.elias.gestobar.controllers;

import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.dto.TableDto;
import com.elias.gestobar.model.dto.TicketDetailDto;
import com.elias.gestobar.model.dto.TicketDto;
import com.elias.gestobar.service.TableApiService;
import com.elias.gestobar.service.TicketApiService;
import com.elias.gestobar.util.AlertHelper;
import com.elias.gestobar.util.ApiException;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderPanelController {

    @FXML private Label  orderTitle;
    @FXML private VBox   orderItemsContainer;
    @FXML private Label  totalLabel;
    @FXML private Button moveTableButton;
    @FXML private Button closeBillButton;

    private final TicketApiService ticketService  = new TicketApiService();
    private final TableApiService  tableService   = new TableApiService();

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
                moveTableButton.setDisable(ticket == null);
                closeBillButton.setDisable(true);
                return;
            }

            for (TicketDetailDto item : ticket.details()) {
                orderItemsContainer.getChildren().add(createOrderItem(item));
            }

            totalLabel.setText(String.format("%.2f €", ticket.total()));
            moveTableButton.setDisable(false);
            closeBillButton.setDisable(false);
        });
    }

    public void clearOrder() {
        Platform.runLater(() -> {
            currentTicket = null;
            orderItemsContainer.getChildren().clear();
            orderTitle.setText("No table selected");
            totalLabel.setText("0.00 €");
            moveTableButton.setDisable(true);
            closeBillButton.setDisable(true);
        });
    }

    public void clearOrderKeepTitle() {
        Platform.runLater(() -> {
            currentTicket = null;
            orderItemsContainer.getChildren().clear();
            totalLabel.setText("0.00 €");
            moveTableButton.setDisable(true);
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
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not update quantity."));
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
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not reload ticket."));
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
        modal.setTitle("Close Bill");
        modal.setResizable(false);

        Label question = new Label("Are you sure you want to close the bill?");
        question.getStyleClass().add("modal-title");

        Button noBtn = new Button("No");
        noBtn.getStyleClass().add("modal-btn-cancel");
        noBtn.setOnAction(e -> modal.close());

        Button yesBtn = new Button("Yes, charge");
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
        modal.setTitle("Collect Payment");
        modal.setResizable(false);

        Label titleLbl = new Label("Collect Payment");
        titleLbl.getStyleClass().add("modal-title-lg");

        Separator sep1 = new Separator();

        Label inputLbl = new Label("Amount given by customer:");
        inputLbl.getStyleClass().add("modal-label");

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.getStyleClass().add("modal-input");

        Label totalLbl = new Label(String.format("Amount to charge:  %.2f €", total));
        totalLbl.getStyleClass().add("modal-amount-total");

        Label changeLbl = new Label("Change to return:  —");
        changeLbl.getStyleClass().add("modal-amount-change");

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                BigDecimal given = new BigDecimal(newVal.replace(",", "."));
                BigDecimal change = given.subtract(total);
                changeLbl.setText(String.format("Change to return:  %.2f €", change));
                changeLbl.getStyleClass().removeAll("modal-change-positive", "modal-change-negative");
                changeLbl.getStyleClass().add(
                    change.compareTo(BigDecimal.ZERO) >= 0 ? "modal-change-positive" : "modal-change-negative"
                );
            } catch (NumberFormatException ex) {
                changeLbl.setText("Change to return:  —");
                changeLbl.getStyleClass().removeAll("modal-change-positive", "modal-change-negative");
            }
        });

        Separator sep2 = new Separator();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("modal-btn-cancel");
        cancelBtn.setOnAction(e -> modal.close());

        Button confirmBtn = new Button("Charge");
        confirmBtn.getStyleClass().add("modal-btn-confirm");
        confirmBtn.setOnAction(e -> {
            modal.close();
            doCloseBill();
        });

        HBox buttons = new HBox(10, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(16, titleLbl, sep1, inputLbl, amountField, totalLbl, changeLbl, sep2, buttons);
        root.setPadding(new Insets(28));
        root.getStyleClass().add("modal-content");

        Scene scene = new Scene(root, 480, 340);
        scene.getStylesheets().add(
            getClass().getResource("/com/elias/gestobar/css/styles.css").toExternalForm()
        );
        modal.setScene(scene);
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
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not close the ticket."));
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

    @FXML
    private void handleMoveTable() {
        if (currentTicket == null) return;
        showMoveTableModal();
    }

    private void showMoveTableModal() {
        Integer currentTableId = currentTicket.tableId();

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Move Order");
        modal.setResizable(false);

        Label title = new Label("Move order to another table");
        title.getStyleClass().add("modal-title");

        Label subtitle = new Label("Loading tables...");
        subtitle.getStyleClass().add("modal-label");

        FlowPane tablesPane = new FlowPane();
        tablesPane.setHgap(10);
        tablesPane.setVgap(10);

        Label legendFree  = new Label("● Free");
        legendFree.getStyleClass().addAll("legend-label", "legend-label-free");
        Label legendEmpty = new Label("● Empty ticket");
        legendEmpty.getStyleClass().addAll("legend-label", "legend-label-empty");
        Label legendBusy  = new Label("● Occupied");
        legendBusy.getStyleClass().addAll("legend-label", "legend-label-busy");
        HBox legend = new HBox(16, legendFree, legendEmpty, legendBusy);
        legend.setAlignment(Pos.CENTER_LEFT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("modal-btn-cancel");
        cancelBtn.setOnAction(e -> modal.close());
        HBox buttons = new HBox(cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(16, title, new Separator(), subtitle, tablesPane, legend, new Separator(), buttons);
        root.getStyleClass().add("modal-content");
        root.setPadding(new Insets(28));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/com/elias/gestobar/css/styles.css").toExternalForm()
        );
        modal.setScene(scene);

        Task<List<TableWithStatus>> loadTask = new Task<>() {
            @Override
            protected List<TableWithStatus> call() throws Exception {
                List<TableDto> tables = tableService.getAllTables();
                List<TableWithStatus> result = new ArrayList<>();
                for (TableDto table : tables) {
                    if (table.tableId().equals(currentTableId)) continue;
                    TicketDto ticket = ticketService.findOpenTicketByTable(table.tableId());
                    MoveTableStatus status;
                    if (ticket == null) {
                        status = MoveTableStatus.FREE;
                    } else if (ticket.details() == null || ticket.details().isEmpty()) {
                        status = MoveTableStatus.EMPTY_TICKET;
                    } else {
                        status = MoveTableStatus.OCCUPIED;
                    }
                    result.add(new TableWithStatus(table, status));
                }
                return result;
            }
        };

        loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            List<TableWithStatus> list = loadTask.getValue();
            if (list.isEmpty()) {
                subtitle.setText("No other tables available.");
                modal.sizeToScene();
                return;
            }
            subtitle.setText("Select target table:");
            tablesPane.getChildren().clear();
            for (TableWithStatus tws : list) {
                tablesPane.getChildren().add(buildMoveTableBtn(tws, modal));
            }

            int cols    = Math.min(list.size(), 5);
            double btnW = 94, gap = 10, padding = 28 * 2;
            double paneW = cols * btnW + (cols - 1) * gap;
            tablesPane.setPrefWrapLength(paneW + 1);
            root.setMinWidth(paneW + padding);
            modal.setMaxHeight(560);
            modal.sizeToScene();
        }));
        loadTask.setOnFailed(e -> Platform.runLater(() ->
            subtitle.setText("Error loading tables.")
        ));
        new Thread(loadTask).start();

        modal.showAndWait();
    }

    private Button buildMoveTableBtn(TableWithStatus tws, Stage modal) {
        Label numLbl = new Label("Table " + tws.table().number());
        numLbl.getStyleClass().add("move-table-num");

        String statusText;
        String statusClass;
        boolean disabled = false;

        switch (tws.status()) {
            case FREE -> {
                statusText  = "● Free";
                statusClass = "move-table-status-free";
            }
            case EMPTY_TICKET -> {
                statusText  = "● Empty";
                statusClass = "move-table-status-empty";
            }
            default -> {
                statusText  = "● Occupied";
                statusClass = "move-table-status-occupied";
                disabled    = true;
            }
        }

        Label statusLbl = new Label(statusText);
        statusLbl.getStyleClass().addAll("move-table-status", statusClass);

        VBox content = new VBox(2, numLbl, statusLbl);
        content.setAlignment(Pos.CENTER);

        String btnClass = switch (tws.status()) {
            case FREE         -> "move-table-btn-free";
            case EMPTY_TICKET -> "move-table-btn-empty";
            default           -> "move-table-btn-occupied";
        };

        Button btn = new Button();
        btn.setGraphic(content);
        btn.getStyleClass().addAll("move-table-btn", btnClass);
        btn.setDisable(disabled);

        if (!disabled) {
            btn.setOnAction(e -> doMoveTable(tws.table(), modal));
        }

        return btn;
    }

    private void doMoveTable(TableDto targetTable, Stage modal) {
        modal.close();
        Integer ticketId = currentTicket.ticketId();

        Task<TicketDto> task = new Task<>() {
            @Override
            protected TicketDto call() throws Exception {
                return ticketService.moveTicket(ticketId, targetTable.tableId());
            }
        };

        task.setOnSucceeded(e -> {
            TicketDto moved = task.getValue();
            renderTicket(moved);
            Platform.runLater(() -> orderTitle.setText("Table " + moved.tableNumber() + " — Order"));
            if (tablePanelController != null) tablePanelController.refreshAndSelectTable(moved.tableId());
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = (ex instanceof ApiException) ? ex.getMessage() : "Could not move the order.";
            AlertHelper.showError("Error moving order", msg);
        });

        new Thread(task).start();
    }

    private enum MoveTableStatus { FREE, EMPTY_TICKET, OCCUPIED }
    private record TableWithStatus(TableDto table, MoveTableStatus status) {}
}
