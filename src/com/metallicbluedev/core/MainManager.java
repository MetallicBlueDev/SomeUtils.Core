package com.metallicbluedev.core;

import com.metallicbluedev.factory.*;

/**
 *
 * @author Sébastien Villemain
 */
public interface MainManager extends EntityProcess {

    public String[] getCommands();

    public void setCommands(String[] commands);

    public Class<?> getMainAppClass();

    public void setMainAppClass(Class<?> appClass);

    /**
     * Ajout de la procédure à gérer.
     *
     * @param type
     */
    public void addMainProcess(Class<? extends MainProcess> type);

    public boolean started();

    public boolean stopImminent();

    /**
     * Retourne le chemin absolu vers la racine de l'application (fonctionne avec une application jar ou non).
     *
     * @return
     */
    public String getApplicationPath();

    /**
     * Démarrage du programme.
     */
    public void start();

    /**
     * Redémarrage complet de l'application.
     *
     * @param hardRestart Simulation d'un redémarrage de l'application ou non.
     */
    public void restart(boolean hardRestart);

    /**
     * Arrêt complet de la fenêtre.
     */
    public void stop();

    /**
     * Lance la mise à jour de l'application.
     */
    public void update();

    public void listen();

    public static MainManager getInstance() {
        return FactoryManager.getInstance(MainManager.class);
    }

}
