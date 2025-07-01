package com.project;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
            System.out.println("5. Show client status");
            System.out.println("6. Stop all clients");
            System.out.println("7. Exit");
            System.out.print("Choose option (1-7): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                handleMenuChoice(choice);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number 1-7.");
            }
        }
    }

    private void handleMenuChoice(int choice) {
        switch (choice) {
            case 1:
                runQuickSimulation();
                break;
            case 2:
                runHeavyLoadSimulation();
                break;
            case 3:
                runLightSimulation();
                break;
            case 4:
                runCustomSimulation();
                break;
            case 5:
                showClientStatus();
                break;
            case 6:
                stopAllClients();
                break;
            case 7:
                stopAllClients();
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please select 1-7.");
        }
    }

    private void runQuickSimulation() {
        System.out.println("\n🚀 Starting Quick Simulation (5 clients)...");
        stopAllClients(); // Stop any running simulation

        startSimulation(5, 3000, 7000, 1, 5, 0.6);
        System.out.println("✓ Quick simulation started! Check the server GUI to see client activity.");
    }

    private void runHeavyLoadSimulation() {
        System.out.println("\n💥 Starting Heavy Load Simulation (10 clients)...");
        stopAllClients();

        startSimulation(10, 1000, 4000, 1, 8, 0.8);
        System.out.println("✓ Heavy load simulation started! Watch the server handle high traffic.");
    }

    private void runLightSimulation() {
        System.out.println("\n🐌 Starting Light Simulation (3 clients)...");
        stopAllClients();

        startSimulation(3, 5000, 12000, 1, 3, 0.4);
        System.out.println("✓ Light simulation started! Clients will make occasional requests.");
    }

    private void runCustomSimulation() {
        System.out.println("\n⚙️ Custom Simulation Setup");

        try {
            System.out.print("Number of clients (1-20): ");
            int numClients = Integer.parseInt(scanner.nextLine().trim());
            numClients = Math.max(1, Math.min(20, numClients));

            System.out.print("Minimum delay between requests (ms, default 2000): ");
            String minDelayStr = scanner.nextLine().trim();
            int minDelay = minDelayStr.isEmpty() ? 2000 : Integer.parseInt(minDelayStr);

            System.out.print("Maximum delay between requests (ms, default 8000): ");
            String maxDelayStr = scanner.nextLine().trim();
            int maxDelay = maxDelayStr.isEmpty() ? 8000 : Integer.parseInt(maxDelayStr);

            System.out.print("Maximum request quantity (default 5): ");
            String maxQtyStr = scanner.nextLine().trim();
            int maxQty = maxQtyStr.isEmpty() ? 5 : Integer.parseInt(maxQtyStr);

            System.out.print("Request probability (0.0-1.0, default 0.7): ");
            String probStr = scanner.nextLine().trim();
            double probability = probStr.isEmpty() ? 0.7 : Double.parseDouble(probStr);

            stopAllClients();
            startSimulation(numClients, minDelay, maxDelay, 1, maxQty, probability);

            System.out.println("✓ Custom simulation started with " + numClients + " clients!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default quick simulation.");
            runQuickSimulation();
        }
    }

    private void startSimulation(int numClients, int minDelay, int maxDelay,
            int minQty, int maxQty, double probability) {

        executorService = Executors.newFixedThreadPool(numClients + 2);

        for (int i = 1; i <= numClients; i++) {
            SimulatedClient client = new SimulatedClient("SimClient-" + i);

            // Configure client behavior
            client.setRequestDelay(minDelay, maxDelay);
            client.setRequestQuantityRange(minQty, maxQty);
            client.setRequestProbability(probability);

            clients.add(client);
            executorService.submit(client);

            // Small delay between client connections
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }

        System.out.println("Started " + numClients + " simulated clients.");
        System.out.println("Configuration:");
        System.out.println("  - Request delay: " + minDelay + "-" + maxDelay + "ms");
        System.out.println("  - Request quantity: " + minQty + "-" + maxQty);
        System.out.println("  - Request probability: " + (probability * 100) + "%");
    }

    private void showClientStatus() {
        if (clients.isEmpty()) {
            System.out.println("No simulated clients are currently running.");
            return;
        }

        System.out.println("\n--- Client Status ---");
        System.out.println("Total simulated clients: " + clients.size());

        int runningCount = 0;
        for (SimulatedClient client : clients) {
            String status = client.isRunning() ? "🟢 Running" : "🔴 Stopped";
            System.out.printf("%-15s %s", client.getClientId(), status);

            // Show local inventory summary
            Map<String, Integer> inventory = client.getLocalInventory();
            if (!inventory.isEmpty()) {
                int totalItems = inventory.values().stream().mapToInt(Integer::intValue).sum();
                System.out.print(" (Local stock: " + totalItems + " items)");
            }
            System.out.println();

            if (client.isRunning())
                runningCount++;
        }

        System.out.println("Running: " + runningCount + " | Stopped: " + (clients.size() - runningCount));
    }

    private void stopAllClients() {
        if (!clients.isEmpty()) {
            System.out.println("Stopping " + clients.size() + " simulated clients...");

            for (SimulatedClient client : clients) {
                client.stop();
            }

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }

            clients.clear();
            System.out.println("✓ All simulated clients stopped.");
        }
    }

    // Preset simulation configurations
    public static class SimulationPresets {
        public static final String STRESS_TEST = "stress";
        public static final String NORMAL_LOAD = "normal";
        public static final String LIGHT_LOAD = "light";

        public static void runPreset(String preset) {
            SimulationLauncher launcher = new SimulationLauncher();

            switch (preset.toLowerCase()) {
                case STRESS_TEST:
                    launcher.startSimulation(15, 500, 2000, 1, 10, 0.9);
                    break;
                case NORMAL_LOAD:
                    launcher.startSimulation(5, 3000, 7000, 1, 5, 0.6);
                    break;
                case LIGHT_LOAD:
                    launcher.startSimulation(3, 8000, 15000, 1, 3, 0.3);
                    break;
            }
        }
    }
}