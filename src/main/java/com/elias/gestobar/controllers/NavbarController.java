package com.elias.gestobar.controllers;

import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.dto.TableDto;
import com.elias.gestobar.service.AuthService;
import com.elias.gestobar.util.ApiException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class NavbarController {

    @FXML private Label  activeTableLabel;
    @FXML private Label  userLabel;
    @FXML private Button btnAdministration;
    @FXML private Button btnDailyBalance;
    @FXML private Button btnUsers;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        userLabel.setText(SessionManager.getInstance().getFullName());

        boolean isAdmin = SessionManager.getInstance().isAdmin();
        btnAdministration.setVisible(isAdmin);
        btnAdministration.setManaged(isAdmin);
        btnDailyBalance.setVisible(isAdmin);
        btnDailyBalance.setManaged(isAdmin);
        btnUsers.setVisible(isAdmin);
        btnUsers.setManaged(isAdmin);
    }

    public void setActiveTable(TableDto table) {
        activeTableLabel.setText("Table " + table.number());
        activeTableLabel.setVisible(true);
        activeTableLabel.setManaged(true);
    }

    public void clearActiveTable() {
        activeTableLabel.setVisible(false);
        activeTableLabel.setManaged(false);
    }

    @FXML
    private void handleAdministration() {
        Router.goTo("administration");
    }

    @FXML
    private void handleDailyBalance() {
        Router.goTo("daily-balance");
    }

    @FXML
    private void handleUsers() {
        Router.goTo("users");
    }

    @FXML
    private void handleLogout() {
        try {
            authService.logout();
        } catch (ApiException ignored) {}
        Router.goToLogin();
    }

    public void handleMain() {
        Router.goTo("main");
    }
}