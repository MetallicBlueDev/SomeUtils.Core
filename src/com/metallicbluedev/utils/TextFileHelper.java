package com.metallicbluedev.utils;

import com.metallicbluedev.logger.*;
import java.io.*;
import java.nio.file.*;

/**
 *
 * @author Sébastien Villemain
 */
public class TextFileHelper {

    public static StringBuilder loadTextFromPath(Path path) {
        StringBuilder lines = new StringBuilder();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                lines.append(line);
            }
        } catch (IOException ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return lines;
    }

    public static boolean hasText(String values, Path path) {
        boolean found = false;
        File reportFile = path.toFile();

        try {

            if (reportFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
                    found = reader.lines().anyMatch(s -> s.equalsIgnoreCase(values));
                }
            }
        } catch (IOException ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return found;
    }

    public static boolean writeText(String values, Path path) {
        boolean writed = false;
        File reportFile = path.toFile();

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile, true))) {
                writer.append(values);
                writer.newLine();
            }
            writed = true;
        } catch (IOException ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return writed;
    }
}
