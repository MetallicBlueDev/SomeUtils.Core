package com.metallicbluedev.core;

import com.metallicbluedev.*;
import com.metallicbluedev.factory.*;
import java.nio.file.*;

/**
 *
 * @author Sébastien Villemain
 */
public interface SettingManager extends EntityProcess, EventObservable<SettingListener> {

    /**
     * Charge la configuration.
     *
     * @param path chemin du fichier de configuration.
     * @return boolean true succes.
     */
    public boolean load(Path path);

    /**
     * Etat du chargement.
     *
     * @return
     */
    public boolean loaded();

    /**
     * Sauvegarde la configuration actuelle.
     *
     * @return boolean true succes.
     */
    public boolean save();

    public boolean canWriteLogFile();

    public String getLogFilePath();

    public int getLogMaxBackup();

    public int getLogMaxLength();

    public static SettingManager getInstance() {
        return FactoryManager.getInstance(SettingManager.class);
    }

}
