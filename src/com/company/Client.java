package com.company;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader reader;
    private PrintWriter writer;
    private String localHost = "127.0.0.1";
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket(localHost, 9999);
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            writer = new PrintWriter(client.getOutputStream(), true);

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMsg;
            while ((inMsg = reader.readLine()) != null) {
                System.out.println(inMsg);
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            reader.close();
            writer.close();
            if (client != null && !client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {

        }
    }

    class InputHandler implements Runnable {
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String msg = inputReader.readLine();
                    if (msg.equals("/quit")) {
                        writer.println(msg);
                        inputReader.close();
                        shutdown();
                    }
                    else {
                        writer.println(msg);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
