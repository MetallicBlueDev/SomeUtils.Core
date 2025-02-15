package com.metallicbluedev.utils;

/**
 * Utilitaire de calcul mathématique.
 *
 * @version 1.05.00
 * @author Sebastien Villemain
 */
public class MathHelper {

    private MathHelper() {
        // NE RIEN FAIRE
    }

    /**
     * Arrondi d'un double avec n éléments après la virgule.
     *
     * @param a La valeur à convertir.
     * @param n Le nombre de décimales à conserver.
     * @return La valeur arrondi à n décimales.
     */
    public static double floor(double a, int n) {
        double p = Math.pow(10.0, n);
        return Math.floor((a * p) + 0.5) / p;
    }

    /**
     * S'assure que la valeur retournée est bien comprise entre les deux bornes.
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static float bindValue(float value, float min, float max) {
        float rslt = value;

        if (value > max) {
            rslt = max;
        } else if (value < min) {
            rslt = min;
        }
        return rslt;
    }

    /**
     * S'assure que la valeur retournée est bien comprise entre les deux bornes.
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static double bindValue(double value, double min, double max) {
        double rslt = value;

        if (value > max) {
            rslt = max;
        } else if (value < min) {
            rslt = min;
        }
        return rslt;
    }

    /**
     * S'assure que la valeur retournée est bien comprise entre les deux bornes.
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static int bindValue(int value, int min, int max) {
        int rslt = value;

        if (value > max) {
            rslt = max;
        } else if (value < min) {
            rslt = min;
        }
        return rslt;
    }

    /**
     * Retourne un nombre aléatoire suivant l'intervalle précisé.
     *
     * @param min Nombre minimum (inclu)
     * @param max Nombre maximum (exclu)
     * @return
     */
    public static int rand(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    /**
     * Retourne un nombre aléatoire suivant l'intervalle précisé.
     *
     * @param min Nombre minimum (inclu)
     * @param max Nombre maximum (exclu)
     * @return
     */
    public static float rand(float min, float max) {
        return (float) ((Math.random() * (max - min)) + min);
    }

    /**
     * Retourne un nombre aléatoire suivant l'intervalle précisé.
     *
     * @param min Nombre minimum (inclu)
     * @param max Nombre maximum (exclu)
     * @return
     */
    public static double rand(double min, double max) {
        return ((Math.random() * (max - min)) + min);
    }

    /**
     * Vérifie l'égalité entre les deux chiffres.
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(float a, float b) {
        return Math.abs(a - b) < 0.001;
    }

    /**
     * Vérifie l'égalité entre les deux chiffres.
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }
}
