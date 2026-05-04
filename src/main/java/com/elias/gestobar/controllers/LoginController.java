package com.elias.gestobar.controllers;

import com.elias.gestobar.model.dto.UserSessionDto;
import com.elias.gestobar.service.AuthService;
import com.elias.gestobar.util.ApiException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.elias.gestobar.model.enums.Role;
import com.elias.gestobar.config.SessionManager;


public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Enter en username salta a password
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // Limpiar error al escribir
        usernameField.textProperty().addListener((o, old, val) -> hideError());
        passwordField.textProperty().addListener((o, old, val) -> hideError());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validación básica
        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor, rellena todos los campos.");
            return;
        }

        // Deshabilitar botón para evitar doble click
        setLoading(true);

        // Llamada en hilo separado para no bloquear la UI
        Task<UserSessionDto> task = new Task<>() {
            @Override
            protected UserSessionDto call() throws Exception {
                return authService.login(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            UserSessionDto user = task.getValue();

            // Guardar en sesión
            SessionManager.getInstance().login(user);

            // Navegar según rol
            if (user.role() == Role.ADMIN) {
                Router.goTo("dashboard-admin");
            } else {
                Router.goTo("dashboard-waiter");
            }

            setLoading(false);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError(ex instanceof ApiException
                    ? ex.getMessage()
                    : "Error de conexión con el servidor.");
            setLoading(false);
        });

        new Thread(task).start();
    }

    // --------------------------------------------------------
    //  Helpers de UI
    // --------------------------------------------------------
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Conectando..." : "→ Log In");
    }
}
