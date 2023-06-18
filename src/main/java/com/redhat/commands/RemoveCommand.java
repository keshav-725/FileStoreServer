package com.redhat.commands;

import com.redhat.constants.CommandConstants;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

public class RemoveCommand {

    public static void handleRemoveCommand(String[] commandTokens, PrintWriter out, Map<String, String> storeFileHashMap, Map<String, String> storeFileHashName) {
        for (int i = 1; i < commandTokens.length; i++) {
            String fileName = commandTokens[i];
            File storeFile = new File(CommandConstants.STORE_DIRECTORY + fileName);

            if (storeFile.exists() && !storeFile.isDirectory()) {
                if (storeFile.delete()) {
                    storeFileHashMap.values().removeAll(Collections.singleton(fileName));
                    storeFileHashName.remove(fileName);
                    out.println("Removed file: " + fileName);
                } else {
                    out.println("Failed to remove file: " + fileName);
                }
            } else {
                out.println("File not found in the store: " + fileName);
            }
        }
    }

}
