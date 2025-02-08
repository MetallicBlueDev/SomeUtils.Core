package com.tr4ncer.taskprogress;

/**
 * Etat de la progression.
 *
 * @version 2.00.00
 * @author Sebastien Villemain
 */
public enum TaskProgressState {

    /**
     * La progression est en attente: impossible de déterminer la progression.
     */
    PROGRESS_WAITING,

    /**
     * La progression est en cours.
     */
    PROGRESS_BEING,

    /**
     * La progression est terminée: aucune opération en attente.
     */
    PROGRESS_COMPLETED;
}
