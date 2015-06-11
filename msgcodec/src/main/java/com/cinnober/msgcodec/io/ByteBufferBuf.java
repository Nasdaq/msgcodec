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

package com.cinnober.msgcodec.io;

import static com.cinnober.msgcodec.io.ByteSource.UTF8;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A ByteBuf backed by a ByteBuffer.
 * Changes to the underlying byte buffer are reflected in the byte buf, and vice versa, including position and limit.
 * 
 * @author mikael.brannstrom
 */
public class ByteBufferBuf implements ByteBuf {

    private final ByteBuffer buf;

    /**
     * Create a new byte buffer buf.
     * @param buf the wrapped byte buffer, not null.
     */
    public ByteBufferBuf(ByteBuffer buf) {
        this.buf = buf;
    }

    /**
     * Returns the underlying byte buffer.
     * @return the underlying byte buffer, not null.
     */
    public ByteBuffer buffer() {
        return buf;
    }

    @Override
    public int position() {
        return buf.position();
    }

    @Override
    public ByteBuf position(int position) {
        buf.position(position);
        return this;
    }

    @Override
    public int limit() {
        return buf.limit();
    }

    @Override
    public int capacity() {
        return buf.capacity();
    }

    @Override
    public ByteBuf limit(int limit) {
        buf.limit(limit);
        return this;
    }

    @Override
    public ByteBuf clear() {
        buf.clear();
        return this;
    }

    @Override
    public ByteBuf flip() {
        buf.flip();
        return this;
    }

    @Override
    public void shift(int position, int length, int distance) {
        ByteBuffers.copy(buf, position, buf, position+distance, length);
    }

    @Override
    public int read() throws IOException {
        try {
            return 0xff & buf.get();
        } catch (BufferUnderflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        try {
            buf.get(b, off, len);
        } catch (BufferUnderflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        try {
            buf.put((byte) b);
        } catch (BufferOverflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            buf.put(b, off, len);
        } catch (BufferOverflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void skip(int len) throws IOException {
        if (position() + len > limit()) {
            throw new IOException("Buffer underflow");
        }
        buf.position(buf.position()+len);
    }
    
    @Override
    public String readStringUtf8(int len) throws IOException {
        if (position() + len > limit()) {
            throw new IOException("Buffer underflow");
        }
        if (buf.hasArray()) {
            final byte[] data = buf.array();
            final int pos = buf.arrayOffset();
            if (len < 128) {
                boolean ascii = true;
                int end = pos+len;
                for (int i=pos; i<end; i++) {
                    if(data[i] < 0) {
                        ascii = false;
                        break;
                    }
                }
                if (ascii) {
                    char[] chars = new char[len];
                    for (int i=0; i<len; i++) {
                        chars[i] = (char) data[pos+i];
                    }
                    buf.position(buf.position()+len);
                    return new String(chars);
                }
            }
            String s = new String(data, pos, len, UTF8);
            buf.position(buf.position()+len);
            return s;
        } else {
            if (len < 128) {
                boolean ascii = true;
                final int pos = buf.position();
                int end = pos+len;
                for (int i=pos; i<end; i++) {
                    if(buf.get(i) < 0) {
                        ascii = false;
                        break;
                    }
                }
                if (ascii) {
                    char[] chars = new char[len];
                    for (int i=0; i<len; i++) {
                        chars[i] = (char) buf.get();
                    }
                    return new String(chars);
                }
                
            }
            return ByteBuf.super.readStringUtf8(len);
        }
    }

    @Override
    public void writeIntLE(int v) throws IOException {
        try {
            buf.putInt(buf.order() == ByteOrder.LITTLE_ENDIAN ? v : Integer.reverseBytes(v));
        } catch (BufferOverflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeLongLE(long v) throws IOException {
        try {
            buf.putLong(buf.order() == ByteOrder.LITTLE_ENDIAN ? v : Long.reverseBytes(v));
        } catch (BufferOverflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int readIntLE() throws IOException {
        try {
            int v = buf.getInt();
            return buf.order() == ByteOrder.LITTLE_ENDIAN ? v : Integer.reverseBytes(v);
        } catch (BufferUnderflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public long readLongLE() throws IOException {
        try {
            long v = buf.getLong();
            return buf.order() == ByteOrder.LITTLE_ENDIAN ? v : Long.reverseBytes(v);
        } catch (BufferUnderflowException e) {
            throw new IOException(e);
        }
    }

}
