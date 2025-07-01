package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class WarehouseServer {
    public static final int PORT = 5000;
    private ConcurrentHashMap<String, Integer> inventory = new ConcurrentHashMap<>();

    public WarehouseServer() {
        // Initialize inventory with some products
        inventory.put("Apples", 100);
        inventory.put("Bananas", 80);
        inventory.put("Oranges", 60);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Warehouse Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, inventory)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new WarehouseServer().start();
    }
}