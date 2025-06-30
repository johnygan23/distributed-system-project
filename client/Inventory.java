package client;

import java.util.HashMap;

public class Inventory {
    private HashMap<String, Integer> stock = new HashMap<>();

    public Inventory() {
        stock.put("Apples", 10);
        stock.put("Bananas", 8);
        stock.put("Oranges", 6);
    }

    public synchronized void addStock(String item, int quantity) {
        stock.put(item, stock.getOrDefault(item, 0) + quantity);
    }

    public synchronized boolean removeStock(String item, int quantity) {
        int available = stock.getOrDefault(item, 0);
        if (available >= quantity) {
            stock.put(item, available - quantity);
            return true;
        }
        return false;
    }

    public HashMap<String, Integer> getStock() {
        return stock;
    }
} 