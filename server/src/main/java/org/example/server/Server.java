package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private ServerSocket serverSocket;
    public static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private boolean isRunning = true;
    private ServerController controller;

    public Server(ServerSocket serverSocket, ServerController controller) {
        this.serverSocket = serverSocket;
        this.controller = controller;
    }

    public void startServer() {
        ServerController.log("🟢 Server started. Waiting for clients...");

        try {
            while (!serverSocket.isClosed() && isRunning) {
                Socket socket = serverSocket.accept();
                ServerController.log("📡 New client connected from: " + socket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(socket, controller);
                clients.add(clientHandler);
                clientHandler.start();

                // Update user list in UI
                controller.updateUserList();
            }
        } catch (IOException e) {
            if (isRunning) {
                ServerController.log("❌ Server error: " + e.getMessage());
            }
        }
    }

    public void closeServerSocket() {
        isRunning = false;
        try {
            // Disconnect all clients
            for (ClientHandler client : clients) {
                client.closeEverything();
            }
            clients.clear();

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            ServerController.log("🔴 Server stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}