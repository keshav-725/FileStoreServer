package com.redhat.commands;

import com.redhat.constants.CommandConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class AddCommand {

    public static void handleAddAndUpdateFile(String[] tokens, BufferedReader reader, PrintWriter writer, Map<String, String> storeFileHashMap, Map<String,String> storeFileHashName) {
        if (tokens.length < 2) {
            writer.println("Usage: ADD <file1> <file2> ...");
            return;
        }

        for (int i = 1; i < tokens.length; i++) {
            String fileName = tokens[i];

            try {
                String fileHash = reader.readLine();
                if (storeFileHashMap.containsKey(fileHash)) {
                    writer.println("EXIST");
                } else {
                    storeFileHashMap.put(fileHash, fileName);

                    if(storeFileHashName.containsKey(fileName)){
                        String oldHashValue = storeFileHashName.get(fileName);
                        storeFileHashMap.remove(oldHashValue);
                        storeFileHashName.replace(fileName, fileHash);
                    }

                    writer.println("NOT_EXIST");

                    File file = new File(CommandConstants.STORE_DIRECTORY, fileName);

                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        String line;
                        while ((line = reader.readLine()) != null && !line.equals("END")) {
                            fileOutputStream.write(line.getBytes());
                            fileOutputStream.write("\n".getBytes());
                        }
                        writer.println("File added successfully: " + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        writer.println("Error adding file: " + fileName);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                writer.println("Error reading file hash: " + fileName);
            }
        }
        writer.println("All files processed from server side");
    }
}
