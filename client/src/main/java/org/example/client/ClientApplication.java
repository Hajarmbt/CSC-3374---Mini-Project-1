package org.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ClientApplication extends Application {

    private String serverIP;
    private int serverPort;

    @Override
    public void init() {
            List<String> args = getParameters().getRaw();

            // default values
            serverIP = "localhost";
            serverPort = 5000;

            if (!args.isEmpty()) {
                serverIP = args.get(0);
                serverPort = Integer.parseInt(args.get(1));

                System.out.println("Server IP: " + serverIP + ":" + serverPort);
            }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 450);

        // Pass connection details to controller
        ClientController controller = fxmlLoader.getController();
        controller.setConnectionDetails(serverIP, serverPort);
        controller.startClientConnection();

        stage.setTitle("Chat Client - " + serverIP + ":" + serverPort);
        stage.setScene(scene);
        stage.show();

        // Handle window close
        stage.setOnCloseRequest(event -> {
            controller.cleanup();
        });
    }
}