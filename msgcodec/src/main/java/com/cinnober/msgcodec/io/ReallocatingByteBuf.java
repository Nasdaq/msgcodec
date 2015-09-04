package com.cinnober.msgcodec.io;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.Function;

import com.cinnober.msgcodec.EncodeBufferOverflowException;

/**
 * A bytebuf implementation that will reallocate the underlying bytebuffer when required.
 */
public class ReallocatingByteBuf implements ByteBuf {
    ByteBuffer buffer;
    final Function<Integer, ByteBuffer> bufferAllocator;
    final int maximumSize;
    int currentSize;
    int limit;

    public ReallocatingByteBuf(int initialSize, int maximumSize, Function<Integer, ByteBuffer> bufferAllocator) {
        buffer = bufferAllocator.apply(Math.min(initialSize, maximumSize));
        this.bufferAllocator = bufferAllocator;
        this.maximumSize = maximumSize;
        limit = maximumSize;
    }


    @Override
    public ByteBuf clear() {
        buffer.clear();
        limit = maximumSize;
        return this;
    }

    public void copyTo(int srcIndex, ReallocatingByteBuf byteBuf, int dstIndex, int length) {
        ByteBuffers.copy(buffer.duplicate(), srcIndex, byteBuf.buffer.duplicate(), dstIndex, length);
    }

    public void copyTo(int srcIndex, ByteBuffer dst, int dstIndex, int length) {
        ByteBuffers.copy(buffer.duplicate(), srcIndex, dst.duplicate(), dstIndex, length);
    }

    public void copyTo(int srcIndex, byte[] dst, int dstIndex, int length) {
        ByteBuffers.copy(buffer.duplicate(), srcIndex, dst, dstIndex, length);
    }

    private static int calculateNewCapacity(int current, int requested, int maximum) {
        int n = current<<1;
        while (n<requested && n>0) {
            n = n<<1;
        }
        return n>0 && n>=requested && n<maximum ? n : maximum;
    }

    private void ensureCapacity(int size) {
        if (buffer.capacity() < size) {
            if (size>maximumSize) {
                throw new EncodeBufferOverflowException("Required buffer capacity "+size+" bytes exceeds maximum allowed - "+maximumSize+" bytes!");
            }
            int newSize = Math.max(buffer.capacity()*2, size);
            ByteBuffer newBuffer = bufferAllocator.apply(calculateNewCapacity(buffer.capacity(), newSize, maximumSize));
            int position = buffer.position();
            ByteBuffers.copy(buffer, 0, newBuffer, 0, buffer.capacity());
            newBuffer.position(position).limit(Math.min(limit, newBuffer.capacity()));
            buffer = newBuffer;
        }
    }

    @Override
    public int position() {
        return buffer.position();
    }

    @Override
    public ByteBuf position(int position) {
        ensureCapacity(position);
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
        buffer.limit(p);
        limit = p;
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
        final int i = buffer.position()+1;
        if (i>=limit) {
            if (i >= maximumSize) {
                throw new EncodeBufferOverflowException("Entity encoding buffer is too small ("+maximumSize+" bytes)!");
            } else {
                throw new EncodeBufferOverflowException("Buffer limit (not capacity) reached - cannot write byte "+i+"!");
            }

        }
        ensureCapacity(i);
        buffer.put((byte)b);
    }

    @Override
    public int read() {
        final int i = buffer.position()+1;
        ensureCapacity(i);
        if (i>limit) {
            throw new BufferUnderflowException();
        }
        return buffer.get();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"[pos="+position()+" lim="+limit+" cap="+capacity()+"]";
    }
}
