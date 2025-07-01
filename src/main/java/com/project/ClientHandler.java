package com.project;

import java.io.*;
import java.net.*;
import java.util.Date;

public class ClientHandler implements Runnable {
    private Socket socket;
    private CentralServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String clientAddress;
    private Date connectedTime;
    private boolean connected;

    public ClientHandler(Socket socket, CentralServer server) {
        this.socket = socket;
        this.server = server;
        this.clientAddress = socket.getInetAddress().getHostAddress();
        this.connectedTime = new Date();
        this.connected = true;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send initial inventory to client
            sendInventoryUpdate();

            String input;
            while ((input = in.readLine()) != null && connected) {
                processClientRequest(input);
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void processClientRequest(String input) {
        String[] parts = input.split(":");

        if (parts.length >= 1) {
            switch (parts[0]) {
                case "REQUEST":
                    handleStockRequest(parts);
                    break;
                case "SHOW":
                    sendInventoryUpdate();
                    break;
                case "PING":
                    out.println("PONG");
                    break;
                default:
                    out.println("INVALID_COMMAND");
                    break;
            }
        }
    }

    private void handleStockRequest(String[] parts) {
        if (parts.length == 3) {
            try {
                String productId = parts[1];
                int amount = Integer.parseInt(parts[2]);

                boolean approved = server.processRequest(productId, amount);

                if (approved) {
                    out.println("APPROVED:" + productId + ":" + amount);
                } else {
                    out.println("DENIED:" + productId + ":" + amount);
                }
            } catch (NumberFormatException e) {
                out.println("INVALID_FORMAT");
            }
        } else {
            out.println("INVALID_REQUEST_FORMAT");
        }
    }

    public void sendInventoryUpdate() {
        if (out != null && connected) {
            out.println("INVENTORY_UPDATE");
            for (Product product : server.getWarehouseStock().values()) {
                out.println("PRODUCT:" + product.getId() + ":" + product.getName() + ":" + product.getQuantity());
            }
            out.println("INVENTORY_END");
        }
    }

    private void cleanup() {
        connected = false;
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        server.removeClient(this);
    }

    // Getters for server GUI
    public String getClientAddress() {
        return clientAddress;
    }

    public Date getConnectedTime() {
        return connectedTime;
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }
}