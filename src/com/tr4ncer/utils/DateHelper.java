package com.tr4ncer.utils;

import com.tr4ncer.logger.*;
import java.text.*;
import java.util.*;

/**
 *
 * @author Sébastien Villemain
 */
public class DateHelper {

    /**
     * Mise en forme de la date complète de façon ordonnée et international.
     */
    public static final String INTERNATIONAL_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formatInternational(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(INTERNATIONAL_FORMAT);
        return sdf.format(date);
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static Date getDateInternational(String dateString) {
        return getDate(dateString, INTERNATIONAL_FORMAT);
    }

    public static Date getDate(String dateString, String pattern) {
        Date value = null;

        if (dateString != null && !dateString.isBlank()) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            try {
                value = sdf.parse(dateString);
            } catch (ParseException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
        return value;
    }

    /**
     * Retourne le tick sous forme minutes secondes millisecondes.
     *
     * @return
     */
    public static int generateTick() {
        return ConvertHelper.toInt(format(new Date(), "mmssSSS"), 0);
    }

    /**
     * Vérifie si c'est noël !
     *
     * @return true c'est noël
     */
    public static boolean isChristmasTime() {
        boolean christmasTime = false;

        int current = Calendar.getInstance().get(Calendar.MONTH);
        if (current == Calendar.DECEMBER) {
            current = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            christmasTime = (Math.abs(25 - current) < 10);
        }
        return christmasTime;
    }

}
