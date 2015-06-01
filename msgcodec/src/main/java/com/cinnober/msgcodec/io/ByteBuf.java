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
}
