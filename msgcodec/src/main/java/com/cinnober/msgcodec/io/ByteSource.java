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
import java.nio.charset.Charset;

/**
 * A byte source to read data from.
 *
 * @author mikael.brannstrom
 */
public interface ByteSource {
    /** UTF-8 charset. */
    static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Read a byte.
     * 
     * @return the byte, in the range [0, 255].
     * @throws IOException if data could not be read, including e.g. {@link java.io.EOFException}.
     */
    int read() throws IOException;

    /**
     * Read data into the byte array.
     * 
     * @param b the byte array, not null.
     * @param off the offset in the byte array
     * @param len the number of bytes to read
     * @throws IOException if data could not be read.
     */
    default void read(byte[] b, int off, int len) throws IOException {
        for (int i=off; i<len; i++) {
            b[i] = (byte) read();
        }
    }
    /**
     * Read data into the byte array.
     * @param b the byte array, not null.
     * @throws IOException if data could not be read.
     */
    default void read(byte[] b) throws IOException {
        read(b, 0, b.length);
    }

    /**
     * Read a UTF-8 encoded string.
     * 
     * @param len the number of bytes to read.
     * @return the string value, not null.
     * @throws IOException if data could not be read, or not decoded as UTF-8.
     */
    default String readStringUtf8(int len) throws IOException {
        byte[] data = new byte[len];
        read(data);
        return new String(data, UTF8);
    }
    /**
     * Skip a number of bytes.
     * @param len the number of bytes to skip.
     * @throws IOException if data could not be read/skipped.
     */
    default void skip(int len) throws IOException {
        for (int i=0; i<len; i++) {
            read();
        }
    }

    /**
     * Read a 4-byte integer, little endian.
     * @return the integer value.
     * @throws IOException if data could not be read.
     */
    default int readIntLE() throws IOException {
        return read() | read() << 8 | read() << 16 | read() << 24;
    }

    /**
     * Read an 8-byte long, little endian.
     * @return the long value.
     * @throws IOException if data could not be read.
     */
    default long readLongLE() throws IOException {
        return
                read() |
                read() << 8 |
                read() << 16 |
                read() << 24 |
                (long)read() << 32 |
                (long)read() << 40 |
                (long)read() << 48 |
                (long)read() << 56;
    }
}
