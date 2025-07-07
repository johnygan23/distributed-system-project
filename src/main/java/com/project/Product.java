package com.project;

import java.util.concurrent.locks.ReentrantLock; // Import ReentrantLock

public class Product {
    private String id;
    private String name;
    private int quantity;
    private final ReentrantLock lock; // Add a ReentrantLock for fine-grained control

    public Product(String id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.lock = new ReentrantLock(); // Initialize the lock for each product
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    // New method to get the lock associated with this product
    public ReentrantLock getLock() {
        return lock;
    }
}