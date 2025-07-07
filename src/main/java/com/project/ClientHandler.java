package com.project;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Executors; // Import for ScheduledExecutorService
import java.util.concurrent.ScheduledExecutorService; // Import for ScheduledExecutorService
import java.util.concurrent.TimeUnit; // Import for TimeUnit

public class ClientHandler implements Runnable {
    private Socket socket;
    private CentralServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String clientAddress;
    private Date connectedTime;
    private boolean connected;

    // Server-side configuration for the random wait time to send to the client
    private int minClientWaitTimeMs = 2500; // Minimum delay for client to wait
    private int maxClientWaitTimeMs = 10000; // Maximum delay for client to wait

    // Executor for scheduling delayed tasks (one per client handler)
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
            System.err.println("Client handler error for " + clientAddress + ": " + e.getMessage());
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
            String productId = parts[1];
            int amount = Integer.parseInt(parts[2]);

            // 1. Generate a random waiting time for the client
            int clientWaitDelay = ThreadLocalRandom.current().nextInt(minClientWaitTimeMs, maxClientWaitTimeMs + 1);

            // 2. Immediately send an acknowledgment with the waiting time
            out.println("ACK_WAITING:" + productId + ":" + amount + ":" + clientWaitDelay);
            System.out.println("Server: Sent ACK for " + productId + " to " + clientAddress + ". Client to wait for " + clientWaitDelay + "ms.");

            // 3. Schedule the actual processing and final response after the delay
            scheduler.schedule(() -> {
                try {
                    // Perform the actual inventory processing
                    boolean approved = server.processRequest(productId, amount); //

                    // Send the final response
                    if (approved) {
                        out.println("APPROVED:" + productId + ":" + amount); //
                        System.out.println("Server: Sent APPROVED for " + productId + " to " + clientAddress);
                    } else {
                        out.println("DENIED:" + productId + ":" + amount); //
                        System.out.println("Server: Sent DENIED for " + productId + " to " + clientAddress);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing delayed request for " + clientAddress + ": " + e.getMessage());
                    out.println("SERVER_ERROR:Delayed processing failed");
                }
            }, clientWaitDelay, TimeUnit.MILLISECONDS);

        } else {
            out.println("INVALID_REQUEST_FORMAT"); //
        }
    }

    public void sendInventoryUpdate() {
        if (out != null && connected) {
            out.println("INVENTORY_UPDATE"); //
            for (Product product : server.getWarehouseStock().values()) { //
                out.println("PRODUCT:" + product.getId() + ":" + product.getName() + ":" + product.getQuantity()); //
            }
            out.println("INVENTORY_END"); //
        }
    }

    private void cleanup() {
        connected = false;
        scheduler.shutdownNow(); // Shut down the scheduler when client disconnects
        try {
            if (in != null)
                in.close(); //
            if (out != null)
                out.close(); //
            if (socket != null)
                socket.close(); //
        } catch (IOException e) {
            System.err.println("Error during cleanup for " + clientAddress + ": " + e.getMessage()); //
        }
        server.removeClient(this); //
    }

    // Getters for server GUI
    public String getClientAddress() {
        return clientAddress; //
    }

    public Date getConnectedTime() {
        return connectedTime; //
    }

    public boolean isConnected() {
        return connected && !socket.isClosed(); //
    }
}