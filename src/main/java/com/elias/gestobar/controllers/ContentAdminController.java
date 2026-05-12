package com.elias.gestobar.controllers;

import com.elias.gestobar.model.dto.ProductDto;
import com.elias.gestobar.model.dto.ProductRequestDto;
import com.elias.gestobar.service.ProductApiService;
import com.elias.gestobar.util.AlertHelper;
import com.elias.gestobar.util.ApiException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

public class ContentAdminController {

    @FXML private VBox      newProductForm;
    @FXML private TextField newName;
    @FXML private TextField newSellPrice;
    @FXML private TextField newCostPrice;
    @FXML private Label     newErrorLabel;
    @FXML private VBox      productListContainer;

    private final ProductApiService productService = new ProductApiService();

    @FXML
    public void initialize() {
        loadProducts();
    }


    private void loadProducts() {
        Task<List<ProductDto>> task = new Task<List<ProductDto>>() {
            @Override
            protected List<ProductDto> call() throws Exception {
                return productService.getAllProducts();
            }
        };
        task.setOnSucceeded(e -> renderProducts(task.getValue()));
        task.setOnFailed(e -> AlertHelper.showError("Error", "No se pudieron cargar los productos."));
        new Thread(task).start();
    }

    private void renderProducts(List<ProductDto> products) {
        productListContainer.getChildren().clear();
        for (ProductDto p : products) {
            productListContainer.getChildren().add(buildViewRow(p));
        }
    }


    private HBox buildViewRow(ProductDto product) {
        Label nameLabel = new Label(product.name());
        nameLabel.getStyleClass().add("product-row-label");
        nameLabel.setPrefWidth(300);

        Label sellLabel = new Label(String.format("%.2f €", product.sellPrice()));
        sellLabel.getStyleClass().add("product-row-label");
        sellLabel.setPrefWidth(130);

        String costText = product.costPrice() != null
                ? String.format("%.2f €", product.costPrice()) : "—";
        Label costLabel = new Label(costText);
        costLabel.getStyleClass().add("product-row-label");
        costLabel.setPrefWidth(130);

        BigDecimal margin = product.costPrice() != null
                ? product.sellPrice().subtract(product.costPrice())
                : BigDecimal.ZERO;
        Label marginLabel = new Label(String.format("%.2f €", margin));
        marginLabel.getStyleClass().add(
                margin.compareTo(BigDecimal.ZERO) > 0 ? "margin-positive" : "margin-negative");
        marginLabel.setPrefWidth(130);

        Button editBtn = new Button("✎");
        editBtn.getStyleClass().add("btn-edit");

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("btn-delete");

        HBox actions = new HBox(4, editBtn, deleteBtn);

        HBox row = new HBox(0, nameLabel, sellLabel, costLabel, marginLabel, actions);
        row.getStyleClass().add("product-row");

        editBtn.setOnAction(e -> switchToEditMode(row, product));
        deleteBtn.setOnAction(e -> handleDelete(product));

        return row;
    }


    private void switchToEditMode(HBox row, ProductDto product) {
        TextField nameField = buildEditField(product.name(), 280);
        TextField sellField = buildEditField(product.sellPrice().toPlainString(), 100);
        TextField costField = buildEditField(
                product.costPrice() != null ? product.costPrice().toPlainString() : "", 100);

        Button confirmBtn = new Button("✓");
        confirmBtn.getStyleClass().add("btn-confirm-sm");

        Button cancelBtn = new Button("✕");
        cancelBtn.getStyleClass().add("btn-cancel-sm");

        HBox actions = new HBox(4, confirmBtn, cancelBtn);
        Region spacer = new Region();

        row.getChildren().setAll(nameField, sellField, costField, spacer, actions);
        row.getStyleClass().setAll("product-row-editing");

        confirmBtn.setOnAction(e ->
                handleConfirmEdit(row, product, nameField, sellField, costField));
        cancelBtn.setOnAction(e -> {
            replaceRow(row, buildViewRow(product));
        });
    }

    private TextField buildEditField(String value, double width) {
        TextField field = new TextField(value);
        field.setPrefWidth(width);
        field.getStyleClass().add("input-field");
        return field;
    }


    private void handleConfirmEdit(HBox row, ProductDto product,
                                   TextField nameField,
                                   TextField sellField,
                                   TextField costField) {
        String name = nameField.getText().trim();
        String sell = sellField.getText().trim();
        String cost = costField.getText().trim();

        if (!isValid(name, sell, cost)) {
            AlertHelper.showError("Error", "Revisa los campos. Nombre obligatorio y precios mayores que 0.");
            return;
        }

        ProductRequestDto request = new ProductRequestDto(
                name,
                new BigDecimal(sell),
                new BigDecimal(cost),
                product.isActive()
        );

        Task<ProductDto> task = new Task<ProductDto>() {
            @Override
            protected ProductDto call() throws Exception {
                return productService.updateProduct(product.productId(), request);
            }
        };

        task.setOnSucceeded(e -> replaceRow(row, buildViewRow(task.getValue())));
        task.setOnFailed(e -> {
            String msg = task.getException() instanceof ApiException
                    ? task.getException().getMessage() : "Error al actualizar.";
            AlertHelper.showError("Error", msg);
        });
        new Thread(task).start();
    }


    private void handleDelete(ProductDto product) {
        boolean confirmed = AlertHelper.showConfirm(
                "Eliminar producto",
                "¿Eliminar \"" + product.name() + "\"?");

        if (!confirmed) return;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                productService.deleteProduct(product.productId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadProducts());
        task.setOnFailed(e -> AlertHelper.showError("Error", "No se pudo eliminar el producto."));
        new Thread(task).start();
    }

    @FXML
    private void handleAddProduct() {
        newName.clear();
        newSellPrice.clear();
        newCostPrice.clear();
        newErrorLabel.setVisible(false);
        newErrorLabel.setManaged(false);
        newProductForm.setVisible(true);
        newProductForm.setManaged(true);
        newName.requestFocus();
    }

    @FXML
    private void handleCancelNew() {
        newProductForm.setVisible(false);
        newProductForm.setManaged(false);
    }

    @FXML
    private void handleConfirmNew() {
        String name = newName.getText().trim();
        String sell = newSellPrice.getText().trim();
        String cost = newCostPrice.getText().trim();

        if (!isValid(name, sell, cost)) {
            newErrorLabel.setText("Revisa los campos. Nombre obligatorio y precios mayores que 0.");
            newErrorLabel.setVisible(true);
            newErrorLabel.setManaged(true);
            return;
        }

        ProductRequestDto request = new ProductRequestDto(
                name, new BigDecimal(sell), new BigDecimal(cost), true);

        Task<ProductDto> task = new Task<ProductDto>() {
            @Override
            protected ProductDto call() throws Exception {
                return productService.createProduct(request);
            }
        };

        task.setOnSucceeded(e -> {
            newProductForm.setVisible(false);
            newProductForm.setManaged(false);
            loadProducts();
        });
        task.setOnFailed(e -> {
            String msg = task.getException() instanceof ApiException
                    ? task.getException().getMessage() : "Error al crear el producto.";
            newErrorLabel.setText(msg);
            newErrorLabel.setVisible(true);
            newErrorLabel.setManaged(true);
        });
        new Thread(task).start();
    }


    private void replaceRow(HBox oldRow, HBox newRow) {
        int index = productListContainer.getChildren().indexOf(oldRow);
        if (index >= 0) {
            productListContainer.getChildren().set(index, newRow);
        }
    }

    private boolean isValid(String name, String sell, String cost) {
        if (name.isEmpty()) return false;
        try {
            BigDecimal s = new BigDecimal(sell);
            BigDecimal c = new BigDecimal(cost);
            return s.compareTo(BigDecimal.ZERO) > 0 && c.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}