module com.elias.gestobar {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;

    opens com.elias.gestobar.controllers to javafx.fxml;

    exports com.elias.gestobar;
}