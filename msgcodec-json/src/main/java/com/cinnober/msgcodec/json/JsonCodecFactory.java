/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.json;

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.StreamCodecFactory;
import com.cinnober.msgcodec.util.ConcurrentBufferPool;
import com.cinnober.msgcodec.util.Pool;
import java.util.Objects;

/**
 * Factory for JsonCodec.
 * 
 * @author mikael.brannstrom
 */
public class JsonCodecFactory implements StreamCodecFactory {

    private final ProtocolDictionary dictionary;

    /**
     * Create a Blink codec factory.
     * 
     * @param dictionary the protocol dictionary to be used by all codec instances, not null.
     */
    public JsonCodecFactory(ProtocolDictionary dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    @Override
    public JsonCodec createStreamCodec() {
        return new JsonCodec(dictionary);
    }
    
}
