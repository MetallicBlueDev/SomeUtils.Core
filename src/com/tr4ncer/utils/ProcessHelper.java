package com.tr4ncer.utils;

import com.tr4ncer.logger.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author Sébastien Villemain
 */
public class ProcessHelper {

    public static boolean executeCli(int timeOutMs, String processPath, String... params) {
        return executeCli(timeOutMs, processPath, null, null, true, params);
    }

    public static boolean executeCli(int timeOutMs, String processPath, Charset consoleCharset, List<String> outputs, boolean defaultReturnValue, String... params) {
        if (outputs != null) {
            outputs.clear();

        }
        boolean executed = defaultReturnValue;

        String[] command = new String[params.length + 1];
        command[0] = processPath;
        System.arraycopy(params, 0, command, 1, params.length);

        Process p = null;

        try {
            p = Runtime.getRuntime().exec(command);

            // Le programme ne se ferme pas : pas de retour possible
            p.waitFor(timeOutMs, TimeUnit.MILLISECONDS);

            if (outputs != null) {
                if (consoleCharset == null) {
                    consoleCharset = Charset.forName("Cp437");
                }

                try (BufferedReader reader = p.inputReader(consoleCharset)) {
                    String line;

                    do {
                        line = reader.readLine();

                        if (line != null) {
                            outputs.add(line);
                        }
                    } while (line != null);
                }

                if (!defaultReturnValue) {
                    executed = !outputs.isEmpty();
                }
            }
        } catch (IOException | InterruptedException ex) {
            LoggerManager.getInstance().addError(ex);
            executed = false;
        }

        if (p != null && p.isAlive()) {
            p.destroy();
        }
        return executed;
    }
}
