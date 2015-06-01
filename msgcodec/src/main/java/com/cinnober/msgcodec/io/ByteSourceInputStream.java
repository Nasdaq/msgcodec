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
