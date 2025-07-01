package com.project;

import java.io.*;
import java.net.*;
import java.util.*;

public class CentralServer {
    private static final int PORT = 5000;
    private Map<String, Product> warehouseStock = new HashMap<>();

    public CentralServer() {
        warehouseStock.put("P001", new Product("P001", "Pen", 100));
        warehouseStock.put("P002", new Product("P002", "Notebook", 50));
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Central Server started...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String input;
                while ((input = in.readLine()) != null) {
                    String[] parts = input.split(":");
                    if (parts.length == 3 && parts[0].equals("REQUEST")) {
                        String productId = parts[1];
                        int amount = Integer.parseInt(parts[2]);
                        Product product = warehouseStock.get(productId);
                        if (product != null && product.getQuantity() >= amount) {
                            product.setQuantity(product.getQuantity() - amount);
                            out.println("APPROVED:" + productId + ":" + amount);
                        } else {
                            out.println("DENIED:" + productId);
                        }
                        System.out.println("=== Updated Inventory ===");
                        for (Product p : warehouseStock.values()) {
                            System.out.println(p.getId() + " - " + p.getName() + ": " + p.getQuantity());
                        }
                        System.out.println("==========================");
                    } else if (input.equals("SHOW")) {
                        for (Product p : warehouseStock.values()) {
                            out.println(p.getId() + " - " + p.getName() + ": " + p.getQuantity());
                        }
                    } else {
                        out.println("INVALID COMMAND");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new CentralServer().start();
    }
}
