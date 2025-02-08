package com.tr4ncer.taskprogress;

import com.tr4ncer.*;

/**
 * Valeur de progression.
 *
 * @version 2.00.00
 * @author Sebastien Villemain
 */
public interface TaskProgressValue extends EventObservable<TaskProgressListener> {

    /**
     * Retourne l'état actuel de la progression.
     *
     * @return
     */
    public TaskProgressValue getProgressState();

    /**
     * Retourne la valeur maximum de progression.
     *
     * @return
     */
    public double getProgressMaxValue();

    /**
     * Retourne la valeur de progression.
     *
     * @return
     */
    public double getProgressValue();

    /**
     * Nombre d'étape disponible.
     *
     * @return
     */
    public int numberOfStep();

    /**
     * Marque que la progression est en attente.
     */
    public void waiting();

    /**
     * Remise à zéro de la valeur.
     */
    public void reset();

    /**
     * Incrémente la valeur de progression pour l'étape courante.
     */
    public void performValue();

    /**
     * Assigne la valeur de progression.
     *
     * @param value
     */
    public void setProgressValue(double value);

    /**
     * Ajoute la valeur à la progression.
     *
     * @param value
     */
    public void addProgressValue(double value);

    /**
     * Retourne l'étape courante.
     * Si aucune étape définie, retourne <code>null</code>.
     *
     * @return IntelligentProgressStep or <code>null</code>.
     */
    public TaskProgressValue getProgressStep();

    /**
     * Calcul et retourne un cran de progression suivant le nombre d'opération à effecuter.
     *
     * @param valueReferred
     * @param nbSteps
     * @return
     */
    public int getProgressStep(double valueReferred, int nbSteps);

    /**
     * Retourne l'étape la plus basse en cours de progression.
     *
     * @return
     */
    public TaskProgressValue getChildProgressStep();

    /**
     * Ajoute une étape.
     *
     * @param stepMaxValue
     */
    public void addProgressStep(double stepMaxValue);

    /**
     * Assigne une valeur maximum de progression.
     *
     * @param maxValue
     */
    public void setProgressMaxValue(double maxValue);

    /**
     * Ajoute plusieurs étapes identiques.
     *
     * @param numberOfStep
     * @param stepMaxValue
     */
    public void addProgressStep(int numberOfStep, double stepMaxValue);

    /**
     * Ajoute une sous étape dans le dernier niveau.
     *
     * @param stepMaxValue
     */
    public void addChildProgressStep(double stepMaxValue);

    /**
     * Ajoute plusieurs sous étapes identiques dans le dernier niveau.
     *
     * @param numberOfStep
     * @param stepMaxValue
     */
    public void addChildProgressStep(int numberOfStep, double stepMaxValue);

}
