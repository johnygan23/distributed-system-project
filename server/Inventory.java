package server;

import java.util.concurrent.ConcurrentHashMap;

public class Inventory {
    private ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();

    public Inventory() {
        stock.put("Apples", 100);
        stock.put("Bananas", 80);
        stock.put("Oranges", 60);
    }

    public synchronized boolean requestStock(String item, int quantity) {
        int available = stock.getOrDefault(item, 0);
        if (available >= quantity) {
            stock.put(item, available - quantity);
            return true;
        }
        return false;
    }

    public ConcurrentHashMap<String, Integer> getStock() {
        return stock;
    }
} 