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
package com.cinnober.msgcodec.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 *
 * An output stream backed by a ByteBuffer.
 *
 * A write will advance the position of the underlying buffer until it reaches the limit,
 * when {@link BufferOverflowException} is thrown.
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
    public void write(int b) throws IOException, BufferOverflowException {
        buffer.put((byte)b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException, BufferOverflowException {
        buffer.put(b, off, len);
    }


}
