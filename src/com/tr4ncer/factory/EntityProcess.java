package com.tr4ncer.factory;

/**
 * Procédure standard d'une entité.
 *
 * @version 3.00.00
 * @author Sebastien Villemain
 */
public interface EntityProcess {

    /**
     * Retourne le nom du processus.
     *
     * @return
     */
    public default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Retourne le information sur le processus.
     *
     * @return
     */
    public String getInformation();

    /**
     * Création de l'entité.
     */
    public void createProcess();

    /**
     * Destruction complète de l'entité.
     */
    public void destroyProcess();
}
