package com.tr4ncer.logger;

import java.util.*;
import java.util.logging.*;

/**
 * Evenement du journal.
 *
 * @version 3.00.00
 * @author Sebastien Villemain
 */
public class LoggerEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final LogRecord record;

    /**
     * Nouvel événement du journal.
     *
     * @param record
     * @param manager
     */
    LoggerEvent(LogRecord record, LoggerManager manager) {
        super(manager);
        this.record = record;

        checkLoggerName(record, manager);
        checkSourceClassName(record);
    }

    /**
     * Retourne l'enregistrement d'origine.
     *
     * @return
     */
    public LogRecord getRecord() {
        return record;
    }

    /**
     * Détermine si c'est un message du mode debug.
     *
     * @return
     */
    public boolean isDebug() {
        return record.getLevel() == Level.FINE;
    }

    /**
     * Détermine si c'est un message d'information.
     *
     * @return
     */
    public boolean isInformation() {
        return record.getLevel() == Level.INFO;
    }

    /**
     * Détermine si c'est un message d'avertissement.
     *
     * @return
     */
    public boolean isWarning() {
        return record.getLevel() == Level.WARNING;
    }

    /**
     * Détermine si c'est un message d'erreur.
     *
     * @return
     */
    public boolean isError() {
        return record.getLevel() == Level.SEVERE;
    }

    private void checkLoggerName(LogRecord lr, LoggerManager manager) {
        if (lr.getLoggerName() == null || lr.getLoggerName().isEmpty()) {
            lr.setLoggerName(manager.getBaseLoggerName());
        }
    }

    private void checkSourceClassName(LogRecord lr) {
        if (lr.getSourceClassName() == null && lr.getThrown() != null) {
            String className = null;
            StackTraceElement[] trace = lr.getThrown().getStackTrace();

            for (StackTraceElement stackTraceElement : trace) {
                className = stackTraceElement.getClassName();

                if (className.equalsIgnoreCase(LoggerEvent.class.getCanonicalName())
                    || className.equalsIgnoreCase(LoggerManager.class.getCanonicalName())
                    || className.startsWith(LoggerNotificator.class.getCanonicalName())) {
                    continue;
                }

                int dotIndex = className.lastIndexOf(".");

                if (dotIndex >= 0) {
                    className = className.substring(dotIndex + 1);
                }
                break;
            }

            lr.setSourceClassName(className);
        }
    }

}
