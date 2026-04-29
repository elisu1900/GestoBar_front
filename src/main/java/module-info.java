module com.elias.gestobar {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.elias.gestobar to javafx.fxml;
    exports com.elias.gestobar;
}