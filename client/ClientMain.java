package client;

import client.gui.InventoryGUI;
import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        try {
            String branchName = args.length > 0 ? args[0] : "Branch1";
            String serverHost = args.length > 1 ? args[1] : "localhost";
            int serverPort = args.length > 2 ? Integer.parseInt(args[2]) : 5000;

            BranchClient branchClient = new BranchClient(branchName);
            branchClient.connect(serverHost, serverPort);

            ChatClient chatClient = new ChatClient(branchClient.out, branchName);
            InventoryGUI gui = new InventoryGUI(branchClient, chatClient);

            // Listen for server messages and update GUI accordingly
            new Thread(() -> {
                branchClient.listen();
            }).start();

            // Hook for updating inventory and chat from BranchClient
            // (You may want to refactor BranchClient to accept callbacks for GUI updates)

            SwingUtilities.invokeLater(() -> gui.setVisible(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 