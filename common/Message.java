package common;

import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable {
    public enum Type { REQUEST, REPLY, UPDATE, CHAT, REPLENISH, DENY }
    public Type type;
    public String sender;
    public String content;
    public Map<String, Integer> inventory;
    public int lamportTimestamp;

    public Message(Type type, String sender, String content, Map<String, Integer> inventory, int lamportTimestamp) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.inventory = inventory;
        this.lamportTimestamp = lamportTimestamp;
    }
} 