package com.metallicbluedev.utils;

import com.metallicbluedev.logger.*;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

/**
 * Convertisseur de type.
 *
 * @version 2.25.00
 * @author Sebastien Villemain
 */
public class ConvertHelper {

    private ConvertHelper() {
        // NE RIEN FAIRE
    }

    /**
     * Conversion d'un objet en byte.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static byte toByte(Object condition, byte defaultCondition) {
        byte value;

        try {
            value = Byte.parseByte(toStringNumber(condition));
        } catch (NumberFormatException ex) {
            value = defaultCondition;
        }
        return value;
    }

    /**
     * Conversion d'un long en tableau de (8) bytes.
     *
     * @param number
     * @param order
     * @return
     */
    public static byte[] toByte(long number, ByteOrder order) {
        byte[] bArray = new byte[8];
        ByteBuffer wrap = ByteBuffer.wrap(bArray);

        if (order != null) {
            wrap.order(order);
        }

        LongBuffer lBuffer = wrap.asLongBuffer();
        lBuffer.put(0, number);
        return bArray;
    }

    /**
     * Conversion d'un long en tableau de (8) bytes.
     *
     * @param number
     * @return
     */
    public static byte[] toByte(long number) {
        return toByte(number, null);
    }

    /**
     * Conversion d'un float en tableau de (4) bytes.
     *
     * @param number
     * @param order
     * @return
     */
    public static byte[] toByte(float number, ByteOrder order) {
        byte[] bArray = new byte[4];
        ByteBuffer wrap = ByteBuffer.wrap(bArray);

        if (order != null) {
            wrap.order(order);
        }

        FloatBuffer fBuffer = wrap.asFloatBuffer();
        fBuffer.put(0, number);
        return bArray;
    }

    /**
     * Conversion d'un float en tableau de (4) bytes.
     *
     * @param number
     * @return
     */
    public static byte[] toByte(float number) {
        return toByte(number, null);
    }

    /**
     * Conversion d'un double en tableau de (8) bytes.
     *
     * @param number
     * @param order
     * @return
     */
    public static byte[] toByte(double number, ByteOrder order) {
        byte[] bArray = new byte[8];
        ByteBuffer wrap = ByteBuffer.wrap(bArray);

        if (order != null) {
            wrap.order(order);
        }

        DoubleBuffer dBuffer = wrap.asDoubleBuffer();
        dBuffer.put(0, number);
        return bArray;
    }

    /**
     * Conversion d'un double en tableau de (8) bytes.
     *
     * @param number
     * @return
     */
    public static byte[] toByte(double number) {
        return toByte(number, null);
    }

    /**
     * Conversion d'un int en tableau de (4) bytes.
     *
     * @param number
     * @param order
     * @return
     */
    public static byte[] toByte(int number, ByteOrder order) {
        byte[] bArray = new byte[4];
        ByteBuffer wrap = ByteBuffer.wrap(bArray);

        if (order != null) {
            wrap.order(order);
        }

        IntBuffer iBuffer = wrap.asIntBuffer();
        iBuffer.put(0, number);
        return bArray;
    }

    /**
     * Conversion d'un int en tableau de (4) bytes.
     *
     * @param number
     * @return
     */
    public static byte[] toByte(int number) {
        return toByte(number, null);
    }

    /**
     * Conversion d'un short en tableau de (2) bytes.
     *
     * @param number
     * @param order
     * @return
     */
    public static byte[] toByte(short number, ByteOrder order) {
        byte[] bArray = new byte[2];
        ByteBuffer wrap = ByteBuffer.wrap(bArray);

        if (order != null) {
            wrap.order(order);
        }

        ShortBuffer sBuffer = wrap.asShortBuffer();
        sBuffer.put(0, number);
        return bArray;
    }

    /**
     * Conversion d'un short en tableau de (2) bytes.
     *
     * @param number
     * @return
     */
    public static byte[] toByte(short number) {
        return toByte(number, null);
    }

    /**
     * Insertion d'un short dans le tableau de bytes.
     *
     * @param buffer
     * @param start
     * @param sample
     * @param order
     * @return
     */
    public static byte[] toByte(byte[] buffer, int start, short sample, ByteOrder order) {
        ByteBuffer wrap = ByteBuffer.wrap(buffer);

        if (order != null) {
            wrap.order(order);
        }
        return wrap.putShort(start, sample).array();
    }

    /**
     * Décodage des bytes base 64 en taableau byte.
     *
     * @param buffer
     * @return
     */
    public static byte[] toByteBase64(byte[] buffer) {
        return Base64.getDecoder().decode(buffer);
    }

    /**
     * Conversion d'un objet en boolean.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static boolean toBoolean(Object condition, boolean defaultCondition) {
        boolean value;

        String booleanString = toStringNumber(condition).toLowerCase();
        value = switch (booleanString) {
            case "0", "false" ->
                false;
            case "1", "true" ->
                true;
            default ->
                defaultCondition;
        };
        return value;
    }

    /**
     * Conversion d'un objet en short.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static short toShort(Object condition, short defaultCondition) {
        short value;

        try {
            value = Short.parseShort(toStringNumber(condition));
        } catch (NumberFormatException ex) {
            value = defaultCondition;
        }
        return value;
    }

    /**
     * Conversion d'un tableau de bytes en short.
     *
     * @param buffer
     * @param start
     * @param order
     * @return
     */
    public static short toShort(byte[] buffer, int start, ByteOrder order) {
        ByteBuffer wrap = ByteBuffer.wrap(buffer);

        if (order != null) {
            wrap.order(order);
        }
        return wrap.getShort(start);
    }

    /**
     * Conversion d'un tableau de (2) bytes en short.
     *
     * @param buffer
     * @return
     */
    public static short toShort(byte[] buffer) {
        return toShort(buffer, 0, null);
    }

    /**
     * Conversion d'un objet en int.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static int toInt(Object condition, int defaultCondition) {
        int value;

        try {
            value = Integer.parseInt(toStringNumber(condition));
        } catch (NumberFormatException ex) {
            value = defaultCondition;
        }
        return value;
    }

    /**
     * Conversion d'un tableau de bytes en int.
     *
     * @param buffer
     * @param start
     * @param order
     * @return
     */
    public static int toInt(byte[] buffer, int start, ByteOrder order) {
        ByteBuffer wrap = ByteBuffer.wrap(buffer);

        if (order != null) {
            wrap.order(order);
        }
        return wrap.getInt(start);
    }

    /**
     * Conversion d'un tableau de (4) bytes en int.
     *
     * @param buffer
     * @return
     */
    public static int toInt(byte[] buffer) {
        return toInt(buffer, 0, null);
    }

    /**
     * Conversion d'un objet en float.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static float toFloat(Object condition, float defaultCondition) {
        float value;

        try {
            value = Float.parseFloat(toStringNumber(condition));
        } catch (NumberFormatException ex) {
            value = defaultCondition;
        }
        return value;
    }

    /**
     * Conversion d'un tableau de bytes en float.
     *
     * @param buffer
     * @param start
     * @param order
     * @return
     */
    public static float toFloat(byte[] buffer, int start, ByteOrder order) {
        ByteBuffer wrap = ByteBuffer.wrap(buffer);

        if (order != null) {
            wrap.order(order);
        }
        return wrap.getFloat(start);
    }

    /**
     * Conversion d'un tableau de (4) bytes en float.
     *
     * @param buffer
     * @return
     */
    public static float toFloat(byte[] buffer) {
        return toFloat(buffer, 0, null);
    }

    /**
     * Conversion d'un objet en double.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static double toDouble(Object condition, double defaultCondition) {
        double value;

        try {
            value = Double.parseDouble(toStringNumber(condition));
        } catch (NumberFormatException ex) {
            value = defaultCondition;
        }
        return value;
    }

    /**
     * Conversion d'un tableau de bytes en double.
     *
     * @param buffer
     * @param start
     * @param order
     * @return
     */
    public static double toDouble(byte[] buffer, int start, ByteOrder order) {
        ByteBuffer wrap = ByteBuffer.wrap(buffer);

        if (order != null) {
            wrap.order(order);
        }
        return wrap.getDouble(start);
    }

    /**
     * Conversion d'un tableau de (8) bytes en double.
     *
     * @param buffer
     * @return
     */
    public static double toDouble(byte[] buffer) {
        return toDouble(buffer, 0, null);
    }

    /**
     * Conversion d'un objet en long.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static long toLong(Object condition, long defaultCondition) {
        long value;

        try {
            value = Long.parseLong(toStringNumber(condition));
        } catch (NumberFormatException ex) {
            value = defaultCondition;
        }
        return value;
    }

    /**
     * Conversion d'un tableau de bytes en long.
     *
     * @param buffer
     * @param start
     * @param order
     * @return
     */
    public static long toLong(byte[] buffer, int start, ByteOrder order) {
        ByteBuffer wrap = ByteBuffer.wrap(buffer);

        if (order != null) {
            wrap.order(order);
        }
        return wrap.getLong(start);
    }

    /**
     * Conversion d'un tableau de (8) bytes en long.
     *
     * @param buffer
     * @return
     */
    public static long toLong(byte[] buffer) {
        return toLong(buffer, 0, null);
    }

    /**
     * Conversion d'un objet en couleur.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static Color toColor(Object condition, Color defaultCondition) {
        Color value;

        try {
            value = Color.decode(toStringNumber(condition));
        } catch (NumberFormatException ex) {
            value = defaultCondition;
        }
        return value;
    }

    /**
     * Conversion de l'objet en temps horodaté.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static long toTimestamp(Object condition, long defaultCondition) {
        long timestamp;

        Date parsedDate = DateHelper.getDateInternational(toString(condition, ""));
        if (parsedDate != null) {
            timestamp = parsedDate.getTime();
        } else {
            timestamp = defaultCondition;
        }
        return timestamp;
    }

    /**
     * Conversion de l'objet en chaine de caractères.
     *
     * @param condition
     * @param defaultCondition
     * @return
     */
    public static String toString(Object condition, String defaultCondition) {
        String str = null;

        if (condition != null) {
            try {
                str = String.valueOf(condition);
            } catch (Exception ex) {
                str = null;
            }
        }

        if (str == null) {
            str = defaultCondition;
        }
        return str;
    }

    /**
     * Conversion d'objet en chaine de caractère "numérique".
     * Supprime les invalides, transforme l'hexadécimal.
     *
     * @param condition
     * @return
     */
    public static String toStringNumber(Object condition) {
        // Nettoyage de la valeur (suppression des caractères spéciaux)
        String number = toString(condition, "").replaceAll("[^A-Za-z-0-9.,#]", "");

        // Détection du format hexadécimal
        if (number.matches("([-]*)([0x|0X|#])*([0-9]*)([A-Fa-f])+([0-9]*)")) {
            // Tentative de conversion de la chaine hexadécimal en chiffre
            try {
                number = String.valueOf(Long.decode(number));
            } catch (NumberFormatException e) {
                try {
                    // Force le décodage même sans préfixe
                    number = String.valueOf(Long.parseLong(number, 16));
                } catch (NumberFormatException ex) {
                    LoggerManager.getInstance().addWarning(ex.getMessage());
                }
            }
        }
        return number;
    }

    /**
     * Décodage de chaine base 64 en chaine.
     *
     * @param buffer
     * @return
     */
    public static String toStringBase64(String buffer) {
        return toStringBase64(buffer.getBytes());
    }

    /**
     * Décodage des bytes base 64 en chaine.
     *
     * @param buffer
     * @return
     */
    public static String toStringBase64(byte[] buffer) {
        return new String(toByteBase64(buffer));
    }

    /**
     * Retourne l'énumeration basé sur la classe d'enumeration et la valeur insensible à la case.
     *
     * @param <T>
     * @param enumType
     * @param value
     * @param defaultCondition
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T toEnum(Class<T> enumType, String value, T defaultCondition) {
        T enumFind = null;

        if (enumType != null && value != null) {
            Field[] fields = enumType.getFields();
            for (Field field : fields) {
                if (field.getName().equalsIgnoreCase(value)) {
                    try {
                        enumFind = (T) field.get(enumType);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        LoggerManager.getInstance().addWarning(ex.getMessage());
                    }
                    break;
                }
            }
        }

        if (enumFind == null) {
            enumFind = defaultCondition;
        }
        return enumFind;
    }

    /**
     * Retourne l'énumeration basé sur la classe d'enumeration et son index.
     *
     * @param <T>
     * @param enumType
     * @param enumValue
     * @param defaultCondition
     * @return
     */
    public static <T extends Enum<T>> T toEnum(Class<T> enumType, int enumValue, T defaultCondition) {
        T enumFind = null;

        if (enumType != null) {
            EnumSet<T> allEnum = EnumSet.allOf(enumType);

            if (!allEnum.isEmpty()) {
                for (T currentEnum : allEnum) {
                    if (currentEnum.ordinal() == enumValue) {
                        enumFind = currentEnum;
                        break;
                    }
                }
            }
        }

        if (enumFind == null) {
            enumFind = defaultCondition;
        }
        return enumFind;
    }

    /**
     * Encodage du tableau de bytes en base 64.
     *
     * @param buffer
     * @return
     */
    public static byte[] toBase64Byte(byte[] buffer) {
        return Base64.getEncoder().encode(buffer);
    }

    /**
     * Encodage de la chaine en base 64.
     *
     * @param buffer
     * @return
     */
    public static String toBase64String(String buffer) {
        return toBase64String(buffer.getBytes());
    }

    /**
     * Encodage du tableau de bytes en chaine base 64.
     *
     * @param buffer
     * @return
     */
    public static String toBase64String(byte[] buffer) {
        return new String(toBase64Byte(buffer));
    }

}
