package com.tr4ncer.utils;

import com.tr4ncer.logger.*;

/**
 * Aide à la manipulation des chaines de caractères.
 *
 * @version 2.00.00
 * @author Sebastien Villemain
 */
public class StringHelper {

    /**
     * Séparateur de ligne.
     */
    public static final String LINE_SEPARATOR = getLineSeparator();

    private StringHelper() {
        // NE RIEN FAIRE
    }

    /**
     * Retourne le séparateur de ligne de l'OS.
     *
     * @return
     */
    private static String getLineSeparator() {
        String separator = null;

        try {
            separator = System.getProperty("line.separator");
        } catch (Exception ex) {
            LoggerManager.getInstance().addError(ex);
        }

        if (separator == null || separator.isEmpty()) {
            separator = "\n";
        }
        return separator;
    }

}
