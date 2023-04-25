package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private ExecutorService threadPool;
    private boolean done;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            threadPool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                threadPool.execute(handler);
            }
        } catch (Exception e) {
            //shutdown
            try {
                done = true;
                threadPool.shutdown();
                if (server != null && !server.isClosed()) {
                    server.close();
                }
            } catch (IOException ioe) {}
        }
    }

    public void broadcast(String msg) {
        for (ConnectionHandler connection: connections) {
            if (connection != null) {
                connection.sendMsg(msg);
            }
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

                String msg;

                writer.println("Please enter your username: ");
                username = reader.readLine(); //TODO: handle edge cases (length, invalid char, null)
                System.out.println(username + " connected");
                broadcast(username + " joined the chat");

                while ((msg = reader.readLine()) != null) {
                    if (msg.startsWith("/name ")) {
                        String[] msgSplit = msg.split(" ", 2);
                        if (msgSplit.length == 2) {
                            String oldName = username;
                            username = msgSplit[1];

                            broadcast(oldName + " renamed to " + username);
                            System.out.println(oldName + " renamed to " + username);
                            writer.println("Name successfully changed to " + username);
                        }
                        else {
                            writer.println("No name provided");
                        }
                    } else if (msg.startsWith("/quit")) {
                        broadcast(username + " has left the chat");
                        shutdown();
                    } else {
                        broadcast(username + ": " + msg);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }

        }

        public void sendMsg(String msg) {
            writer.println(msg);
        }

        public void shutdown() {
            try {
                reader.close();
                writer.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {}
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
