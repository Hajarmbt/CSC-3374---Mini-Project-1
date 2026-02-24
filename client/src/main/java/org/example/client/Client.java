package org.example.client;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private MessageListener messageListener;

    public Client(Socket socket, MessageListener listener) {
        try {
            this.socket = socket;
            this.messageListener = listener;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void sendMessageToServer(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    if (messageListener != null) {
                        messageListener.onMessageReceived(message);
                    }
                }
            } catch (IOException e) {
                closeEverything();
            }
        }).start();
    }

    public void closeEverything() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
