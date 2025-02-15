package com.metallicbluedev.core;

import com.metallicbluedev.factory.*;
import com.metallicbluedev.logger.*;
import com.metallicbluedev.utils.*;
import java.io.*;
import java.util.*;

/**
 * Gestion d'un point d'entrée d'une application.
 *
 * @version 2.00.00
 * @author Sebastien Villemain
 */
public abstract class GenericMainManager implements MainManager {

    /**
     * Procédure de construction des entités.
     */
    private final List<Class<? extends MainProcess>> mainProcesses = new ArrayList<>(1);

    /**
     * Détermine si c'est une version de l'application en développement.
     */
    private boolean developmentBuild = false;

    /**
     * Etat de l'application.
     */
    private AppMainMode currentMode = AppMainMode.STOPPED;

    /**
     * Nouvel état de l'application.
     */
    private AppMainMode nextMode = AppMainMode.STOPPED;

    private String[] commands = null;

    private Class<?> mainAppClass = null;

    private boolean running = true;

    private boolean hardRestart = false;

    /**
     * Etat possible de l'application.
     */
    private static enum AppMainMode {
        /**
         * L'application est en cours de démarrage.
         */
        STARTING,

        /**
         * L'application est démarrée.
         */
        STARTED,

        /**
         * L'application est en cours d'arrêt.
         */
        STOPPING,

        /**
         * L'application est arrêtée.
         */
        STOPPED,

        /**
         * L'application est en cours de redémarrage.
         */
        RESTARTING,

        /**
         * L'application est en cours de mise à jour.
         */
        UPDATING;
    }

    protected GenericMainManager() {
        /**
         * Il faut signaler que l'application est en cours de démarrage.
         */
        nextMode = AppMainMode.STARTING;
        currentMode = nextMode;
        mainAppClass = this.getClass();
    }

    /**
     * Change le type de l'application.
     *
     * @param developmentBuild
     */
    public final void setDevelopmentBuild(boolean developmentBuild) {
        this.developmentBuild = developmentBuild;
    }

    /**
     * Détermine si l'application est en cours de développement.
     *
     * @return
     */
    public boolean isDevelopmentBuild() {
        return developmentBuild;
    }

    @Override
    public void setCommands(String[] commands) {
        this.commands = commands;
    }

    /**
     * Retourne la ligne de commande et les paramètres à executer pour l'application.
     *
     * @return
     */
    @Override
    public final String[] getCommands() {
        return commands;
    }

    @Override
    public Class<?> getMainAppClass() {
        return mainAppClass;
    }

    @Override
    public void setMainAppClass(Class<?> mainAppClass) {
        this.mainAppClass = mainAppClass;
    }

    @Override
    public void addMainProcess(Class<? extends MainProcess> type) {
        mainProcesses.add(type);
    }

    @Override
    public final void start() {
        nextMode = AppMainMode.STARTING;
    }

    @Override
    public final void restart(boolean hardRestart) {
        this.hardRestart = hardRestart;
        nextMode = AppMainMode.RESTARTING;

    }

    @Override
    public final void stop() {
        nextMode = AppMainMode.STOPPING;
    }

    @Override
    public final void update() {
        nextMode = AppMainMode.UPDATING;
    }

    private void setNextMode(AppMainMode testMode, AppMainMode nextMode) {
        if (this.nextMode == testMode) {
            this.nextMode = nextMode;
        }
    }

    @Override
    public final void listen() {
        try {
            do {
                switch (nextMode) {
                    case STARTED -> {
                        runMainProcesses();
                        setNextMode(AppMainMode.STARTED, AppMainMode.STOPPED);
                    }
                    case STARTING -> {
                        executeStart();
                        setNextMode(AppMainMode.STARTING, AppMainMode.STARTED);
                    }
                    case STOPPING -> {
                        executeStop();
                        setNextMode(AppMainMode.STOPPING, AppMainMode.STOPPED);
                    }
                    case STOPPED ->
                        waitNextMode();
                    case RESTARTING -> {
                        executeRestart();
                        setNextMode(AppMainMode.RESTARTING, AppMainMode.STOPPED);
                    }
                    case UPDATING -> {
                        executeUpdate();
                        setNextMode(AppMainMode.UPDATING, AppMainMode.STOPPED);
                    }
                    default ->
                        throw new AssertionError();
                }
            } while (running);
        } catch (Exception e) {
            LoggerManager.getInstance().addError(e);
        } finally {
            executeStop();
        }
    }

    @Override
    public String getInformation() {
        StringBuilder builder = new StringBuilder();

        builder.append("System Shell loaded: ");

        for (String shell : SystemHelper.SYSTEM_SHELL) {
            builder.append(shell);
        }

        builder.append(StringHelper.LINE_SEPARATOR);
        builder.append(StringHelper.LINE_SEPARATOR);

        if (!mainProcesses.isEmpty()) {
            builder.append("Main processes:");
            builder.append(StringHelper.LINE_SEPARATOR);

            for (Class<? extends EntityProcess> type : mainProcesses) {
                if (FactoryManager.hasInstance(type)) {
                    builder.append(FactoryManager.getInstance(type).getName());
                    builder.append(StringHelper.LINE_SEPARATOR);
                }
            }
        }
        return builder.toString();
    }

    @Override
    public void createProcess() {
    }

    @Override
    public void destroyProcess() {
    }

    /**
     * Vérifie si l'application a correctement été démarrée.
     *
     * @return
     */
    @Override
    public final boolean started() {
        return currentMode == AppMainMode.STARTED;
    }

    /**
     * Détermine si l'application est arrêtée ou va être arrêtée.
     *
     * @return
     */
    @Override
    public final boolean stopImminent() {
        return currentMode == AppMainMode.STOPPING
               || currentMode == AppMainMode.STOPPED;
    }

    protected abstract void onExecuteCommands();

    /**
     * Début de sous-procédure de démarrage.
     */
    protected abstract void onStartBeginning();

    /**
     * Fin de sous-procédure de démarrage.
     */
    protected abstract void onStartEnding();

    /**
     * Début de la sous-procédure d'arrêt.
     */
    protected abstract void onStopBeginning();

    /**
     * Fin de la sous-procédure d'arrêt.
     */
    protected abstract void onStopEnding();

    protected abstract boolean updateDetected();

    protected abstract void onUpdate();

    protected abstract ProcessBuilder getRestartProcessBuilder();

    private void executeStart() {
        if (currentMode != AppMainMode.STARTED) {
            if (currentMode == AppMainMode.STARTING) {
                LoggerManager.getInstance().addDebug("Booting...");
            } else {
                LoggerManager.getInstance().addDebug("Trying to start main process...");
            }

            // Verrouillage du démarrage
            currentMode = AppMainMode.STARTING;

            // Mise en place du gestionnaire pour les événement de fermeture forcé
            FactoryManager.getInstance(FastShutdownManager.class);

            // Création rapide de la configuration
            // Vérification des commandes
            onExecuteCommands();

            // Début de la sous procédure de démarrage
            try {
                onStartBeginning();
            } catch (Exception e) {
                LoggerManager.getInstance().addError(e);
            }

            if (updateDetected()) {
                // Reprise d'une mise à jour interrompue
                update();
            } else {
                createMainProcesses();
                startMainProcesses();

                // Si la procédure de démarrage est toujours effective
                if (currentMode == AppMainMode.STARTING) {
                    // Fin de la sous procédure de démarrage
                    onStartEnding();

                    // Le démarrage est un succès
                    currentMode = AppMainMode.STARTED;
                }
            }
        }
    }

    private void createMainProcesses() {
        // Création des procédures dans l'ordre
        for (Class<? extends MainProcess> type : mainProcesses) {
            if (currentMode != AppMainMode.STARTING) {
                break;
            }

            try {
                FactoryManager.getInstance(type);
            } catch (Exception e) {
                LoggerManager.getInstance().addError(e);
                executeStop();
                break;
            }
        }
    }

    private void startMainProcesses() {
        // Création des procédures dans l'ordre
        for (Class<? extends MainProcess> type : mainProcesses) {
            if (currentMode != AppMainMode.STARTING) {
                break;
            }

            if (!FactoryManager.hasInstance(type)) {
                continue;
            }

            try {
                MainProcess process = FactoryManager.getInstance(type);

                if (!process.running()) {
                    LoggerManager.getInstance().addDebug("Starting " + process.getName());
                    process.start();
                }
            } catch (Exception e) {
                LoggerManager.getInstance().addError(e);
                executeStop();
                break;
            }
        }
    }

    private void runMainProcesses() {
        // Création des procédures dans l'ordre
        for (Class<? extends MainProcess> type : mainProcesses) {
            if (currentMode != AppMainMode.STARTED) {
                break;
            }

            if (!FactoryManager.hasInstance(type)) {
                continue;
            }

            try {
                MainProcess process = FactoryManager.getInstance(type);

                if (process.canRunInMain()) {
                    LoggerManager.getInstance().addDebug("Running " + process.getName());
                    process.run();
                }
            } catch (Exception e) {
                LoggerManager.getInstance().addError(e);
                executeStop();
                break;
            }
        }
    }

    private void stopMainProcesses() {
        // Arrêt des procédures dans l'ordre inverse
        for (int i = mainProcesses.size() - 1; i >= 0; i--) {
            Class<? extends MainProcess> type = mainProcesses.get(i);

            if (!FactoryManager.hasInstance(type)) {
                continue;
            }

            try {
                MainProcess process = FactoryManager.getInstance(type);

                if (process.running()) {
                    LoggerManager.getInstance().addDebug("Stopping " + process.getName());
                    process.stop();
                }
            } catch (Exception e) {
                LoggerManager.getInstance().addError(e);
                break;
            }
        }
    }

    private void destroyMainProcesses() {
        // Destruction des procédures dans l'ordre inverse
        for (int i = mainProcesses.size() - 1; i >= 0; i--) {
            Class<? extends MainProcess> type = mainProcesses.get(i);

            try {
                FactoryManager.dispose(type);
            } catch (Exception e) {
                LoggerManager.getInstance().addError(e);
            }
        }
    }

    private void waitNextMode() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LoggerManager.getInstance().addError(ex);
        }
    }

    private void executeStop() {
        if (currentMode != AppMainMode.STOPPING
            && currentMode != AppMainMode.STOPPED) {
            if (currentMode != AppMainMode.RESTARTING
                && currentMode != AppMainMode.UPDATING) {
                LoggerManager.getInstance().addDebug("Stopping...");

                // Verrouillage de l'arrêt
                currentMode = AppMainMode.STOPPING;
            } else {
                LoggerManager.getInstance().addDebug("Trying to stop main process...");
            }

            // Début de la sous procédure d'arrêt
            onStopBeginning();

            stopMainProcesses();
            destroyMainProcesses();

            // Fin de la sous procédure d'arrêt
            try {
                onStopEnding();
            } catch (Exception e) {
                LoggerManager.getInstance().addError(e);
            }

            // Arrêt  complet si possible
            stoppingApplication();

            // Arrêt de l'application effectuée
            currentMode = AppMainMode.STOPPED;
        }
    }

    private void executeRestart() {
        if (currentMode != AppMainMode.RESTARTING) {
            LoggerManager.getInstance().addDebug("Restarting...");

            ProcessBuilder pBuilder = null;

            if (hardRestart) {
                pBuilder = getRestartProcessBuilder();
            }

            if (pBuilder != null) {
                // Arrêt complet de l'application
                executeStop();

                // Lance une nouvelle instance de l'application
                try {
                    // Nouvelle instance de l'application à démarrer
                    if (pBuilder.start() != null) {
                        LoggerManager.getInstance().addInformation("Restarting process...");
                    }
                } catch (IOException ex) {
                    LoggerManager.getInstance().addError("Unable to execute command line (" + pBuilder.toString() + ").", ex);
                }
            } else {
                if (currentMode == AppMainMode.STARTED) {
                    // Vérrouillage de la procédure
                    currentMode = AppMainMode.RESTARTING;

                    // Arrêt partiel de l'application
                    executeStop();
                }

                // Si l'application est bien signalé comme arrêtée
                if (currentMode == AppMainMode.STOPPED) {
                    start();
                }
            }
        }

    }

    private void executeUpdate() {
        if (currentMode != AppMainMode.UPDATING) {
            LoggerManager.getInstance().addDebug("Trying to update...");

            AppMainMode oldMode = currentMode;

            // Arrêt de l'application si elle est en cours d'utilisation
            if (currentMode == AppMainMode.STARTED || currentMode == AppMainMode.STARTING) {
                // Vérrouillage de la procédure
                currentMode = AppMainMode.UPDATING;

                // Arrêt partiel de l'application
                executeStop();
            }

            // Mise à jour possible uniquement l'application n'est plus utilisée
            if (currentMode == AppMainMode.STOPPED) {
                // Re-vérrouillage de la procédure
                currentMode = AppMainMode.UPDATING;

                // Execution du module de mise à jour
                onUpdate();

                // Libère le verrouillage
                currentMode = oldMode;

                // Arrêt complet de l'application
                stoppingApplication();
            }
        }
    }

    /**
     * Lance l'assitant de fermeture et arrête de la supervision de l'application.
     * Le gestionnaire va vérifier que l'application s'arrête
     */
    private void stoppingApplication() {
        // Arrêt de l'application uniquement si elle est démarrée ou en cours de démarrage
        if (currentMode == AppMainMode.STOPPING) {
            running = false;
            String applicationName = getMainAppClass().getSimpleName();

            // Arrêt de la mesure des performances
            FactoryManager.dispose(PerformanceMeasurement.class);

            LoggerManager.getInstance().addDebug("Attempt to clean the remaining instances of "
                                                 + ((applicationName != null) ? applicationName : "application"));
            LoggerManager.getInstance().setLevelOff();

            // Recherche du gestionnaire de fermeture
            FastShutdownManager fastShutdown = FactoryManager.getInstance(FastShutdownManager.class);

            // Lancement du daemon de fermeture
            if (fastShutdown != null) {
                fastShutdown.setDaemon(true);
                fastShutdown.start();
            }

            FactoryManager.dispose();
        }
    }

}
