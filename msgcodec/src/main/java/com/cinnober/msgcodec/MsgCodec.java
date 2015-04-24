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
package com.cinnober.msgcodec;

import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** 
 * A codec that can encode and decode messages.
 * 
 * <p>NOTE: A message codec implementation is NOT thread-safe, unless the documentation says so.</p>
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
