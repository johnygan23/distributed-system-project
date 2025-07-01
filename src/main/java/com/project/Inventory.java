package com.project;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Map<String, Product> products = new HashMap<>();

    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    public Product getProduct(String id) {
        return products.get(id);
    }

    public Map<String, Product> getAllProducts() {
        return products;
    }

    public void updateProduct(String id, int newQty) {
        if (products.containsKey(id)) {
            products.get(id).setQuantity(newQty);
        }
    }
}
