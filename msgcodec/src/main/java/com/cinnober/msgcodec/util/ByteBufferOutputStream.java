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
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 *
 * An output stream backed by a ByteBuffer.
 *
 * A write will advance the position of the underlying buffer until it reaches the limit,
 * when an IOException is thrown.
 *
 * @author mikael.brannstrom
 *
 */
public class ByteBufferOutputStream extends OutputStream {
    private ByteBuffer buffer;

    /**
     * Create a new byte buffer output stream.
     *
     * @param buffer the buffer to write to, or null if uninitialized.
     * Writing to an uninitialized stream will throw {@link NullPointerException}.
     */
    public ByteBufferOutputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Set the underlying buffer to write to.
     *
     * @param buffer the buffer to write to, or null if uninitialized.
     * Writing to an uninitialized stream will throw {@link NullPointerException}.
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
    public void write(int b) throws IOException {
        try {
            buffer.put((byte)b);
        } catch (BufferOverflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            buffer.put(b, off, len);
        } catch (BufferOverflowException e) {
            throw new IOException(e);
        }
    }


}
