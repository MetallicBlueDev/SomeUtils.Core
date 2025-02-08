package com.tr4ncer.io;

import com.tr4ncer.logger.*;
import com.tr4ncer.utils.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

/**
 * Lecture et écriture sur le flux de données.
 *
 * @version 1.05.01
 * @author Sebastien Villemain
 */
public class GenericStream implements Closeable {

    /**
     * Flux d'entrée du fichier.
     */
    private final InputStream input;

    /**
     * Flux de sortie du fichier.
     */
    private final OutputStream output;

    /**
     * L'objet géré.
     */
    private final Object raw;

    /**
     * Affecte les flux.
     *
     * @param input
     * @param output
     */
    public GenericStream(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        raw = null;
    }

    /**
     * Ouvre et affecte les flux.
     *
     * @param file
     * @throws FileNotFoundException
     */
    public GenericStream(File file) throws FileNotFoundException {
        input = new FileInputStream(file);
        output = new FileOutputStream(file);
        raw = file;
    }

    /**
     * Ouvre et affecte les flux.
     *
     * @param buffer
     */
    public GenericStream(ByteBuffer buffer) {
        input = new ByteBufferInputStream(buffer);
        output = new ByteBufferOutputStream(buffer);
        raw = buffer;
    }

    /**
     * Retourne l'objet buffer géré.
     * En cas d'erreur, retourne <code>null</code>.
     *
     * @return
     */
    public ByteBuffer getRawByteBuffer() {
        ByteBuffer bb = null;

        if (raw instanceof ByteBuffer byteBuffer) {
            bb = byteBuffer;
        }
        return bb;
    }

    /**
     * Retourne l'objet buffer géré.
     * En cas d'erreur, retourne <code>null</code>.
     *
     * @return
     */
    public File getRawFile() {
        File f = null;

        if (raw instanceof File file) {
            f = file;
        }
        return f;
    }

    /**
     * Lecture des données.
     *
     * @param buffer
     * @return
     * @throws RuntimeException
     */
    public int readData(byte[] buffer) throws RuntimeException {
        return readData(buffer, 0, buffer.length);
    }

    /**
     * Lecture des données à la position cible.
     *
     * @param buffer
     * @param off
     * @param len
     * @return
     * @throws RuntimeException
     */
    public int readData(byte[] buffer, int off, int len) throws RuntimeException {
        try {
            return input.read(buffer, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lecture des données réservées.
     *
     * @param nbBytes
     */
    public void readReserved(int nbBytes) {
        byte[] buffer = new byte[nbBytes];
        readData(buffer);
    }

    /**
     * Lecture de la donnée suivante.
     * Retourne le résulat sous forme d'objet typé.
     * Support des valeurs nulles.
     * Ne retourne pas de tableau.
     *
     * @return Object or <code>null</code>
     */
    private Object readDataTyped() {
        Object data = null;

        // Lecture de la taille de la chaine
        byte[] buffer = new byte[4];
        readData(buffer);
        int size = ConvertHelper.toInt(buffer);

        if (size > 0) {
            // Lecture de la chaine
            buffer = new byte[size];
            readData(buffer);

            String dataType = new String(buffer);
            switch (dataType) {
                case "Boolean" -> {
                    buffer = new byte[1];
                    readData(buffer);
                    data = (buffer[0] == 1);
                }
                case "Byte" -> {
                    buffer = new byte[1];
                    readData(buffer);
                    data = buffer[0];
                }
                case "Short" -> {
                    buffer = new byte[2];
                    readData(buffer);
                    data = ConvertHelper.toShort(buffer);
                }
                case "Integer" -> {
                    buffer = new byte[4];
                    readData(buffer);
                    data = ConvertHelper.toInt(buffer);
                }
                case "Float" -> {
                    buffer = new byte[4];
                    readData(buffer);
                    data = ConvertHelper.toFloat(buffer);
                }
                case "Double" -> {
                    buffer = new byte[8];
                    readData(buffer);
                    data = ConvertHelper.toDouble(buffer);
                }
                case "Long" -> {
                    buffer = new byte[8];
                    readData(buffer);
                    data = ConvertHelper.toLong(buffer);
                }
                case "String" -> {
                    // Lecture de la taille de la chaine
                    buffer = new byte[4];
                    readData(buffer);
                    size = ConvertHelper.toInt(buffer);

                    if (size > 0) {
                        buffer = new byte[size];
                        readData(buffer);
                        data = new String(buffer);
                    }
                }
                case "Serializable" -> {
                    // Lecture de la taille de la chaine
                    buffer = new byte[4];
                    readData(buffer);
                    size = ConvertHelper.toInt(buffer);

                    if (size > 0) {
                        buffer = new byte[size];
                        readData(buffer);

                        try {
                            try (ByteArrayInputStream bis = new ByteArrayInputStream(buffer); ObjectInputStream in = new ObjectInputStream(bis)) {
                                data = in.readObject();
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            LoggerManager.getInstance().addError(e);
                        }
                    }
                }
                case "Nullable" -> {
                    // Lecture du byte de séparation
                    buffer = new byte[1];
                    readData(buffer);

                    // Affecte la donnée a une valeur nulle
                    data = null;
                }
            }
        }
        return data;
    }

    public boolean readBoolean() {
        return ConvertHelper.toBoolean(readDataTyped(), false);
    }

    public byte readByte() {
        return ConvertHelper.toByte(readDataTyped(), (byte) 0);
    }

    public short readShort() {
        return ConvertHelper.toShort(readDataTyped(), (short) 0);
    }

    public int readInt() {
        return ConvertHelper.toInt(readDataTyped(), 0);
    }

    public float readFloat() {
        return ConvertHelper.toFloat(readDataTyped(), 0);
    }

    public double readDouble() {
        return ConvertHelper.toDouble(readDataTyped(), 0);
    }

    public long readLong() {
        return ConvertHelper.toLong(readDataTyped(), 0);
    }

    public String readString() {
        return ConvertHelper.toString(readDataTyped(), null);
    }

    public Object readSerializable() {
        return readDataTyped();
    }

    public void readSerializableAndMerge(Object newObject) {
        Object readSerializable = readSerializable();
        merge(newObject, readSerializable);
    }

    /**
     * Ecriture des données.
     *
     * @param buffer
     * @throws RuntimeException
     */
    public void writeData(byte[] buffer) throws RuntimeException {
        writeData(buffer, 0, buffer.length);
    }

    /**
     * Ecriture des données.
     *
     * @param buffer
     * @param off
     * @param len
     * @throws RuntimeException
     */
    public void writeData(byte[] buffer, int off, int len) throws RuntimeException {
        try {
            output.write(buffer, off, len);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Ecriture des données réservées.
     *
     * @param nbBytes
     */
    public void writeReserved(int nbBytes) {
        byte[] buffer = new byte[nbBytes];
        Arrays.fill(buffer, (byte) 0x00);
        writeData(buffer);
    }

    /**
     * Ecriture d'une donnée typée.
     * Support des tableaux et des valeurs nulles.
     *
     * @param data
     */
    public void writeDataTyped(Object data) throws RuntimeException {
        int length = 0;

        if (data != null) {
            try {
                length = Array.getLength(data);
            } catch (IllegalArgumentException e) {
                LoggerManager.getInstance().addError(e);
            }
        }

        if (length > 0) {
            // Support des tableaux
            for (int index = 0; index < length; index++) {
                writeDataTyped(Array.get(data, index));
            }
        } else {
            String dataType = getSupportedObjectType(data);

            if (dataType == null) {
                throw new RuntimeException("Unsupported data type"
                                           + ((data != null) ? ": " + data.getClass().getSimpleName() : " [null]")
                                           + ".");
            }

            // Ecriture du type de donnée
            writeData(ConvertHelper.toByte(dataType.length()));
            writeData(dataType.getBytes());

            byte[] dataBytes = new byte[1];
            switch (dataType) {
                case "Boolean" ->
                    dataBytes[0] = (((boolean) data)) ? (byte) 1 : (byte) 0;
                case "Byte" ->
                    dataBytes[0] = (byte) data;
                case "Short" ->
                    dataBytes = ConvertHelper.toByte((short) data);
                case "Integer" ->
                    dataBytes = ConvertHelper.toByte((int) data);
                case "Float" ->
                    dataBytes = ConvertHelper.toByte((float) data);
                case "Double" ->
                    dataBytes = ConvertHelper.toByte((double) data);
                case "Long" ->
                    dataBytes = ConvertHelper.toByte((long) data);
                case "String" -> {
                    if (data != null) {
                        String dataString = (String) data;

                        // Ecriture de la taille de la chaine
                        writeData(ConvertHelper.toByte(dataString.length()));
                        dataBytes = dataString.getBytes();
                    } else {
                        // Rien a écrire: la taille est de zéro
                        dataBytes[0] = 0x00;
                    }
                }
                case "Serializable" -> {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
                            out.writeObject(data);
                        }

                        dataBytes = bos.toByteArray();

                        // Ecriture de la taille de la chaine
                        writeData(ConvertHelper.toByte(dataBytes.length));
                    } catch (IOException | RuntimeException e) {
                        LoggerManager.getInstance().addError(e);
                    }
                }
                case "Nullable" ->
                    dataBytes[0] = 0x00;
            }

            // Ecriture de la donnée
            writeData(dataBytes);
        }
    }

    /**
     * Retourne le type de l'objet.
     *
     * @param data
     * @return String or <code>null</code>
     */
    private static String getSupportedObjectType(Object data) {
        String dataType = null;

        // Support des valeurs nulles
        if (data instanceof Boolean) {
            dataType = "Boolean";
        } else if (data instanceof Byte) {
            dataType = "Byte";
        } else if (data instanceof Short) {
            dataType = "Short";
        } else if (data instanceof Integer) {
            dataType = "Integer";
        } else if (data instanceof Float) {
            dataType = "Float";
        } else if (data instanceof Double) {
            dataType = "Double";
        } else if (data instanceof Long) {
            dataType = "Long";
        } else if (data instanceof String) {
            dataType = "String";
        } else if (data instanceof Serializable) {
            dataType = "Serializable";
        } else if (data == null) {
            dataType = "Nullable";
        }
        return dataType;
    }

    /**
     * Détermine si le type d'objet est supporté.
     *
     * @param data
     * @return
     */
    public static boolean isSupportedObjectType(Object data) {
        return getSupportedObjectType(data) != null;
    }

    /**
     * Fermeture du flux.
     */
    @Override
    public void close() {
        if (input != null) {
            try {
                input.close();
            } catch (IOException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }

        if (output != null) {
            try {
                output.close();
            } catch (IOException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
    }

    /**
     * Retourne le nombre de bytes restant.
     *
     * @return
     */
    public int available() {
        int rslt = -1;

        if (input != null) {
            try {
                rslt = input.available();
            } catch (IOException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
        return rslt;
    }

    /**
     * Ajoute les données du flux à cette séquence.
     *
     * @param stream
     */
    public void append(GenericStream stream) {
        Object rawObject;

        if (stream.input.markSupported()) {
            try {
                stream.input.reset();
            } catch (IOException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }

        do {
            rawObject = stream.readDataTyped();

            if (rawObject != null) {
                writeDataTyped(rawObject);
            }
        } while (rawObject != null);
    }

    /**
     * Affecte les données pour chaque champs.
     *
     * @param newObject
     * @param serializedObject
     */
    private static void merge(Object newObject, Object serializedObject) {
        if (serializedObject != null && newObject != null) {
            Class<?> currentType = newObject.getClass();

            while (currentType != null && currentType != Object.class) {
                for (Field field : currentType.getDeclaredFields()) {
                    if (Modifier.isTransient(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers())
                        || Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    field.setAccessible(true);

                    try {
                        field.set(newObject, field.get(serializedObject));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        LoggerManager.getInstance().addError(e);
                    }
                }

                currentType = currentType.getSuperclass();
            }
        }
    }
}
