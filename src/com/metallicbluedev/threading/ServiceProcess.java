package com.metallicbluedev.threading;

/**
 * Procédure pour un service standart.
 *
 * @version 1.07.06
 * @author Sebastien Villemain
 */
public interface ServiceProcess extends Runnable {

    /**
     * Procédure de démarrage du service.
     */
    public void start();

    /**
     * Procédure d'arrêt du service.
     */
    public void stop();

    /**
     * Détermine l'état du service.
     *
     * @return
     */
    public boolean running();
}
