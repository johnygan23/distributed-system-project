package com.project;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.table.DefaultTableModel;

public class BranchClientApp extends JFrame {
    private JTextArea outputArea;
    private JTextField inputField;
    private PrintWriter out;
    private BufferedReader in;
    private Inventory inventory = new Inventory(); // Local branch inventory
    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    public BranchClientApp(String branchName) {
        setTitle("Branch Client - " + branchName);
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        outputArea = new JTextArea();
        inputField = new JTextField();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Setup inventory table
        String[] columnNames = {"Product ID", "Name", "Quantity"};
        tableModel = new DefaultTableModel(columnNames, 0);
        inventoryTable = new JTable(tableModel);

        // Add components to splitPane
        splitPane.setTopComponent(new JScrollPane(inventoryTable));
        splitPane.setBottomComponent(new JScrollPane(outputArea));

        add(splitPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        initializeInventory();

        inputField.addActionListener(e -> {
            String msg = inputField.getText();
            out.println(msg);
            inputField.setText("");
        });

        connectToServer();
        new Thread(this::listenToServer).start();
    }

    private void refreshInventoryTable() {
        tableModel.setRowCount(0); // Clear existing rows
        for (Product p : inventory.getAllProducts().values()) {
            Object[] row = {p.getId(), p.getName(), p.getQuantity()};
            tableModel.addRow(row);
        }
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenToServer() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                outputArea.append(line + "\n");

                if (line.startsWith("APPROVED:")) {
                    String[] parts = line.split(":");
                    String productId = parts[1];
                    int amount = Integer.parseInt(parts[2]);

                    Product p = inventory.getProduct(productId);
                    if (p != null) {
                        p.setQuantity(p.getQuantity() + amount);
                    } else {
                        inventory.addProduct(new Product(productId, "Unknown", amount));
                    }
                    refreshInventoryTable();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeInventory() {
        inventory.addProduct(new Product("P001", "Pen", 5));
        inventory.addProduct(new Product("P002", "Notebook", 3));
        refreshInventoryTable();
    }

    public static void main(String[] args) {
        String branchName = args.length > 0 ? args[0] : "Default";
        SwingUtilities.invokeLater(() -> new BranchClientApp(branchName).setVisible(true));
    }
}