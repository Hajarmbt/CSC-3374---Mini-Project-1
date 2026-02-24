package org.example.client;

import javafx.application.Application;

public class ClientLauncher {
    public static void main(String[] args) {
        // Default values
        String serverIP = "localhost";
        int port = 5000;

        // Parse command line arguments: java TCPClient <ServerIPAddress> <PortNumber>
        if (args.length >= 2) {
            serverIP = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: 3333");
            }
        } else {
            System.out.println("Usage: java TCPClient <ServerIPAddress> <PortNumber>");
            System.out.println("Using default: localhost 3333");
        }

        // Store for Client to use
        System.setProperty("server.ip", serverIP);
        System.setProperty("server.port", String.valueOf(port));

        Application.launch(ClientApplication.class, args);
    }
}