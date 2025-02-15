package com.metallicbluedev.utils;

import com.metallicbluedev.logger.*;
import com.metallicbluedev.taskprogress.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.stream.*;

/**
 *
 * @author Sébastien Villemain
 */
public class FileHelper {

    /**
     * Séparateur de ligne.
     */
    public static final String FILE_SEPARATOR = getFileSeparator();

    /**
     * Taille du tampon pour la lecture d'une donnée.
     */
    public static final int OPTIMIZED_BUFFER_SIZE = getOptimizedBufferSize();

    private final TaskProgressContainer progressContainer;

    public FileHelper() {
        this(new TaskProgressContainer());
    }

    public FileHelper(TaskProgressContainer progressContainer) {
        this.progressContainer = progressContainer;
    }

    public static List<FileStore> getFileStores() {
        List<FileStore> fileStores = new ArrayList<>();

        try {
            for (FileStore fileStore : FileSystems.getDefault().getFileStores()) {
                fileStores.add(fileStore);
            }
        } catch (Exception ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return Collections.unmodifiableList(fileStores);
    }

    public static boolean moveToPath(Path sourceFile, Path targetDirectory) {
        boolean success = false;
        int retry = 4;

        do {
            try {
                Path targetWithFileName = targetDirectory.resolve(sourceFile.getFileName());
                Files.move(sourceFile, targetWithFileName, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                success = true;
            } catch (IOException ex) {
                if (ex.getMessage().contains("The process cannot access the file because it is being used by another process") || ex.getMessage().contains("Le processus ne peut pas")) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex2) {
                        LoggerManager.getInstance().addError(ex);
                    }

                    retry--;
                } else {
                    retry = 0;
                }

                if (retry <= 0) {
                    LoggerManager.getInstance().addError(ex);
                }
            }
        } while (!success && retry > 0);

        if (!success) {
            LoggerManager.getInstance().addError("Unable to move " + sourceFile.toString() + " to " + targetDirectory.toString());
        }
        return success;
    }

    public static Set<File> listNames(String dir, boolean fileMode) {
        return Stream.of(new File(dir).listFiles())
            .filter(file -> (fileMode && !file.isDirectory()) || (!fileMode && file.isDirectory()))
            .collect(Collectors.toSet());
    }

    /**
     * Vérifie la présence du fichier et tente de le créer si demandé.
     * En cas de problème, lance une exception.
     *
     * @param file Instance du fichier à tester.
     * @param canCreate true pour tenter la création du fichier si il n'existe pas.
     * @throws IOException Signifie qu'il est impossible d'utiliser le fichier.
     */
    public static void checkFile(File file, boolean canCreate) throws IOException {
        checkFile(file, canCreate, false);
    }

    /**
     * Vérifie la présence du fichier et tente de le créer si demandé.
     * En cas de problème, lance une exception.
     *
     * @param filePath Instance du fichier à tester.
     * @param canCreate true pour tenter la création du fichier si il n'existe pas.
     * @throws IOException Signifie qu'il est impossible d'utiliser le fichier.
     */
    public static void checkFile(String filePath, boolean canCreate) throws IOException {
        if (filePath == null) {
            throw new IOException("File path is null.");
        }

        File file = getFile(filePath);
        checkFile(file, canCreate);
    }

    /**
     * Vérifie la présence du dossier et tente de le créer si demandé.
     * En cas de problème, lance une exception.
     *
     * @param file Instance du fichier à tester.
     * @param canCreate true pour tenter la création du fichier si il n'existe pas.
     * @throws IOException Signifie qu'il est impossible d'utiliser le fichier.
     */
    public static void checkFolder(File file, boolean canCreate) throws IOException {
        checkFile(file, canCreate, true);
    }

    /**
     * Vérifie la présence du dossier et tente de le créer si demandé.
     * En cas de problème, lance une exception.
     *
     * @param folderPath Instance du fichier à tester.
     * @param canCreate true pour tenter la création du fichier si il n'existe pas.
     * @throws IOException Signifie qu'il est impossible d'utiliser le fichier.
     */
    public static void checkFolder(String folderPath, boolean canCreate) throws IOException {
        if (folderPath == null) {
            throw new IOException("File path is null.");
        }

        File folder = getFile(folderPath);
        checkFolder(folder, canCreate);
    }

    /**
     * Retourne le chemin interne suivant le contexte d'execution.
     * En cas d'erreur, retourne
     * <code>null</code>.
     *
     * @param localPath
     * @return String or <code>null</code>
     */
    public static URL getInternalRootPath(String localPath) {
        URL rootPath = null;

        // Récupère la pile d'appel
        Throwable throwable = new Throwable();
        StackTraceElement[] trace = throwable.getStackTrace();

        // Recherche dans la pile
        Class<?> currentElementClass = null;
        for (StackTraceElement element : trace) {
            Class<?> elementClass = PackagesHelper.getClass(element.getClassName());

            if (currentElementClass == null) {
                currentElementClass = elementClass;
            }

            if (elementClass.equals(currentElementClass)) {
                // Supprime les appels à cette classe
                continue;
            }

            try {
                rootPath = elementClass.getResource(localPath);
            } catch (Exception ex) {
                LoggerManager.getInstance().addError(ex);
            }

            if (rootPath != null) {
                break;
            }
        }
        return rootPath;
    }

    /**
     * Retourne l'instance du chemin vers le fichier.
     * Reconnait tout type de séparateur de chemin.
     *
     * @param fileUrl
     * @return File[2] 0 => Cible principal; 1 => Cible secondaire
     */
    public static File[] getFile(URL fileUrl) {
        String masterFilePath = null;
        String slavefilePath = null;

        if (fileUrl != null && fileUrl.getProtocol() != null && !fileUrl.getProtocol().isEmpty()) {
            masterFilePath = fileUrl.toExternalForm();

            if (masterFilePath != null) {
                // Permet d'éviter les problèmes d'espace et autre...
                try {
                    masterFilePath = URLDecoder.decode(masterFilePath, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    LoggerManager.getInstance().addError(ex);
                }

                if (masterFilePath.startsWith("jar:file:")) {
                    // Exemple: jar:file:/E:/NetbeansProjects/Library/dist/Library.jar!/trstudio/classlibrary/util/PackagesHelper.class
                    // Suppression du protocole jar:file:
                    int index = masterFilePath.indexOf("!");

                    if (index >= 0) {
                        slavefilePath = masterFilePath.substring(index + 1, masterFilePath.length());
                        masterFilePath = masterFilePath.substring(9, index);

                        if (slavefilePath.startsWith("/")) {
                            slavefilePath = slavefilePath.substring(1);
                        }
                    }
                } else if (masterFilePath.startsWith("file:")) {
                    // Suppression du protocole file:
                    masterFilePath = masterFilePath.substring(5, masterFilePath.length());
                }
            }
        } else if (fileUrl != null) {
            try {
                masterFilePath = URLDecoder.decode(fileUrl.getPath(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
        return new File[]{
            getFile(masterFilePath),
            getFile(slavefilePath)};
    }

    /**
     * Retourne le nom réel du fichier (sans extension).
     *
     * @see #getFileName(java.io.File)
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath) {
        File file = getFile(filePath);
        return getFileName(file);
    }

    /**
     * Retourne le nom réel du fichier (sans extension).
     *
     * @param file
     * @return
     */
    public static String getFileName(File file) {
        String fileName = getFullFileName(file);

        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            fileName = fileName.substring(0, index);
        } else {
            // Le fichier n'a pas de nom
            // Cas possible pour les fichiers du type ".lock"
            fileName = "";
        }
        return fileName;
    }

    /**
     * Retourne le nom complet du fichier (avec extension).
     *
     * @see #getFullFileName(java.io.File)
     * @param filePath
     * @return
     */
    public static String getFullFileName(String filePath) {
        File file = getFile(filePath);
        return getFullFileName(file);
    }

    /**
     * Retourne le nom complet du fichier (avec extension).
     *
     * @param file
     * @return
     */
    public static String getFullFileName(File file) {
        if (file == null) {
            file = new File("");
        }

        // Reconnait tout type de séparateur de chemin
        return file.getName();
    }

    /**
     * Retourne le nom réel de l'extension (sans le point).
     *
     * @see #getExtensionName(java.io.File)
     * @param filePath
     * @return
     */
    public static String getExtensionName(String filePath) {
        File file = getFile(filePath);
        return getExtensionName(file);
    }

    /**
     * Retourne le nom réel de l'extension (sans le point).
     *
     * @param file
     * @return
     */
    public static String getExtensionName(File file) {
        String extensionName = getFullFileName(file);

        int index = extensionName.lastIndexOf(".");

        if (index >= 0 && (index + 1) < extensionName.length()) {
            extensionName = extensionName.substring(index + 1);
        } else {
            // Le fichier n'a pas d'extension
            extensionName = "";
        }
        return extensionName;
    }

    /**
     * Retourne le fichier avec l'extension demandée.
     *
     * @param filePath
     * @param extensionName
     * @return
     */
    public static File setExtensionName(String filePath, String extensionName) {
        File file = getFile(filePath);
        return setExtensionName(file, extensionName);
    }

    /**
     * Retourne le fichier avec l'extension demandée.
     *
     * @param file
     * @param extensionName
     * @return
     */
    public static File setExtensionName(File file, String extensionName) {
        File fileWithExtensionName;
        String extension = getExtensionName(file);

        // Nettoyage de l'extension à ajouter
        if (extensionName == null || extensionName.isEmpty() || (extensionName.charAt(0) == '.' && extensionName.length() == 1)) {
            extensionName = "";
        } else if (extensionName.charAt(0) == '.') {
            extensionName = extensionName.substring(1, extensionName.length());
        }

        if (!extension.equals(extensionName)) {
            String path = file.getAbsolutePath();

            if (path.length() > 0) {
                // Nettoyage de l'ancienne extension
                if (extension.length() > 0) {
                    path = path.substring(0, path.lastIndexOf(extension));
                }

                boolean hasSeparator = path.charAt(path.length() - 1) == '.';

                // Intégration de l'extension
                if (!extensionName.isEmpty()) {
                    // Ajout du séparateur
                    if (!hasSeparator) {
                        path += ".";
                    }

                    // Ajout de l'extension
                    path += extensionName;
                } else {
                    // Mode de suppression complet de l'extension
                    if (hasSeparator) {
                        path = path.substring(0, path.length() - 1);
                    }
                }
            }

            fileWithExtensionName = getFile(path);
        } else {
            fileWithExtensionName = file;
        }
        return fileWithExtensionName;
    }

    /**
     * Détermine si le fichier a l'extension demandée.
     *
     * @param filePath
     * @param ext Extension sans le point.
     * @return
     */
    public static boolean isExtensionFile(String filePath, String ext) {
        File file = getFile(filePath);
        return isExtensionFile(file, ext);
    }

    /**
     * Détermine si le fichier a l'extension demandée.
     *
     * @param file
     * @param ext Extension sans le point.
     * @return
     */
    public static boolean isExtensionFile(File file, String ext) {
        return getExtensionName(file).equalsIgnoreCase(ext);
    }

    /**
     * Retourne le nom du dossier racine.
     *
     * @see #getRootName(java.io.File)
     * @param filePath
     * @return
     */
    public static String getRootName(String filePath) {
        File file = getFile(filePath);
        return getRootName(file);
    }

    /**
     * Retourne le nom du dossier racine.
     *
     * @param file
     * @return
     */
    public static String getRootName(File file) {
        String rootName = null;

        try {
            Path root = file.toPath().getRoot();

            if (root != null) {
                rootName = root.toString();
            }
        } catch (Exception ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return rootName;
    }

    /**
     * Retourne le nom du dernier dossier.
     *
     * @see #getLastDirectoryName(java.io.File)
     * @param filePath
     * @return
     */
    public static String getLastDirectoryName(String filePath) {
        File file = getFile(filePath);
        return getLastDirectoryName(file);
    }

    /**
     * Retourne le nom du dernier dossier.
     *
     * @param file
     * @return
     */
    public static String getLastDirectoryName(File file) {
        String directoryName = null;

        if (!file.isDirectory()) {
            File parentFile = file.getParentFile();

            if (parentFile != null) {
                directoryName = parentFile.getName();
            }
        } else {
            directoryName = file.getName();
        }
        return directoryName;
    }

    /**
     * Gérer l'archivage de fichier suivant la taille maximum demandée et le nombre de fichier à archiver.
     * Retourne l'instance du fichier le plus récent dans les archives.
     *
     * @param filePath Chemin vers le fichier.
     * @param maxLength Taille maximum d'un fichier.
     * @param maxBackupFile Nombre maximum de fichier à archiver.
     * @return
     * @throws IOException
     */
    public static File backupFile(String filePath, int maxLength, int maxBackupFile) throws IOException {
        String extensionName = getExtensionName(filePath);

        if (!extensionName.isEmpty()) {
            filePath = filePath.substring(0, filePath.lastIndexOf("." + extensionName)) + "##." + extensionName;
        }

        File file = new File(filePath.replace("##", "0"));

        // Vérification du chemin du fichier
        checkFile(file, true);

        // Vérification de la taille du fichier
        if (maxLength > 1 && file.length() > maxLength) {
            if (maxBackupFile > 1) {
                // Archivage des anciens log
                File fileTester;
                for (int index = maxBackupFile - 1; index >= 0; index--) {
                    fileTester = new File(filePath.replace("##", String.valueOf(index)));

                    if (fileTester.exists()) {
                        if (index == (maxBackupFile - 1)) {
                            fileTester.delete();
                        } else {
                            fileTester.renameTo(new File(filePath.replace("##", String.valueOf(index + 1))));
                        }
                    }
                }
            }
        }
        return file;
    }

    /**
     * Calcul et retourne le checksum du fichier.
     * Retourne
     * <code>null</code> en cas d'échec.
     *
     * @see #getChecksum(java.io.File, long)
     * @param filePath
     * @return
     */
    public byte[] getChecksum(String filePath) {
        File file = getFile(filePath);
        return getChecksum(file);
    }

    /**
     * Calcul et retourne le checksum du fichier.
     * Retourne
     * <code>null</code> en cas d'échec.
     *
     * @see #getChecksum(java.io.File, long)
     * @param numberOfBytesReadedLimit
     * @param filePath
     *
     * @return
     */
    public byte[] getChecksum(String filePath, int numberOfBytesReadedLimit) {
        File file = getFile(filePath);
        return getChecksum(file, numberOfBytesReadedLimit);
    }

    /**
     * Calcul et retourne le checksum (SHA1) du fichier.
     * Retourne
     * <code>null</code> en cas d'échec.
     *
     * @see #getChecksum(java.io.File, long)
     * @param file
     * @return
     */
    public byte[] getChecksum(File file) {
        return getChecksum(file, -1);
    }

    /**
     * Calcul et retourne le checksum (SHA1) du fichier.
     * Retourne
     * <code>null</code> en cas d'échec.
     *
     * @param file
     * @param numberOfBytesReadedLimit Limite le nombre de bytes à lire, permet de gagner du temps sur les très grands fichier.
     * @return
     */
    public byte[] getChecksum(File file, long numberOfBytesReadedLimit) {
        byte[] checksum = null;

        if (numberOfBytesReadedLimit > 0) {
            progressContainer.addProgressStep(numberOfBytesReadedLimit);
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");

            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] data = new byte[OPTIMIZED_BUFFER_SIZE];

                int numberOfBytesReaded;
                int totalNumberOfBytesReaded = 0;

                while ((numberOfBytesReaded = inputStream.read(data)) != -1) {
                    if (numberOfBytesReadedLimit > 0) {
                        progressContainer.addProgressValue(numberOfBytesReaded);
                    }

                    // Vérification de la limite maximum à lire
                    if (numberOfBytesReadedLimit > 0 && totalNumberOfBytesReaded >= numberOfBytesReadedLimit) {
                        break;
                    }

                    messageDigest.update(data, 0, numberOfBytesReaded);
                    totalNumberOfBytesReaded += numberOfBytesReaded;
                }
            }

            checksum = messageDigest.digest();
        } catch (NoSuchAlgorithmException | IOException ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return checksum;
    }

    /**
     * Détermine si les fichiers peuvent être considéré comme identiques.
     *
     * @see #hasChecksumEqual(java.io.File, java.io.File)
     * @param filePath1
     * @param filePath2
     * @return
     */
    public boolean hasChecksumEqual(String filePath1, String filePath2) {
        File file1 = getFile(filePath1);
        File file2 = getFile(filePath2);
        return hasChecksumEqual(file1, file2);
    }

    /**
     * Détermine si les fichiers peuvent être considéré comme identiques.
     *
     * @param file1
     * @param file2
     * @return
     */
    public boolean hasChecksumEqual(File file1, File file2) {
        boolean isEqual = false;

        File smallestFile = file1;
        long smallestFileLength = smallestFile.length();

        File largestFile = file2;
        long largestFileLength = largestFile.length();

        // Optimisation pour gagner du temps sur la comparaison entre un très grand fichier et un petit fichier
        if (smallestFileLength > largestFileLength) {
            long tempLength = smallestFileLength;

            smallestFile = file2;
            smallestFileLength = largestFileLength;

            largestFile = file1;
            largestFileLength = tempLength;
        }

        // Vérification précise uniquement si il y a une insertitude sur la taille
        if (!(largestFileLength > 0) || !(smallestFileLength > 0)
            || (largestFileLength - smallestFileLength) < 2097152) {
            progressContainer.setProgressWaiting();

            // Nous calculons le petit fichier en premier
            byte[] digest1 = getChecksum(smallestFile);

            if (digest1 != null) {
                // Puis nous calculons le plus grand fichier, en limitant la taille
                byte[] digest2 = getChecksum(largestFile, smallestFileLength);

                if (digest2 != null) {
                    isEqual = MessageDigest.isEqual(digest1, digest2);
                }
            }
        }
        return isEqual;
    }

    /**
     * Copie le contenu du fichier source dans le fichier de destination.
     *
     * @param source
     * @param destination
     * @return
     * @throws java.io.IOException
     */
    public boolean copy(File source, File destination) throws IOException {
        boolean rslt;

        progressContainer.addProgressStep(source.length());

        try (FileInputStream sourceInput = new FileInputStream(source); FileOutputStream destinationOutput = new FileOutputStream(destination)) {
            byte[] buffer = new byte[OPTIMIZED_BUFFER_SIZE];
            int numberOfBytesReaded;

            while ((numberOfBytesReaded = sourceInput.read(buffer)) > 0) {
                progressContainer.addProgressValue(numberOfBytesReaded);
                destinationOutput.write(buffer, 0, numberOfBytesReaded);
            }

            rslt = true;
        }

        if (rslt) {
            destination.setLastModified(source.lastModified());
        }
        return rslt;
    }

    /**
     * Retourne l'instance du chemin vers un fichier inexistant.
     *
     * @param source
     * @return
     */
    public static File getFileAvailable(File source) {
        File destination = null;

        if (source != null) {
            destination = source;

            String filePath = setExtensionName(source, "").getAbsolutePath();
            String extensionName = getExtensionName(source);
            int security = 5;

            while (destination.exists() && security >= 0) {
                int newTick = ConvertHelper.toInt(DateHelper.format(new Date(), "mmssSSS"), 0);
                destination = setExtensionName(filePath + "_" + newTick, extensionName);
                security--;
            }
        }
        return destination;
    }

    /**
     * Liste les fichiers de façon récursive en appliquant les filtres demandées.
     *
     * @param rootPath
     * @param fileFilter
     * @param filenameFilter
     * @return
     */
    public static File[] listAllFiles(String rootPath, FileFilter fileFilter, FilenameFilter filenameFilter) {
        List<File> filesList = new ArrayList<>();

        if (rootPath != null) {
            File rootFile = new File(rootPath);
            File[] listFiles = rootFile.listFiles();

            if (listFiles != null) {
                Collections.addAll(filesList, listAllFilesFiltered(rootFile, fileFilter, filenameFilter));

                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        Collections.addAll(filesList, listAllFiles(file.getAbsolutePath(), fileFilter, filenameFilter));
                    }
                }
            }
        }

        File[] files = new File[filesList.size()];
        filesList.toArray(files);
        return files;
    }

    /**
     * Mappe toutes les fichiers de façon récursive.
     *
     * @param mapFiles
     * @param directory
     * @param removePrefix
     */
    public static void listAllFiles(Map<String, Long> mapFiles, File directory, String removePrefix) {
        if (directory != null) {
            if (directory.isDirectory()) {
                File[] listFiles = directory.listFiles();

                if (listFiles != null) {
                    for (File file : listFiles) {
                        if (file != null) {
                            if (file.isDirectory()) {
                                listAllFiles(mapFiles, file, removePrefix);
                            } else {
                                String finalPath = file.getAbsolutePath();

                                // Recherche et supprime le préfixe
                                if (removePrefix != null
                                    && !removePrefix.isEmpty()
                                    && finalPath.startsWith(removePrefix)) {
                                    finalPath = finalPath.substring(removePrefix.length());
                                }

                                mapFiles.put(finalPath, file.lastModified());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retourne le Mime type du fichier.
     *
     * @param filePath
     * @return String[3] 0 => Mime type; 1 => Data type (part 1); 2 => Data info (part 2)
     */
    public static String[] getMimeType(String filePath) {
        String mimeType[] = new String[3];

//        try {
//            mimeType[0] = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
//        } catch (Exception e) {
//            try {
//                mimeType[0] = new MimetypesFileTypeMap().getContentType(filePath);
//            } catch (Exception ex) {
//                  LoggerManager.getInstance().addError(ex);
//            }
//        }
        if (mimeType[0] == null
            || mimeType[0].isEmpty()
            || mimeType[0].equalsIgnoreCase("application/octet-stream")) {
            String urlMimeType = URLConnection.getFileNameMap().getContentTypeFor(filePath);

            if (urlMimeType != null) {
                mimeType[0] = urlMimeType;
            }
        }

        if (mimeType[0] != null) {
            int index = mimeType[0].indexOf("/");

            if (index > 0) {
                mimeType[1] = mimeType[0].substring(0, index).trim().toLowerCase();
                mimeType[2] = mimeType[0].substring(index + 1, mimeType[0].length()).trim();
            }

            mimeType[1] = mimeType[1] != null ? mimeType[1] : "";
            mimeType[2] = mimeType[2] != null ? mimeType[2] : "";
        } else {
            Arrays.fill(mimeType, "");
        }
        return mimeType;
    }

    /**
     * Retourne le Mime type du fichier.
     *
     * @param file
     * @return String[3] 0 => Mime type; 1 => Data type (part 1); 2 => Data info (part 2)
     */
    public static String[] getMimeType(File file) {
        URL fileUrl = null;

        try {
            fileUrl = file.toURI().toURL();
        } catch (MalformedURLException ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return getMimeType(fileUrl);
    }

    /**
     * Retourne le Mime type du fichier.
     *
     * @param fileUrl
     * @return String[3] 0 => Mime type; 1 => Data type (part 1); 2 => Data info (part 2)
     */
    public static String[] getMimeType(URL fileUrl) {
        String filePath = null;

        try {
            filePath = fileUrl.toExternalForm();
        } catch (Exception ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return getMimeType(filePath);
    }

    public static boolean deleteDirectories(Path pathToBeDeleted) throws IOException {
        Files.walk(pathToBeDeleted)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        return Files.exists(pathToBeDeleted);
    }

    /**
     * Retourne l'instance du chemin vers le fichier.
     * Reconnait tout type de séparateur de chemin.
     *
     * @param filePath
     * @return
     */
    private static File getFile(String filePath) {
        if (filePath == null) {
            filePath = "";
        }

        // Reconnait tout type de séparateur de chemin
        File file = new File(filePath);
        return file;
    }

    /**
     * Liste les fichiers en appliquant le filtre.
     *
     * @param rootFile
     * @param fileFilter
     * @param filenameFilter
     * @return
     */
    private static File[] listAllFilesFiltered(File rootFile, FileFilter fileFilter, FilenameFilter filenameFilter) {
        File[] listFiles;

        if (fileFilter != null) {
            listFiles = rootFile.listFiles(fileFilter);
        } else if (filenameFilter != null) {
            listFiles = rootFile.listFiles(filenameFilter);
        } else {
            listFiles = rootFile.listFiles();
        }
        return listFiles;
    }

    /**
     * Vérifie la présence du fichier ou dossier et tente de le créer si demandé.
     * En cas de problème, lance une exception.
     *
     * @param file Instance du fichier à tester.
     * @param canCreate true pour tenter la création du fichier si il n'existe pas.
     * @param isFolder Détermine le type de test (sur un fichier ou un dossier)
     * @throws IOException Signifie qu'il est impossible d'utiliser le fichier.
     */
    private static void checkFile(File file, boolean canCreate, boolean isFolder) throws IOException {
        if (canCreate) {
            if (!file.exists()) {
                String dirsPath = null;

                if (!isFolder) {
                    // Recherche des dossiers de destination
                    int indexOfFileName = file.getPath().indexOf(file.getName());

                    if (indexOfFileName > 0) {
                        dirsPath = file.getPath().substring(0, indexOfFileName);
                    }
                } else {
                    dirsPath = file.getAbsolutePath();
                }

                // Creation des dossiers de destination
                File tempDirs = getFile(dirsPath);
                tempDirs.mkdirs();

                if (!isFolder) {
                    // Creation du fichier de destination
                    file.createNewFile();
                }
            }
        }

        if (!file.exists()) {
            throw new IOException("Path (" + file.getPath() + ") not exists.");
        }

        if (isFolder) {
            if (!file.isDirectory()) {
                throw new IOException("Folder (" + file.getPath() + ") is invalid.");
            }
        } else {
            if (!file.isFile()) {
                throw new IOException("File (" + file.getPath() + ") is invalid.");
            }
        }

        if (canCreate) {
            if (!file.canWrite()) {
                LoggerManager.getInstance().addWarning("Unable to writing file data (" + file.getPath() + ").");
            }
        }

        if (!file.canRead()) {
            LoggerManager.getInstance().addWarning("Unable to reading file data (" + file.getPath() + ").");
        }
    }

    /**
     * Retourne le séparateur de fichier de l'OS.
     *
     * @return
     */
    private static String getFileSeparator() {
        String separator = null;

        try {
            separator = System.getProperty("file.separator");
        } catch (Exception ex) {
            LoggerManager.getInstance().addError(ex);
        }

        if (separator == null || separator.isEmpty()) {
            separator = "/";
        }
        return separator;
    }

    /**
     * Retourne la taille optimisée pour la lecture de donnée.
     *
     * @return
     */
    private static int getOptimizedBufferSize() {
        int bufferSize = 0;

        // TODO SVN Recherche une méthode pour caluler dynamiquement le buffer
        if (bufferSize < 50) {
            bufferSize = 1024 * 4;
        }
        return bufferSize;
    }
}
