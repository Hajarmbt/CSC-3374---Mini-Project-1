package org.example.server;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;
    private boolean readOnly = false;
    private ServerController controller;

    public ClientHandler(Socket socket, ServerController controller) {
        try {
            this.socket = socket;
            this.controller = controller;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Read username (first message from client)
            this.username = reader.readLine();

            // Check if username is valid
            if (username == null || username.trim().isEmpty() || username.equals("READ_ONLY")) {
                readOnly = true;
                username = "READ_ONLY_" + socket.getPort();
                sendMessage("[SERVER] You are in READ-ONLY MODE. You cannot send messages.");
                ServerController.log("👤 Read-only client connected: " + username);
            } else {
                ServerController.log("👤 User joined: " + username);
            }

            // Broadcast join message to all clients
            broadcast("🟢 " + username + " joined the chat");

        } catch (IOException e) {
            closeEverything();
        }
    }

    @Override
    public void run() {
        String message;

        try {
            while ((message = reader.readLine()) != null) {

                // Handle read-only mode
                if (readOnly) {
                    sendMessage("[SERVER] You cannot send messages in READ-ONLY mode.");
                    continue;
                }

                // Handle disconnect commands
                if (message.equalsIgnoreCase("bye") || message.equalsIgnoreCase("end")) {
                    broadcast("🔴 " + username + " left the chat");
                    ServerController.log("🔴 User disconnected: " + username);
                    break;
                }

                // Handle allUsers command
                if (message.equalsIgnoreCase("allUsers")) {
                    sendActiveUsers();
                } else {
                    // Regular message
                    String formatted = "[" + LocalTime.now().withNano(0) + "] " + username + ": " + message;
                    broadcast(formatted);
                    ServerController.log("💬 " + username + ": " + message);
                }
            }
        } catch (IOException e) {
            ServerController.log("❌ Connection error with " + username);
        } finally {
            closeEverything();
        }
    }

    private void sendActiveUsers() {
        try {
            writer.write("📋 Active users:");
            writer.newLine();
            for (ClientHandler client : Server.clients) {
                writer.write("   • " + client.username);
                writer.newLine();
            }
            writer.flush();
            ServerController.log("📋 " + username + " requested user list");
        } catch (IOException e) {
            closeEverything();
        }
    }

    private void broadcast(String message) {
        for (ClientHandler client : Server.clients) {
            try {
                if (client != this) {
                    client.sendMessage(message);
                }
            } catch (Exception e) {
                client.closeEverything();
            }
        }
    }

    private void sendMessage(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void closeEverything() {
        try {
            Server.removeClient(this);
            if (controller != null) {
                controller.updateUserList();
            }
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
}