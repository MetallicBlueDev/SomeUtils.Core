package com.metallicbluedev.utils;

import com.metallicbluedev.core.*;
import com.metallicbluedev.factory.*;
import com.metallicbluedev.logger.*;
import com.metallicbluedev.taskprogress.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * Aide à la manipulation des packages.
 *
 * @version 1.58.00
 * @author Sebastien Villemain
 */
public class PackagesHelper {

    private final TaskProgressContainer progressContainer;

    public PackagesHelper() {
        this(new TaskProgressContainer());
    }

    public PackagesHelper(TaskProgressContainer progressContainer) {
        this.progressContainer = progressContainer;
    }

    /**
     * Retourne la liste des classes trouvées pour le package cible.
     *
     * @param <T>
     * @param packageName
     * @param recursive
     * @return
     */
    public <T> List<Class<T>> getClasses(String packageName, boolean recursive) {
        return getClasses(packageName, recursive, null);
    }

    /**
     * Retourne la liste des classes trouvées pour le package cible.
     *
     * @param <T>
     * @param packageName
     * @param recursive
     * @param filter
     * @return
     */
    public <T> List<Class<T>> getClasses(String packageName, boolean recursive, Class<T> filter) {
        List<Class<T>> classes = new ArrayList<>();
        ClassLoader currentClassLoader = ClassLoader.getSystemClassLoader();

        if (currentClassLoader != null) {
            List<URL> resources = new ArrayList<>();

            try {
                String packagePath = packageName.replace(".", "/");
                Enumeration<URL> resourcesLoaded = currentClassLoader.getResources(packagePath);

                while (resourcesLoaded.hasMoreElements()) {
                    URL url = resourcesLoaded.nextElement();

                    if (url != null) {
                        resources.add(url);
                    }
                }
            } catch (IOException ex) {
                LoggerManager.getInstance().addError(ex);
            }

            progressContainer.addProgressStep(resources.size());

            // Rercherche des classes
            for (URL resource : resources) {
                List<Class<T>> classesFound;

                // Recherche du chemin
                String directoryPath;
                if (resource.getProtocol().equalsIgnoreCase("JAR")) {
                    directoryPath = resource.toExternalForm();
                } else {
                    directoryPath = resource.getFile();
                }

                // Permet d'éviter les problèmes d'espace et autre...
                try {
                    directoryPath = URLDecoder.decode(directoryPath, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    LoggerManager.getInstance().addError(ex);
                }

                if (directoryPath.startsWith("jar:file:")) {
                    // Suppression du protocole jar:file:
                    int index = directoryPath.indexOf("!");
                    directoryPath = directoryPath.substring(9, index);

                    classesFound = findClassesInJarFile(directoryPath, packageName, recursive, filter);
                } else {
                    classesFound = findClassesInDirectory(new File(directoryPath), packageName, recursive, filter);
                }

                if (!classesFound.isEmpty()) {
                    classes.addAll(classesFound);
                }

                progressContainer.addProgressValue(1);
            }
        }
        return classes;
    }

    /**
     * Retourne toutes les classes trouvées dans un dossier.
     *
     * @param directory
     * @param packageName
     * @param recursive
     * @param filter
     * @return
     */
    private <T> List<Class<T>> findClassesInDirectory(File directory, String packageName, boolean recursive, Class<T> filter) {
        List<Class<T>> classes = new ArrayList<>();

        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files.length > 0) {
                progressContainer.addProgressStep(files.length);
            }

            // Parcours du dossier
            for (File file : files) {
                if (file.isDirectory()) {
                    if (recursive) {
                        String subPackageName;

                        if (!packageName.isEmpty()) {
                            subPackageName = packageName + "." + file.getName();
                        } else {
                            subPackageName = file.getName();
                        }

                        List<Class<T>> findClasses = findClassesInDirectory(file, subPackageName, recursive, filter);
                        classes.addAll(findClasses);
                    }
                } else {
                    findClassFiltered(classes, filter, file.getName(), packageName);
                }

                progressContainer.addProgressValue(1);
            }
        }
        return classes;
    }

    /**
     * Retourne toutes les classes trouvées dans une bibliothèque JAR.
     *
     * @param jarPath
     * @param packageName
     * @param recursive
     * @param filter
     * @return
     */
    private <T> List<Class<T>> findClassesInJarFile(String jarPath, String packageName, boolean recursive, Class<T> filter) {
        List<Class<T>> classes = new ArrayList<>();

        try {
            int jarLength;

            try (JarFile file = new JarFile(jarPath)) {
                // Nombre de fichier dans le pack
                jarLength = file.size();
            }

            int jarExtractedLength = 0;

            progressContainer.addProgressStep(jarLength);

            // Ouverture du flux au format Zip
            try (JarInputStream jarInput = new JarInputStream(new BufferedInputStream(new FileInputStream(jarPath)))) {
                JarEntry jarEntry;

                // Parcours tous les fichiers
                String packagePath = packageName.replace(".", "/");
                while (jarExtractedLength < jarLength) {
                    // Entrée suivante
                    jarEntry = jarInput.getNextJarEntry();
                    if (jarEntry == null) {
                        break;
                    }

                    String jarEntryPath = jarEntry.getName();

                    if (jarEntryPath.startsWith(packagePath)) {
                        if (!recursive) {
                            String recursivePathTest = jarEntryPath.replace(packagePath, "");

                            if (recursivePathTest.lastIndexOf("/") > 0) {
                                continue;
                            }
                        }

                        findClassFiltered(classes, filter, jarEntryPath.replace("/", "."), null);
                    }

                    jarExtractedLength++;
                    progressContainer.addProgressValue(1);
                }
            }
        } catch (IOException ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return classes;
    }

    /**
     * Applique les critères de recherche sur la classe.
     *
     * @param classes
     * @param filter
     * @param classPath
     * @param packageName
     */
    private static <T> void findClassFiltered(List<Class<T>> classes, Class<T> filter, String classPath, String packageName) {
        if (classPath.endsWith(".class")) {
            Class<T> classFound = getClass(classPath, packageName);

            if (classFound != null) {
                if (filter == null
                    || (filter.isAssignableFrom(classFound))) {
                    classes.add(classFound);
                }
            }
        }
    }

    /**
     * Retourne l'instance de la classe.
     * Si aucune classe trouvée, retourne
     * <code>null</code>.
     *
     * @param <T>
     * @param classPath Nom de la classe (avec ou sans le chemin)
     * @param packageName Chemin complèmentaire
     * @param initialize
     * @param loader
     * @return Class<?> or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String classPath, String packageName, boolean initialize, ClassLoader loader) {
        Class<T> classFound = null;
        String className = classPath;

        // Suppression de l'extension
        if (className.endsWith(".class")) {
            className = classPath.substring(0, classPath.length() - 6);
        }

        // Ajout du chemin du package
        if (packageName != null) {
            className = packageName + "." + className;
        }

        try {
            classFound = (Class<T>) Class.forName(className, initialize, loader);
        } catch (LinkageError | ClassNotFoundException ex) {
            LoggerManager.getInstance().addError(ex.getMessage());
        }
        return classFound;
    }

    /**
     * Retourne l'instance de la classe.
     * Si aucune classe trouvée, retourne
     * <code>null</code>.
     *
     * @param <T>
     * @param classPath Nom de la classe (avec ou sans le chemin)
     * @param packageName Chemin complèmentaire
     * @return Class<?> or <code>null</code>.
     */
    public static <T> Class<T> getClass(String classPath, String packageName) {
        // Recherche de la classe avec initialisation
        Class<T> classFound = getClass(classPath, packageName, true, Thread.currentThread().getContextClassLoader());

        if (classFound == null) {
            // Classe non trouvée ou impossible d'initialiser la classe
            classFound = getClass(classPath, packageName, false, Thread.currentThread().getContextClassLoader());
        }
        return classFound;
    }

    /**
     * Retourne l'instance de la classe.
     * Si aucune classe trouvée, retourne
     * <code>null</code>.
     *
     * @param <T>
     * @param classPath Nom de la classe (avec ou sans le chemin)
     * @return Class<?> or <code>null</code>.
     */
    public static <T> Class<T> getClass(String classPath) {
        return getClass(classPath, null);
    }

    /**
     * Retourne l'addresse de la classe.
     *
     * @param currentClass
     * @return
     */
    public static URL getPath(Class<?> currentClass) {
        String path = "/" + currentClass.getName().replace(".", "/") + ".class";
        return currentClass.getResource(path);
    }

    /**
     * Détermine le contexte d'execution.
     *
     * @return
     */
    public static boolean isJarFile() {
        boolean rslt = false;

        if (FactoryManager.hasInstance(MainManager.class)) {
            URL url = getPath(MainManager.getInstance().getClass());
            rslt = url.getProtocol().equalsIgnoreCase("jar");
        }
        return rslt;
    }

    /**
     * Création d'une nouvelle instance de la classe précisée.
     *
     * @param <T>
     * @param type
     * @param initargs
     * @return
     */
    public static <T> T makeInstance(Class<T> type, Object... initargs) {
        T instance = null;

        if (type != null) {
            try {
                Constructor<T> constructor;
                int numberOfArgs = (initargs != null ? initargs.length : 0);

                if (numberOfArgs > 0) {
                    Class<?>[] parameterTypes = new Class<?>[numberOfArgs];

                    for (int i = 0; i < numberOfArgs; i++) {
                        if (initargs[i] != null) {
                            parameterTypes[i] = initargs[i].getClass();
                        } else {
                            parameterTypes[i] = Object.class;
                        }
                    }
                    constructor = type.getConstructor(parameterTypes);
                } else {
                    constructor = type.getDeclaredConstructor();
                }

                if (constructor != null) {
                    constructor.setAccessible(true);

                    if (numberOfArgs > 0) {
                        instance = constructor.newInstance(initargs);
                    } else {
                        instance = constructor.newInstance();
                    }
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
        return instance;
    }

    /**
     * Retourne le nom de la classe appelante.
     *
     * @param backList
     * @return
     */
    public static String searchClassName(List<String> backList) {
        String className = null;

        Throwable throwable = new Throwable();
        StackTraceElement[] trace = throwable.getStackTrace();

        backList.add(PackagesHelper.class.getCanonicalName());

        for (StackTraceElement stackTraceElement : trace) {
            className = stackTraceElement.getClassName();

            if (backList.contains(className)) {
                continue;
            }

            int dotIndex = className.lastIndexOf(".");

            if (dotIndex >= 0) {
                className = className.substring(dotIndex + 1);
            }
            break;
        }
        return className;
    }
}
