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
package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.ByteSink;
import com.cinnober.msgcodec.ByteSource;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.StreamCodecInstantiationException;
import com.cinnober.msgcodec.util.ConcurrentBufferPool;
import com.cinnober.msgcodec.util.InputStreamSource;
import com.cinnober.msgcodec.util.LimitInputStream;
import com.cinnober.msgcodec.util.OutputStreamSink;
import com.cinnober.msgcodec.util.Pool;
import com.cinnober.msgcodec.util.TempOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Blink codec can serialize and deserialize Java objects according to
 * the Blink compact binary encoding format.
 *
 * Null values are supported in encode and decode.
 *
 * <p>See the <a href="http://blinkprotocol.org/s/BlinkSpec-beta2.pdf">Blink Specification beta2 - 2013-02-05.</a>
 *
 * @author mikael.brannstrom
 * @see BlinkCodecFactory
 *
 */
public class BlinkCodec implements StreamCodec {
    private static final Logger log = Logger.getLogger(BlinkCodec.class.getName());

    private final GeneratedCodec generatedCodec;
    private final ProtocolDictionary dictionary;

    private final Pool<byte[]> bufferPool;

    private final int maxBinarySize;
    private final int maxSequenceLength;

    /**
     * Create a Blink codec, with an internal buffer pool of 8192 bytes.
     *
     * @param dictionary the definition of the messages to be understood by the codec.
     */
    BlinkCodec(ProtocolDictionary dictionary) throws StreamCodecInstantiationException {
        this(dictionary, new ConcurrentBufferPool(8192, 1));
    }
    /**
     * Create a Blink codec.
     *
     * @param dictionary the definition of the messages to be understood by the codec.
     * @param bufferPool the buffer pool, needed for temporary storage while <em>encoding</em>.
     */
    public BlinkCodec(ProtocolDictionary dictionary, Pool<byte[]> bufferPool) throws StreamCodecInstantiationException {
        this(dictionary, bufferPool, 10 * 1048576, 1_000_000, CodecOption.AUTOMATIC);
    }
    /**
     * Create a Blink codec.
     *
     * @param dictionary the definition of the messages to be understood by the codec.
     * @param bufferPool the buffer pool, needed for temporary storage while <em>encoding</em>.
     * @param maxBinarySize the maximum binary size (including strings) allowed while decoding, or -1 for no limit.
     * @param maxSequenceLength the maximum sequence length allowed while decoding, or -1 for no limit.
     * @param codecOption controls which kind of underlying codec to use, not null.
     */
    BlinkCodec(ProtocolDictionary dictionary, Pool<byte[]> bufferPool,
            int maxBinarySize, int maxSequenceLength, CodecOption codecOption) throws StreamCodecInstantiationException {
        if (!dictionary.isBound()) {
            throw new IllegalArgumentException("ProtocolDictionary not bound");
        }
        this.bufferPool = bufferPool;
        Objects.requireNonNull(codecOption);

        this.maxBinarySize = maxBinarySize;
        this.maxSequenceLength = maxSequenceLength;
        this.dictionary = dictionary;

        GeneratedCodec generatedCodecTmp = null;
        if (codecOption != CodecOption.INSTRUCTION_CODEC_ONLY) {
            try {
                Class<GeneratedCodec> generatedCodecClass =
                        GeneratedCodecClassLoader.getInstance().getGeneratedCodecClass(dictionary);
                Constructor<GeneratedCodec> constructor =
                        generatedCodecClass.getConstructor(new Class<?>[]{ BlinkCodec.class, ProtocolDictionary.class });
                generatedCodecTmp = constructor.newInstance(this, dictionary);
            } catch (Exception e) {
                log.log(Level.WARNING,
                        "Could instantiate generated codec for dictionary UID " + dictionary.getUID(), e);
                if (codecOption == CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY) {
                    throw new StreamCodecInstantiationException(e);
                }
                log.log(Level.INFO, "Fallback to (slower) instruction based codec for dictionary UID {0}",
                        dictionary.getUID());
            }
        }
        if (generatedCodecTmp == null) {
            generatedCodecTmp = new InstructionCodec(this, dictionary);
        }
        generatedCodec = generatedCodecTmp;
    }

    Pool<byte[]> bufferPool() {
        return bufferPool;
    }

    // PENDING: this method should be package private. For some reason the generated codec cannot access package private stuff...
    public int getMaxBinarySize() {
        return maxBinarySize;
    }

    int getMaxSequenceLength() {
        return maxSequenceLength;
    }

    ProtocolDictionary getDictionary() {
        return dictionary;
    }

    @Override
    public void encode(Object group, OutputStream out) throws IOException {
        encode(group, new OutputStreamSink(out));
    }
    public void encode(Object group, ByteSink out) throws IOException {
        generatedCodec.writeDynamicGroup(out, group);
    }

    @Override
    public Object decode(InputStream in) throws IOException {
        return decode(new InputStreamSource(in));
    }

    public Object decode(ByteSource in) throws IOException {
        try {
            return generatedCodec.readDynamicGroupNull(in);
        } catch(GroupDecodeException|FieldDecodeException e) {
            Throwable t = e;
            StringBuilder str = new StringBuilder();
            for (;;) {
                if (t instanceof GroupDecodeException) {
                    str.append('(').append(((GroupDecodeException)t).getGroupName()).append(')');
                    t = t.getCause();
                } else if(t instanceof FieldDecodeException) {
                    str.append('.').append(((FieldDecodeException)t).getFieldName());
                    t = t.getCause();
                } else {
                    throw new DecodeException("Could not decode field "+str.toString(), t);
                }
            }
        }
    }
}
