package client;

import common.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class BranchClient {
    private String branchName;
    private HashMap<String, Integer> localInventory = new HashMap<>();
    private Socket socket;
    ObjectOutputStream out;
    private ObjectInputStream in;

    public BranchClient(String branchName) {
        this.branchName = branchName;
        localInventory.put("Apples", 10);
        localInventory.put("Bananas", 8);
        localInventory.put("Oranges", 6);
    }

    public void connect(String host, int port) throws Exception {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void requestReplenishment(String item, int quantity) throws Exception {
        Message msg = new Message(Message.Type.REPLENISH, branchName, item + ":" + quantity, null, 0);
        out.writeObject(msg);
    }

    public void listen() {
        new Thread(() -> {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    switch (msg.type) {
                        case UPDATE:
                            if (msg.inventory != null) {
                                // Update local inventory to match server
                                localInventory.clear();
                                localInventory.putAll(msg.inventory);
                                System.out.println("Inventory updated: " + localInventory);
                            }
                            break;
                        case DENY:
                            System.out.println("Request denied by server: " + msg.content);
                            break;
                        case CHAT:
                            System.out.println("[Chat] " + msg.sender + ": " + msg.content);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public HashMap<String, Integer> getLocalInventory() {
        return localInventory;
    }
} 