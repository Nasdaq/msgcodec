/*
 * Copyright (c) 2013 Cinnober Financial Technology AB, Stockholm,
 * Sweden. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 *
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */
package com.cinnober.msgcodec.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * An input stream backed by a ByteBuffer.
 *
 * A read will advance the position of the underlying buffer until it reaches the limit,
 * when {@link BufferUnderflowException} is thrown.
 *
 * @author mikael.brannstrom
 *
 */
public class ByteBufferInputStream extends InputStream {
    private ByteBuffer buffer;

    /**
     * Create a new byte buffer input stream.
     *
     * @param buffer the buffer to read from, or null if uninitialized.
     * Reading from an uninitialized stream will throw {@link NullPointerException}.
     */
    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }
    /**
     * Set the underlying buffer to read from.
     *
     * @param buffer the buffer to read from, or null if uninitialized.
     * Reading from an uninitialized stream will throw {@link NullPointerException}.
     */
    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
    /**
     * Returns the underlying buffer.
     *
     * @return the buffer.
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int read() throws IOException, BufferUnderflowException {
        return buffer.get() & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException, BufferUnderflowException {
        if (len > buffer.remaining()) {
            len = buffer.remaining();
        }
        buffer.get(b, off, len);
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n > buffer.remaining()) {
            n = buffer.remaining();
        }
        buffer.position((int)(buffer.position() + n));
        return n;
    }

    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }
}
