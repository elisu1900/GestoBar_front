package com.elias.gestobar.controllers;

import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.dto.ProductDto;
import com.elias.gestobar.model.dto.TableDto;
import com.elias.gestobar.model.dto.TicketDto;
import com.elias.gestobar.service.ProductApiService;
import com.elias.gestobar.service.TicketApiService;
import com.elias.gestobar.util.AlertHelper;
import com.elias.gestobar.util.ApiException;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProductPanelController {

    @FXML private FlowPane productsContainer;

    private final ProductApiService productService = new ProductApiService();
    private final TicketApiService  ticketService  = new TicketApiService();

    private OrderPanelController orderPanelController;

    private TableDto selectedTable  = null;
    private Long     activeTicketId = null;

    @FXML
    public void initialize() {
        loadProducts();
    }


    public void setOrderPanelController(OrderPanelController controller) {
        this.orderPanelController = controller;
    }


    public void onTableSelected(TableDto table) {
        this.selectedTable  = table;
        this.activeTicketId = null;
    }

    public void clearTable() {
        this.selectedTable  = null;
        this.activeTicketId = null;
    }


    private void loadProducts() {
        Task<List<ProductDto>> task = new Task<List<ProductDto>>() {
            @Override
            protected List<ProductDto> call() throws Exception {
                return productService.getActiveProducts();
            }
        };

        task.setOnSucceeded(e -> renderProducts(task.getValue()));
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not load products."));
        new Thread(task).start();
    }

    private void renderProducts(List<ProductDto> products) {
        productsContainer.getChildren().clear();
        if (products.isEmpty()) {
            Label hint = new Label("Add products to view them");
            hint.getStyleClass().add("empty-hint");
            productsContainer.getChildren().add(hint);
            return;
        }
        for (ProductDto product : products) {
            VBox cell = createProductCell(product);
            productsContainer.getChildren().add(cell);
        }
    }

    private VBox createProductCell(ProductDto product) {
        Button btn = new Button(product.name());
        btn.getStyleClass().add("product-btn");
        btn.setOnAction(e -> handleProductClick(product));

        Label price = new Label(String.format("%.2f €", product.sellPrice()));
        price.getStyleClass().add("product-price");

        VBox cell = new VBox(2, btn, price);
        cell.setAlignment(Pos.CENTER);
        return cell;
    }


    private void handleProductClick(ProductDto product) {
        if (selectedTable == null) {
            AlertHelper.showError("No table selected", "Please select a table first.");
            return;
        }

        Task<TicketDto> task = new Task<TicketDto>() {
            @Override
            protected TicketDto call() throws Exception {
                return addProductToTicket(product);
            }
        };

        task.setOnSucceeded(e -> {
            TicketDto ticket = task.getValue();
            if (orderPanelController != null) {
                orderPanelController.renderTicket(ticket);
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = ex instanceof ApiException ? ex.getMessage() : "Error adding product.";
            AlertHelper.showError("Error", msg);
        });

        new Thread(task).start();
    }

    private TicketDto addProductToTicket(ProductDto product) throws ApiException {
        if (activeTicketId == null) {
            TicketDto newTicket = ticketService.createTicket(selectedTable.tableId());
            activeTicketId = newTicket.ticketId().longValue();
            SessionManager.getInstance().setActiveTicketId(activeTicketId);
        }
        ticketService.addProduct(activeTicketId.intValue(), product.productId(), 1);
        return ticketService.getTicket(activeTicketId.intValue());
    }
    public void setActiveTicketId(Integer ticketId) {
        this.activeTicketId = ticketId != null ? ticketId.longValue() : null;
    }
}