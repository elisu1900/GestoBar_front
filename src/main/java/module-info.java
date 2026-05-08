module GestoBar.main {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    exports com.elias.gestobar to javafx.graphics, javafx.fxml;
    opens com.elias.gestobar to javafx.fxml;
    opens com.elias.gestobar.controllers to javafx.fxml;
    opens com.elias.gestobar.model.dto to com.fasterxml.jackson.databind;
    opens com.elias.gestobar.model.enums to com.fasterxml.jackson.databind;
}