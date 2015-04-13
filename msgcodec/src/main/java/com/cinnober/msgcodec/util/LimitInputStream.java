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
// PENDING: remove this class?
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

    protected final InputStream in;
    private int limit = -1;

    /** Create a new limit input stream with no limit initially.
     *
     * @param in the wrapped input stream.
     */
    public LimitInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Returns the number of bytes remaining to the limit.
     * @return the limit.
     */
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
     * @param offset the start offset in the buffer
     * @param length the number of bytes to read
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
