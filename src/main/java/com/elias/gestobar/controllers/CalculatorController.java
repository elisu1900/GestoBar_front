package com.elias.gestobar.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;

public class CalculatorController {

    @FXML private Label display;

    private String  currentInput = "";
    private String  operator     = "";
    private double  firstOperand = 0;
    private boolean newInput     = false;

    @FXML
    private void initialize() {
        display.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
            }
        });
    }

    private void handleKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case NUMPAD0, DIGIT0 -> appendDigit("0");
            case NUMPAD1, DIGIT1 -> appendDigit("1");
            case NUMPAD2, DIGIT2 -> appendDigit("2");
            case NUMPAD3, DIGIT3 -> appendDigit("3");
            case NUMPAD4, DIGIT4 -> appendDigit("4");
            case NUMPAD5, DIGIT5 -> appendDigit("5");
            case NUMPAD6, DIGIT6 -> appendDigit("6");
            case NUMPAD7, DIGIT7 -> appendDigit("7");
            case NUMPAD8, DIGIT8 -> appendDigit("8");
            case NUMPAD9, DIGIT9 -> appendDigit("9");
            case ADD              -> setOperator("+");
            case SUBTRACT, MINUS  -> setOperator("-");
            case MULTIPLY         -> setOperator("*");
            case DIVIDE           -> setOperator("/");
            case DECIMAL, PERIOD  -> addDot();
            case ENTER, EQUALS    -> calculate();
            case BACK_SPACE       -> backspace();
            case ESCAPE           -> clear();
            default -> {}
        }
    }

    @FXML
    private void handleDigit(ActionEvent e) {
        appendDigit(((Button) e.getSource()).getText());
    }

    @FXML
    private void handleDot(ActionEvent e) {
        addDot();
    }

    @FXML
    private void handleOp(ActionEvent e) {
        setOperator(((Button) e.getSource()).getText());
    }

    @FXML
    private void handleEquals(ActionEvent e) {
        calculate();
    }

    @FXML
    private void handleClear(ActionEvent e) {
        clear();
    }

    private void appendDigit(String digit) {
        if (newInput) { currentInput = ""; newInput = false; }
        currentInput += digit;
        display.setText(currentInput);
    }

    private void addDot() {
        if (newInput) { currentInput = "0"; newInput = false; }
        if (!currentInput.contains(".")) {
            currentInput = currentInput.isEmpty() ? "0." : currentInput + ".";
            display.setText(currentInput);
        }
    }

    private void setOperator(String op) {
        operator     = op;
        firstOperand = currentInput.isEmpty() ? 0 : Double.parseDouble(currentInput);
        newInput     = true;
    }

    private void calculate() {
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

    private void backspace() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            display.setText(currentInput.isEmpty() ? "0" : currentInput);
        }
    }

    private void clear() {
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
