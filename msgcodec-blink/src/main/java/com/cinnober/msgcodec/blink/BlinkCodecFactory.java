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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.MsgCodecInstantiationException;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.MsgCodecFactory;
import com.cinnober.msgcodec.util.ConcurrentBufferPool;
import com.cinnober.msgcodec.util.Pool;
import java.util.Objects;

/**
 * Factory for BlinkCodec.
 * 
 * @author mikael.brannstrom
 */
public class BlinkCodecFactory implements MsgCodecFactory {

    private final Schema schema;
    private Pool<byte[]> bufferPool;
    private int maxBinarySize = 10 * 1_048_576; // 10 MB
    private int maxSequenceLength = 1_000_000;
    private CodecOption codecOption;

    /**
     * Create a Blink codec factory.
     * 
     * @param schema the protocol schema to be used by all codec instances, not null.
     */
    public BlinkCodecFactory(Schema schema) {
        if (!schema.isBound()) {
            throw new IllegalArgumentException("Schema must be bound");
        }
        this.schema = schema;
        this.bufferPool = new ConcurrentBufferPool(8192, 10);
        this.codecOption = CodecOption.AUTOMATIC;
    }

    /**
     * Set the buffer pool.
     * 
     * @param bufferPool the buffer pool to be used by all codec instances, not null.
     * @return this factory.
     */
    public BlinkCodecFactory setBufferPool(Pool<byte[]> bufferPool) {
        this.bufferPool = Objects.requireNonNull(bufferPool);
        return this;
    }

    /**
     * Set the maxiumum binary size allowed while decoding.
     * 
     * @param maxBinarySize the maximum binary size (including strings) allowed while decoding, or -1 for no limit.
     * @return this factory.
     */
    public BlinkCodecFactory setMaxBinarySize(int maxBinarySize) {
        this.maxBinarySize = maxBinarySize;
        return this;
    }

    /**
     * Set the maxium sequence length allowed while decoding.
     * 
     * @param maxSequenceLength the maximum sequence length allowed while decoding, or -1 for no limit.
     * @return this factory.
     */
    public BlinkCodecFactory setMaxSequenceLength(int maxSequenceLength) {
        this.maxSequenceLength = maxSequenceLength;
        return this;
    }

    BlinkCodecFactory setCodecOption(CodecOption codecOption) {
        this.codecOption = Objects.requireNonNull(codecOption);
        return this;
    }

    @Override
    public BlinkCodec createCodec() throws MsgCodecInstantiationException {
        return new BlinkCodec(schema, bufferPool, maxBinarySize, maxSequenceLength, codecOption);
    }
    
}
