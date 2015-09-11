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

import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSource;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.io.ByteArrayBuf;
import java.io.IOException;

/**
 * Base class for a dynamically generated compact blink codec for a specific schema.
 * 
 * <p><b>Note: internal use only!</b>
 *
 * <p>
 * A GeneratedCompactCodec sub class represents a schema.
 * A GeneratedCompactCodec instance is tied to a specific {@link BlinkCodec} instance which holds the encode buffers
 * and any max binary size settings.
 * 
 * @author mikael brannstrom
 */


/* Note: This class should be package private, but cannot since the dynamically generated classes are loaded
   from another class loader, i.e. don't share the package with this class (regardless of package name). */
public abstract class GeneratedCompactCodec extends GeneratedCodec {

    /** Reference to the blink codec. */
    protected final BlinkCodec codec;

    /**
     * Constructor.
     * The constructor of the subclass should have the signature <code>(BlinkCodec, Schema)</code>.
     * 
     * @param codec the blink codec, not null.
     */
    public GeneratedCompactCodec(BlinkCodec codec) {
        super(codec.getMaxBinarySize());
        this.codec = codec;
    }
    
    @Override
    public void writeDynamicGroup(ByteSink out, Object group) throws IOException, IllegalArgumentException {
        if (out instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) out;
            int start = buf.position();
            buf.skip(2); // size
            writeStaticGroupWithId(buf, group);
            int end = buf.position();
            int size = end - start - 2;
            if (size < 1<<7) {
                buf.shift(start+2, size, -1);
                buf.position(start);
                buf.limit(buf.capacity());
                BlinkOutput.writeVLC7(buf, size);
                buf.position(end-1);
            } else if (size < 1<<14) {
                buf.position(start);
                BlinkOutput.writeVLC14(buf, size);
                buf.position(end);
            } else {
                int sizeOfSize = BlinkOutput.sizeOfUnsignedVLC(size);
                buf.shift(start+2, size, sizeOfSize-2);
                buf.position(start);
                buf.limit(buf.capacity());
                BlinkOutput.writeVLC(buf, size, sizeOfSize);
                buf.position(end + sizeOfSize - 2);
            }
        } else {
            byte[] tmpBuf = codec.bufferPool().get();
            try {
                ByteArrayBuf tmpOut = new ByteArrayBuf(tmpBuf);
                writeDynamicGroup(tmpOut, group);
                tmpOut.flip();
                tmpOut.copyTo(out);
            } finally {
                codec.bufferPool().release(tmpBuf);
            }
        }
    }

    @Override
    public void writeDynamicGroupNull(ByteSink out, Object group) throws IOException {
        if (group == null) {
            BlinkOutput.writeNull(out);
        } else {
            writeDynamicGroup(out, group);
        }
    }
    
    @Override
    public Object readDynamicGroup(ByteSource in) throws IOException {
        int size = BlinkInput.readUInt32(in);
        return readDynamicGroup(size, in);
    }

    @Override
    public Object readDynamicGroupNull(ByteSource in) throws IOException {
        Integer sizeObj = BlinkInput.readUInt32Null(in);
        if (sizeObj == null) {
            return null;
        }
        int size = sizeObj.intValue();
        return readDynamicGroup(size, in);
    }

    private Object readDynamicGroup(int size, ByteSource in) throws IOException {
        ByteBuf inbuf;
        if (in instanceof ByteBuf) {
            inbuf = (ByteBuf) in;
        } else {
            inbuf = new PositionByteSource(in);
        }
        int expectedEndPos = inbuf.position() + size;
        int groupId = BlinkInput.readUInt32(inbuf);
        try {
            Object group = readStaticGroup(groupId, inbuf);
            
            int skip = expectedEndPos - inbuf.position();
            if (skip < 0) {
                throw new DecodeException("Malformed dynamic group. Read " + (-skip) + " bytes beyond group size.");
            } else if (skip > 0) {
                in.skip(skip);
            }
            return group;
        } catch (Exception e) {
            GroupDef groupDef = codec.getSchema().getGroup(groupId);
            if (groupDef != null) {
                throw new GroupDecodeException(groupDef.getName(), e);
            } else {
                throw e;
            }
        }
    }
}
