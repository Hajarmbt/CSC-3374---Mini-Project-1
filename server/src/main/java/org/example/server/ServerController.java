package org.example.server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.ListCell;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServerController implements Initializable {

    @FXML
    private ScrollPane sp_main;
    @FXML
    private VBox vbox_messages;
    @FXML
    private ListView<String> userListView;
    @FXML
    private Label portLabel;
    @FXML
    private Label userCountLabel;
    @FXML
    private Circle statusCircle;
    @FXML
    private Label statusLabel;
    @FXML
    private Button stopButton;
    @FXML
    private Button clearButton;

    private static VBox staticVBox;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private Map<String, String> userColors = new HashMap<>();
    private Random random = new Random();
    private Server server;
    private int serverPort = 3333;
    private Timer userListUpdateTimer;

    public void setServerPort(int port) {
        this.serverPort = port;
        if (portLabel != null) {
            portLabel.setText("Port: " + port);
        }
        try {
            server = new Server(new ServerSocket(serverPort), this);
            // Run server on background thread
            new Thread(server::startServer).start();

            log("🟢 Server started on port " + serverPort);
            updateStatus("RUNNING", Color.GREEN);

            // Start timer to update user list periodically
            startUserListUpdates();

        } catch (IOException e) {
            log("❌ Error starting server on port " + serverPort);
            log("❌ " + e.getMessage());
            updateStatus("ERROR", Color.RED);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        staticVBox = vbox_messages;

        // Initialize UI
        updateStatus("STARTING", Color.GRAY);
        portLabel.setText("Port: " + serverPort);

        // Auto-scroll to bottom when new messages arrive
        vbox_messages.heightProperty().addListener((obs, oldVal, newVal) ->
                sp_main.setVvalue((Double) newVal));

        // Button actions
        stopButton.setOnAction(e -> stopServer());
        clearButton.setOnAction(e -> clearLog());

        // Initial user list update
        updateUserList();
    }

    private void startUserListUpdates() {
        userListUpdateTimer = new Timer(true);
        userListUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateUserList();
            }
        }, 0, 1000); // Update every second
    }

    public void updateUserList() {
        Platform.runLater(() -> {
            userListView.getItems().clear();
            for (ClientHandler client : Server.clients) {
                String username = client.getUsername();
                userListView.getItems().add(username);

                // Assign random color if not already assigned
                userColors.computeIfAbsent(username, k ->
                        String.format("#%06x", random.nextInt(0xFFFFFF)));
            }

            // Apply random colors to user list
            userListView.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        String color = userColors.get(item);
                        // Make text readable based on background color
                        setStyle("-fx-background-color: " + color + "; " +
                                "-fx-text-fill: black; " +
                                "-fx-padding: 5 10 5 10; " +
                                "-fx-font-weight: bold;");
                    }
                }
            });

            // Update user count
            int count = Server.clients.size();
            userCountLabel.setText("Users: " + count);
        });
    }

    public static void log(String message) {
        Platform.runLater(() -> {
            String timestamp = sdf.format(new Date());
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(new Insets(5, 10, 5, 10));

            Text text = new Text("[" + timestamp + "] " + message);

            TextFlow flow = new TextFlow(text);

            // Different styling for different message types
            if (message.contains("🟢")) {
                flow.setStyle("-fx-background-color: #d4edda; -fx-background-radius: 15px; -fx-padding: 8 15 8 15;");
                text.setFill(Color.DARKGREEN);
            } else if (message.contains("🔴")) {
                flow.setStyle("-fx-background-color: #f8d7da; -fx-background-radius: 15px; -fx-padding: 8 15 8 15;");
                text.setFill(Color.DARKRED);
            } else if (message.contains("❌")) {
                flow.setStyle("-fx-background-color: #f8d7da; -fx-background-radius: 15px; -fx-padding: 8 15 8 15;");
                text.setFill(Color.RED);
            } else if (message.contains("📋")) {
                flow.setStyle("-fx-background-color: #cce5ff; -fx-background-radius: 15px; -fx-padding: 8 15 8 15;");
                text.setFill(Color.DARKBLUE);
            } else {
                flow.setStyle("-fx-background-color: #e2e3e5; -fx-background-radius: 15px; -fx-padding: 8 15 8 15;");
                text.setFill(Color.BLACK);
            }

            hbox.getChildren().add(flow);
            staticVBox.getChildren().add(hbox);
        });
    }

    private void updateStatus(String status, Color color) {
        Platform.runLater(() -> {
            statusLabel.setText(status);
            statusCircle.setFill(color);
        });
    }

    public void stopServer() {
        if (server != null) {
            server.closeServerSocket();
            if (userListUpdateTimer != null) {
                userListUpdateTimer.cancel();
            }
            log("🔴 Server stopped");
            updateStatus("STOPPED", Color.RED);
            stopButton.setDisable(true);
            updateUserList();
        }
        // Stop JavaFX application
        Platform.exit();
        System.exit(0);
    }

    private void clearLog() {
        vbox_messages.getChildren().clear();
        log("📋 Log cleared");
    }


}