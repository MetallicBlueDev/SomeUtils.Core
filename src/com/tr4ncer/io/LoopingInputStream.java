package com.tr4ncer.io;

import com.tr4ncer.logger.*;
import com.tr4ncer.utils.*;
import java.io.*;

/**
 * Cette classe réagi comme un InputStream à la différence que la
 * lecture tourne indéfiniement, jusqu'à la fermeture du buffer.
 * <p>
 * Inspiré d'une excellente idée de David Brackeen.
 *
 * @version 1.02.00
 * @author David Brackeen - based on the book "Developing Games in Java".
 * @author Sebastien Villemain
 */
public class LoopingInputStream extends InputStream {

    /**
     * Indique l'état de fermeture.
     */
    private final InputStream input;

    /**
     * Nombre de boucle à executer.
     */
    private int nbLoops = 0;

    public LoopingInputStream(InputStream input) {
        this(input, 0);
    }

    public LoopingInputStream(InputStream input, int nbLoops) {
        this.input = input;
        this.nbLoops = nbLoops;

        try {
            input.mark(Integer.MAX_VALUE);
        } catch (Exception ex) {
            LoggerManager.getInstance().addError("Failed to initialize stream.", ex);
        }
    }

    /**
     * Lecture des données en boucle.
     * Si le flux est fermé ou si la nombre de lecture est atteint,
     * retourne <code>-1</code>.
     *
     * @param buffer
     * @param offset
     * @param length
     * @return
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        // Nombre de boucle
        int loop = 0;

        // Nombre de byte lu
        int totalBytesRead = 0;

        try {
            // Boucle jusqu'a la lecture complète
            while (totalBytesRead < length) {
                // Lecture depuis la dernière position
                int numBytesRead = input.read(
                    buffer,
                    offset + totalBytesRead,
                    length - totalBytesRead);

                if (numBytesRead > 0) {
                    // Il reste des données à lire
                    // Prépare la lecture suivante
                    totalBytesRead += numBytesRead;
                } else {
                    // Il ne reste plus rien à lire...
                    // Vérification de la boucle
                    if (nbLoops > 0) {
                        loop++;

                        // Si le nombre de boucle a été executé
                        if (loop >= nbLoops) {
                            // On arrête
                            break;
                        }
                    }

                    // On recommence
                    input.reset();
                    totalBytesRead = 0;
                }
            }
        } catch (IOException ex) {
            LoggerManager.getInstance().addWarning(ex.getMessage());
            totalBytesRead = -1;
        }
        return totalBytesRead;
    }

    @Override
    public int read() throws IOException {
        byte[] data = new byte[FileHelper.OPTIMIZED_BUFFER_SIZE];
        return read(data, 0, data.length);
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        input.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        input.reset();
    }

    @Override
    public boolean markSupported() {
        return input.markSupported();
    }
}
