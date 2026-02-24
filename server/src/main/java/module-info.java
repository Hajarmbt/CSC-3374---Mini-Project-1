module org.example.server {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.example.server to javafx.fxml;
    exports org.example.server;
}