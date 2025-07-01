package client;

import client.gui.InventoryGUI;
import javax.swing.*;
import java.util.Map;

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

            // Set up GUI callback
            branchClient.setMessageCallback(new BranchClient.MessageCallback() {
                @Override
                public void onInventoryUpdate(Map<String, Integer> inventory) {
                    // GUI will auto-refresh, but we could force update here
                }

                @Override
                public void onChatMessage(String sender, String message) {
                    gui.appendChatMessage("[" + sender + "] " + message);
                }

                @Override
                public void onRequestDenied(String reason) {
                    gui.appendChatMessage("SERVER: " + reason);
                }
            });

            // Start listening (remove extra Thread wrapper)
            branchClient.listen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}