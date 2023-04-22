package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(9999);
            Socket client = server.accept();
            connections.add(new ConnectionHandler(client));
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: handle
        }
    }

    public void broadcast(String msg) {
        for (ConnectionHandler connection: connections) {
            //TODO: handle edge cases (just null?)
            connection.sendMsg(msg);
        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader reader;
        private PrintWriter writer;
        private String username;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer = new PrintWriter(client.getOutputStream(), true);

                writer.println("Please enter your username: ");
                username = reader.readLine(); //TODO: handle edge cases (length, invalid char, null)
                System.out.println(username + " connected");
                broadcast(username + " joined the chat");
            } catch (IOException e) {
                //TODO: handle
            }

        }

        public void sendMsg(String msg) {
            writer.println(msg);
        }
    }
}
