package com.elias.gestobar.controllers;

import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.enums.Role;
import com.elias.gestobar.util.AlertHelper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Router {

    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void goTo(String fxmlName) {
        goTo(fxmlName, null);
    }

    public static void goTo(String fxmlName, Role requiredRole) {
        if (requiredRole != null
                && SessionManager.getInstance().getRole() != requiredRole) {
            AlertHelper.showError("Access denied",
                    "You don't have permission to access this screen.");
            return;
        }

        try {
            var url = Router.class.getResource("/com/elias/gestobar/view/" + fxmlName + ".fxml");
            if (url == null) throw new IOException("FXML not found: " + fxmlName);

            FXMLLoader loader = new FXMLLoader(url);
            loader.setClassLoader(Router.class.getClassLoader());
            Parent root = loader.load();
            Scene scene = primaryStage.getScene();

            if (scene == null) {
                primaryStage.setScene(new Scene(root));
            } else {
                scene.setRoot(root);
            }

            primaryStage.show();

        } catch (Exception e) {
            try (var fw = new java.io.FileWriter(
                    System.getProperty("user.home") + "/gestobar-error.log", true)) {
                fw.write("=== Error loading: " + fxmlName + " ===\n");
                fw.write(e + "\n");
                for (var el : e.getStackTrace()) fw.write("  at " + el + "\n");
                Throwable cause = e.getCause();
                while (cause != null) {
                    fw.write("Caused by: " + cause + "\n");
                    for (var el : cause.getStackTrace()) fw.write("  at " + el + "\n");
                    cause = cause.getCause();
                }
                fw.write("\n");
            } catch (Exception ignored) {}
            AlertHelper.showError("Navigation error",
                    "Failed to load screen: " + fxmlName + "\n" + e.getMessage());
        }
    }

    //Shortcuts
    public static void goToSplash()          { goTo("splash"); }
    public static void goToLogin()           { goTo("login"); }
    public static void goToMain()            { goTo("main"); }
    public static void goToAdmin()           { goTo("administration", Role.ADMIN); }
    public static void goToDailyBalance()    { goTo("daily-balance", Role.ADMIN); }
    public static void goToUsers()           { goTo("users", Role.ADMIN); }
}
