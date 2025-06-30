package server;

import common.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ConcurrentHashMap<String, Integer> inventory;
    private static List<ObjectOutputStream> clientOutputs = new ArrayList<>();
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static RicartAgrawala ricartAgrawala = new RicartAgrawala();

    public ClientHandler(Socket socket, ConcurrentHashMap<String, Integer> inventory) {
        this.socket = socket;
        this.inventory = inventory;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            synchronized (clientOutputs) {
                clientOutputs.add(out);
            }
            while (true) {
                Message msg = (Message) in.readObject();
                switch (msg.type) {
                    case REPLENISH:
                        // Ricart-Agrawala mutual exclusion for stock update
                        ricartAgrawala.requestCS();
                        String[] parts = msg.content.split(":");
                        String item = parts[0];
                        int qty = Integer.parseInt(parts[1]);
                        boolean approved = false;
                        synchronized (inventory) {
                            int available = inventory.getOrDefault(item, 0);
                            if (available >= qty) {
                                inventory.put(item, available - qty);
                                approved = true;
                            }
                        }
                        if (approved) {
                            // Broadcast update to all clients
                            Message updateMsg = new Message(Message.Type.UPDATE, "Server", item + ":" + qty, new ConcurrentHashMap<>(inventory), msg.lamportTimestamp);
                            broadcast(updateMsg);
                        } else {
                            Message denyMsg = new Message(Message.Type.DENY, "Server", "Denied: Not enough stock", null, msg.lamportTimestamp);
                            out.writeObject(denyMsg);
                        }
                        ricartAgrawala.releaseCS();
                        break;
                    case CHAT:
                        // Broadcast chat message to all clients
                        broadcast(msg);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized (clientOutputs) {
                clientOutputs.remove(out);
            }
        }
    }

    private void broadcast(Message msg) {
        synchronized (clientOutputs) {
            for (ObjectOutputStream o : clientOutputs) {
                try {
                    o.writeObject(msg);
                    o.flush();
                } catch (Exception ignored) {}
            }
        }
    }
} 