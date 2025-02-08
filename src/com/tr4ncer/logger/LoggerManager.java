package com.tr4ncer.logger;

import com.tr4ncer.core.*;
import com.tr4ncer.factory.*;
import com.tr4ncer.utils.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.event.*;

/**
 * Le journal d'evenements.
 * Publication d'événement dans le journal avec le minimum d'impact sur les performances de l'application.
 *
 * @version 3.00.00
 * @author Sebastien Villemain
 */
public final class LoggerManager implements CentralizedLoggerManager {

    /**
     * Extension du fichier journal.
     */
    public static final String EXTENSION_NAME = "log";

    private static final Object LOCKER = new Object();

    /**
     * Journal principal.
     */
    private final Logger baseLogger;

    /**
     * Liste des écouteurs d'événement.
     */
    private final EventListenerList listeners;

    /**
     * Liste des derniers messages.
     */
    private final List<LoggerEvent> records = new ArrayList<>();

    /**
     * Flux vers le fichier.
     */
    private FileHandler loggerFileHandler;

    /**
     * Détermine si une réinitialisation est nécessaire.
     */
    private boolean resetLoggerFile = false;

    /**
     * Flux vers la console.
     */
    private ConsoleHandler loggerConsoleHandler;

    /**
     * Chemin vers le fichier de log.
     */
    private String filePath = null;

    /**
     * Nombre maximum de log à archiver.
     */
    private int maxBackup = 0;

    /**
     * Taille maximum d'un fichier.
     */
    private int maxLength = 0;

    /**
     * Listeur de message.
     */
    public LoggerManager() {
        this(null, 0, 0);
    }

    /**
     * Listeur de message.
     *
     * @param filePath
     * @param maxBackup
     * @param maxLength
     */
    public LoggerManager(String filePath, int maxBackup, int maxLength) {
        baseLogger = createLogger();
        baseLogger.setUseParentHandlers(false);
        listeners = new EventListenerList();

        setFilePath(filePath);
        setMaxBackup(maxBackup);
        setMaxLength(maxLength);
    }

    @Override
    public String getBaseLoggerName() {
        return baseLogger.getName();
    }

    /**
     * Retourne le journal d'événement central.
     *
     * @return
     */
    public static CentralizedLoggerManager getInstance() {
        synchronized (LOCKER) {
            return !FactoryManager.isDisposing() ? FactoryManager.getInstance(LoggerManager.class) : new FakeLoggerManager();
        }
    }

    @Override
    public void setFilePath(String filePath) {
        if (filePath != null) {
            this.filePath = FileHelper.setExtensionName(filePath, EXTENSION_NAME).getAbsolutePath();
            resetLoggerFile = true;
        }
    }

    @Override
    public void setMaxBackup(int maxBackup) {
        this.maxBackup = maxBackup;
        resetLoggerFile = true;
    }

    @Override
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        resetLoggerFile = true;
    }

    /**
     * Accepte tous les messages.
     */
    public void setLevelAll() {
        if (baseLogger.getLevel() != Level.ALL) {
            baseLogger.setLevel(Level.ALL);

            if (loggerFileHandler != null) {
                loggerFileHandler.setLevel(Level.ALL);
            }

            if (loggerConsoleHandler != null) {
                loggerConsoleHandler.setLevel(Level.ALL);
            }
        }
    }

    @Override
    public void setLevelOff() {
        if (baseLogger.getLevel() != Level.OFF) {
            baseLogger.setLevel(Level.OFF);

            if (loggerFileHandler != null) {
                loggerFileHandler.setLevel(Level.OFF);
            }

            if (loggerConsoleHandler != null) {
                loggerConsoleHandler.setLevel(Level.OFF);
            }
        }
    }

    @Override
    public void addListener(LoggerListener listener) {
        listeners.add(LoggerListener.class, listener);
    }

    @Override
    public void removeListener(LoggerListener listener) {
        listeners.remove(LoggerListener.class, listener);
    }

    /**
     * Détermine si la configuration permet l'écriture du journale.
     *
     * @return
     */
    public boolean canWrite() {
        boolean allowed = true;

        // Si une configuration est disponible
        if (FactoryManager.hasInstance(SettingManager.class)) {
            // Et que la configuration ne veux pas de log
            if (!FactoryManager.getInstance(SettingManager.class).canWriteLogFile()) {
                // On saute la sauvegarde
                allowed = false;
            }
        }

        if (allowed) {
            try {
                File folder = new File(filePath);
                FileHelper.checkFolder(folder.getParentFile(), true);
            } catch (IOException ex) {
                addError(ex);
                allowed = false;
            }
        }
        return allowed;
    }

    @Override
    public void addMessage(LogRecord lr) {
        if (lr != null) {
            if (FactoryManager.hasInstance(LoggerNotificator.class)) {
                LoggerNotificator notificator = FactoryManager.getInstance(LoggerNotificator.class);

                if (notificator.isAlive()) {
                    // Permet de remonter la pile d'appel pour définir la classe source
                    if (lr.getThrown() == null) {
                        lr.setThrown(new Throwable());
                    }

                    notificator.appendMessage(this, lr);
                }
            }
        }
    }

    @Override
    public void addDebug(String message) {
        addMessage(new LogRecord(Level.FINE, message));
    }

    @Override
    public void addInformation(String message) {
        addMessage(new LogRecord(Level.INFO, message));
    }

    @Override
    public void addWarning(String message) {
        addMessage(new LogRecord(Level.WARNING, message));
    }

    @Override
    public void addError(String message) {
        StringBuilder details = new StringBuilder();
        details.append(message);
        details.append(StringHelper.LINE_SEPARATOR);
        details.append("Thread name: ");
        details.append(Thread.currentThread().getName());

        addMessage(new LogRecord(Level.SEVERE, details.toString()));
    }

    @Override
    public void addError(Exception error) {
        String message = (error != null ? (error.getMessage() != null && !error.getMessage().isEmpty() ? error.getMessage() : error.getClass().getSimpleName()) : "Exception is null.");

        StringBuilder details = new StringBuilder();
        details.append(message);
        details.append(StringHelper.LINE_SEPARATOR);
        details.append("Thread name: ");
        details.append(Thread.currentThread().getName());

        LogRecord record = new LogRecord(Level.SEVERE, details.toString());
        record.setThrown(error);
        addMessage(record);
    }

    @Override
    public void addError(String message, Exception ex) {
        StringBuilder details = new StringBuilder();
        details.append(message);
        details.append(StringHelper.LINE_SEPARATOR);
        details.append("Thread name: ");
        details.append(Thread.currentThread().getName());

        LogRecord record = new LogRecord(Level.SEVERE, details.toString());
        record.setThrown(ex);
        addMessage(record);
    }

    @Override
    public boolean isEmpty() {
        synchronized (records) {
            return records.isEmpty();
        }
    }

    @Override
    public String getLastDebugMessage() {
        return getLastMessage(Level.FINE);
    }

    @Override
    public LoggerEvent getLastDebugEvent() {
        return getLastEvent(Level.FINE);
    }

    @Override
    public String getLastInformationMessage() {
        return getLastMessage(Level.INFO);
    }

    @Override
    public LoggerEvent getLastInformationEvent() {
        return getLastEvent(Level.INFO);
    }

    @Override
    public String getLastWarningMessage() {
        return getLastMessage(Level.WARNING);
    }

    @Override
    public LoggerEvent getLastWarningEvent() {
        return getLastEvent(Level.WARNING);
    }

    @Override
    public String getLastErrorMessage() {
        return getLastMessage(Level.SEVERE);
    }

    @Override
    public LoggerEvent getLastErrorEvent() {
        return getLastEvent(Level.SEVERE);
    }

    @Override
    public String getLastMessage() {
        return getLastMessage(Level.ALL);
    }

    @Override
    public LoggerEvent getLastEvent() {
        return getLastEvent(Level.ALL);
    }

    @Override
    public String getInformation() {
        return baseLogger.toString();
    }

    @Override
    public void createProcess() {
        if (canConfigure()) {
            initialize();
            checkHandlers();
            addLoggerThread();
        }
    }

    @Override
    public void destroyProcess() {
        closeLogger();
        clearRecords();
        removeLoggerThread();
    }

    /**
     * Publication du message.
     *
     * @param message
     */
    void digestMessage(LogRecord message) {
        if (message != null) {
            LoggerEvent event = new LoggerEvent(message, this);
            baseLogger.log(event.getRecord());
            addRecord(event);
            notifyLoggerListenerOnChanged(event);
        }
    }

    /**
     * Vérification des flux instanciés.
     */
    void checkHandlers() {
        if (canConfigure() && resetLoggerFile) {
            checkConsoleHandler();
            checkFileHandler();

            if (baseLogger.getLevel() == null) {
                setLevelAll();
            }
        }
    }

    private void addLoggerThread() {
        synchronized (LOCKER) {
            if (FactoryManager.hasInstance(LoggerNotificator.class)) {
                FactoryManager.getInstance(LoggerNotificator.class).addPooledThread();
            }
        }
    }

    private void addRecord(LoggerEvent event) {
        synchronized (records) {
            records.add(event);
        }
    }

    private void checkConsoleHandler() throws SecurityException {
        if (loggerConsoleHandler == null) {
            openConsoleHandler();
        }
    }

    private void checkFileHandler() throws SecurityException {
        if (loggerFileHandler == null
            || resetLoggerFile) {
            closeFileHandler();

            if (canWrite()) {
                openFileHandler();
            }
            resetLoggerFile = false;
        }
    }

    private void clearRecords() {
        synchronized (records) {
            records.clear();
        }
    }

    private void closeConsoleHandler() throws SecurityException {
        if (loggerConsoleHandler != null) {
            baseLogger.removeHandler(loggerConsoleHandler);
            loggerConsoleHandler.close();
        }
    }

    private void closeFileHandler() throws SecurityException {
        if (loggerFileHandler != null) {
            baseLogger.removeHandler(loggerFileHandler);
            loggerFileHandler.close();
        }
    }

    private void closeLogger() throws SecurityException {
        closeFileHandler();
        closeConsoleHandler();
    }

    private void initialize() {
        SettingManager settingManager = FactoryManager.getInstance(SettingManager.class);

        if (settingManager != null) {
            if (filePath == null) {
                setFilePath(settingManager.getLogFilePath());
            }

            if (maxBackup == 0) {
                setMaxBackup(settingManager.getLogMaxBackup());

            }

            if (maxLength == 0) {
                setMaxLength(settingManager.getLogMaxLength());
            }
        }
    }

    private String makeLoggerName() throws ExceptionInInitializerError {
        String loggerName = null;

        if (canConfigure()) {
            LoggerNotificator notificator = FactoryManager.getInstance(LoggerNotificator.class);

            if (notificator == null) {
                throw new ExceptionInInitializerError();
            }

            if (notificator.getPooledThreadIndex() > 0) {
                loggerName = ".Index" + notificator.getPooledThreadIndex();
            }
        }
        return loggerName;
    }

    private Logger createLogger() {
        String loggerName = makeLoggerName();
        return Logger.getLogger("Log" + (loggerName != null ? loggerName : ""));
    }

    /**
     * Retourne le dernier message enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @param logLevel
     * @return
     */
    private String getLastMessage(Level logLevel) {
        String message = null;
        LoggerEvent lastEvent = getLastEvent(logLevel);

        if (lastEvent != null) {
            message = lastEvent.getRecord().getMessage();
        }
        return message;
    }

    /**
     * Retourne le dernier message enregistré.
     * Si aucun message, retourne <code>null</code>.
     *
     * @param logLevel
     * @return
     */
    private LoggerEvent getLastEvent(Level logLevel) {
        LoggerEvent lastEvent = null;

        synchronized (records) {
            for (LoggerEvent event : records) {
                if (lastEvent == null) {
                    lastEvent = event;
                }

                if (logLevel != null
                    || logLevel != Level.ALL) {
                    if (logLevel != event.getRecord().getLevel()) {
                        continue;
                    }
                }

                if (lastEvent.getRecord().getMillis() < event.getRecord().getMillis()) {
                    lastEvent = event;
                }
            }
        }
        return lastEvent;
    }

    private static boolean canConfigure() {
        return FactoryManager.hasInstance(MainManager.class)
               && !MainManager.getInstance().stopImminent();
    }

    private void notifyLoggerListenerOnChanged(LoggerEvent event) {
        // Transfert du message
        for (LoggerListener listener : listeners.getListeners(LoggerListener.class)) {
            listener.onChanged(event);
        }
    }

    private void openConsoleHandler() throws SecurityException {
        if (loggerConsoleHandler == null) {
            loggerConsoleHandler = new ConsoleHandler();
            loggerConsoleHandler.setFormatter(new LoggerFormatter());
            baseLogger.addHandler(loggerConsoleHandler);
        }
    }

    private void openFileHandler() throws SecurityException {
        if (loggerFileHandler == null) {
            try {
                loggerFileHandler = new FileHandler(filePath, maxLength, maxBackup, true);
            } catch (IOException | SecurityException ex) {
                addError(ex);
            }

            if (loggerFileHandler != null) {
                loggerFileHandler.setFormatter(new LoggerFormatter());
                baseLogger.addHandler(loggerFileHandler);
            }
        }
    }

    private void removeLoggerThread() {
        synchronized (LOCKER) {
            if (FactoryManager.hasInstance(LoggerNotificator.class)) {
                // Demande la suppression d'un thread
                FactoryManager.getInstance(LoggerNotificator.class).removePooledThread();
            }
        }
    }
}
