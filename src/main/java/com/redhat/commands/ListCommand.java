package com.redhat.commands;

import com.redhat.constants.CommandConstants;

import java.io.File;
import java.io.PrintWriter;

public class ListCommand {

    public static void handleListCommand(PrintWriter out) {
        File storeDirectory = new File(CommandConstants.STORE_DIRECTORY);
        File[] files = storeDirectory.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    out.println(file.getName());
                }
            }
        } else {
            out.println("No files found in the store.");
        }
    }

}
