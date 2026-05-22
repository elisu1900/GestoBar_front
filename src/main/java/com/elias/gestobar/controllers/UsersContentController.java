package com.elias.gestobar.controllers;

import com.elias.gestobar.model.dto.UserDto;
import com.elias.gestobar.model.dto.UserRequestDto;
import com.elias.gestobar.service.UserApiService;
import com.elias.gestobar.util.AlertHelper;
import com.elias.gestobar.util.ApiException;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class UsersContentController {

    @FXML private VBox          newUserForm;
    @FXML private TextField     newUsername;
    @FXML private PasswordField newPassword;
    @FXML private ComboBox<String> newRole;
    @FXML private Label         newErrorLabel;
    @FXML private VBox          userListContainer;
    @FXML private TextField     newLastName;


    private final UserApiService userService = new UserApiService();

    @FXML
    public void initialize() {
        newRole.setItems(FXCollections.observableArrayList("ADMIN", "WAITER"));
        newRole.getSelectionModel().selectFirst();
        loadUsers();
    }

    private void loadUsers() {
        Task<List<UserDto>> task = new Task<List<UserDto>>() {
            @Override
            protected List<UserDto> call() throws Exception {
                return userService.getAllUsers();
            }
        };
        task.setOnSucceeded(e -> renderUsers(task.getValue()));
        task.setOnFailed(e -> AlertHelper.showError("Error", "Could not load users."));
        new Thread(task).start();
    }

    private void renderUsers(List<UserDto> users) {
        userListContainer.getChildren().clear();
        for (UserDto user : users) {
            userListContainer.getChildren().add(buildUserRow(user));
        }
    }

    private HBox buildUserRow(UserDto user) {
        Label avatar = new Label(getInitial(user));
        avatar.getStyleClass().add(isAdmin(user) ? "user-avatar-admin" : "user-avatar-waiter");

        Label nameLabel = new Label(user.name());
        nameLabel.getStyleClass().add("product-row-label");

        HBox nameBox = new HBox(10, avatar, nameLabel);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setPrefWidth(300);

        Label roleLabel = new Label(getRoleDisplay(user));
        roleLabel.getStyleClass().add(isAdmin(user) ? "badge-info" : "badge-neutral");

        HBox roleBox = new HBox(roleLabel);
        roleBox.setPrefWidth(200);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> handleDelete(user));

        HBox row = new HBox(0, nameBox, roleBox, spacer, deleteBtn);
        row.getStyleClass().add("product-row");
        return row;
    }

    private void handleDelete(UserDto user) {
        boolean confirmed = AlertHelper.showConfirm(
                "Delete user",
                "Delete user \"" + user.name() + "\"?");

        if (!confirmed) return;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                userService.deleteUser(user.userId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadUsers());
        task.setOnFailed(e -> {
            String msg = task.getException() instanceof ApiException
                    ? task.getException().getMessage() : "Could not delete user.";
            AlertHelper.showError("Error", msg);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleAddUser() {
        newUsername.clear();
        newPassword.clear();
        newRole.getSelectionModel().selectFirst();
        newErrorLabel.setVisible(false);
        newErrorLabel.setManaged(false);
        newUserForm.setVisible(true);
        newUserForm.setManaged(true);
        newUsername.requestFocus();
        newLastName.clear();

    }

    @FXML
    private void handleCancelNew() {
        newUserForm.setVisible(false);
        newUserForm.setManaged(false);
    }

    @FXML
    private void handleConfirmNew() {
        String username = newUsername.getText().trim();
        String lastName = newLastName.getText().trim();
        String password = newPassword.getText();
        String role     = newRole.getValue();

        if (!isValid(username,lastName, password, role)) return;

        UserRequestDto request = new UserRequestDto(
                username,
                lastName,
                password,
                role,
                true
        );

        Task<UserDto> task = new Task<UserDto>() {
            @Override
            protected UserDto call() throws Exception {
                return userService.createUser(request);
            }
        };

        task.setOnSucceeded(e -> {
            newUserForm.setVisible(false);
            newUserForm.setManaged(false);
            loadUsers();
        });
        task.setOnFailed(e -> {
            String msg = task.getException() instanceof ApiException
                    ? task.getException().getMessage() : "Error creating user.";
            AlertHelper.showError("Error creating user", msg);
        });
        new Thread(task).start();
    }

    private boolean isValid(String username,String lastName, String password, String role) {
        if (username.isEmpty()) {
            showError("Username cannot be empty.");
            return false;
        }
        if (lastName.isEmpty()) {
            showError("Last name cannot be empty.");
            return false;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return false;
        }
        if (role == null || role.isEmpty()) {
            showError("Please select a role.");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        newErrorLabel.setText(msg);
        newErrorLabel.setVisible(true);
        newErrorLabel.setManaged(true);
    }

    private boolean isAdmin(UserDto user) {
        return "ADMIN".equalsIgnoreCase(user.role());
    }

    private String getRoleDisplay(UserDto user) {
        return isAdmin(user) ? "Administrator" : "Waiter / Staff";
    }

    private String getInitial(UserDto user) {
        if (user.name() != null && !user.name().isEmpty()) {
            return String.valueOf(user.name().charAt(0)).toUpperCase();
        }
        return "?";
    }
}
