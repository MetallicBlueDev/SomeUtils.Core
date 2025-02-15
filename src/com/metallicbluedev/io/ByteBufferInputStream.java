package com.metallicbluedev.io;

import java.io.*;
import java.nio.*;

/**
 * Flux d'entrée pour un ByteBuffer.
 *
 * @version 1.00.01
 * @author Sebastien Villemain
 */
public class ByteBufferInputStream extends InputStream {

    /**
     * Flux à gérer.
     */
    private final ByteBuffer buffer;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        int rslt = -1;

        if (buffer.hasRemaining()) {
            rslt = buffer.get();
        }
        return rslt;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        len = Math.min(len, buffer.remaining());

        if (len > 0) {
            buffer.get(b, off, len);
        }
        return len;
    }

    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }
}
