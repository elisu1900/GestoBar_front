module com.elias.gestobar {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.elias.gestobar.controllers to javafx.fxml;

    exports com.elias.gestobar;
}