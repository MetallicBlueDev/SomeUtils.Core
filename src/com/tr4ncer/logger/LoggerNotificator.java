package com.tr4ncer.logger;

import com.tr4ncer.factory.*;
import com.tr4ncer.threading.*;
import java.util.logging.*;

/**
 * Notificateur d'événement du logger.
 * Gestionnaire des threads transferant les messages.
 *
 * @version 3.00.00
 * @author Sebastien Villemain
 */
public class LoggerNotificator extends ThreadPool implements EntityProcess {

    protected LoggerNotificator() {
        super(0, LoggerNotificator.class.getSimpleName(), LoggerManager.class.getSimpleName());
    }

    @Override
    public void createProcess() {
        setMaxPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public String getInformation() {
        return "PooledThreadIndex=" + getPooledThreadIndex();
    }

    // NOTE: Ne doit surtout pas être executé dans un contexte de syncrhonisation
    // Sinon, ceci ne sert à rien et les messsages seront bloqués.
    @Override
    public void destroyProcess() {
        int counter = 0;

        // Patiente un peu pour finir de publier tous les messages
        // Puis arrêt complet du notificateur
        while (queueSize() > 0 && counter < 100) {
            counter++;

            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                break;
            }
        }

        super.destroyProcess();
    }

    @Override
    protected void pooledThreadStarted() {
        // NE RIEN FAIRE ICI
    }

    @Override
    protected void pooledThreadStopped() {
        // NE RIEN FAIRE ICI
    }

    /**
     * Démarre l'apprentissage du message.
     *
     * @param manger
     * @param message
     */
    void appendMessage(LoggerManager manger, LogRecord message) {
        runTask(new LoggerNotificator.LoggerNotificatorPlayer(manger, message));
    }

    /**
     * Routine d'exécution du transfert de message.
     */
    private static class LoggerNotificatorPlayer implements ServiceProcess {

        /**
         * Le logger concerné.
         */
        private final LoggerManager manager;

        /**
         * Le message à transfèrer.
         */
        private final LogRecord message;

        private boolean running = false;

        /**
         * Nouvelle routine de transfert.
         *
         * @param manager
         * @param message
         */
        private LoggerNotificatorPlayer(LoggerManager manager, LogRecord message) {
            this.manager = manager;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                running = true;
                manager.checkHandlers();
                manager.digestMessage(message);
            } finally {
                running = false;
            }
        }

        @Override
        public void start() {
            // NE RIEN FAIRE ICI
        }

        @Override
        public void stop() {
            // NE RIEN FAIRE ICI
        }

        @Override
        public boolean running() {
            return running;
        }
    }
}
