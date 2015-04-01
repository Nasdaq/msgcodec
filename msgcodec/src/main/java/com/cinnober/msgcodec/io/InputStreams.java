/*
 * Copyright (c) 2014 Cinnober Financial Technology AB, Stockholm,
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
