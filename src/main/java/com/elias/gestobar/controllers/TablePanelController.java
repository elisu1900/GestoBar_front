package com.elias.gestobar.controllers;

import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.dto.TableDto;
import com.elias.gestobar.model.dto.TicketDto;
import com.elias.gestobar.service.TableApiService;
import com.elias.gestobar.service.TicketApiService;
import com.elias.gestobar.util.AlertHelper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class TablePanelController {

    @FXML private FlowPane tablesContainer;

    private final TableApiService  tableService  = new TableApiService();
    private final TicketApiService ticketService = new TicketApiService();

    private OrderPanelController   orderPanelController;
    private NavbarController       navbarController;
    private ProductPanelController productPanelController;

    private Button   selectedButton = null;
    private TableDto selectedTable  = null;

    @FXML
    public void initialize() {
        loadTables();
    }

    public void setOrderPanelController(OrderPanelController controller) {
        this.orderPanelController = controller;
    }

    public void setNavbarController(NavbarController controller) {
        this.navbarController = controller;
    }

    public void setProductPanelController(ProductPanelController controller) {
        this.productPanelController = controller;
    }

    private void loadTables() {
        Task<List<TableDto>> task = new Task<List<TableDto>>() {
            @Override
            protected List<TableDto> call() throws Exception {
                return tableService.getAllTables();
            }
        };
        task.setOnSucceeded(e -> renderTables(task.getValue()));
        task.setOnFailed(e -> AlertHelper.showError("Error", "No se pudieron cargar las mesas."));
        new Thread(task).start();
    }

    private void renderTables(List<TableDto> tables) {
        tablesContainer.getChildren().clear();
        for (TableDto table : tables) {
            tablesContainer.getChildren().add(createTableButton(table));
        }
    }

    private Button createTableButton(TableDto table) {
        Button btn = new Button("Table\n" + table.number());
        btn.getStyleClass().add("table-btn");
        btn.setPrefWidth(72);
        btn.setPrefHeight(60);
        btn.setWrapText(true);
        btn.setOnAction(e -> handleTableClick(table, btn));
        return btn;
    }

    private void handleTableClick(TableDto table, Button btn) {
        // Deseleccionar anterior
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("table-btn-selected");
            if (!selectedButton.getStyleClass().contains("table-btn")) {
                selectedButton.getStyleClass().add("table-btn");
            }
        }

        selectedButton = btn;
        selectedTable  = table;
        btn.getStyleClass().remove("table-btn");
        btn.getStyleClass().add("table-btn-selected");

        if (navbarController != null)
            navbarController.setActiveTable(table);

        if (orderPanelController != null)
            orderPanelController.onTableSelected(table);

        // ← esto faltaba: notificar al panel de productos
        if (productPanelController != null)
            productPanelController.onTableSelected(table);

        resolveTicketForTable(table);
    }

    private void resolveTicketForTable(TableDto table) {
        Task<TicketDto> task = new Task<TicketDto>() {
            @Override
            protected TicketDto call() throws Exception {
                // Preguntar al backend si la mesa tiene ticket abierto
                return ticketService.findOpenTicketByTable(table.tableId());
            }
        };

        task.setOnSucceeded(e -> {
            TicketDto ticket = task.getValue();
            if (ticket != null) {
                // Mesa tiene ticket abierto — cargarlo
                SessionManager.getInstance().setActiveTicketId((long) ticket.ticketId());
                if (productPanelController != null)
                    productPanelController.setActiveTicketId(ticket.ticketId());
                if (orderPanelController != null)
                    orderPanelController.renderTicket(ticket);
            } else {
                // Mesa libre — limpiar estado
                SessionManager.getInstance().clearActiveTicketId();
                if (productPanelController != null)
                    productPanelController.setActiveTicketId(null);
                if (orderPanelController != null)
                    orderPanelController.clearOrder();
            }
        });

        task.setOnFailed(e -> {
            SessionManager.getInstance().clearActiveTicketId();
            if (orderPanelController != null) orderPanelController.clearOrder();
        });

        new Thread(task).start();
    }

    public void clearSelection() {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("table-btn-selected");
            if (!selectedButton.getStyleClass().contains("table-btn")) {
                selectedButton.getStyleClass().add("table-btn");
            }
            selectedButton = null;
        }
        selectedTable = null;
    }

    public TableDto getSelectedTable() { return selectedTable; }
    public void refresh()              { loadTables(); }
}