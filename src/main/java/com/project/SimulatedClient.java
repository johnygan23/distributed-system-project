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
    private Map<String, Product> warehouseStock = new HashMap<>();
    private Map<String, Integer> localInventory = new HashMap<>();
    private Random random = new Random();

    // Simulation parameters
    private int minRequestDelay = 2000; // 2 seconds
    private int maxRequestDelay = 8000; // 8 seconds
    private int minRequestQuantity = 1;
    private int maxRequestQuantity = 10;
    private double requestProbability = 0.7; // 70% chance to make a request each cycle

    // Available products to request
    private String[] availableProducts = { "P001", "P002", "P003", "P004", "P005" };

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
            log("Connected to server");
            return true;
        } catch (IOException e) {
            log("Failed to connect: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void run() {
        if (!connect()) {
            return;
        }

        // Start listening to server responses
        new Thread(this::listenToServer).start();

        // Request initial inventory
        requestInventory();

        // Main simulation loop
        while (running) {
            try {
                // Random delay between actions
                int delay = ThreadLocalRandom.current().nextInt(minRequestDelay, maxRequestDelay + 1);
                Thread.sleep(delay);

                // Decide what action to take
                double action = random.nextDouble();

                if (action < requestProbability) {
                    makeRandomStockRequest();
                } else if (action < 0.9) {
                    requestInventory();
                } else {
                    sendPing();
                }

            } catch (InterruptedException e) {
                log("Simulation interrupted");
                break;
            }
        }

        disconnect();
    }

    private void listenToServer() {
        try {
            String line;
            boolean receivingInventory = false;

            while ((line = in.readLine()) != null && running) {
                if (line.equals("INVENTORY_UPDATE")) {
                    receivingInventory = true;
                    warehouseStock.clear();
                    continue;
                } else if (line.equals("INVENTORY_END")) {
                    receivingInventory = false;
                    log("Received inventory update - " + warehouseStock.size() + " products available");
                    continue;
                }

                if (receivingInventory && line.startsWith("PRODUCT:")) {
                    String[] parts = line.split(":");
                    if (parts.length == 4) {
                        String id = parts[1];
                        String name = parts[2];
                        int quantity = Integer.parseInt(parts[3]);
                        warehouseStock.put(id, new Product(id, name, quantity));
                    }
                } else if (line.startsWith("APPROVED:")) {
                    String[] parts = line.split(":");
                    String productId = parts[1];
                    int amount = Integer.parseInt(parts[2]);

                    // Update local inventory
                    localInventory.put(productId, localInventory.getOrDefault(productId, 0) + amount);
                    log("✓ APPROVED: " + productId + " x" + amount + " (Local stock: " + localInventory.get(productId)
                            + ")");

                } else if (line.startsWith("DENIED:")) {
                    String[] parts = line.split(":");
                    String productId = parts[1];
                    log("✗ DENIED: " + productId + " (Insufficient warehouse stock)");

                } else if (line.equals("PONG")) {
                    log("Server responded to ping");
                }
            }
        } catch (IOException e) {
            if (running) {
                log("Lost connection to server: " + e.getMessage());
            }
        }
    }

    private void makeRandomStockRequest() {
        if (warehouseStock.isEmpty()) {
            requestInventory();
            return;
        }

        // Select random product from available products
        String productId = availableProducts[random.nextInt(availableProducts.length)];

        // Check if product exists in warehouse
        Product warehouseProduct = warehouseStock.get(productId);
        if (warehouseProduct == null || warehouseProduct.getQuantity() == 0) {
            log("Skipping request for " + productId + " (not available in warehouse)");
            return;
        }

        // Generate random quantity (but not more than available)
        int maxAvailable = Math.min(warehouseProduct.getQuantity(), maxRequestQuantity);
        int requestQuantity = ThreadLocalRandom.current().nextInt(minRequestQuantity, maxAvailable + 1);

        log("Requesting " + requestQuantity + " units of " + productId +
                " (Warehouse has: " + warehouseProduct.getQuantity() + ")");

        out.println("REQUEST:" + productId + ":" + requestQuantity);
    }

    private void requestInventory() {
        out.println("SHOW");
    }

    private void sendPing() {
        out.println("PING");
    }

    private void disconnect() {
        running = false;
        try {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            // Ignore cleanup errors
        }
        log("Disconnected from server");
    }

    private void log(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "] " + clientId + ": " + message);
    }

    // Getters for monitoring
    public String getClientId() {
        return clientId;
    }

    public Map<String, Integer> getLocalInventory() {
        return new HashMap<>(localInventory);
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
        disconnect();
    }

    // Configuration methods
    public void setRequestDelay(int min, int max) {
        this.minRequestDelay = min;
        this.maxRequestDelay = max;
    }

    public void setRequestQuantityRange(int min, int max) {
        this.minRequestQuantity = min;
        this.maxRequestQuantity = max;
    }

    public void setRequestProbability(double probability) {
        this.requestProbability = Math.max(0.0, Math.min(1.0, probability));
    }
}