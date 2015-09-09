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

import java.nio.ByteBuffer;

/**
 * A byte buffer.
 *
 * ByteBuf has the same semantics as {@link java.nio.ByteBuffer}.
 * 
 * @author mikael.brannstrom
 */
public interface ByteBuf extends ByteSource, ByteSink {
    /**
     * Return the position.
     * @return the position.
     */
    int position();
    
    /**
     * Set the position.
     * @param position the new position.
     * @return this ByteBuf.
     */
    ByteBuf position(int position);

    /**
     * Return the limit.
     * @return the limit.
     */
    int limit();

    /**
     * Set the limit.
     * @param limit the new limit.
     * @return this ByteBuf.
     */
    ByteBuf limit(int limit);

    /**
     * Returns the capacity.
     * @return the capacity.
     */
    int capacity();

    /**
     * Set position to zero and limit to capacity.
     * @return this ByteBuf.
     */
    ByteBuf clear();

    /**
     * Set position to zero, and limit to previous position value.
     * @return this ByteBuf.
     */
    ByteBuf flip();

    /**
     * Shift the bytes in this buffer.
     * @param position the position in this buffer
     * @param length the number of bytes
     * @param distance the distance to move the data. A positive number shifts right, a negative number shifts left.
     */
    void shift(int position, int length, int distance);

    /**
     * Returns the number of elements between the current position and the limit.
     * @return The number of elements remaining in this buffer
     */
    default int remaining() {
        return limit() - position();
    }

    /**
     * Tells whether there are any elements between the current position and the limit.
     * @return true if, and only if, there is at least one element remaining in this buffer
     */
    default boolean hasRemaining() {
        return remaining() != 0;
    }
    
    /**
     * Returns the currently allocated size (valid for resizing buffer types, should otherwise be the capacity)
     * @return number of bytes
     */
    int allocation();
    
    public ByteBuffer getByteBuffer();
}
