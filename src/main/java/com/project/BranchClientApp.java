package com.project;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class BranchClientApp extends JFrame {
    private JTextArea outputArea;
    private JTextField inputField;
    private PrintWriter out;
    private BufferedReader in;

    public BranchClientApp(String branchName) {
        setTitle("Branch Client - " + branchName);
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        outputArea = new JTextArea();
        inputField = new JTextField();

        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(e -> {
            String msg = inputField.getText();
            out.println(msg);
            inputField.setText("");
        });

        connectToServer();
        new Thread(this::listenToServer).start();
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String branchName = args.length > 0 ? args[0] : "Default";
        SwingUtilities.invokeLater(() -> new BranchClientApp(branchName).setVisible(true));
    }
}