package com.redhat.commands;

import com.redhat.constants.CommandConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class WordCountCommand {
    public static void handleWordCountCommand(PrintWriter out, ExecutorService threadPool) {
        File storeDirectory = new File(CommandConstants.STORE_DIRECTORY);
        File[] files = storeDirectory.listFiles();

        if (files != null && files.length > 0) {
            AtomicInteger wordCount = new AtomicInteger(0);

            List<Callable<Void>> tasks = new ArrayList<>();
            for (File file : files) {
                if (file.isFile()) {
                    tasks.add(() -> {
                        try {
                            int count = countWordsInFile(file);
                            wordCount.addAndGet(count);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
                }
            }

            try {
                threadPool.invokeAll(tasks);
                out.println("Total word count: " + wordCount.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            out.println("No files found in the store.");
        }
    }

    private static int countWordsInFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.trim().split(CommandConstants.WORD_DELIMITER_REGEX);
                count += words.length;
            }
        }
        return count;
    }
}
