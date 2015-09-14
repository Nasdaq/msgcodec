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

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Function;

import com.cinnober.msgcodec.EncodeBufferOverflowException;
import com.cinnober.msgcodec.EncodeBufferUnderflowException;

/**
 * A bytebuf implementation that will reallocate the underlying bytebuffer when required.
 */
public class ReallocatingByteBuf implements ByteBuf {
    private ByteBuffer buffer;
    private final Function<Integer, ByteBuffer> bufferAllocator;
    private final int maximumSize;
    private int size;
    private int limit;
    private final char[] chars = new char[128];

    public ReallocatingByteBuf(int initialSize, int maximumSize, Function<Integer, ByteBuffer> bufferAllocator) {
        int newSize = Math.min(initialSize, maximumSize);
        buffer = bufferAllocator.apply(newSize);
        this.bufferAllocator = bufferAllocator;
        this.maximumSize = maximumSize;
        limit = maximumSize;
        size = newSize;
    }


    @Override
    public ByteBuf clear() {
        buffer.clear();
        limit = maximumSize;
        return this;
    }

    private static int calculateNewCapacity(int current, int requested, int maximum) {
        int n = current<<1;
        while (n<requested && n>0) {
            n = n<<1;
        }
        return n>0 && n>=requested && n<maximum ? n : maximum;
    }

    private void ensureCapacity(int askSize) {
        if (askSize > size) {
            if (askSize > maximumSize) {
                throw new EncodeBufferOverflowException("Required buffer capacity: "+askSize+" bytes exceeds maximum: "+maximumSize+" bytes!");
            }
            if (askSize > limit) {
                throw new EncodeBufferOverflowException("Required buffer capacity: "+askSize+" bytes exceeds limit: "+limit+" bytes!");
            }
            int newSize = Math.max(buffer.capacity()*2, askSize);
            ByteBuffer newBuffer = bufferAllocator.apply(calculateNewCapacity(buffer.capacity(), newSize, maximumSize));
            int position = buffer.position();
            ByteBuffers.copy(buffer, 0, newBuffer, 0, buffer.capacity());
            newBuffer.position(position).limit(Math.min(limit, newBuffer.capacity()));
            buffer = newBuffer;
            size = newSize;
        }
    }

    private void ensureReadCapacity(int askSize) {
        if (askSize > maximumSize) {
            throw new EncodeBufferUnderflowException("Required buffer capacity: "+askSize+" bytes exceeds maximum: "+maximumSize+" bytes!");
        }
        if (askSize > limit) {
            throw new EncodeBufferUnderflowException("Required buffer capacity: "+askSize+" bytes exceeds limit: "+limit+" bytes!");
        }
        throw new RuntimeException("Reading out of bounds: " + askSize);
    }
    
    
    @Override
    public int position() {
        return buffer.position();
    }

    @Override
    public ByteBuf position(int position) {
        if(position > size) {
            ensureCapacity(position);
        }
        buffer.position(position);
        return this;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public ByteBuf limit(int limit) {
        this.limit = limit;
        if(limit < size) {
            size = limit;
        }
        return this;
    }

    @Override
    public int capacity() {
        return maximumSize;
    }

    public int allocation() { return buffer.capacity(); }


    @Override
    public ByteBuf flip() {
        int p = buffer.position();
        limit(p);
        buffer.position(0);
        return this;
    }

    /**
     * Shift the bytes in this buffer.
     * @param position the position in this buffer
     * @param length the number of bytes
     * @param distance the distance to move the data. A positive number shifts right, a negative number shifts left.
     */
    @Override
    public void shift(int position, int length, int distance) {
        ensureCapacity(position+length+distance);
        ByteBuffers.shiftRight(buffer.duplicate(), position, length, distance);
    }

    public int get(int i) {
        return buffer.get(i);
    }

    @Override
    public void write(int b) {
        if(buffer.position() >= size) {
            ensureCapacity(buffer.position() + 1);
        }
        buffer.put((byte)b);
    }

    @Override
    public int read() {
        if (buffer.position() >= size) {
            ensureReadCapacity(buffer.position()+1);
        }
        return buffer.get() & 0xff;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"[pos="+position()+" lim="+limit+" cap="+capacity()+"]";
    }
    
    @Override
    public ByteBuffer getByteBuffer() {
        return buffer;
    }
    
    @Override
    public void skip(int len) throws IOException {
        if (buffer.position() + len > size) {
            ensureCapacity(buffer.position() + len);
        }
        buffer.position(buffer.position()+len);
    }
    
    @Override
    public String readStringUtf8(int len) throws IOException {
        if (position() + len > limit()) {
            throw new BufferUnderflowException();
        }
        if (len < 128) {
            boolean ascii = true;
            for (int i=0; i<len; i++) {
                chars[i] = (char) buffer.get();
                if(chars[i] < 0) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                return new String(chars, 0, len);
            }
            else {
                buffer.position(buffer.position() - len);
            }
        }
        return ByteBuf.super.readStringUtf8(len);
    }

    @Override
    public void writeIntLE(int v) throws IOException {
        if (buffer.position() + 4 > size) {
            ensureCapacity(buffer.position()+4);
        }
        buffer.putInt(buffer.order() == ByteOrder.LITTLE_ENDIAN ? v : Integer.reverseBytes(v));
    }

    @Override
    public void writeLongLE(long v) throws IOException {
        if (buffer.position() + 8 > size) {
            ensureCapacity(position()+8);
        }
        buffer.putLong(buffer.order() == ByteOrder.LITTLE_ENDIAN ? v : Long.reverseBytes(v));
    }

    @Override
    public int readIntLE() throws IOException {
        try {
            int v = buffer.getInt();
            return buffer.order() == ByteOrder.LITTLE_ENDIAN ? v : Integer.reverseBytes(v);
        } catch (BufferUnderflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public long readLongLE() throws IOException {
        try {
            long v = buffer.getLong();
            return buffer.order() == ByteOrder.LITTLE_ENDIAN ? v : Long.reverseBytes(v);
        } catch (BufferUnderflowException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void pad(int n) throws IOException {
        if (buffer.position() + n > size) {
            ensureCapacity(position()+n);
        }
        for (int i=0; i<n; i++) {
            write(0);
        }
    }
    
}
