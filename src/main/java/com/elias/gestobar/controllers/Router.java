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
            AlertHelper.showError("Acceso denegado",
                    "No tienes permiso para acceder a esta pantalla.");
            return;
        }

        try {
            var url = Router.class.getResource("/com/elias/gestobar/view/" + fxmlName + ".fxml");
            if (url == null) throw new IOException("FXML no encontrado: " + fxmlName);

            Parent root = FXMLLoader.load(url);
            Scene scene = primaryStage.getScene();

            if (scene == null) {
                primaryStage.setScene(new Scene(root));
            } else {
                scene.setRoot(root);
            }

            primaryStage.show();

        } catch (IOException e) {
            AlertHelper.showError("Error de navegación",
                    "No se pudo cargar la pantalla: " + fxmlName);
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
