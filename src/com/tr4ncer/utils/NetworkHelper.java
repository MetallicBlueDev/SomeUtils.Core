package com.tr4ncer.utils;

import com.tr4ncer.logger.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author Sébastien Villemain
 */
public class NetworkHelper {

    public static void waitForHostReachable(String ipAddress, int maxRetry, int timeout) {
        boolean isReachable;

        do {
            maxRetry--;

            try {
                isReachable = isHostReachable(ipAddress, timeout);
            } catch (IOException ex) {
                isReachable = false;
            }

            if (!isReachable) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException ex) {
                    LoggerManager.getInstance().addError(ex);
                }
            }
        } while (!isReachable && maxRetry > 0);
    }

    public static boolean isHostReachable(String ipAddress, int timeout)
        throws IOException {
        InetAddress hostAddress = InetAddress.getByName(ipAddress);
        return hostAddress.isReachable(timeout);
    }
}
