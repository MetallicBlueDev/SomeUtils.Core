package com.tr4ncer.core;

import com.tr4ncer.factory.*;
import com.tr4ncer.logger.*;
import com.tr4ncer.threading.*;
import com.tr4ncer.utils.*;
import static java.lang.Thread.State.BLOCKED;
import static java.lang.Thread.State.NEW;
import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.TIMED_WAITING;
import static java.lang.Thread.State.WAITING;
import java.lang.management.*;
import java.util.*;
import javax.management.*;

/**
 * Mesure les performances Java.
 *
 * @version 1.12.01
 * @author Sebastien Villemain
 */
public final class PerformanceMeasurement implements ServiceProcess, EntityProcess {

    /**
     * Point de mesure.
     */
    private final Map<Long, MeasureTimes> history = new HashMap<>();

    /**
     * Temps entre chaque mesure.
     */
    private long intervalOfMeasure = 3000;

    /**
     * L'identifiant du thread mesurant les performances.
     */
    private long threadId = 0;

    /**
     * Valeur de cache pour le temps d'utilisation du CPU.
     */
    private long cpuTimeCache = -1;

    /**
     * Valeur de cache pour le temps d'utilisation du système.
     */
    private long userTimeCache = -1;

    /**
     * Valeur de cache pour l'utilisation du processeur.
     */
    private long cpuUsageCache = -1;

    /**
     * Pourcentage d'utilisation maximum.
     */
    private int maximumUsage = 80;

    /**
     * Facteur de vitesse demandé par l'utilisateur.
     * Pour une vitesse rapide: facteur à 0.20 par exemple.
     * Pour une vitesse normale: facteur à 1.
     * Pour une vitesse lente: facteur à 1.70 par exemple.
     */
    private float waitUserFactor = 1;

    protected PerformanceMeasurement() {
        // NE RIEN FAIRE
    }

    /**
     * Intervale de temps en seconde entre chaque mesure.
     *
     * @param intervalOfMeasure
     */
    public void setIntervalOfMeasure(long intervalOfMeasure) {
        if (intervalOfMeasure < 1) {
            intervalOfMeasure = 1;
        }
        this.intervalOfMeasure = intervalOfMeasure * 1000;
    }

    /**
     * Création ou mise à jour des points de mesures.
     */
    @Override
    public final void run() {
        waitMainManagerStarted();

        LoggerManager.getInstance().addDebug("Performance measurement is enabled.");

        memoryNotification();

        try {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            bean.setThreadContentionMonitoringEnabled(true);
            bean.setThreadCpuTimeEnabled(true);

            while (running()) {
                update(bean);

                double usage = getUsage();

                if (usage >= 50) {
                    LoggerManager.getInstance().addDebug("CPU usage: " + usage + "%");
                }

                try {
                    Thread.sleep(intervalOfMeasure);
                } catch (InterruptedException ex) {
                    LoggerManager.getInstance().addError(ex);
                    break;
                }
            }
        } catch (UnsupportedOperationException e) {
            LoggerManager.getInstance().addWarning("Performance measurement is not supported.");
        }
    }

    /**
     * Retourne le temps d'utilisation du processeur en nanosecondes.
     *
     * @return
     */
    public final long getTotalCpuTime() {
        if (!(cpuTimeCache > 0)) {
            cpuTimeCache = 0;

            for (MeasureTimes times : history.values()) {
                cpuTimeCache += times.getCpuTime();
            }
        }
        return cpuTimeCache;
    }

    /**
     * Retourne le temps d'utilisation du système en nanosecondes.
     *
     * @return
     */
    public final long getTotalUserTime() {
        if (!(userTimeCache > 0)) {
            userTimeCache = 0;

            for (MeasureTimes times : history.values()) {
                userTimeCache += times.getUserTime();
            }
        }
        return userTimeCache;
    }

    /**
     * Retourne le pourcentage de temps de traitement que l'application utilise.
     *
     * @return
     */
    public final double getUsage() {
        if (!(cpuUsageCache > 0)) {
            cpuUsageCache = 0;

            for (MeasureTimes times : history.values()) {
                cpuUsageCache += times.getUsage();
            }
        }
        return MathHelper.bindValue(cpuUsageCache, 0, 100);
    }

    /**
     * Retourne le temps de réponse du système en nanosecondes.
     *
     * @return
     */
    public long getTotalSystemTime() {
        return getTotalCpuTime() - getTotalUserTime();
    }

    /**
     * Change le pourcentage maximum d'utilisation.
     *
     * @param maximumUsage
     */
    public void setMaximumUsage(int maximumUsage) {
        if (maximumUsage >= 5 && maximumUsage < 100) {
            this.maximumUsage = maximumUsage;
        }
    }

    /**
     * Détermine si le système est surchargé.
     *
     * @return
     */
    public boolean isOverloaded() {
        return (getUsage() >= maximumUsage);
    }

    /**
     * Cesse temporairement l'exécution du thread.
     * Si l'application est surchargé, un coefficient est appliqué sur l'attente de base.
     *
     * @param waitBase
     */
    public void optimizedSleep(int waitBase) {
        try {
            if (waitBase < 1) {
                waitBase = 1;
            }

            float waitOverloadedFactor = 1;

            if (isOverloaded()) {
                waitOverloadedFactor += (getUsage() / 100);
            }

            Thread.sleep((int) ((waitBase * waitUserFactor) * waitOverloadedFactor));
        } catch (InterruptedException ex) {
            LoggerManager.getInstance().addError(ex);
        }
    }

    /**
     * Cesse temporairement l'exécution du thread.
     */
    public void optimizedSleep() {
        optimizedSleep(50);
    }

    /**
     * Affecte le facteur d'attente utilisateur.
     * Permet de gérer la vitesse d'exécution.
     *
     * @param waitUserFactor
     */
    public void setWaitUserFactor(float waitUserFactor) {
        this.waitUserFactor = MathHelper.bindValue(waitUserFactor, 0.05F, 10F);
    }

    /**
     * Retourne le facteur d'attente utilisateur.
     *
     * @return
     */
    public float getWaitUserFactor() {
        return waitUserFactor;
    }

    /**
     * L'utilisateur demande une vitesse d'exécution rapide.
     */
    public void setWaitUserFactorFast() {
        setWaitUserFactor(0.15F);
    }

    /**
     * L'utilisateur demande une vitesse d'exécution normale.
     */
    public void setWaitUserFactorNormal() {
        setWaitUserFactor(1.00F);
    }

    /**
     * L'utilisateur demande une vitesse d'exécution lente.
     */
    public void setWaitUserFactorSlow() {
        setWaitUserFactor(3.00F);
    }

    /**
     * Détermine si la vitesse d'exécution demandée par l'utilisateur est rapide.
     *
     * @return
     */
    public boolean isWaitUserFactorFast() {
        return waitUserFactor < 1;
    }

    /**
     * Détermine si la vitesse d'exécution demandée par l'utilisateur est normale.
     *
     * @return
     */
    public boolean isWaitUserFactorNormal() {
        return waitUserFactor == 1;
    }

    /**
     * Détermine si la vitesse d'exécution demandée par l'utilisateur est lente.
     *
     * @return
     */
    public boolean isWaitUserFactorSlow() {
        return waitUserFactor > 1;
    }

    @Override
    public final void start() {
        if (threadId <= 0) {
            Thread monitor = new ThreadHolderTask(this);
            monitor.setPriority(Thread.MIN_PRIORITY);
            monitor.setDaemon(true);

            threadId = monitor.threadId();
            monitor.start();
        }
    }

    @Override
    public final void stop() {
        if (threadId > 0) {
            threadId = -1;
            history.clear();
        }
    }

    @Override
    public final boolean running() {
        return (threadId > 0);
    }

    @Override
    public String getInformation() {
        StringBuilder builder = new StringBuilder();

        for (MeasureTimes measure : history.values()) {
            builder.append("Service ");
            builder.append(measure.id);
            builder.append(" Usage=");
            builder.append(measure.getUsage());
            builder.append(" CpuTime=");
            builder.append(measure.getCpuTime());
            builder.append(" UserTime=");
            builder.append(measure.getUserTime());
            builder.append(StringHelper.LINE_SEPARATOR);
        }
        return builder.toString();
    }

    @Override
    public void createProcess() {
    }

    @Override
    public void destroyProcess() {
        stop();
    }

    public void waitMainManagerStarted() {
        // Démarrage différé pour ne pas altérer les performances
        while (running()
               && FactoryManager.hasInstance(MainManager.class)
               && !MainManager.getInstance().started()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LoggerManager.getInstance().addError(e);
            }
        }
    }

    /**
     * Vérification de la mémoire utilisable.
     */
    private void memoryNotification() {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();

        if (bean != null) {
            if (bean instanceof NotificationBroadcaster nb) {
                nb.addNotificationListener(new MemoryNotificationListener(), null, null);
            }
        }
    }

    /**
     * Mise à jour de la mesure de performances.
     *
     * @param bean
     * @throws UnsupportedOperationException
     */
    private void update(ThreadMXBean bean) throws UnsupportedOperationException {
        Thread[] services = SystemHelper.getServices();

        // Mesure des performances
        for (Thread service : services) {
            if (service.threadId() == threadId) {
                continue;
            }

            MeasureTimes times = null;
            if (history.containsKey(service.threadId())) {
                times = history.get(service.threadId());
            }

            long cpu = bean.getThreadCpuTime(service.threadId());
            long user = bean.getThreadUserTime(service.threadId());

            if (times == null) {
                if (service.threadId() > 0) {
                    times = new MeasureTimes(service.threadId(), cpu, user);
                    history.put(service.threadId(), times);
                }
            } else {
                switch (service.getState()) {
                    case RUNNABLE:
                        break;
                    case BLOCKED:
                    case TIMED_WAITING:
                        if (service.isInterrupted()) {
                            times.startCpuTime = cpu;
                            times.startUserTime = user;
                        }
                        break;
                    case TERMINATED:
                        if (history.containsKey(service.threadId())) {
                            history.remove(service.threadId());
                        }
                        break;
                    case NEW:
                    case WAITING:
                    default:
                        times.startCpuTime = cpu;
                        times.startUserTime = user;
                        break;
                }

                times.endCpuTime = cpu;
                times.endUserTime = user;
            }
        }

        // Nettoyage de l'historique
        Iterator<MeasureTimes> iterator = history.values().iterator();
        while (iterator.hasNext()) {
            MeasureTimes measureTimes = iterator.next();
            boolean canRemove = true;

            for (Thread service : services) {
                if (measureTimes.id == service.threadId()) {
                    canRemove = false;
                    break;
                }
            }

            if (canRemove) {
                iterator.remove();
            }
        }

        // Reset du cache
        cpuTimeCache = -1;
        userTimeCache = -1;
        cpuUsageCache = -1;
    }

    /**
     * Point de mesure.
     */
    private static class MeasureTimes {

        private final long startTime;

        final long id;

        long startCpuTime;

        long startUserTime;

        long endCpuTime;

        long endUserTime;

        private MeasureTimes(long id, long startCpuTime, long startUserTime) {
            this.id = id;
            this.startCpuTime = startCpuTime;
            this.startUserTime = startUserTime;
            endCpuTime = startCpuTime;
            endUserTime = startUserTime;
            startTime = System.nanoTime();
        }

        public final double getUsage() {
            double cpuUsage = 0;
            double cpu = getCpuTime();

            if (cpu > 0) {
                cpuUsage = MathHelper.bindValue((100 * cpu) / (System.nanoTime() - startTime), 0, 100);
            }
            return cpuUsage;
        }

        public final long getCpuTime() {
            return endCpuTime - startCpuTime;
        }

        public final long getUserTime() {
            return endUserTime - startUserTime;
        }
    }

    /**
     * Réception des notifications de mémoire.
     */
    private static class MemoryNotificationListener implements NotificationListener {

        @Override
        public void handleNotification(Notification notification, Object handback) {
            if (notification.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                long usedMemory = -1;

                // Console pour la mémoire utilisable
                MemoryPoolMXBean tenuredGenPool = findTenuredGenPool();

                if (tenuredGenPool != null) {
                    usedMemory = tenuredGenPool.getUsage().getUsed();
                }

                LoggerManager.getInstance().addWarning("The available memory is low. Used: " + usedMemory + "%.");
            }
        }
    }

    /**
     * Recherche de la console pour la mémoire utilisable.
     * Retourne la console si possible, sinon retourne
     * <code>null</code>.
     *
     * @return MemoryPoolMXBean or <code>null</code>.
     */
    private static MemoryPoolMXBean findTenuredGenPool() {
        MemoryPoolMXBean bean = null;

        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            // I don't know whether this approach is better, or whether
            // we should rather check for the pool name "Tenured Gen"?
            if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
                bean = pool;
                break;
            }
        }

        if (bean == null) {
            LoggerManager.getInstance().addWarning("Could not find tenured space.");
        }
        return bean;
    }
}
