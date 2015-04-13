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

package com.cinnober.msgcodec.tap;

import com.cinnober.msgcodec.StreamCodecInstantiationException;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.StreamCodecFactory;
import com.cinnober.msgcodec.util.ConcurrentBufferPool;
import com.cinnober.msgcodec.util.Pool;
import java.util.Objects;

/**
 * Factory for TapCodec.
 * 
 * @author mikael.brannstrom
 */
public class TapCodecFactory implements StreamCodecFactory {

    private final ProtocolDictionary dictionary;
    private Pool<byte[]> bufferPool;

    /**
     * Create a Blink codec factory.
     * 
     * @param dictionary the protocol dictionary to be used by all codec instances, not null.
     */
    public TapCodecFactory(ProtocolDictionary dictionary) {
        if (!dictionary.isBound()) {
            throw new IllegalArgumentException("Dictionary must be bound");
        }
        this.dictionary = dictionary;
        this.bufferPool = new ConcurrentBufferPool(8192, 10);
    }

    /**
     * Set the buffer pool.
     *
     * @param bufferPool the buffer pool to be used by all codec instances, not null.
     * @return this factory.
     */
    public TapCodecFactory setBufferPool(Pool<byte[]> bufferPool) {
        this.bufferPool = Objects.requireNonNull(bufferPool);
        return this;
    }

    @Override
    public TapCodec createStreamCodec() throws StreamCodecInstantiationException{
        return new TapCodec(dictionary, bufferPool);
    }
    
}
