package com.redhat;

import com.redhat.commands.AddCommand;
import com.redhat.commands.FreqWordCommand;
import com.redhat.commands.ListCommand;
import com.redhat.commands.RemoveCommand;
import com.redhat.commands.WordCountCommand;
import com.redhat.constants.CommandConstants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileStoreServer {

    private static Map<String, String> storeFileHashMap = new HashMap<>();
    private static Map<String, String> storeFileHashName = new HashMap<>();
    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) {
        try {
            cleanupFileStore();
            ServerSocket serverSocket = new ServerSocket(CommandConstants.PORT);
            System.out.println("Server started on port " + CommandConstants.PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Handle client request in a separate thread
                executorService.execute(() -> handleClientRequest(clientSocket));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read the request from the client
            String request = reader.readLine();

            if (request == null) {
                return;
            }

            // Parse the request
            String[] tokens = request.trim().split("\\s+");
            if (tokens.length == 0) {
                return;
            }

            String command = tokens[0].toLowerCase();

            switch (command) {
                case "add":
                case "update":
                    AddCommand.handleAddAndUpdateFile(tokens, reader, writer, storeFileHashMap, storeFileHashName);
                    break;
                case "ls":
                    ListCommand.handleListCommand(writer);
                    break;
                case "rm":
                    RemoveCommand.handleRemoveCommand(tokens, writer, storeFileHashMap, storeFileHashName);
                    break;
                case "wc":
                    WordCountCommand.handleWordCountCommand(writer, executorService);
                    break;
                case "freq-words":
                    FreqWordCommand.handleFreqWordsCommand(tokens, writer, executorService);
                    break;
                default:
                    writer.println("Invalid command");
            }

            // Close the connection
            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cleanupFileStore() {
        File directory = new File(CommandConstants.STORE_DIRECTORY);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            System.out.println("All files deleted from the file store.");
        }
    }
}
