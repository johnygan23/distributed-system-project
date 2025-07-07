package com.project;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SimulatedClient implements Runnable {
    private String clientId;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private boolean running = true;
    private Map<String, Product> warehouseStock = new HashMap<>(); // This isn't strictly needed for client simulation logic here
    private Map<String, Integer> localInventory = new HashMap<>();
    private Random random = new Random();

    // Simulation parameters
    private int minRequestDelay = 2000; // 2 seconds delay BEFORE sending request (client thought/prep time)
    private int maxRequestDelay = 8000; // 8 seconds delay BEFORE sending request
    private int minRequestQuantity = 1;
    private int maxRequestQuantity = 10;
    private double requestProbability = 0.7; // 70% chance to make a request each cycle

    // Available products to request
    private String[] availableProducts = { "P001", "P002", "P003", "P004", "P005" };

    // New: Specific product targeting for lock simulation
    private String targetProductId = null; // If set, client will primarily request this product
    private double targetProductBias = 0.8; // Probability of requesting the target product if set

    public SimulatedClient(String clientId) {
        this.clientId = clientId;
        // Add some randomness to each client's behavior
        this.minRequestDelay += random.nextInt(1000);
        this.maxRequestDelay += random.nextInt(2000);
    }

    public boolean connect() {
        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            log("Connected to server.");
            // Request initial inventory update (optional, just for completeness)
            out.println("SHOW");
            return true;
        } catch (IOException e) {
            log("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void run() {
        if (!connect()) {
            return;
        }

        // Separate thread for listening to server responses
        new Thread(this::listenToServer).start();

        while (running) {
            try {
                // Client's internal "thought" or "preparation" delay before deciding to send a request
                int prepDelay = ThreadLocalRandom.current().nextInt(minRequestDelay, maxRequestDelay + 1);
                Thread.sleep(prepDelay);

                if (random.nextDouble() < requestProbability) {
                    sendStockRequest();
                } else {
                    // Sometimes just show inventory or ping
                    if (random.nextBoolean()) {
                        out.println("SHOW");
                        log("Requested warehouse inventory.");
                    } else {
                        out.println("PING");
                        log("Sent PING.");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Client " + clientId + " interrupted.");
                running = false;
            } catch (Exception e) {
                log("Error in client run loop: " + e.getMessage());
                running = false;
            }
        }
        disconnect();
    }

    private void listenToServer() {
        try {
            String line;
            while ((line = in.readLine()) != null && running) {
                processServerMessage(line);
            }
        } catch (IOException e) {
            if (running) { // Only log if not intentionally disconnected
                log("Server connection lost: " + e.getMessage());
            }
        } finally {
            disconnect(); // Ensure cleanup if listener thread terminates
        }
    }

    private void processServerMessage(String message) {
        String[] parts = message.split(":");
        switch (parts[0]) {
            case "INVENTORY_UPDATE":
                // Handle inventory update (optional for simulation focus)
                log("Received warehouse inventory update.");
                break;
            case "PRODUCT":
                // Process individual product updates (optional)
                break;
            case "INVENTORY_END":
                log("Warehouse inventory update complete.");
                break;
            case "ACK_WAITING":
                if (parts.length == 4) {
                    String productId = parts[1];
                    int amount = Integer.parseInt(parts[2]);
                    int clientWaitDelay = Integer.parseInt(parts[3]);
                    log("Received ACK for " + productId + " (" + amount + "). Client waiting for " + clientWaitDelay + "ms as instructed by server...");
                    try {
                        Thread.sleep(clientWaitDelay); // Client waits as instructed by server
                        log("Client finished waiting for " + productId + ". Expecting final response.");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log("Client wait interrupted for " + productId);
                    }
                }
                break;
            case "APPROVED":
                if (parts.length == 3) {
                    String productId = parts[1];
                    int amount = Integer.parseInt(parts[2]);
                    localInventory.put(productId, localInventory.getOrDefault(productId, 0) + amount);
                    log("Replenishment APPROVED for " + amount + " units of " + productId);
                }
                break;
            case "DENIED":
                if (parts.length == 3) {
                    String productId = parts[1];
                    log("Replenishment DENIED for " + productId);
                }
                break;
            case "PONG":
                log("Received PONG from server.");
                break;
            case "SERVER_ERROR":
                log("Server error: " + (parts.length > 1 ? parts[1] : "Unknown error"));
                break;
            default:
                // log("Unknown server message: " + message);
                break;
        }
    }

    private void sendStockRequest() {
        String productId;
        // Logic to choose product for request
        if (targetProductId != null && random.nextDouble() < targetProductBias) {
            productId = targetProductId; // Prioritize target product
        } else {
            // Pick a random product from the available list
            productId = availableProducts[random.nextInt(availableProducts.length)];
        }
        int amount = ThreadLocalRandom.current().nextInt(minRequestQuantity, maxRequestQuantity + 1);

        out.println("REQUEST:" + productId + ":" + amount);
        log("Sent request for " + amount + " units of " + productId);
    }

    private void disconnect() {
        running = false; // Mark as not running before closing streams
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            log("Disconnected from server.");
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    private void log(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new Date()); // Milliseconds for precision
        System.out.println("[" + timestamp + "] " + clientId + ": " + message);
    }

    // New configuration method for targeted product requests
    public void setTargetProduct(String productId, double bias) {
        this.targetProductId = productId;
        this.targetProductBias = bias;
    }

    // Getters and other methods (unchanged)
    public String getClientId() { return clientId; }
    public Map<String, Integer> getLocalInventory() { return new HashMap<>(localInventory); }
    public boolean isRunning() { return running; }
    public void stop() { running = false; }
    public void setRequestDelay(int min, int max) { this.minRequestDelay = min; this.maxRequestDelay = max; }
    public void setRequestQuantityRange(int min, int max) { this.minRequestQuantity = min; this.maxRequestQuantity = max; }
    public void setRequestProbability(double probability) { this.requestProbability = probability; }
}