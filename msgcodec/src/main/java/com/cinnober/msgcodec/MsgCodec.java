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
package com.cinnober.msgcodec;

import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** 
 * A codec that can encode and decode messages.
 * 
 * <p>NOTE: A stream codec implementation is NOT thread-safe, unless the documentation says so.</p>
 *
 * <p>Codec implementations may support encoding and decoding null values. Trying to encode a null
 * value with an implementation that does not support it, will cause an unchecked exception to be thrown,
 * typically a NullPointerException.</p>
 * 
 * @author mikael.brannstrom
 * @see MsgCodecFactory
 */
public interface MsgCodec {
    /**
     * Write the group to the specified stream.
     * 
     * @param group the group to encode.
     * @param out the stream to write to, not null.
     * @throws IOException if the underlying stream throws an exception.
     * @throws IllegalArgumentException if the group is not correct or complete, e.g. a required field is missing.
     * Partial data may have been written to the byte sink.
     */
    void encode(Object group, OutputStream out) throws IOException, IllegalArgumentException;
    /**
     * Write the group to the byte sink.
     *
     * @param group the group to encode.
     * @param out the byte sink to write to, not null.
     * @throws IOException if the underlying byte sink throws an exception.
     * @throws IllegalArgumentException if the group is not correct or complete, e.g. a required field is missing.
     * Partial data may have been written to the byte sink.
     */
    void encode(Object group, ByteSink out) throws IOException, IllegalArgumentException;
    /**
     * Read a group from the specified stream.
     *
     * @param in the stream to read from, not null.
     * @return the decoded value.
     * @throws IOException if the underlying stream throws an exception.
     * @throws DecodeException if the value could not be decoded, or if a required field is missing.
     */
    Object decode(InputStream in) throws IOException, DecodeException;
    /**
     * Read a group from the specified byte source.
     *
     * @param in the byte source to read from, not null.
     * @return the decoded value.
     * @throws IOException if the underlying byte source throws an exception.
     * @throws DecodeException if the value could not be decoded, or if a required field is missing.
     */
    Object decode(ByteSource in) throws IOException, DecodeException;
}
