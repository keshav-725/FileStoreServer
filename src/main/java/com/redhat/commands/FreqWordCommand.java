package com.redhat.commands;

import com.redhat.constants.CommandConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class FreqWordCommand {
    public static void handleFreqWordsCommand(String[] commandTokens, PrintWriter out, ExecutorService threadPool) {
        int limit = CommandConstants.DEFAULT_FREQ_WORDS_LIMIT;
        boolean descendingOrder = true;

        for (int i = 1; i < commandTokens.length; i++) {
            String token = commandTokens[i];

            if (token.equalsIgnoreCase("--limit") || token.equalsIgnoreCase("-n")) {
                if (i + 1 < commandTokens.length) {
                    try {
                        limit = Integer.parseInt(commandTokens[i + 1]);
                    } catch (NumberFormatException e) {
                        out.println("Invalid limit value: " + commandTokens[i + 1]);
                        return;
                    }
                    i++;
                } else {
                    out.println("Missing limit value.");
                    return;
                }
            } else if (token.equalsIgnoreCase("--order")) {
                if (i + 1 < commandTokens.length) {
                    String order = commandTokens[i + 1].toLowerCase();
                    if (order.equals("asc")) {
                        descendingOrder = false;
                    } else if (!order.equals("dsc")) {
                        out.println("Invalid order value: " + commandTokens[i + 1]);
                        return;
                    }
                    i++;
                } else {
                    out.println("Missing order value.");
                    return;
                }
            } else {
                out.println("Invalid argument: " + token);
                return;
            }
        }

        File storeDirectory = new File(CommandConstants.STORE_DIRECTORY);
        File[] files = storeDirectory.listFiles();

        if (files != null && files.length > 0) {
            Map<String, Integer> wordCountMap = new HashMap<>();

            List<Callable<Void>> tasks = new ArrayList<>();
            for (File file : files) {
                if (file.isFile()) {
                    tasks.add(() -> {
                        try {
                            countWordsInFile(file, wordCountMap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
                }
            }

            try {
                threadPool.invokeAll(tasks);

                List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(wordCountMap.entrySet());
                sortedEntries.sort(descendingOrder ? Map.Entry.comparingByValue(Comparator.reverseOrder()) : Map.Entry.comparingByValue());

                int count = Math.min(limit, sortedEntries.size());
                for (int i = 0; i < count; i++) {
                    Map.Entry<String, Integer> entry = sortedEntries.get(i);
                    out.println(entry.getValue() + "\t" + entry.getKey());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            out.println("No files found in the store.");
        }
    }

    private static void countWordsInFile(File file, Map<String, Integer> wordCountMap) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.trim().split(CommandConstants.WORD_DELIMITER_REGEX);
                for (String word : words) {
                    wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                }
            }
        }
    }
}
