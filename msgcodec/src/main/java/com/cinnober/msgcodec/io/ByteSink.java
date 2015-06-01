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

/**
 * A byte sink to write data to.
 *
 * @author mikael.brannstrom
 */
public interface ByteSink {
    /**
     * Write a byte.
     * @param b the byte to write.
     * @throws IOException if data could not be written.
     */
    void write(int b) throws IOException;

    /**
     * Write bytes from a byte array.
     * @param b the byte array to write from, not null.
     * @param off the offset in the byte array.
     * @param len the number of bytes to write.
     * @throws IOException if data could not be written.
     */
    default void write(byte[] b, int off, int len) throws IOException {
        for (int i=off; i<len; i++) {
            write(b[i]);
        }
    }

    /**
     * Write bytes from a byte array.
     * @param b the byte array to write from, not null.
     * @throws IOException if data could not be written.
     */
    default void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Write a 4-byte integer, little endian.
     * @param v the integer value.
     * @throws IOException if data could not be written.
     */
    default void writeIntLE(int v) throws IOException {
        write(v);
        write(v >> 8);
        write(v >> 16);
        write(v >> 24);
    }

    /**
     * Write an 8-byte long, little endian.
     * @param v the long value.
     * @throws IOException if data could not be written.
     */
    default void writeLongLE(long v) throws IOException {
        write((int) v);
        write((int) (v >> 8));
        write((int) (v >> 16));
        write((int) (v >> 24));
        write((int) (v >> 32));
        write((int) (v >> 40));
        write((int) (v >> 48));
        write((int) (v >> 56));
    }

    /**
     * Write zero bytes.
     * @param n the number of zero bytes to write.
     * @throws IOException if data could not be written.
     */
    default void pad(int n) throws IOException {
        for (int i=0; i<n; i++) {
            write(0);
        }
    }
}
