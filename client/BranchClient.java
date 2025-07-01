package client;

import common.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BranchClient {
    private String branchName;
    private HashMap<String, Integer> localInventory = new HashMap<>();
    private Socket socket;
    ObjectOutputStream out;
    private ObjectInputStream in;
    private MessageCallback callback;
    private LamportClock lamportClock = new LamportClock();

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
        lamportClock.tick();
        Message msg = new Message(Message.Type.REPLENISH, branchName, item + ":" + quantity, null,
                lamportClock.getTime());
        out.writeObject(msg);
    }

    public void listen() {
        new Thread(() -> {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    switch (msg.type) {
                        case UPDATE:
                            if (msg.inventory != null && callback != null) {
                                localInventory.clear();
                                localInventory.putAll(msg.inventory);
                                callback.onInventoryUpdate(localInventory);
                            }
                            break;
                        case DENY:
                            if (callback != null) {
                                callback.onRequestDenied(msg.content);
                            }
                            break;
                        case CHAT:
                            if (callback != null) {
                                callback.onChatMessage(msg.sender, msg.content);
                            }
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

    public void setMessageCallback(MessageCallback callback) {
        this.callback = callback;
    }

    // Move interface inside the class as static nested interface
    public static interface MessageCallback {
        void onInventoryUpdate(Map<String, Integer> inventory);

        void onChatMessage(String sender, String message);

        void onRequestDenied(String reason);
    }
}