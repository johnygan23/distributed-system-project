package com.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

public class BranchClientApp extends JFrame {
    private JTextArea logArea;
    private PrintWriter out;
    private BufferedReader in;
    private Inventory localInventory = new Inventory(); // Local branch inventory
    private JTable localInventoryTable;
    private DefaultTableModel localTableModel;
    private JTable warehouseInventoryTable;
    private DefaultTableModel warehouseTableModel;
    private Map<String, Product> warehouseStock = new HashMap<>();
    private String branchName;

    public BranchClientApp(String branchName) {
        this.branchName = branchName;
        initializeGUI();
        initializeLocalInventory();
        connectToServer();
        new Thread(this::listenToServer).start();
    }

    private void initializeGUI() {
        setTitle("Branch Client - " + branchName);
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Local Inventory Tab
        JPanel localInventoryPanel = createLocalInventoryPanel();
        tabbedPane.addTab("Local Inventory", localInventoryPanel);

        // Warehouse Stock Tab
        JPanel warehousePanel = createWarehousePanel();
        tabbedPane.addTab("Warehouse Stock", warehousePanel);

        // Request Stock Tab
        JPanel requestPanel = createRequestPanel();
        tabbedPane.addTab("Request Stock", requestPanel);

        // Logs Tab
        JPanel logsPanel = createLogsPanel();
        tabbedPane.addTab("Activity Logs", logsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout());
        JLabel statusLabel = new JLabel("Connected to Central Server");
        statusLabel.setForeground(Color.CYAN);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createLocalInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = { "Product ID", "Name", "Quantity" };
        localTableModel = new DefaultTableModel(columns, 0);
        localInventoryTable = new JTable(localTableModel);
        localInventoryTable.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(localInventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Control panel for local inventory
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton sellButton = new JButton("Sell Product");

        refreshButton.addActionListener(_ -> refreshLocalInventoryTable());
        sellButton.addActionListener(_ -> showSellDialog());

        controlPanel.add(refreshButton);
        controlPanel.add(sellButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWarehousePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = { "Product ID", "Name", "Available Quantity" };
        warehouseTableModel = new DefaultTableModel(columns, 0);
        warehouseInventoryTable = new JTable(warehouseTableModel);
        warehouseInventoryTable.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(warehouseInventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh Warehouse");
        refreshButton.addActionListener(_ -> requestWarehouseInventory());
        controlPanel.add(refreshButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Request form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Product selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Product ID:"), gbc);

        JTextField productIdField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(productIdField, gbc);

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Quantity:"), gbc);

        JTextField quantityField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(quantityField, gbc);

        // Request button
        JButton requestButton = new JButton("Request Stock");
        requestButton.setPreferredSize(new Dimension(150, 40));
        requestButton.addActionListener(_ -> {
            String productId = productIdField.getText().trim();
            String quantityText = quantityField.getText().trim();

            if (!productId.isEmpty() && !quantityText.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityText);
                    requestStock(productId, quantity);
                    productIdField.setText("");
                    quantityField.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid quantity number!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(requestButton, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Quick request buttons for common products
        JPanel quickPanel = new JPanel(new FlowLayout());
        quickPanel.setBorder(BorderFactory.createTitledBorder("Quick Requests"));

        String[] commonProducts = { "P001", "P002", "P003", "P004", "P005" };
        for (String productId : commonProducts) {
            JButton quickButton = new JButton("Request " + productId);
            quickButton.addActionListener(_ -> {
                String input = JOptionPane.showInputDialog(this, "Enter quantity for " + productId + ":");
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int quantity = Integer.parseInt(input.trim());
                        requestStock(productId, quantity);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid quantity!");
                    }
                }
            });
            quickPanel.add(quickButton);
        }

        panel.add(quickPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Clear Logs");
        clearButton.addActionListener(_ -> logArea.setText(""));
        controlPanel.add(clearButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshLocalInventoryTable() {
        localTableModel.setRowCount(0);
        for (Product p : localInventory.getAllProducts().values()) {
            Object[] row = { p.getId(), p.getName(), p.getQuantity() };
            localTableModel.addRow(row);
        }
    }

    private void refreshWarehouseInventoryTable() {
        SwingUtilities.invokeLater(() -> {
            warehouseTableModel.setRowCount(0);
            for (Product p : warehouseStock.values()) {
                Object[] row = { p.getId(), p.getName(), p.getQuantity() };
                warehouseTableModel.addRow(row);
            }
        });
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            log("Connected to Central Server");
            requestWarehouseInventory();
        } catch (IOException e) {
            log("Failed to connect to server: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to connect to Central Server!\n" + e.getMessage());
        }
    }

    private void listenToServer() {
        try {
            String line;
            boolean receivingInventory = false;

            while ((line = in.readLine()) != null) {
                if (line.equals("INVENTORY_UPDATE")) {
                    receivingInventory = true;
                    warehouseStock.clear();
                    continue;
                } else if (line.equals("INVENTORY_END")) {
                    receivingInventory = false;
                    refreshWarehouseInventoryTable();
                    log("Warehouse inventory updated");
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

                    // Add to local inventory
                    Product localProduct = localInventory.getProduct(productId);
                    if (localProduct != null) {
                        localProduct.setQuantity(localProduct.getQuantity() + amount);
                    } else {
                        // Get product name from warehouse stock
                        Product warehouseProduct = warehouseStock.get(productId);
                        String name = warehouseProduct != null ? warehouseProduct.getName() : "Unknown";
                        localInventory.addProduct(new Product(productId, name, amount));
                    }
                    refreshLocalInventoryTable();
                    log("Stock request APPROVED: " + productId + " (+" + amount + ")");
                    JOptionPane.showMessageDialog(this,
                            "Stock request approved!\nReceived " + amount + " units of " + productId);

                } else if (line.startsWith("DENIED:")) {
                    String[] parts = line.split(":");
                    String productId = parts[1];
                    log("Stock request DENIED: " + productId);
                    JOptionPane.showMessageDialog(this,
                            "Stock request denied for " + productId + "\nInsufficient stock in warehouse.",
                            "Request Denied", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (IOException e) {
            log("Connection to server lost: " + e.getMessage());
        }
    }

    private void requestStock(String productId, int quantity) {
        if (out != null) {
            out.println("REQUEST:" + productId + ":" + quantity);
            log("Requested " + quantity + " units of " + productId);
        } else {
            JOptionPane.showMessageDialog(this, "Not connected to server!");
        }
    }

    private void requestWarehouseInventory() {
        if (out != null) {
            out.println("SHOW");
        }
    }

    private void showSellDialog() {
        if (localInventory.getAllProducts().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products in local inventory!");
            return;
        }

        String[] productIds = localInventory.getAllProducts().keySet().toArray(new String[0]);
        String selectedId = (String) JOptionPane.showInputDialog(this,
                "Select product to sell:", "Sell Product",
                JOptionPane.QUESTION_MESSAGE, null, productIds, productIds[0]);

        if (selectedId != null) {
            String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity to sell:");
            try {
                int quantity = Integer.parseInt(quantityStr);
                Product product = localInventory.getProduct(selectedId);
                if (product.getQuantity() >= quantity) {
                    product.setQuantity(product.getQuantity() - quantity);
                    refreshLocalInventoryTable();
                    log("Sold " + quantity + " units of " + selectedId);
                } else {
                    JOptionPane.showMessageDialog(this, "Not enough stock in local inventory!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid quantity format!");
            }
        }
    }

    private void initializeLocalInventory() {
        localInventory.addProduct(new Product("P001", "Pen", 5));
        localInventory.addProduct(new Product("P002", "Notebook", 3));
        refreshLocalInventoryTable();
        log("Local inventory initialized");
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.util.Date().toString();
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String branchName = args.length > 0 ? args[0] : "Branch-1";
            new BranchClientApp(branchName).setVisible(true);
        });
    }
}