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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is an input stream that provides an optional read limit. 
 * 
 * <p>All read methods guarantees that the number of bytes requested are also read, 
 * or an {@link EOFException} is thrown. 
 * 
 * @author mikael.brannstrom
 *
 */
public class LimitInputStream extends InputStream {

    private InputStream in;
    private int limit = -1;
    
    /** Create a new limit input stream with no limit initially.
     * 
     * @param in the wrapped input stream.
     */
    protected LimitInputStream(InputStream in) {
        this.in = in;
    }
    
    /** Returns the number of bytes remaining to the limit. */
    public int limit() {
        return limit;
    }
    
    /** Set the limit.
     * 
     * @param limit the number of bytes to be able to read, or -1 for no limit.
     */
    public void limit(int limit) {
        this.limit = limit;
    }

    /** Read the next byte from the underlying input stream.
     * @return the next byte (never -1).
     * @throws EOFException if there is no more data.
     * @throws LimitException if the limit has been reached.
     */
    @Override
    public int read() throws IOException {
        if (limit == 0) {
            throw new LimitException();
        }
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        if (limit > 0) {
            limit--;
        }
        return b;
    }

    /** Read bytes from the underlying input stream.
     * 
     * @param buf the buffer into which the data is read 
     * 
     * @return the number of bytes read, always bytes.length.
     * @throws EOFException if there is no more data.
     * @throws LimitException if the limit has been reached.
     */
    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }
    
    /** Read bytes from the underlying input stream.
     * 
     * @param buf the buffer into which the data is read
     * @param offset
     * @param length 
     * 
     * @return the number of bytes read, always length.
     * @throws EOFException if there is no more data.
     * @throws LimitException if the limit has been reached.
     */
    @Override
    public int read(byte[] buf, int offset, int length) throws IOException {
        if (length > limit && limit >= 0) {
            read(buf, offset, limit);
            throw new LimitException();
        }
        final int totalRead = length;
        while (length > 0) {
            int read = in.read(buf, offset, length);
            if (read == -1) {
                throw new EOFException();
            }
            offset += read;
            length -= read;
            if (limit > 0) {
                limit -= read;
            }
        }
        return totalRead;
    }
    
    /** Skip bytes from the underlying input stream.
     * 
     * @param n the number of bytes to skip 
     * 
     * @return the number of bytes skipped, always n.
     * @throws EOFException if there is no more data.
     * @throws LimitException if the limit has been reached.
     */
    @Override
    public long skip(long n) throws IOException {
        if (n > limit && limit >= 0) {
            skip(limit);
            throw new LimitException();
        }
        final long totalSkipped = n;
        while (n > 0) {
            long skipped = in.skip(n);
            if (limit > 0) {
                limit -= skipped;
            }
        }
        return totalSkipped;
    }
    
}
