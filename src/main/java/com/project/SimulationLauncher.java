package com.project;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SimulationLauncher {
    private List<SimulatedClient> clients = new ArrayList<>();
    private ExecutorService executorService;
    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Distributed Inventory System - Client Simulation ===");
        System.out.println();

        SimulationLauncher launcher = new SimulationLauncher();
        launcher.showMenu();
    }

    private void showMenu() {
        while (true) {
            System.out.println("\n--- Simulation Menu ---");
            System.out.println("1. Quick simulation (5 clients, moderate activity)");
            System.out.println("2. Heavy load simulation (10 clients, high activity)");
            System.out.println("3. Light simulation (3 clients, low activity)");
            System.out.println("4. Custom simulation");
            System.out.println("5. Simulate High Contention (All clients target P001)"); // New Scenario
            System.out.println("6. Simulate High Concurrency (Clients target diverse products)"); // New Scenario
            System.out.println("7. Show client status");
            System.out.println("8. Stop all clients");
            System.out.println("9. Exit");
            System.out.print("Choose option (1-9): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                handleMenuChoice(choice);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void handleMenuChoice(int choice) {
        stopAllClients(); // Stop any existing clients before starting new ones

        switch (choice) {
            case 1: // Quick simulation
                startSimulation(5, 3000, 7000, 1, 5, 0.6, null, 0); // No specific product target
                break;
            case 2: // Heavy load
                startSimulation(10, 500, 2000, 1, 10, 0.9, null, 0); // No specific product target
                break;
            case 3: // Light load
                startSimulation(3, 8000, 15000, 1, 3, 0.4, null, 0); // No specific product target
                break;
            case 4: // Custom simulation
                System.out.print("Enter number of clients: ");
                int numClients = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter min request delay (ms): ");
                int minDelay = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter max request delay (ms): ");
                int maxDelay = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter min request quantity: ");
                int minQty = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter max request quantity: ");
                int maxQty = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter request probability (0.0-1.0): ");
                double prob = Double.parseDouble(scanner.nextLine());
                startSimulation(numClients, minDelay, maxDelay, minQty, maxQty, prob, null, 0);
                break;
            case 5: // Simulate High Contention
                // All clients target P001 with high bias
                startSimulation(8, 500, 1500, 1, 5, 0.9, "P001", 1.0); // 1.0 bias means always target P001
                System.out.println("\n--- Running High Contention Scenario (all clients target P001) ---");
                System.out.println("Observe server logs for lock contention on P001.");
                break;
            case 6: // Simulate High Concurrency
                // Clients target different products, or all target with low bias to spread requests
                startSimulation(8, 500, 1500, 1, 5, 0.9, null, 0); // No specific product bias, random selection
                // Or you could make clients specifically target different products if you spawn them individually:
                // startHighConcurrencyScenario();
                System.out.println("\n--- Running High Concurrency Scenario (clients target diverse products) ---");
                System.out.println("Observe server logs for parallel processing of different products.");
                break;
            case 7: // Show client status (changed from 5)
                showClientStatus();
                break;
            case 8: // Stop all clients (changed from 6)
                stopAllClients();
                break;
            case 9: // Exit (changed from 7)
                stopAllClients();
                System.out.println("Exiting simulation launcher. Goodbye!");
                scanner.close();
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // Modified startSimulation to accept target product and bias
    private void startSimulation(int numClients, int minDelay, int maxDelay, int minQty, int maxQty,
                                 double requestProb, String targetProductId, double targetProductBias) {
        executorService = Executors.newFixedThreadPool(numClients);
        clients.clear(); // Clear previous clients

        System.out.println("Starting " + numClients + " simulated clients...");
        for (int i = 0; i < numClients; i++) {
            SimulatedClient client = new SimulatedClient("Client-" + (i + 1));
            client.setRequestDelay(minDelay, maxDelay);
            client.setRequestQuantityRange(minQty, maxQty);
            client.setRequestProbability(requestProb);
            if (targetProductId != null) {
                client.setTargetProduct(targetProductId, targetProductBias);
            } else {
                // For diverse concurrency, ensure clients pick randomly from all available
                // If you want to explicitly assign, you'd do it here based on client index
            }
            clients.add(client);
            executorService.submit(client);
        }
        System.out.println("✓ Clients started.");
    }

    private void showClientStatus() {
        // ... (unchanged)
        int runningCount = 0;
        for (SimulatedClient client : clients) {
            if (client.isRunning()) {
                runningCount++;
            }
        }
        System.out.println("Running: " + runningCount + " | Stopped: " + (clients.size() - runningCount));
    }

    private void stopAllClients() {
        if (!clients.isEmpty()) {
            System.out.println("Stopping " + clients.size() + " simulated clients...");

            // Use a parallel stream for faster stopping, or just a loop
            clients.parallelStream().forEach(SimulatedClient::stop);

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                try {
                    // Wait a bit for tasks to terminate
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow(); // Force shutdown
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            clients.clear();
            System.out.println("✓ All simulated clients stopped.");
        }
    }

    // Helper for specific high concurrency if needed (optional)
    /*
    private void startHighConcurrencyScenario() {
        stopAllClients(); // Stop previous clients
        executorService = Executors.newFixedThreadPool(5); // Example
        clients.clear();

        String[] products = {"P001", "P002", "P003", "P004", "P005"};
        for (int i = 0; i < 5; i++) {
            SimulatedClient client = new SimulatedClient("Client-Conc-" + (i + 1));
            client.setRequestDelay(500, 1500);
            client.setRequestQuantityRange(1, 5);
            client.setRequestProbability(0.9);
            // Each client primarily targets a different product
            client.setTargetProduct(products[i % products.length], 1.0); // 100% bias
            clients.add(client);
            executorService.submit(client);
        }
        System.out.println("✓ High Concurrency clients started (each targeting a different product).");
    }
    */
}