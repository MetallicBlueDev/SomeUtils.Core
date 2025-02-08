package com.tr4ncer.utils;

import com.tr4ncer.logger.*;
import com.tr4ncer.threading.*;
import java.io.*;
import java.lang.management.*;
import java.util.*;
import java.util.regex.*;

/**
 * Aide à la manipulation du système d'exploitation.
 *
 * @version 1.15.02
 * @author Sebastien Villemain
 */
public class SystemHelper {

    /**
     * Commande permettant de lancer le shell sous les systèmes Windows 9x.
     */
    private static final String[] DEFAULT_WIN9X_SHELL = {
        "command.com",
        "/C"};

    /**
     * Commande permettant de lancer le shell sous les systèmes Windows NT/XP/Vista/7+.
     */
    private static final String[] DEFAULT_WINNT_SHELL = {
        "cmd.exe",
        "/C"};

    /**
     * Commande permettant de lancer le shell sous les systèmes Unix/Linux/MacOS/BSD.
     */
    private static final String[] DEFAULT_UNIX_SHELL = {
        "/bin/sh",
        "-c"};

    /**
     * Le nom du système ocurant.
     */
    public static final String OS_NAME = getOSName();

    /**
     * Shell du système courant.
     */
    public static final List<String> SYSTEM_SHELL = getSystemShell();

    /**
     * Détermine si la machine virtuelle fonctionne avec le support du mode debug.
     */
    public static final boolean DEBUGGING_SUPPORT = isDebuggingSupport();

    private SystemHelper() {
        // NE RIEN FAIRE
    }

    /**
     * Retourne le nom du système ocurant.
     *
     * @return
     */
    private static String getOSName() {
        String osName;

        try {
            osName = System.getProperty("os.name").toUpperCase().trim();
        } catch (Exception e) {
            osName = "";
        }
        return osName;
    }

    /**
     * Retourne le shell courant sous forme d'un tableau de String
     * représentant les différents paramètres a exécuter.<br/>
     * Le shell à utiliser dépend du système d'exploitation et
     * de certaine variable d'environnement (<b>%ComSpec%</b> sous Windows,
     * <b>$SHELL</b> sous les autres systèmes).
     *
     * @return Le tableau de paramètre utile à l'exécution du shell.
     */
    private static List<String> getSystemShell() {
        List<String> systemShell = new ArrayList<>(2);

        // On détermine le shell selon deux cas : Windows ou autres
        if (isWindows()) {
            // On tente de déterminer le shell selon la variable d'environnement ComSpec
            String comSpec = null;
            try {
                comSpec = System.getenv("ComSpec");
            } catch (Exception ex) {
                LoggerManager.getInstance().addError(ex);
            }

            if (comSpec != null) {
                systemShell.add(comSpec);
                systemShell.add("/C");
            } else {
                // Sinon on détermine le shell selon le nom précis du système
                systemShell.addAll(Arrays.asList(isWindows9xShell() ? DEFAULT_WIN9X_SHELL : DEFAULT_WINNT_SHELL));
            }
        } else {
            // On tente de déterminer le shell selon la variable d'environnement SHELL
            String shell = null;
            try {
                shell = System.getenv("SHELL");
            } catch (Exception ex) {
                LoggerManager.getInstance().addError(ex);
            }

            if (shell != null) {
                systemShell.add(shell);
                systemShell.add("-c");
            } else {
                // Sinon on utilise le shell par défaut
                systemShell.addAll(Arrays.asList(DEFAULT_UNIX_SHELL));
            }
        }
        return Collections.unmodifiableList(systemShell);
    }

    /**
     * Vérifie si la machine virtuelle fonctionne avec le support du mode debug.
     *
     * @return
     */
    private static boolean isDebuggingSupport() {
        boolean rslt = false;

        Pattern p = Pattern.compile("-Xdebug|jdwp");
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (p.matcher(arg).find()) {
                rslt = true;
                break;
            }
        }
        return rslt;
    }

    /**
     * Détermine si c'est une système Windows.
     *
     * @return
     */
    public static boolean isWindows() {
        return OS_NAME.startsWith("WINDOWS");
    }

    /**
     * Détermine si c'est un système Mac.
     *
     * @return
     */
    public static boolean isMac() {
        return OS_NAME.startsWith("MAC");
    }

    /**
     * Détermine si c'est un système Linux.
     *
     * @return
     */
    public static boolean isLinux() {
        return OS_NAME.startsWith("LINUX");
    }

    /**
     * Détermine si c'est une système Windows version 3.x.
     *
     * @return
     */
    public static boolean isWindows3() {
        return OS_NAME.startsWith("WINDOWS 3");
    }

    /**
     * Détermine si c'est une système Windows version 95.
     *
     * @return
     */
    public static boolean isWindows95() {
        return OS_NAME.startsWith("WINDOWS 95");
    }

    /**
     * Détermine si c'est une système Windows version 98.
     *
     * @return
     */
    public static boolean isWindows98() {
        return OS_NAME.startsWith("WINDOWS 98");
    }

    /**
     * Détermine si c'est une système Windows version 2000.
     *
     * @return
     */
    public static boolean isWindowsME() {
        return OS_NAME.startsWith("WINDOWS ME");
    }

    /**
     * Détermine si c'est une système Windows version 9x.
     * Représente les anciennes plateforme.
     *
     * @return
     */
    public static boolean isWindows9xShell() {
        return isWindows3() || isWindows95() || isWindows98() || isWindowsME();
    }

    /**
     * Détermine si c'est une système Mac OS X.
     *
     * @return
     */
    public static boolean isMacOSX() {
        return OS_NAME.startsWith("MAC OS X");
    }

    /**
     * Détermine si c'est une système Solaris.
     *
     * @return
     */
    public static boolean isSolaris() {
        return OS_NAME.startsWith("SOLARIS");
    }

    /**
     * Détermine si c'est un système Android.
     * Attention, le système d'exploitation que retourne Android est Linux.
     *
     * @return
     */
    public static boolean isAndroid() {
        return PackagesHelper.getClass("android.os.Process") != null;
    }

    /**
     * Retourne l'instance du service.
     * Si le thread n'est pas un service, retourne
     * <code>null</code>.
     *
     * @param thread
     * @return ServiceProcess or <code>null</code>.
     */
    public static ServiceProcess getService(Thread thread) {
        ServiceProcess service = null;

        if (thread != null) {
            switch (thread) {
                case ServiceProcess serviceProcess ->
                    service = serviceProcess;
                case ThreadHolderTask threadHolderTask ->
                    service = threadHolderTask.getTask();
                default -> {
                }
            }
        }
        return service;
    }

    /**
     * Retourne la liste des services.
     *
     * @param threads
     * @return
     */
    public static ServiceProcess[] getServices(Thread[] threads) {
        List<ServiceProcess> servicesList = new ArrayList<>();

        if (threads != null) {
            for (Thread thread : threads) {
                ServiceProcess service = getService(thread);

                if (service != null) {
                    servicesList.add(service);
                }
            }
        }

        ServiceProcess[] services = new ServiceProcess[servicesList.size()];
        return servicesList.toArray(services);
    }

    /**
     * Retourne la liste des threads supportant un service.
     *
     * @return
     */
    public static Thread[] getServices() {
        List<Thread> services = new ArrayList<>();
        Thread[] threads = new Thread[Thread.activeCount()];
        int count = Thread.enumerate(threads);

        for (int i = 0; i < count; i++) {
            Thread thread = threads[i];

            if (getService(thread) != null) {
                services.add(thread);
            }
        }

        Thread[] servicesList = new Thread[services.size()];
        return services.toArray(servicesList);
    }

    /**
     * Retourne le chemin vers l'executable java.
     *
     * @return
     */
    public static String getJavaBinary() {
        String javaBinary;

        if (isWindows()) {
            javaBinary = "javaw.exe";
        } else {
            javaBinary = "java";
        }

        String javaHome = null;

        try {
            javaHome = System.getProperty("java.home");
        } catch (Exception ex) {
            LoggerManager.getInstance().addError(ex);
        }

        if (javaHome != null) {
            File java = new File(new File(javaHome), "/bin/" + javaBinary);

            if (java.exists() && java.isFile()) {
                javaBinary = java.getAbsolutePath();
            }
        }
        return javaBinary;
    }
}
