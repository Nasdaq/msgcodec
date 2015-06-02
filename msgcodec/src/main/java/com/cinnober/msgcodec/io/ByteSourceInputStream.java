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
import java.io.InputStream;
import java.util.Objects;

/**
 * An input stream wrapper for a byte source.
 * @author mikael.brannstrom
 */
public class ByteSourceInputStream extends InputStream {
    private final ByteSource src;

    /**
     * Create a new byte source input stream.
     * @param src the wrapped byte source to read from, not null.s
     */
    public ByteSourceInputStream(ByteSource src) {
        this.src = Objects.requireNonNull(src);
    }

    /**
     * Read a byte.
     * @return the byte read. Never returns -1. EOF is instead signaled with an {@link java.io.EOFException}.
     * @throws IOException if data could not be read, including reaching EOF.
     */
    @Override
    public int read() throws IOException {
        return src.read();
    }

    /**
     * Read data into the byte array.
     * @param b the byte array, not null.
     * @param off the offset in the byte array
     * @param len the number of bytes to read
     * @return the number of bytes read, which is always equals to <code>len</code>.
     * @throws IOException if data could not be read.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        src.read(b, off, len);
        return len;
    }

    /**
     * Read data into the byte array.
     * @param b the byte array, not null.
     * @return the number of bytes read, which is always equals to <code>b.length</code>.
     * @throws IOException if data could not be read.
     */
    @Override
    public int read(byte[] b) throws IOException {
        src.read(b);
        return b.length;
    }

}
