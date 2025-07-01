package com.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class CentralServer extends JFrame {
    private static final int PORT = 5000;
    private Map<String, Product> warehouseStock = new HashMap<>();
    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;
    private JTextArea logArea;
    private JTable clientsTable;
    private DefaultTableModel clientsTableModel;
    private List<ClientHandler> connectedClients = new ArrayList<>();
    private ServerSocket serverSocket;
    private boolean serverRunning = false;

    public CentralServer() {
        initializeGUI();
        initializeInventory();
        startServer();
    }

    private void initializeGUI() {
        setTitle("Central Warehouse Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create main panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Inventory tab
        JPanel inventoryPanel = createInventoryPanel();
        tabbedPane.addTab("Inventory", inventoryPanel);

        // Clients tab
        JPanel clientsPanel = createClientsPanel();
        tabbedPane.addTab("Connected Clients", clientsPanel);

        // Logs tab
        JPanel logsPanel = createLogsPanel();
        tabbedPane.addTab("Server Logs", logsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton addStockButton = new JButton("Add Stock");
        JButton removeStockButton = new JButton("Remove Stock");
        JButton refreshButton = new JButton("Refresh All");

        addStockButton.addActionListener(e -> showAddStockDialog());
        removeStockButton.addActionListener(e -> showRemoveStockDialog());
        refreshButton.addActionListener(e -> {
            refreshInventoryTable();
            refreshClientsTable();
        });

        controlPanel.add(addStockButton);
        controlPanel.add(removeStockButton);
        controlPanel.add(refreshButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = { "Product ID", "Name", "Quantity" };
        inventoryTableModel = new DefaultTableModel(columns, 0);
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = { "Client ID", "IP Address", "Connected Time", "Status" };
        clientsTableModel = new DefaultTableModel(columns, 0);
        clientsTable = new JTable(clientsTableModel);
        clientsTable.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(clientsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void initializeInventory() {
        warehouseStock.put("P001", new Product("P001", "Pen", 100));
        warehouseStock.put("P002", new Product("P002", "Notebook", 50));
        warehouseStock.put("P003", new Product("P003", "Pencil", 75));
        warehouseStock.put("P004", new Product("P004", "Eraser", 30));
        warehouseStock.put("P005", new Product("P005", "Ruler", 25));
        refreshInventoryTable();
        log("Inventory initialized with " + warehouseStock.size() + " products.");
    }

    private void refreshInventoryTable() {
        SwingUtilities.invokeLater(() -> {
            inventoryTableModel.setRowCount(0);
            for (Product product : warehouseStock.values()) {
                Object[] row = { product.getId(), product.getName(), product.getQuantity() };
                inventoryTableModel.addRow(row);
            }
        });
    }

    private void refreshClientsTable() {
        SwingUtilities.invokeLater(() -> {
            clientsTableModel.setRowCount(0);
            synchronized (connectedClients) {
                for (int i = 0; i < connectedClients.size(); i++) {
                    ClientHandler client = connectedClients.get(i);
                    Object[] row = { "Client-" + (i + 1), client.getClientAddress(),
                            client.getConnectedTime(), client.isConnected() ? "Connected" : "Disconnected" };
                    clientsTableModel.addRow(row);
                }
            }
        });
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                serverRunning = true;
                log("Central Server started on port " + PORT);

                while (serverRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                        synchronized (connectedClients) {
                            connectedClients.add(clientHandler);
                        }
                        new Thread(clientHandler).start();
                        refreshClientsTable();
                        log("New client connected: " + clientSocket.getInetAddress());
                    } catch (IOException e) {
                        if (serverRunning) {
                            log("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                log("Server error: " + e.getMessage());
            }
        }).start();
    }

    private void showAddStockDialog() {
        JDialog dialog = new JDialog(this, "Add Stock", true);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField quantityField = new JTextField();

        dialog.add(new JLabel("Product ID:"));
        dialog.add(idField);
        dialog.add(new JLabel("Product Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Quantity:"));
        dialog.add(quantityField);

        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());

                if (warehouseStock.containsKey(id)) {
                    Product existing = warehouseStock.get(id);
                    existing.setQuantity(existing.getQuantity() + quantity);
                } else {
                    warehouseStock.put(id, new Product(id, name, quantity));
                }

                refreshInventoryTable();
                broadcastInventoryUpdate();
                log("Added stock: " + id + " - " + name + " (+" + quantity + ")");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid quantity format!");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(cancelButton);

        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showRemoveStockDialog() {
        String[] productIds = warehouseStock.keySet().toArray(new String[0]);
        if (productIds.length == 0) {
            JOptionPane.showMessageDialog(this, "No products available!");
            return;
        }

        String selectedId = (String) JOptionPane.showInputDialog(this,
                "Select product to remove stock:", "Remove Stock",
                JOptionPane.QUESTION_MESSAGE, null, productIds, productIds[0]);

        if (selectedId != null) {
            String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity to remove:");
            try {
                int quantity = Integer.parseInt(quantityStr);
                Product product = warehouseStock.get(selectedId);
                if (product.getQuantity() >= quantity) {
                    product.setQuantity(product.getQuantity() - quantity);
                    refreshInventoryTable();
                    broadcastInventoryUpdate();
                    log("Removed stock: " + selectedId + " (-" + quantity + ")");
                } else {
                    JOptionPane.showMessageDialog(this, "Not enough stock available!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid quantity format!");
            }
        }
    }

    public synchronized boolean processRequest(String productId, int amount) {
        Product product = warehouseStock.get(productId);
        if (product != null && product.getQuantity() >= amount) {
            product.setQuantity(product.getQuantity() - amount);
            refreshInventoryTable();
            broadcastInventoryUpdate();
            log("Request approved: " + productId + " (-" + amount + ") - Remaining: " + product.getQuantity());
            return true;
        } else {
            log("Request denied: " + productId + " (requested: " + amount +
                    ", available: " + (product != null ? product.getQuantity() : 0) + ")");
            return false;
        }
    }

    public Map<String, Product> getWarehouseStock() {
        return warehouseStock;
    }

    private void broadcastInventoryUpdate() {
        synchronized (connectedClients) {
            for (ClientHandler client : connectedClients) {
                client.sendInventoryUpdate();
            }
        }
    }

    public void removeClient(ClientHandler client) {
        synchronized (connectedClients) {
            connectedClients.remove(client);
        }
        refreshClientsTable();
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
            new CentralServer().setVisible(true);
        });
    }
}
