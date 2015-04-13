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

import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSource;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgCodecInstantiationException;
import com.cinnober.msgcodec.util.ConcurrentBufferPool;
import com.cinnober.msgcodec.util.InputStreamSource;
import com.cinnober.msgcodec.util.OutputStreamSink;
import com.cinnober.msgcodec.util.Pool;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Objects;
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
public class BlinkCodec implements MsgCodec {
    private static final Logger log = Logger.getLogger(BlinkCodec.class.getName());

    private final GeneratedCodec generatedCodec;
    private final Schema schema;

    private final Pool<byte[]> bufferPool;

    private final int maxBinarySize;
    private final int maxSequenceLength;

    /**
     * Create a Blink codec, with an internal buffer pool of 8192 bytes.
     *
     * @param schema the definition of the messages to be understood by the codec.
     */
    BlinkCodec(Schema schema) throws MsgCodecInstantiationException {
        this(schema, new ConcurrentBufferPool(8192, 1));
    }
    /**
     * Create a Blink codec.
     *
     * @param schema the definition of the messages to be understood by the codec.
     * @param bufferPool the buffer pool, needed for temporary storage while <em>encoding</em>.
     */
    public BlinkCodec(Schema schema, Pool<byte[]> bufferPool) throws MsgCodecInstantiationException {
        this(schema, bufferPool, 10 * 1048576, 1_000_000, CodecOption.AUTOMATIC);
    }
    /**
     * Create a Blink codec.
     *
     * @param schema the definition of the messages to be understood by the codec.
     * @param bufferPool the buffer pool, needed for temporary storage while <em>encoding</em>.
     * @param maxBinarySize the maximum binary size (including strings) allowed while decoding, or -1 for no limit.
     * @param maxSequenceLength the maximum sequence length allowed while decoding, or -1 for no limit.
     * @param codecOption controls which kind of underlying codec to use, not null.
     */
    BlinkCodec(Schema schema, Pool<byte[]> bufferPool,
            int maxBinarySize, int maxSequenceLength, CodecOption codecOption) throws MsgCodecInstantiationException {
        if (!schema.isBound()) {
            throw new IllegalArgumentException("Schema not bound");
        }
        this.bufferPool = bufferPool;
        Objects.requireNonNull(codecOption);

        this.maxBinarySize = maxBinarySize;
        this.maxSequenceLength = maxSequenceLength;
        this.schema = schema;

        GeneratedCodec generatedCodecTmp = null;
        if (codecOption != CodecOption.INSTRUCTION_CODEC_ONLY) {
            try {
                Class<GeneratedCodec> generatedCodecClass =
                        GeneratedCodecClassLoader.getInstance().getGeneratedCodecClass(schema);
                Constructor<GeneratedCodec> constructor =
                        generatedCodecClass.getConstructor(new Class<?>[]{ BlinkCodec.class, Schema.class });
                generatedCodecTmp = constructor.newInstance(this, schema);
            } catch (Exception e) {
                log.log(Level.WARNING,
                        "Could instantiate generated codec for schema UID " + schema.getUID(), e);
                if (codecOption == CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY) {
                    throw new MsgCodecInstantiationException(e);
                }
                log.log(Level.INFO, "Fallback to (slower) instruction based codec for schema UID {0}",
                        schema.getUID());
            }
        }
        if (generatedCodecTmp == null) {
            generatedCodecTmp = new InstructionCodec(this, schema);
        }
        generatedCodec = generatedCodecTmp;
    }

    Pool<byte[]> bufferPool() {
        return bufferPool;
    }

    int getMaxBinarySize() {
        return maxBinarySize;
    }

    int getMaxSequenceLength() {
        return maxSequenceLength;
    }

    Schema getSchema() {
        return schema;
    }

    @Override
    public void encode(Object group, OutputStream out) throws IOException {
        encode(group, new OutputStreamSink(out));
    }
    @Override
    public void encode(Object group, ByteSink out) throws IOException {
        generatedCodec.writeDynamicGroup(out, group);
    }

    @Override
    public Object decode(InputStream in) throws IOException {
        return decode(new InputStreamSource(in));
    }

    @Override
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
