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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TablePanelController {

    @FXML
    private FlowPane tablesContainer;

    private final TableApiService  tableService  = new TableApiService();
    private final TicketApiService ticketService = new TicketApiService();

    private OrderPanelController   orderPanelController;
    private NavbarController       navbarController;
    private ProductPanelController productPanelController;

    private Button   selectedButton   = null;
    private TableDto selectedTable    = null;

    private final Set<Integer> occupiedTableIds = new HashSet<>();

    private record LoadResult(List<TableDto> tables, Set<Integer> occupied) {}

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
        Task<LoadResult> task = new Task<>() {
            @Override
            protected LoadResult call() throws Exception {
                List<TableDto> tables = tableService.getAllTables();
                Set<Integer> occupied = new HashSet<>();
                for (TableDto table : tables) {
                    TicketDto ticket = ticketService.findOpenTicketByTable(table.tableId());
                    if (ticket != null) occupied.add(table.tableId());
                }
                return new LoadResult(tables, occupied);
            }
        };
        task.setOnSucceeded(e -> {
            LoadResult result = task.getValue();
            occupiedTableIds.clear();
            occupiedTableIds.addAll(result.occupied());
            renderTables(result.tables());
        });
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not load tables."));
        new Thread(task).start();
    }

    private void renderTables(List<TableDto> tables) {
        List<TableDto> sorted = new ArrayList<>(tables);
        Collections.sort(sorted, Comparator.comparingInt(TableDto::number));
        tablesContainer.getChildren().clear();
        for (TableDto table : sorted) {
            tablesContainer.getChildren().add(createTableButton(table));
        }
    }

    private Button createTableButton(TableDto table) {
        Button btn = new Button("Table\n" + table.number());
        btn.setPrefWidth(72);
        btn.setPrefHeight(60);
        btn.setWrapText(true);
        applyBaseStyle(btn, table.tableId());
        btn.setOnAction(e -> handleTableClick(table, btn));
        return btn;
    }

    private void applyBaseStyle(Button btn, Integer tableId) {
        btn.getStyleClass().removeAll("table-btn", "table-btn-occupied", "table-btn-selected");
        if (occupiedTableIds.contains(tableId)) {
            btn.getStyleClass().add("table-btn-occupied");
        } else {
            btn.getStyleClass().add("table-btn");
        }
    }

    private void handleTableClick(TableDto table, Button btn) {
        if (selectedButton != null) {
            applyBaseStyle(selectedButton, selectedTable.tableId());
        }

        selectedButton = btn;
        selectedTable  = table;
        btn.getStyleClass().removeAll("table-btn", "table-btn-occupied");
        btn.getStyleClass().add("table-btn-selected");

        if (navbarController != null)
            navbarController.setActiveTable(table);

        if (orderPanelController != null)
            orderPanelController.onTableSelected(table);

        if (productPanelController != null)
            productPanelController.onTableSelected(table);

        resolveTicketForTable(table);
    }

    private void resolveTicketForTable(TableDto table) {
        Task<TicketDto> task = new Task<>() {
            @Override
            protected TicketDto call() throws Exception {
                return ticketService.findOpenTicketByTable(table.tableId());
            }
        };

        task.setOnSucceeded(e -> {
            TicketDto ticket = task.getValue();
            if (ticket != null) {
                occupiedTableIds.add(table.tableId());
                SessionManager.getInstance().setActiveTicketId((long) ticket.ticketId());
                if (productPanelController != null)
                    productPanelController.setActiveTicketId(ticket.ticketId());
                if (orderPanelController != null)
                    orderPanelController.renderTicket(ticket);
            } else {
                occupiedTableIds.remove(table.tableId());
                SessionManager.getInstance().clearActiveTicketId();
                if (productPanelController != null)
                    productPanelController.setActiveTicketId(null);
                if (orderPanelController != null)
                    orderPanelController.clearOrderKeepTitle();
            }
        });

        task.setOnFailed(e -> {
            occupiedTableIds.remove(table.tableId());
            SessionManager.getInstance().clearActiveTicketId();
            if (productPanelController != null)
                productPanelController.setActiveTicketId(null);
            if (orderPanelController != null)
                orderPanelController.clearOrderKeepTitle();
        });

        new Thread(task).start();
    }

    public void markTableOccupied(Integer tableId) {
        occupiedTableIds.add(tableId);
        tablesContainer.getChildren().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .filter(b -> b != selectedButton)
                .forEach(b -> {
                    String label = "Table\n" + tableId;
                    if (b.getText().equals(label)) applyBaseStyle(b, tableId);
                });
    }

    public void markTableFree(Integer tableId) {
        occupiedTableIds.remove(tableId);
        tablesContainer.getChildren().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .filter(b -> b != selectedButton)
                .forEach(b -> {
                    String label = "Table\n" + tableId;
                    if (b.getText().equals(label)) applyBaseStyle(b, tableId);
                });
    }

    public void clearSelection() {
        if (selectedButton != null) {
            applyBaseStyle(selectedButton, selectedTable != null ? selectedTable.tableId() : -1);
            selectedButton = null;
        }
        selectedTable = null;
    }

    public TableDto getSelectedTable() {
        return selectedTable;
    }

    public void refresh() {
        loadTables();
    }

    public void refreshAndSelectTable(Integer targetTableId) {
        Task<LoadResult> task = new Task<>() {
            @Override
            protected LoadResult call() throws Exception {
                List<TableDto> tables = tableService.getAllTables();
                Set<Integer> occupied = new HashSet<>();
                for (TableDto table : tables) {
                    TicketDto ticket = ticketService.findOpenTicketByTable(table.tableId());
                    if (ticket != null) occupied.add(table.tableId());
                }
                return new LoadResult(tables, occupied);
            }
        };

        task.setOnSucceeded(e -> javafx.application.Platform.runLater(() -> {
            LoadResult result = task.getValue();
            occupiedTableIds.clear();
            occupiedTableIds.addAll(result.occupied());

            selectedButton = null;
            selectedTable  = null;
            tablesContainer.getChildren().clear();

            List<TableDto> sorted = new ArrayList<>(result.tables());
            Collections.sort(sorted, Comparator.comparingInt(TableDto::number));
            for (TableDto table : sorted) {
                Button btn = createTableButton(table);
                tablesContainer.getChildren().add(btn);
                if (table.tableId().equals(targetTableId)) {
                    selectedButton = btn;
                    selectedTable  = table;
                    btn.getStyleClass().removeAll("table-btn", "table-btn-occupied");
                    btn.getStyleClass().add("table-btn-selected");
                    if (navbarController != null)    navbarController.setActiveTable(table);
                    if (productPanelController != null) productPanelController.onTableSelected(table);
                }
            }
        }));
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not load tables."));
        new Thread(task).start();
    }
}
