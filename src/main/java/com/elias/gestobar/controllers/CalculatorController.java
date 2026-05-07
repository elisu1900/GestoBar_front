package com.elias.gestobar.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CalculatorController {

    @FXML private Label display;

    private String  currentInput = "";
    private String  operator     = "";
    private double  firstOperand = 0;
    private boolean newInput     = false;

    @FXML
    private void handleDigit(ActionEvent e) {
        String digit = ((Button) e.getSource()).getText();
        if (newInput) { currentInput = ""; newInput = false; }
        currentInput += digit;
        display.setText(currentInput);
    }

    @FXML
    private void handleDot(ActionEvent e) {
        if (newInput) { currentInput = "0"; newInput = false; }
        if (!currentInput.contains(".")) {
            currentInput = currentInput.isEmpty() ? "0." : currentInput + ".";
            display.setText(currentInput);
        }
    }

    @FXML
    private void handleOp(ActionEvent e) {
        operator     = ((Button) e.getSource()).getText();
        firstOperand = currentInput.isEmpty() ? 0 : Double.parseDouble(currentInput);
        newInput     = true;
    }

    @FXML
    private void handleEquals(ActionEvent e) {
        if (operator.isEmpty() || currentInput.isEmpty()) return;
        double second = Double.parseDouble(currentInput);
        double result = switch (operator) {
            case "+" -> firstOperand + second;
            case "-" -> firstOperand - second;
            case "*" -> firstOperand * second;
            case "/" -> second != 0 ? firstOperand / second : 0;
            default  -> second;
        };
        display.setText(formatResult(result));
        currentInput = display.getText();
        operator     = "";
        newInput     = true;
    }

    @FXML
    private void handleClear(ActionEvent e) {
        currentInput = "";
        operator     = "";
        firstOperand = 0;
        newInput     = false;
        display.setText("0");
    }

    private String formatResult(double val) {
        return val == (long) val
                ? String.valueOf((long) val)
                : String.format("%.2f", val);
    }
}
