package org.example.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerApplication extends Application {

    public int port;

    @Override
    public void init() {
        // Read port from system property set by ServerLauncher
        String portStr = System.getProperty("server.port", "3333");
        port = Integer.parseInt(portStr);

        System.out.println("ServerApplication will use port: " + port);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 550);

        // Pass port to controller
        ServerController controller = fxmlLoader.getController();
        controller.setServerPort(port);

        stage.setTitle("Chat Server - Port: " + port);
        stage.setScene(scene);
        stage.show();

        // Handle window close
        stage.setOnCloseRequest(event -> {
            controller.stopServer();
        });
    }
}