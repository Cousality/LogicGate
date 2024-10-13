module com.example.logicgate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.logicgate to javafx.fxml;
    exports com.example.logicgate;
}