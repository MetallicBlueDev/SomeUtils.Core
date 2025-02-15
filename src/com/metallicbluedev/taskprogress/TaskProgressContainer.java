package com.metallicbluedev.taskprogress;

import java.util.*;

/**
 *
 * @author Sébastien Villemain
 */
public class TaskProgressContainer {

    /**
     * Tableau des valeurs de progression liées au thread.
     */
    private static final Map<String, TaskProgressValue> progressValues = new HashMap<>();

    /**
     * Attache au thread une valeur de progression.
     *
     * @param progressValue
     */
    public void attachProgressValue(TaskProgressValue progressValue) {
        String key = getProgressValueKey();
        progressValues.put(key, progressValue);
    }

    /**
     * Détache du thread la valeur de progression liée.
     */
    public void detachProgressValue() {
        String key = getProgressValueKey();

        if (progressValues.containsKey(key)) {
            progressValues.remove(key);
        }
    }

    /**
     * Ajoute une étape de progression.
     *
     * @param maxValue
     */
    public void addProgressStep(double maxValue) {
        TaskProgressValue progressValue = getProgressValue();

        if (progressValue != null) {
            TaskProgressValue childProgressStep = progressValue.getChildProgressStep();

            if (childProgressStep != null) {
                childProgressStep.setProgressMaxValue(childProgressStep.getProgressMaxValue() + maxValue);
            }
        }
    }

    /**
     * Notifie la progression.
     *
     * @param value
     */
    public void addProgressValue(double value) {
        TaskProgressValue progressValue = getProgressValue();

        if (progressValue != null) {
            progressValue.addProgressValue(value);
        }
    }

    /**
     * Marque la progression en attente.
     */
    public void setProgressWaiting() {
        TaskProgressValue progressValue = getProgressValue();

        if (progressValue != null) {
            progressValue.waiting();
        }
    }

    /**
     * Retourne la clé liant le thread à la valeur de progression.
     *
     * @return
     */
    private String getProgressValueKey() {
        return Thread.currentThread().getName();
    }

    /**
     * Retourne la valeur de progression liée au thread.
     *
     * @return
     */
    private TaskProgressValue getProgressValue() {
        TaskProgressValue value = null;
        String key = getProgressValueKey();

        if (progressValues.containsKey(key)) {
            value = progressValues.get(key);
        }
        return value;
    }
}
