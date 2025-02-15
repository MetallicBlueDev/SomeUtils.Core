package com.metallicbluedev.threading;

import com.metallicbluedev.logger.*;
import com.metallicbluedev.utils.*;
import java.util.*;

/**
 * Groupe contenant un nombre limité de Threads dédié à une tâche.
 *
 * @version 2.01.00
 * @author Sebastien Villemain
 */
public abstract class ThreadPool extends ThreadGroup {

    /**
     * Index des groupes de Threads.
     */
    private static int THREAD_POOL_INDEX = 0;

    /**
     * Liste de tâches du groupe.
     */
    private final Deque<ServiceProcess> taskQueue;

    /**
     * Verrou de synchronisation de Thread (pour les pauses).
     */
    private final Object pausedLock;

    /**
     * Nom de base des threads du groupe.
     */
    private String pooledThreadName = null;

    /**
     * Index des threads.
     */
    private volatile int pooledThreadIndex = 0;

    /**
     * Etat du groupe de threads.
     */
    private volatile boolean alive = false;

    /**
     * Etat de la pause.
     */
    private volatile boolean paused = false;

    /**
     * Nouveau groupe de Threads.
     *
     * @param numberOfThreads Nombre de Thread dans le groupe.
     * @param poolName Nom du groupe.
     * @param pooledName Nom de membre du groupe.
     */
    public ThreadPool(int numberOfThreads, String poolName, String pooledName) {
        super(nextThreadPoolId() + "-" + poolName);

//        setDaemon(true);
        alive = true;
        taskQueue = new LinkedList<>();
        pausedLock = new Object();
        this.pooledThreadName = pooledName;

        /*
         * Activation des Threads
         */
        for (int i = 0; i < numberOfThreads; i++) {
            addPooledThread();
        }
    }

    /**
     * Retourne l'identifiant du prochain groupe de thread.
     *
     * @return
     */
    private static int nextThreadPoolId() {
        return THREAD_POOL_INDEX++;
    }

    /**
     * Retourne le nom de base des threads membre du groupe.
     *
     * @return
     */
    public final String getPooledThreadName() {
        return pooledThreadName;
    }

    /**
     * Change le nom de base des threads membre du groupe.
     *
     * @param pooledName Nom de base des threads.
     * @param setToAll Mettre à jour tous les threads du groupe.
     */
    public final void setPooledThreadName(String pooledName, boolean setToAll) {
        if (pooledName != null) {
            this.pooledThreadName = pooledName;

            if (setToAll) {
                Thread[] pooledThreads = getPooledThreads();

                for (Thread pooledThread : pooledThreads) {
                    pooledThread.setName(pooledName);
                }
            }
        }
    }

    /**
     * Retourne l'indexage actuel des threads.
     *
     * @return
     */
    public final synchronized int getPooledThreadIndex() {
        return pooledThreadIndex;
    }

    /**
     * Ajout d'un thread au groupe.
     */
    public final synchronized void addPooledThread() {
        // Nouveau Thread commun au groupe
        PooledThread pooledThread = new PooledThread();
        pooledThread.start();
    }

    /**
     * Suppression d'un thread du groupe.
     */
    public final synchronized void removePooledThread() {
        ServiceProcess[] pooledThreads = getServices();

        if (pooledThreads.length > 0) {
            ServiceProcess pooledThread = pooledThreads[pooledThreads.length - 1];
            pooledThread.stop();
        } else {
            LoggerManager.getInstance().addWarning("There are no services to stop.");
        }
    }

    /**
     * Arrêt de la tache du même type que la cible.
     *
     * @param taskService
     */
    public final synchronized void removePooledThread(Class<? extends ServiceProcess> taskService) {
        removePooledThreads(null, taskService, false);
    }

    /**
     * Arrêt de toutes les taches du même type que la cible.
     *
     * @param taskService
     */
    public final synchronized void removePooledThreads(Class<? extends ServiceProcess> taskService) {
        removePooledThreads(null, taskService, true);
    }

    /**
     * Arrêt de la tache ciblée.
     *
     * @param taskService
     */
    public final synchronized void removePooledThread(ServiceProcess taskService) {
        removePooledThreads(taskService, null, false);
    }

    /**
     * Arrêt de toutes les taches ciblées.
     *
     * @param taskService
     */
    public final synchronized void removePooledThreads(ServiceProcess taskService) {
        removePooledThreads(taskService, null, true);
    }

    /**
     * Arrêt d'une tache spécifique.
     *
     * @param taskService
     * @param taskServiceClass
     * @param removeAll
     */
    private void removePooledThreads(ServiceProcess taskService, Class<? extends ServiceProcess> taskServiceClass, boolean removeAll) {
        if (taskService != null || taskServiceClass != null) {
            ServiceProcess[] pooledThreads = getServices();

            for (ServiceProcess service : pooledThreads) {
                if (service instanceof PooledThread pooledThread) {
                    if (pooledThread.task != null) {
                        boolean canRemove = false;

                        if (taskServiceClass != null) {
                            if (pooledThread.task.getClass().isAssignableFrom(taskServiceClass)) {
                                canRemove = true;
                            }
                        } else {
                            if (pooledThread.task.equals(taskService)) {
                                canRemove = true;
                            }
                        }

                        if (canRemove) {
                            pooledThread.stop();

                            if (!removeAll) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retourne la liste des threads du groupe.
     *
     * @return
     */
    public Thread[] getPooledThreads() {
        Thread[] threads = new Thread[activeCount()];
        enumerate(threads);
        return threads;
    }

    /**
     * Retourne la liste des services du groupe.
     *
     * @return
     */
    public ServiceProcess[] getServices() {
        Thread[] threads = getPooledThreads();
        ServiceProcess[] services = SystemHelper.getServices(threads);
        return services;
    }

    /**
     * Suspend tous les threads.
     *
     * @param paused
     */
    public final void setPaused(boolean paused) {
        if (this.paused != paused) {
            synchronized (pausedLock) {
                this.paused = paused;

                if (!paused) {
                    pausedLock.notifyAll();
                }
            }
        }
    }

    /**
     * Vérifie l'état de pause.
     *
     * @return
     */
    public final boolean isPaused() {
        return paused;
    }

    /**
     * Vérifie si le groupe est encore en activité.
     *
     * @return
     */
    public final boolean isAlive() {
        return alive;
    }

    /**
     * Nouvelle tâche à executer.
     * <p>
     * La tâche est ajouté à la liste,
     * le prochain Thread libre l'executera.
     *
     * @param task
     */
    public final synchronized void runTask(ServiceProcess task) {
        if (!alive) {
            throw new IllegalStateException(getClass().getSimpleName() + " is not alive.");
        }

        if (taskQueue != null && task != null) {
            taskQueue.add(task);
            notifyAll();
        }
    }

    /**
     * Retourne la prochaine tâche. Cette méthode est bloquante.
     *
     * @param pooledThread
     * @return Runnable Une tâche executable
     * @throws InterruptedException
     */
    private synchronized ServiceProcess getTask(ServiceProcess pooledThread) throws InterruptedException {
        ServiceProcess service;

        // Si il n'y a pas eu d'initialisation...
        if (taskQueue == null) {
            service = null;
        } else {
            // Si la file est vide, on attend la prochaine tâche
            while (taskQueue.isEmpty()) {
                if (!alive && !pooledThread.running()) {
                    // Traitement impossible
                    break;
                }

                wait();
            }

            if (alive && pooledThread.running()) {
                service = taskQueue.removeFirst();
            } else {
                service = null;
            }
        }
        return service;
    }

    /**
     * Retourne la taille de la file d'attente.
     *
     * @return
     */
    protected final synchronized int queueSize() {
        return taskQueue.size();
    }

    /**
     * Ferme le groupe de Threads.
     * <p>
     * Toutes les tâches en cours sont arrêtées.
     * Toutes les tâches en attentes sont perdues.
     */
    public void destroyProcess() {
        if (alive) {
            // Désactivation du groupe et nettoyage de la file
            synchronized (this) {
                alive = false;

                if (taskQueue != null) {
                    taskQueue.clear();
                }
            }

            ServiceProcess[] services = getServices();

            for (ServiceProcess pooledThread : services) {
                pooledThread.stop();
            }

            waitForInterrupt(null);

            try {
                interrupt();
            } catch (Exception e) {
                LoggerManager.getInstance().addError(e);
            }
        }
    }

    /**
     * Attente avant interruption.
     *
     * @param pooledThread
     */
    private void waitForInterrupt(PooledThread pooledThread) {
        int counter = 0;

        while (counter < 100
               && ((pooledThread == null && activeCount() > 0)
                   || (pooledThread != null && pooledThread.task != null))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                LoggerManager.getInstance().addError(ex);
            }

            counter++;
        }
    }

    /**
     * Suspend le groupe et attend que tous les Threads en cours termine leurs travail.
     */
    public void join() {
        // Notifie les threads de se groupe qu'ils ne sont plus actifs
        synchronized (this) {
            alive = false;
            notifyAll();
        }

        // Attente de la fin du travail
        Thread[] threads = new Thread[activeCount()];
        int count = enumerate(threads);

        for (int i = 0; i < count; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
    }

    /**
     * Signal que le Thread commun a démarré.
     */
    protected abstract void pooledThreadStarted();

    /**
     * Signal que le Thread commun est arrêté.
     */
    protected abstract void pooledThreadStopped();

    /**
     * Vérifie l'état de pause.
     *
     * @throws InterruptedException
     */
    protected final void checkPauseLock() throws InterruptedException {
        // Synchronisation via le verrou
        synchronized (pausedLock) {
            // Si une pause a été demandé, on arrête
            while (paused) {
                pausedLock.wait();
            }
        }
    }

    /**
     * Thread membre du groupe.
     */
    private class PooledThread implements ServiceProcess {

        /**
         * Détermine l'état local de la procédure.
         */
        private volatile boolean running = false;

        /**
         * La tâtche en cours d'execution.
         */
        private ServiceProcess task = null;

        @Override
        public void run() {
            try {
                // Signal que le Thread a démarré
                pooledThreadStarted();

                while (alive && running) {
                    // Synchronisation sur le verrou de pause
                    try {
                        checkPauseLock();
                    } catch (InterruptedException ex) {
                        LoggerManager.getInstance().addError(ex);
                    }

                    // Récupération de la tâche
                    try {
                        task = getTask(this);
                    } catch (InterruptedException ex) {
                        LoggerManager.getInstance().addError(ex);
                    }

                    // Vérification de la tâche
                    if (task != null) {
                        // Execution de la tâche
                        try {
                            task.start();
                            task.run();
                        } catch (Throwable t) {
                            ThreadPool.this.uncaughtException(Thread.currentThread(), t);
                        }

                        task = null;
                    }
                }

                // Signal la fin du Thread
                pooledThreadStopped();
            } finally {
                running = false;
            }
        }

        @Override
        public void start() {
            running = true;

            Thread pooledThread = new ThreadHolderTask(ThreadPool.this, this, pooledThreadIndex + "-" + pooledThreadName);
            pooledThread.start();

            pooledThreadIndex++;
        }

        @Override
        public void stop() {
            if (running) {
                running = false;

                // Arrêt de la tache
                if (task != null) {
                    task.stop();
                }

                Thread[] pooledThreads = getPooledThreads();

                for (Thread pooledThread : pooledThreads) {
                    ServiceProcess service = SystemHelper.getService(pooledThread);

                    if (service != null) {
                        if (service.equals(this)) {
                            // Donne une chance à la tache de se terminer toute seule
                            waitForInterrupt(this);

                            // Coupure du Thread
                            pooledThread.interrupt();
                            pooledThreadIndex--;
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public boolean running() {
            return running;
        }
    }
}
