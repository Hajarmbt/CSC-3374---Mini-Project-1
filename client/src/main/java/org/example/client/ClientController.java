package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable, MessageListener {

    @FXML
    private ScrollPane sp_main;
    @FXML
    private VBox vbox_messages;
    @FXML
    private TextField tf_message;
    @FXML
    private Button button_send;
    @FXML
    private Label statusLabel;
    @FXML
    private Circle statusCircle;
    @FXML
    private Label connectionInfoLabel;

    private Client client;
    private boolean readOnly = false;
    private String username;
    private String serverIP;
    private int serverPort;

    public void setConnectionDetails(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        if (connectionInfoLabel != null) {
            connectionInfoLabel.setText(serverIP + ":" + serverPort);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize status
        updateStatus("CONNECTING", Color.GRAY);

    }

    public void startClientConnection() {
        try {
            client = new Client(new Socket(serverIP, serverPort), this);

            // Show username dialog
            showUsernameDialog();

            // Start listening for messages
            client.startListening();

            updateStatus("ONLINE", Color.GREEN);

        } catch (Exception e) {
            updateStatus("CONNECTION FAILED", Color.RED);
            showErrorDialog("Connection Error",
                    "Could not connect to server at " + serverIP + ":" + serverPort +
                            "\nPlease make sure the server is running.");
            e.printStackTrace();
        }

        // Auto-scroll to bottom when new messages arrive
        vbox_messages.heightProperty().addListener((obs, oldVal, newVal) ->
                sp_main.setVvalue((Double) newVal));

        // Send button action
        button_send.setOnAction(event -> sendMessage());

        // ENTER key support
        tf_message.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });
    }

    private void showUsernameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Username");
        dialog.setHeaderText("Enter your username");
        dialog.setContentText("Username (leave empty for read-only mode):");

        Optional<String> result = dialog.showAndWait();

        username = result.map(String::trim).orElse("");

        if (username.isEmpty()) {
            readOnly = true;
            username = "READ_ONLY";
            tf_message.setDisable(true);
            button_send.setDisable(true);
            updateStatus("READ-ONLY MODE", Color.ORANGE);
        } else {
            updateStatus("ONLINE", Color.GREEN);
        }

        // Send username as first message (protocol)
        if (client != null) {
            client.sendMessageToServer(username);
        }
    }

    // ===== SEND MESSAGE =====
    private void sendMessage() {
        if (readOnly) {
            showAlert("Read-Only Mode", "You cannot send messages in read-only mode.");
            tf_message.clear();
            return;
        }

        String message = tf_message.getText().trim();

        if (!message.isEmpty()) {

            // Handle special commands
            if (message.equalsIgnoreCase("allUsers")) {
                client.sendMessageToServer(message);
                tf_message.clear();
                return;
            }

            // Handle disconnect commands
            if (message.equalsIgnoreCase("bye") || message.equalsIgnoreCase("end")) {
                // Show own message
                addOwnMessage(message);

                // Notify server
                client.sendMessageToServer(message);

                // Give time for message to send
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Cleanup and exit
                cleanup();
                Platform.exit();
                return;
            }

            // Regular message
            addOwnMessage(message);
            client.sendMessageToServer(message);
            tf_message.clear();
        }
    }

    // ===== RECEIVE MESSAGE FROM SERVER =====
    @Override
    public void onMessageReceived(String message) {
        Platform.runLater(() -> {
            // Check if it's a special server message
            if (message.startsWith("[SERVER]") || message.contains("active users:")) {
                addServerMessage(message, true); // Special styling for server messages
            } else {
                addServerMessage(message, false);
            }
        });
    }

    // ===== UI HELPERS =====
    private void addOwnMessage(String message) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(5, 10, 5, 10));

        Text text = new Text(message);
        text.setFill(Color.WHITE);

        TextFlow flow = new TextFlow(text);
        flow.setStyle(
                "-fx-background-color: rgb(12,125,242);" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 8 15 8 15;"
        );

        hbox.getChildren().add(flow);
        vbox_messages.getChildren().add(hbox);
    }

    private void addServerMessage(String message, boolean isSystemMessage) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5, 10, 5, 10));

        Text text = new Text(message);

        TextFlow flow = new TextFlow(text);

        if (isSystemMessage) {
            // System message styling
            flow.setStyle(
                    "-fx-background-color: rgb(255, 228, 181);" +
                            "-fx-background-radius: 20px;" +
                            "-fx-padding: 8 15 8 15;"
            );
            text.setFill(Color.DARKBLUE);
        } else {
            // Regular message styling
            flow.setStyle(
                    "-fx-background-color: rgb(233,233,235);" +
                            "-fx-background-radius: 20px;" +
                            "-fx-padding: 8 15 8 15;"
            );
            text.setFill(Color.BLACK);
        }

        hbox.getChildren().add(flow);
        vbox_messages.getChildren().add(hbox);
    }

    private void updateStatus(String status, Color color) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(status);
            }
            if (statusCircle != null) {
                statusCircle.setFill(color);
            }
        });
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showErrorDialog(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void cleanup() {
        if (client != null) {
            client.closeEverything();
        }
        updateStatus("OFFLINE", Color.RED);
    }
}