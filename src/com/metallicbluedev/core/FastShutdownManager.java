package com.metallicbluedev.core;

import com.metallicbluedev.*;
import com.metallicbluedev.factory.*;
import com.metallicbluedev.logger.*;
import com.metallicbluedev.threading.*;
import com.metallicbluedev.utils.*;
import javax.swing.event.*;

/**
 * Gestionnaire de fermeture.
 * <p>
 * <ul>
 * <li>Détection de quelques fermetures imprévues du programme:</li>
 * <li>Controle-C</li>
 * <li>kill -2</li>
 * <li>kill -15</li>
 * <li>Mais aussi exécution d'un traitement en parallèle grace au daemon:</li>
 * <li>Détection d'un problème d'arrêt de l'application</li>
 * <li>Arrêt de l'application au bout d'un certain temps</li>
 * </ul>
 *
 * @version 2.02.01
 * @author Sebastien Villemain
 */
public final class FastShutdownManager extends Thread implements EntityProcess, EventObservable<FastShutdownListener> {

    /**
     * Temps maximum d'attente (en ms).5000
     */
    private static final int MAX_WAITING_TIME = 30000;

    /**
     * Temps d'attente avant fermeture du programme.
     */
    protected int waitingTime = MAX_WAITING_TIME;

    /**
     * Liste des écouteurs d'événements.
     */
    private final EventListenerList listeners;

    protected FastShutdownManager() {
        super();
        listeners = new EventListenerList();
    }

    @Override
    public void addListener(FastShutdownListener listener) {
        listeners.add(FastShutdownListener.class, listener);
    }

    @Override
    public String getInformation() {
        return "WaitingTime=" + waitingTime;
    }

    @Override
    public void removeListener(FastShutdownListener listener) {
        listeners.remove(FastShutdownListener.class, listener);
    }

    @Override
    public void run() {
        // Fonctionnement différent suivant le type de thread
        if (isDaemon()) {
            shutdownByDaemon();
        } else {
            shutdownByProcess();
        }
    }

    /**
     * Moniteur de sécurité pour surveiller l'arrêt de l'application.
     * Traitement additionnel effectué par le daemon en parallèle de l'application.
     */
    private void shutdownByDaemon() {
        waitVirtualMachine();

        // NOTE: Les messages risques de ne pas aboutir car la notification peut être elle même déjà arrêtée
        LoggerManager.getInstance().addWarning("Triggering the forced closure.");

        logAliveThreads();

        // Destruction de toutes les ressources
        FactoryManager.dispose();

        destroyThreadGroup();

        stopServices();
    }

    private void waitVirtualMachine() {
        int waitingTimer = 0;

        // On attend par intermitance que la JVM arrête l'application elle-même
        do {
            waitingTimer += 1000;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LoggerManager.getInstance().addError(ex);
            }

            if (FactoryManager.isDisposing()) {
                break;
            }
        } while (waitingTimer <= waitingTime);
    }

    private void logAliveThreads() {
        Thread[] threads = new Thread[Thread.activeCount()];
        int count = Thread.enumerate(threads);

        // Affichage des threads encore en cours d'utilisation
        for (int i = 0; i < count; i++) {
            Thread thread = threads[i];

            if (!canManage(thread)) {
                continue;
            }

            if (thread.isAlive()) {
                if (FactoryManager.hasInstance(LoggerManager.class)) {
                    LoggerManager.getInstance().addError(
                        ((thread.getThreadGroup() != null) ? thread.getThreadGroup().getName() + ":" : "")
                        + thread.getName()
                        + " will be shut down brutally"
                        + ((thread.getState() != null) ? " (" + thread.getState().toString() + ")" : "") + ".");
                }
            }
        }
    }

    private boolean canManage(Thread t) {
        return t != this && !t.getName().equalsIgnoreCase("DestroyJavaVM");
    }

    private void destroyThreadGroup() {
        int count;
        // Recherche du thread parent le plus en haut de la hiérarchie
        ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
        while (root.getParent() != null) {
            root = root.getParent();
        }

        // Liste tous les groupements de threads
        ThreadGroup[] childGroups = new ThreadGroup[root.activeGroupCount()];
        count = root.enumerate(childGroups);

        // Arrêt des groupes de threads que nous pouvons gérer
        for (int i = 0; i < count; i++) {
            ThreadGroup threadGroup = childGroups[i];

            if (threadGroup instanceof ThreadPool threadPool) {
                threadPool.destroyProcess();
            }
        }
    }

    private void stopServices() {
        Thread[] threads = new Thread[Thread.activeCount()];
        int count = Thread.enumerate(threads);

        // Arrêt des services encore en cours d'utilisation
        for (int i = 0; i < count; i++) {
            Thread thread = threads[i];

            if (!canManage(thread)) {
                continue;
            }

            if (thread.isAlive()) {
                ServiceProcess service = SystemHelper.getService(thread);

                if (service != null) {
                    service.stop();
                }
            }
        }
    }

    /**
     * Moniteur automatiquement démarré par le runtime de la VM qui s'éexcute juste avant l'arrêt de l'application.
     * Traitement qui sera effectué suite à une interruption du programme.
     * Cette interruption peut être programmé, c'est a dire un arrêt demandé par le client,
     * ou bien elle peut être causé par un crash imprévu.
     */
    private void shutdownByProcess() {
        FastShutdownEvent event = new FastShutdownEvent(this);

        for (FastShutdownListener listener : listeners.getListeners(FastShutdownListener.class)) {
            listener.onChanged(event);
        }
    }

    @Override
    public void createProcess() {
        setName(getClass().getSimpleName());
        setPriority(Thread.MAX_PRIORITY);
        Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public void destroyProcess() {
    }
}
