/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.tap;

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
    private final Pool<byte[]> bufferPool;

    /**
     * Create a Blink codec factory.
     * 
     * @param dictionary the protocol dictionary to be used by all codec instances, not null.
     */
    public TapCodecFactory(ProtocolDictionary dictionary) {
        this(dictionary, new ConcurrentBufferPool(8192, 10));
    }

    /**
     * Create a Blink codec factory.
     * 
     * @param dictionary the protocol dictionary to be used by all codec instances, not null.
     * @param bufferPool the buffer pool to be used by all codec instances, not null.
     */
    public TapCodecFactory(ProtocolDictionary dictionary, Pool<byte[]> bufferPool) {
        this.dictionary = Objects.requireNonNull(dictionary);
        this.bufferPool = Objects.requireNonNull(bufferPool);
    }
    
    @Override
    public TapCodec createStreamCodec() {
        return new TapCodec(dictionary, bufferPool);
    }
    
}
