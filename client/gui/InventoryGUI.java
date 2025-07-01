package client.gui;

import client.BranchClient;
import client.ChatClient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class InventoryGUI extends JFrame {
    private BranchClient branchClient;
    private ChatClient chatClient;
    private DefaultTableModel inventoryTableModel;
    private JTable inventoryTable;
    private JTextField itemNameField;
    private JTextField quantityField;
    private JTextArea chatArea;
    private JTextField chatInputField;
    private JLabel statusLabel;
    private Timer refreshTimer;

    public InventoryGUI(BranchClient branchClient, ChatClient chatClient) {
        this.branchClient = branchClient;
        this.chatClient = chatClient;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        startRefreshTimer();

        setTitle("Branch Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        // Inventory table setup
        String[] columnNames = { "Item", "Quantity" };
        inventoryTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.getTableHeader().setReorderingAllowed(false);

        // Input fields for replenishment
        itemNameField = new JTextField(15);
        quantityField = new JTextField(10);

        // Chat components
        chatArea = new JTextArea(10, 30);
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        chatArea.setBackground(Color.WHITE);
        chatInputField = new JTextField(30);

        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());

        // Load initial inventory data
        updateInventoryTable();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Create main panels
        JPanel topPanel = createInventoryPanel();
        JPanel centerPanel = createReplenishmentPanel();
        JPanel bottomPanel = createChatPanel();
        JPanel statusPanel = createStatusPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.PAGE_END);
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Current Inventory"));

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setPreferredSize(new Dimension(400, 150));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Add refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> updateInventoryTable());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReplenishmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Request Replenishment"));

        // Create input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Item name input
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(itemNameField, gbc);

        // Quantity input
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(quantityField, gbc);

        // Request button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton requestButton = new JButton("Request Replenishment");
        requestButton.setBackground(new Color(70, 130, 180));
        requestButton.setForeground(Color.WHITE);
        requestButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        inputPanel.add(requestButton, gbc);

        panel.add(inputPanel, BorderLayout.NORTH);

        // Add action listener for request button
        requestButton.addActionListener(e -> handleReplenishmentRequest());

        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Chat"));

        // Chat display area
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(400, 150));
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Chat input panel
        JPanel chatInputPanel = new JPanel(new BorderLayout());
        JButton sendButton = new JButton("Send");
        sendButton.setBackground(new Color(34, 139, 34));
        sendButton.setForeground(Color.WHITE);

        chatInputPanel.add(chatInputField, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);

        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(chatInputPanel, BorderLayout.SOUTH);

        // Add action listeners for chat
        sendButton.addActionListener(e -> sendChatMessage());
        chatInputField.addActionListener(e -> sendChatMessage());

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private void setupEventHandlers() {
        // Handle Enter key in quantity field
        quantityField.addActionListener(e -> handleReplenishmentRequest());

        // Handle table selection
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = inventoryTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String itemName = (String) inventoryTableModel.getValueAt(selectedRow, 0);
                    itemNameField.setText(itemName);
                }
            }
        });
    }

    private void startRefreshTimer() {
        // Auto-refresh inventory every 5 seconds
        refreshTimer = new Timer(5000, e -> updateInventoryTable());
        refreshTimer.start();
    }

    private void updateInventoryTable() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Integer> inventory = branchClient.getLocalInventory();

                // Clear existing data
                inventoryTableModel.setRowCount(0);

                // Add current inventory data
                for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                    Object[] row = { entry.getKey(), entry.getValue() };
                    inventoryTableModel.addRow(row);
                }

                // Update status
                updateStatus("Inventory updated at " + java.time.LocalTime.now().toString().substring(0, 8));

            } catch (Exception e) {
                updateStatus("Error updating inventory: " + e.getMessage());
            }
        });
    }

    private void handleReplenishmentRequest() {
        String itemName = itemNameField.getText().trim();
        String quantityText = quantityField.getText().trim();

        if (itemName.isEmpty()) {
            showError("Please enter an item name");
            return;
        }

        if (quantityText.isEmpty()) {
            showError("Please enter a quantity");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showError("Quantity must be a positive number");
                return;
            }

            // Send replenishment request
            branchClient.requestReplenishment(itemName, quantity);

            // Clear input fields
            itemNameField.setText("");
            quantityField.setText("");

            // Update status
            updateStatus("Replenishment request sent for " + quantity + " " + itemName);

            // Show confirmation
            showInfo("Replenishment request sent successfully!");

        } catch (NumberFormatException e) {
            showError("Quantity must be a valid number");
        } catch (Exception e) {
            showError("Error sending request: " + e.getMessage());
            updateStatus("Error: " + e.getMessage());
        }
    }

    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            try {
                chatClient.sendChat(message);
                appendChatMessage("You: " + message);
                chatInputField.setText("");
                updateStatus("Chat message sent");
            } catch (Exception e) {
                showError("Error sending chat message: " + e.getMessage());
            }
        }
    }

    public void appendChatMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }
}