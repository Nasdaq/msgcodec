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

import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.StreamCodecInstantiationException;
import com.cinnober.msgcodec.util.ConcurrentBufferPool;
import com.cinnober.msgcodec.util.LimitInputStream;
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

    /** The size preambles. The top of the stack refers to the
     * dynamic group that is currently being encoded.
     * The stack is only non-empty while encoding a message.
     */
    private final Stack<Preamble> preambleStack = new Stack<>();
    /** The internal buffer used for temporary storage of encoded dynamic groups.
     * This is needed in order to know how large an encoded dynamic group is, which
     * is written in the preamble.
     */
    private final TempOutputStream internalBuffer;
    /** Blink output stream wrapped around the {@link #internalBuffer}. */
    private final BlinkOutputStream internalStream;

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
        if (bufferPool != null) {
            this.internalBuffer = new TempOutputStream(bufferPool);
            this.internalStream = new BlinkOutputStream(internalBuffer);
        } else {
            this.internalBuffer = null;
            this.internalStream = null;
        }
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
        try {
             generatedCodec.writeDynamicGroup(out, group);
        } catch (Throwable t) {
            preambleStack.clear();
            internalBuffer.reset();
            throw t;
        }
    }
    @Override
    public Object decode(InputStream in) throws IOException {
        try {
            if (in instanceof LimitInputStream) {
                return generatedCodec.readDynamicGroupNull((LimitInputStream)in);
            } else {
                return generatedCodec.readDynamicGroupNull(new BlinkInputStream(in));
            }
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

    OutputStream preambleBegin() {
        Preamble preamble = new Preamble();
        if (!preambleStack.isEmpty()) {
            preambleStack.peek().startChild(preamble);
        }
        preambleStack.push(preamble);
        return internalStream;
    }

    void preambleEnd(OutputStream out) throws IOException {
        Preamble preamble = preambleStack.pop();
        preamble.end();
        if (preambleStack.isEmpty()) {
            preamble.flush(out);
        }
    }

    private int sizeOfPreamble(int size) {
        return BlinkOutputStream.sizeOfUnsignedVLC(size);
    }

    private class Preamble {
        /** Linked list stuff. */
        private Preamble firstChild;
        /** Linked list stuff. */
        private Preamble lastChild;
        /** Linked list stuff. */
        private Preamble nextSibling;
        /** The position of the first byte in this group. */
        private final int startPosition;
        /** The position of the first byte after this group. */
        private int endPosition;

        /** The size of this group, excluding the preamble of this group.
         * I.e. the size value to be written in the preamble. */
        private int size;
        /** The number of bytes added by the preamble(s) of this group and all child groups. */
        private int addedSize;

        public Preamble() {
            startPosition = internalBuffer.position();
        }
        void startChild(Preamble preamble) {
            if (firstChild == null) {
                firstChild = preamble;
            }
            if (lastChild != null) {
                lastChild.nextSibling = preamble;
            }
            lastChild = preamble;
        }
        void end() {
            endPosition = internalBuffer.position();
        }
        private int getAddedSize() {
            return addedSize;
        }
        private void calculateSize() {
            size = endPosition - startPosition;
            addedSize = 0;
            for (Preamble child = firstChild; child != null; child = child.nextSibling) {
                child.calculateSize();
                addedSize += child.getAddedSize();
            }
            size += addedSize;
            addedSize += sizeOfPreamble(size);
        }
        private void copyTo(OutputStream out) throws IOException {
            // write size preamble
            BlinkOutput.writeUInt32(out, size);
            int position = startPosition;
            for (Preamble child = firstChild; child != null; child = child.nextSibling) {
                internalBuffer.copyTo(out, position, child.startPosition);
                child.copyTo(out);
                position = child.endPosition;
            }
            internalBuffer.copyTo(out, position, endPosition);
        }
        /**
         * Flush the temporary encoded group, add size preambles and finally reset the internal buffer.
         */
        public void flush(OutputStream out) throws IOException {
            calculateSize();
            copyTo(out);
            internalBuffer.reset();
        }
    }
}
