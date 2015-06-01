/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
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
