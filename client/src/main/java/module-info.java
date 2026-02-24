module org.example.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.example.client to javafx.fxml;
    exports org.example.client;
}