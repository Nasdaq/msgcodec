package com.cinnober.msgcodec.io;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.cinnober.msgcodec.EncodeBufferOverflowException;
import com.cinnober.msgcodec.EncodeBufferUnderflowException;

/**
 * A bytebuf implementation that will reallocate the underlying bytebuffer when required.
 */
public class ReallocatingArray implements ByteBuf {
    private byte[] buffer;
    private final int maximumSize;
    private int size; // minimum of limit and current allocated size
    private int position;
    private int limit;

    public ReallocatingArray(int initialSize, int maximumSize) {
        buffer = new byte[Math.min(initialSize, maximumSize)];
        size = buffer.length;
        this.maximumSize = maximumSize;
        limit = maximumSize;
    }


    @Override
    public ByteBuf clear() {
        limit = maximumSize;
        position = 0;
        return this;
    }

    private static int calculateNewCapacity(int current, int requested, int maximum) {
        int n = current<<1;
        while (n<requested && n>0) {
            n = n<<1;
        }
        return n>0 && n>=requested && n<maximum ? n : maximum;
    }

    private void ensureCapacity(int askedSize) {
        if (size < askedSize) {
            if (askedSize >= limit) {
                throw new EncodeBufferOverflowException("Required buffer capacity: "+askedSize+" bytes exceeds limit: "+limit+" bytes!");
            }
            if (askedSize>maximumSize) {
                throw new EncodeBufferOverflowException("Required buffer capacity: "+askedSize+" bytes exceeds maximum allowed: "+maximumSize+" bytes!");
            }
            int newSize = Math.max(buffer.length*2, askedSize);
            byte[] buffer2 = new byte[calculateNewCapacity(buffer.length, newSize, maximumSize)];
            System.arraycopy(buffer, 0 , buffer2, 0, buffer.length);
            size = newSize;
            buffer = buffer2;
        }
    }

    private void ensureReadCapacity(int askSize) {
        if (askSize>maximumSize) {
            throw new EncodeBufferUnderflowException("Required buffer capacity: "+askSize+" bytes exceeds maximum: "+maximumSize+" bytes!");
        }
        if (askSize >= limit) {
            throw new EncodeBufferUnderflowException("Required buffer capacity: "+askSize+" bytes exceeds limit: "+limit+" bytes!");
        }
        throw new RuntimeException("Reading out of bounds: " + askSize);
    }
    
    @Override
    public int position() {
        return position;
    }

    @Override
    public ByteBuf position(int position) {
        this.position = position;
        ensureCapacity(position);
        return this;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public ByteBuf limit(int limit) {
        this.limit = limit;
        if (limit < size) {
            size = limit;
        }
        return this;
    }

    @Override
    public int capacity() {
        return maximumSize;
    }

    public int allocation() { return buffer.length; }


    @Override
    public ByteBuf flip() {
        limit = position;
        position = 0;
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
        if (distance > 0) {
            ensureCapacity(position+length+distance);
            for (int i = position + length-1; i >= position; i++) {
                buffer[i + distance] = buffer[i];
            }
        }
        else if (distance < 0) {
            for (int i = position; i < length; i++) {
                buffer[i + distance] = buffer[i];
            }
        }
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        if (position+len > limit) {
            throw new IOException("Buffer underflow");
        }
        System.arraycopy(buffer, position, b, off, len);
        position += len;
    }

    @Override
    public void skip(int len) throws IOException {
        if (position+len > size) {
            ensureCapacity(position+len);
        }
        position += len;
    }

    @Override
    public String readStringUtf8(int len) throws IOException {
        if (len < 128) {
            boolean ascii = true;
            int end = position+len;
            for (int i=position; i<end; i++) {
                if(buffer[i] < 0) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                char[] chars = new char[len];
                for (int i=0; i<len; i++) {
                    chars[i] = (char) buffer[position+i];
                }
                position += len;
                return new String(chars);
            }
        }


        String s = new String(buffer, position, len, UTF8);
        position += len;
        if (position > limit) {
            throw new IOException("Buffer underflow");
        }
        
        return s;
    }
    
    public int get(int i) {
        return buffer[i];
    }

    @Override
    public void write(int b) {
        if(position>=size) {
            ensureCapacity(position+1);
        }
        buffer[position++] = (byte) b;
    }

    @Override
    public int read() {
        if (position >= size) {
            ensureReadCapacity(position+1);
        }
        return buffer[position++];
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"[pos="+position()+" lim="+limit+" cap="+capacity()+"]";
    }
    
    @Override
    public void writeIntLE(int v) throws IOException {
        if (position + 4 > size) {
            ensureCapacity(position+4);
        }
        buffer[position] = (byte) v;
        buffer[position+1] = (byte) (v >> 8);
        buffer[position+2] = (byte) (v >> 16);
        buffer[position+3] = (byte) (v >> 24);
        position += 4;
    }

    @Override
    public void writeLongLE(long v) throws IOException {
        if (position + 8 > size) {
            ensureCapacity(position+8);
        }
        buffer[position] = (byte) v;
        buffer[position+1] = (byte) (v >> 8);
        buffer[position+2] = (byte) (v >> 16);
        buffer[position+3] = (byte) (v >> 24);
        buffer[position+4] = (byte) (v >> 32);
        buffer[position+5] = (byte) (v >> 40);
        buffer[position+6] = (byte) (v >> 48);
        buffer[position+7] = (byte) (v >> 56);
        position += 8;
    }
    
    
    public ByteBuffer getBuffer() {
        return null;
    }
}
