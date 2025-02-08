package com.tr4ncer.core;

import com.tr4ncer.utils.*;
import java.io.*;

/**
 *
 * @author Sébastien Villemain
 */
public class DefaultMainManager extends GenericMainManager {

    @Override
    public String getApplicationPath() {
        String path;

        // Sous Windows, utilisation du chemin relatif
        if (!SystemHelper.isWindows()) {
            File[] file = FileHelper.getFile(PackagesHelper.getPath(getMainAppClass()));
            path = file[0].getAbsolutePath();

            // Suppression de la classe ou du fichier jar
            int index = path.lastIndexOf(FileHelper.FILE_SEPARATOR);

            if (index >= 0) {
                path = path.substring(0, index);
            }

            if (!FileHelper.isExtensionFile(file[0], "jar")) {
                Package pack = getMainAppClass().getPackage();

                // Suppression du chemin du package
                if (pack != null) {
                    String packPath = pack.getName().replace(".", FileHelper.FILE_SEPARATOR);

                    if (path.endsWith(packPath)) {
                        path = path.substring(0, path.length() - packPath.length());
                    }
                }
            }

            if (!path.endsWith(FileHelper.FILE_SEPARATOR)) {
                path += FileHelper.FILE_SEPARATOR;
            }
        } else {
            path = new File("").getAbsolutePath() + FileHelper.FILE_SEPARATOR;
        }
        return path;
    }

    @Override
    protected ProcessBuilder getRestartProcessBuilder() {
        return null;
    }

    @Override
    protected void onExecuteCommands() {
    }

    @Override
    protected void onStartBeginning() {
    }

    @Override
    protected void onStartEnding() {
    }

    @Override
    protected void onStopBeginning() {
    }

    @Override
    protected void onStopEnding() {
    }

    @Override
    protected void onUpdate() {
    }

    @Override
    protected boolean updateDetected() {
        return false;
    }

}
