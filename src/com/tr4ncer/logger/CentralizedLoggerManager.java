package com.tr4ncer.logger;

import com.tr4ncer.*;
import com.tr4ncer.factory.*;
import java.util.logging.*;

/**
 *
 * @author Sébastien Villemain
 */
public interface CentralizedLoggerManager extends EntityProcess, EventObservable<LoggerListener> {

    /**
     * Retourne le nom de base du journal.
     *
     * @return
     */
    public String getBaseLoggerName();

    /**
     * Affecte le chemin complet vers le fichier de journal.
     *
     * @param filePath
     */
    public void setFilePath(String filePath);

    /**
     * Affecte le nombre maximum de fichier de journal possible.
     *
     * @param maxBackup
     */
    public void setMaxBackup(int maxBackup);

    /**
     * Affecte la taille maximum du fichier de journal.
     *
     * @param maxLength
     */
    public void setMaxLength(int maxLength);

    /**
     * Annule tous les messages.
     */
    public void setLevelOff();

    /**
     * Ajout d'un message d'événement.
     *
     * @param message
     */
    public void addMessage(LogRecord message);

    /**
     * Ajout d'un message de debug.
     *
     * @param message
     */
    public void addDebug(String message);

    /**
     * Ajout d'un message d'information.
     *
     * @param message
     */
    public void addInformation(String message);

    /**
     * Ajout d'un message d'avertissement.
     *
     * @param message
     */
    public void addWarning(String message);

    /**
     * Ajout d'un message d'erreur.
     *
     * @param message
     */
    public void addError(String message);

    /**
     * Ajout d'un message d'erreur.
     *
     * @param error
     */
    public void addError(Exception error);

    /**
     * Ajout d'un message d'erreur.
     *
     * @param message
     * @param ex
     */
    public void addError(String message, Exception ex);

    /**
     * Détermine si des messages sont disponibles.
     *
     * @return
     */
    public boolean isEmpty();

    /**
     * Retourne le dernier message du mode debug enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public String getLastDebugMessage();

    /**
     * Retourne le dernier message du mode debug enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public LoggerEvent getLastDebugEvent();

    /**
     * Retourne le dernier message d'information enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public String getLastInformationMessage();

    /**
     * Retourne le dernier message d'information enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public LoggerEvent getLastInformationEvent();

    /**
     * Retourne le dernier message d'avertissement enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public String getLastWarningMessage();

    /**
     * Retourne le dernier message d'avertissement enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public LoggerEvent getLastWarningEvent();

    /**
     * Retourne le dernier message d'erreur enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public String getLastErrorMessage();

    /**
     * Retourne le dernier message d'erreur enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public LoggerEvent getLastErrorEvent();

    /**
     * Retourne le dernier message enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public String getLastMessage();

    /**
     * Retourne le dernier message enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @return
     */
    public LoggerEvent getLastEvent();
}
