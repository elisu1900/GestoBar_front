package com.elias.gestobar.controllers;

import com.elias.gestobar.model.dto.TableDto;
import com.elias.gestobar.model.dto.TableRequestDto;
import com.elias.gestobar.model.dto.TicketDto;
import com.elias.gestobar.service.TableApiService;
import com.elias.gestobar.service.TicketApiService;
import com.elias.gestobar.util.AlertHelper;
import com.elias.gestobar.util.ApiException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class ContentAdminTablesController {

    @FXML private VBox      newTableForm;
    @FXML private TextField newNumber;
    @FXML private TextField newCapacity;
    @FXML private Label     newErrorLabel;
    @FXML private VBox      tableListContainer;

    private final TableApiService  tableService  = new TableApiService();
    private final TicketApiService ticketService = new TicketApiService();

    @FXML
    public void initialize() {
        loadTables();
    }

    private void loadTables() {
        Task<List<TableDto>> task = new Task<>() {
            @Override
            protected List<TableDto> call() throws Exception {
                return tableService.getAllTables();
            }
        };
        task.setOnSucceeded(e -> renderTables(task.getValue()));
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not load tables."));
        new Thread(task).start();
    }

    private void renderTables(List<TableDto> tables) {
        tableListContainer.getChildren().clear();
        for (TableDto table : tables) {
            tableListContainer.getChildren().add(buildTableRow(table));
        }
    }

    private HBox buildTableRow(TableDto table) {
        Label numberLabel = new Label("Table " + table.number());
        numberLabel.getStyleClass().add("product-row-label");
        numberLabel.setPrefWidth(200);

        Label capacityLabel = new Label(table.capacity() + " seats");
        capacityLabel.getStyleClass().add("product-row-label");
        capacityLabel.setPrefWidth(200);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> handleDelete(table));

        HBox row = new HBox(0, numberLabel, capacityLabel, spacer, deleteBtn);
        row.getStyleClass().add("product-row");
        return row;
    }

    private void handleDelete(TableDto table) {
        Task<TicketDto> checkTask = new Task<>() {
            @Override
            protected TicketDto call() throws Exception {
                return ticketService.findOpenTicketByTable(table.tableId());
            }
        };
        checkTask.setOnSucceeded(e -> {
            TicketDto openTicket = checkTask.getValue();
            if (openTicket != null) {
                boolean confirmed = AlertHelper.showConfirm(
                        "Delete table",
                        "Table " + table.number() + " has an open ticket.\n\n" +
                        "Are you sure you want to delete it? The open ticket will be closed as cancelled.");
                if (!confirmed) return;
                doDeleteWithCancel(table, openTicket.ticketId());
            } else {
                boolean confirmed = AlertHelper.showConfirm(
                        "Delete table",
                        "Delete Table " + table.number() + "?");
                if (!confirmed) return;
                doDelete(table);
            }
        });
        checkTask.setOnFailed(e -> {
            String msg = checkTask.getException() instanceof ApiException
                    ? checkTask.getException().getMessage() : "Could not check table status.";
            AlertHelper.showError("Error", msg);
        });
        new Thread(checkTask).start();
    }

    private void doDeleteWithCancel(TableDto table, Integer ticketId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ticketService.cancelTicket(ticketId);
                tableService.deactivateTable(table.tableId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadTables());
        task.setOnFailed(e -> {
            String msg = task.getException() instanceof ApiException
                    ? task.getException().getMessage() : "Could not delete table.";
            AlertHelper.showError("Error", msg);
        });
        new Thread(task).start();
    }

    private void doDelete(TableDto table) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                tableService.deactivateTable(table.tableId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadTables());
        task.setOnFailed(e -> {
            String msg = task.getException() instanceof ApiException
                    ? task.getException().getMessage() : "Could not delete table.";
            AlertHelper.showError("Error", msg);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleAddTable() {
        newNumber.clear();
        newCapacity.clear();
        newErrorLabel.setVisible(false);
        newErrorLabel.setManaged(false);
        newTableForm.setVisible(true);
        newTableForm.setManaged(true);
        newNumber.requestFocus();
    }

    @FXML
    private void handleCancelNew() {
        newTableForm.setVisible(false);
        newTableForm.setManaged(false);
    }

    @FXML
    private void handleConfirmNew() {
        String numberStr  = newNumber.getText().trim();
        String capacityStr = newCapacity.getText().trim();

        if (!isValid(numberStr, capacityStr)) return;

        TableRequestDto request = new TableRequestDto(
                Integer.parseInt(numberStr),
                Integer.parseInt(capacityStr));

        Task<TableDto> task = new Task<>() {
            @Override
            protected TableDto call() throws Exception {
                return tableService.createTable(request);
            }
        };
        task.setOnSucceeded(e -> {
            newTableForm.setVisible(false);
            newTableForm.setManaged(false);
            loadTables();
        });
        task.setOnFailed(e -> {
            String msg = task.getException() instanceof ApiException
                    ? task.getException().getMessage() : "Error creating table.";
            AlertHelper.showError("Error creating table", msg);
        });
        new Thread(task).start();
    }

    private boolean isValid(String numberStr, String capacityStr) {
        try {
            int n = Integer.parseInt(numberStr);
            int c = Integer.parseInt(capacityStr);
            if (n < 1) { showError("Table number must be greater than 0."); return false; }
            if (c < 1) { showError("Capacity must be greater than 0."); return false; }
            return true;
        } catch (NumberFormatException e) {
            showError("Table number and capacity must be valid integers.");
            return false;
        }
    }

    private void showError(String msg) {
        newErrorLabel.setText(msg);
        newErrorLabel.setVisible(true);
        newErrorLabel.setManaged(true);
    }
}
