package com.elias.gestobar.controllers;

import com.elias.gestobar.model.dto.DailyBalanceDto;
import com.elias.gestobar.model.dto.ProductBalanceDto;
import com.elias.gestobar.service.BalanceApiService;
import com.elias.gestobar.util.AlertHelper;
import com.elias.gestobar.util.ApiException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

public class BalanceContentController {

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalCostsLabel;
    @FXML private Label realProfitLabel;

    @FXML private VBox  breakdownContainer;

    @FXML private HBox  totalsRow;
    @FXML private Label totalQtyLabel;
    @FXML private Label totalRevLabel;
    @FXML private Label totalCostRowLabel;
    @FXML private Label totalProfitLabel;

    @FXML private HBox confirmBox;

    private final BalanceApiService balanceService = new BalanceApiService();

    @FXML
    public void initialize() {
        loadBalance();
    }

    private void loadBalance() {
        Task<DailyBalanceDto> task = new Task<DailyBalanceDto>() {
            @Override
            protected DailyBalanceDto call() throws Exception {
                return balanceService.getDailyBalance();
            }
        };

        task.setOnSucceeded(e -> renderBalance(task.getValue()));
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not load daily balance."));
        new Thread(task).start();
    }

    private void renderBalance(DailyBalanceDto balance) {
        totalRevenueLabel.setText(formatEuro(balance.totalRevenue()));
        totalCostsLabel.setText(formatEuro(balance.totalCosts()));
        realProfitLabel.setText(formatEuro(balance.realProfit()));

        breakdownContainer.getChildren().clear();

        List<ProductBalanceDto> breakdown = balance.breakdown();
        if (breakdown == null || breakdown.isEmpty()) {
            Label empty = new Label("No sales data for today.");
            empty.setStyle("-fx-text-fill: #6B7280; -fx-font-family: 'Segoe UI'; " +
                           "-fx-font-size: 13px; -fx-padding: 16;");
            breakdownContainer.getChildren().add(empty);
            totalsRow.setVisible(false);
            totalsRow.setManaged(false);
            return;
        }

        for (ProductBalanceDto item : breakdown) {
            breakdownContainer.getChildren().add(buildBreakdownRow(item));
        }

        int totalQty = breakdown.stream()
                .mapToInt(ProductBalanceDto::qtySold).sum();
        BigDecimal totalRev = breakdown.stream()
                .map(ProductBalanceDto::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = breakdown.stream()
                .map(ProductBalanceDto::cost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = breakdown.stream()
                .map(ProductBalanceDto::profit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalQtyLabel.setText(String.valueOf(totalQty));
        totalRevLabel.setText(formatEuro(totalRev));
        totalCostRowLabel.setText(formatEuro(totalCost));
        totalProfitLabel.setText(formatEuro(totalProfit));

        totalsRow.setVisible(true);
        totalsRow.setManaged(true);
    }

    private HBox buildBreakdownRow(ProductBalanceDto item) {
        Label nameLabel = new Label(item.productName());
        nameLabel.getStyleClass().add("product-row-label");
        nameLabel.setPrefWidth(280);

        Label qtyLabel = new Label(String.valueOf(item.qtySold()));
        qtyLabel.getStyleClass().add("product-row-label");
        qtyLabel.setPrefWidth(100);

        Label revLabel = new Label(formatEuro(item.revenue()));
        revLabel.getStyleClass().add("product-row-label");
        revLabel.setPrefWidth(120);

        Label costLabel = new Label(formatEuro(item.cost()));
        costLabel.getStyleClass().add("balance-cost-value");
        costLabel.setPrefWidth(120);

        Label profitLabel = new Label(formatEuro(item.profit()));
        profitLabel.getStyleClass().add("balance-profit-value");
        profitLabel.setPrefWidth(120);

        HBox row = new HBox(0, nameLabel, qtyLabel, revLabel, costLabel, profitLabel);
        row.getStyleClass().add("product-row");
        return row;
    }

    @FXML
    private void handleResetDay() {
        confirmBox.setVisible(true);
        confirmBox.setManaged(true);
    }

    @FXML
    private void handleCancelReset() {
        confirmBox.setVisible(false);
        confirmBox.setManaged(false);
    }

    @FXML
    private void handleConfirmReset() {
        confirmBox.setVisible(false);
        confirmBox.setManaged(false);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                balanceService.resetDay();
                return null;
            }
        };

        task.setOnSucceeded(e -> loadBalance());
        task.setOnFailed(e -> {
            String msg = task.getException() instanceof ApiException
                    ? task.getException().getMessage() : "Error resetting the day.";
            AlertHelper.showError("Error", msg);
        });
        new Thread(task).start();
    }


    private String formatEuro(BigDecimal value) {
        if (value == null) return "0.00 €";
        return String.format("%.2f €", value);
    }
}
