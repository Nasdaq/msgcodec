/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 The MsgCodec Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
