package client;

import common.Message;
import java.io.ObjectOutputStream;

public class ChatClient {
    private ObjectOutputStream out;
    private String branchName;

    public ChatClient(ObjectOutputStream out, String branchName) {
        this.out = out;
        this.branchName = branchName;
    }

    public void sendChat(String message) {
        try {
            Message chatMsg = new Message(Message.Type.CHAT, branchName, message, null, 0);
            out.writeObject(chatMsg);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 