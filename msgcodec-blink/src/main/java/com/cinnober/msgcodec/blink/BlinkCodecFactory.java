/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
