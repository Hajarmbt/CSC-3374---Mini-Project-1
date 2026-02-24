//package org.example.server;
//
//import javafx.application.Application;
//
//public class ServerLauncher {
//    private static int PORT = 3333; // Default port
//
//    public static void main(String[] args) {
//        // Check if port is provided as command line argument
//        if (args.length >= 1) {
//            try {
//                PORT = Integer.parseInt(args[0]);
//                System.out.println("Server starting on port: " + PORT);
//            } catch (NumberFormatException e) {
//                System.out.println("Invalid port number. Using default: 3333");
//            }
//        }
//
//        // Store port for ServerApplication to use
//        System.setProperty("server.port", String.valueOf(PORT));
//
//        Application.launch(ServerApplication.class, args);
//    }
//
//    public static int getPort() {
//        return PORT;
//    }
//}
package org.example.server;

import javafx.application.Application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerLauncher {

    private static String IP = "127.0.0.1"; // default
    private static int PORT = 3333;         // default

    public static void main(String[] args) {

        // Load config.properties
        Properties prop = new Properties();
        try (InputStream is = ServerLauncher.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                prop.load(is);
                IP = prop.getProperty("server.ip", IP);
                PORT = Integer.parseInt(prop.getProperty("server.port", String.valueOf(PORT)));
            } else {
                System.out.println("Config file not found! Using defaults.");
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading config: " + e.getMessage());
            System.out.println("Using default IP and port.");
        }

        // Override port from command line if provided
        if (args.length >= 1) {
            try {
                PORT = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port argument. Using config/default: " + PORT);
            }
        }

        System.out.println("Server starting at IP=" + IP + " PORT=" + PORT);

        // Set system properties so ServerApplication can read them
        System.setProperty("server.ip", IP);
        System.setProperty("server.port", String.valueOf(PORT));

        // Launch JavaFX application
        Application.launch(ServerApplication.class, args);
    }

    // Optional getters if needed
    public static int getPort() {
        return PORT;
    }

    public static String getIP() {
        return IP;
    }
}