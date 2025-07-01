package server;

import common.Message;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RicartAgrawala {
    // States for the algorithm
    private enum State {
        IDLE, REQUESTING, IN_CS
    }

    private State currentState = State.IDLE;
    private int lamportClock = 0;
    private int myRequestTimestamp = 0;
    private String nodeId;
    private Set<String> nodesInSystem = new HashSet<>();
    private Set<String> repliesReceived = new HashSet<>();
    private Queue<String> deferredReplies = new ConcurrentLinkedQueue<>();
    private static List<ObjectOutputStream> clientOutputs = new ArrayList<>();

    public RicartAgrawala() {
        this.nodeId = "SERVER_" + System.currentTimeMillis();
    }

    public RicartAgrawala(String nodeId) {
        this.nodeId = nodeId;
    }

    public synchronized void addNode(String nodeId) {
        nodesInSystem.add(nodeId);
    }

    public synchronized void removeNode(String nodeId) {
        nodesInSystem.remove(nodeId);
        repliesReceived.remove(nodeId);
    }

    public synchronized void updateLamportClock(int receivedTimestamp) {
        lamportClock = Math.max(lamportClock, receivedTimestamp) + 1;
    }

    public synchronized int getLamportClock() {
        return ++lamportClock;
    }

    public synchronized void requestCS() {
        if (currentState != State.IDLE) {
            return; // Already requesting or in CS
        }

        currentState = State.REQUESTING;
        myRequestTimestamp = getLamportClock();
        repliesReceived.clear();

        // Send REQUEST messages to all other nodes
        for (String node : nodesInSystem) {
            sendRequestMessage(node, myRequestTimestamp);
        }

        // Wait for replies from all nodes
        while (repliesReceived.size() < nodesInSystem.size()) {
            try {
                wait(100); // Wait with timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // All replies received, enter critical section
        currentState = State.IN_CS;
    }

    public synchronized void releaseCS() {
        if (currentState != State.IN_CS) {
            return; // Not in critical section
        }

        currentState = State.IDLE;

        // Send deferred replies
        while (!deferredReplies.isEmpty()) {
            String nodeId = deferredReplies.poll();
            sendReplyMessage(nodeId);
        }

        notifyAll(); // Notify any waiting threads
    }

    public synchronized void handleRequestMessage(String senderId, int timestamp) {
        updateLamportClock(timestamp);

        boolean shouldReply = false;

        if (currentState == State.IDLE) {
            shouldReply = true;
        } else if (currentState == State.REQUESTING) {
            // Compare timestamps to break ties
            if (timestamp < myRequestTimestamp ||
                    (timestamp == myRequestTimestamp && senderId.compareTo(nodeId) < 0)) {
                shouldReply = true;
            } else {
                // Defer the reply
                deferredReplies.offer(senderId);
            }
        }
        // If in CS, defer the reply (already handled by adding to deferredReplies)

        if (shouldReply) {
            sendReplyMessage(senderId);
        } else if (currentState == State.IN_CS) {
            deferredReplies.offer(senderId);
        }
    }

    public synchronized void handleReplyMessage(String senderId) {
        if (currentState == State.REQUESTING) {
            repliesReceived.add(senderId);
            notifyAll(); // Notify waiting requestCS thread
        }
    }

    private void sendRequestMessage(String nodeId, int timestamp) {
        try {
            Message requestMsg = new Message(Message.Type.REQUEST, this.nodeId,
                    "CS_REQUEST", null, timestamp);
            // In a real distributed system, this would send to the specific node
            // For this implementation, we'll use a simplified approach
            broadcastToNode(requestMsg, nodeId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendReplyMessage(String nodeId) {
        try {
            Message replyMsg = new Message(Message.Type.REPLY, this.nodeId,
                    "CS_REPLY", null, getLamportClock());
            broadcastToNode(replyMsg, nodeId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastToNode(Message msg, String targetNodeId) {
        // In a distributed system, this would send to a specific node
        // For this server implementation, we simulate the communication
        synchronized (clientOutputs) {
            for (ObjectOutputStream out : clientOutputs) {
                try {
                    out.writeObject(msg);
                    out.flush();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void setClientOutputs(List<ObjectOutputStream> outputs) {
        synchronized (clientOutputs) {
            clientOutputs.clear();
            clientOutputs.addAll(outputs);
        }
    }

    public synchronized boolean isInCriticalSection() {
        return currentState == State.IN_CS;
    }

    public synchronized State getCurrentState() {
        return currentState;
    }

    public synchronized String getNodeId() {
        return nodeId;
    }
}