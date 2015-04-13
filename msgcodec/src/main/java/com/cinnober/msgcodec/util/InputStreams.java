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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for InputStreams.
 *
 * @author mikael.brannstrom
 */
public class InputStreams {
    /**
     * Read data into the specified byte array.
     * 
     * <p>See the general contract of the {@link java.io.DataInput#readFully(byte[])}.
     *
     * @param in the stream to read from, not null.
     * @param b the byte array into which the data is read, not null.
     * @throws EOFException if this input stream reaches the end before reading all the bytes.
     * @throws IOException if the stream throws an exception.
     * @see java.io.DataInput#readFully(byte[])
     */
    public static void readFully(InputStream in, byte b[]) throws IOException {
        readFully(in, b, 0, b.length);
    }

    /**
     * Read data into the specified byte array.
     *
     * <p>See the general contract of the {@link java.io.DataInput#readFully(byte[])}.
     *
     * @param in the stream to read from, not null.
     * @param b the byte array into which the data is read, not null.
     * @param off the start offset of the data.
     * @param len the number of bytes to read.
     * @throws EOFException if this input stream reaches the end before reading all the bytes.
     * @throws IOException if the stream throws an exception.
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    public static void readFully(InputStream in, byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }
}
