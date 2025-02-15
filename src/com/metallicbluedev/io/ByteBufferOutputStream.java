package com.metallicbluedev.io;

import java.io.*;
import java.nio.*;

/**
 * Flux de sortie pour un ByteBuffer.
 *
 * @version 1.00.01
 * @author Sebastien Villemain
 */
public class ByteBufferOutputStream extends OutputStream {

    /**
     * Flux à gérer.
     */
    private final ByteBuffer buffer;

    public ByteBufferOutputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.put((byte) b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        buffer.put(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        buffer.compact();
    }
}
