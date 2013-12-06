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
package com.cinnober.msgcodec.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.cinnober.msgcodec.util.Pool;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Reusable output stream where temporary encoded data can be stored.
 * The data is stored in pooled byte arrays.
 *
 * @author mikael.brannstrom
 *
 */
public class TempOutputStream extends OutputStream {

    private final Pool<byte[]> bufferPool;

    /** All buffers currently in use. */
    private final ArrayList<byte[]> buffers = new ArrayList<>();
    /** The current (last) buffer. */
    private byte[] currentBuffer;
    /** The next byte to write in currentBuffer. */
    private int currentPosition;
    /** The number of bytes before currentBuffer. */
    private int currentOffset;

    public TempOutputStream(Pool<byte[]> bufferPool) {
        this.bufferPool = bufferPool;
    }

    /** 
     * Returns the number of bytes written to this buffer.
     * @return the number of bytes written to this buffer.
     */
    public int position() {
        return currentPosition + currentOffset;
    }

    /**
     * Discard any data after the specified position.
     * @param position the new position after this operation.
     */
    public void truncate(int position) {
        if (position > position()) {
            throw new IllegalArgumentException("Position must be <= current position");
        }
        if (position < 0) {
            throw new IllegalArgumentException("Position must be >= 0");
        }
        while (position <= currentOffset) {
            byte[] buf = buffers.remove(buffers.size()-1);
            currentOffset -= buf.length;
            bufferPool.release(buf);
        }
        currentBuffer = buffers.isEmpty() ? null : buffers.get(buffers.size()-1);
        currentPosition = position - currentOffset;
    }

    /**
     * Reset this stream.
     * The number of bytes written is restored to zero, any internal buffers are released.
     */
    public void reset() {
        for (byte[] buf : buffers) {
            bufferPool.release(buf);
        }
        buffers.clear();
        currentBuffer = null;
        currentPosition = 0;
        currentOffset = 0;
    }

    /**
     * Close this stream and release any buffers to the pool.
     */
    @Override
    public void close() {
        reset();
    }

    /**
     * Allocate a buffer
     */
    protected void allocate() {
        currentOffset += currentPosition;
        currentPosition = 0;
        currentBuffer = bufferPool.get();
        buffers.add(currentBuffer);
    }

    @Override
    public void write(int b) {
        if (currentBuffer == null || currentPosition == currentBuffer.length) {
            allocate();
        }
        currentBuffer[currentPosition++] = (byte)b;
    }

    @Override
    public void write(byte[] buf) {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int offset, int length) {
        if (currentBuffer == null) {
            allocate();
        }
        while (length > 0) {
            int available = currentBuffer.length - currentPosition;
            if (length > available) {
                System.arraycopy(buf, offset, currentBuffer, currentPosition, available);
                currentPosition += available;
                offset += available;
                length -= available;
                allocate();
            } else {
                System.arraycopy(buf, offset, currentBuffer, currentPosition, length);
                currentPosition += length;
                break;
            }
        }
    }

    /**
     * Copy bytes from this buffer to the specified output stream.
     *
     * @param out the stream to write to.
     * @param start the position of the first byte to write
     * @param end the position after the last byte to write
     * @throws IOException the output stream throws an exception.
     */
    public void copyTo(OutputStream out, int start, int end) throws IOException {
        if (start == end) {
            return;
        }
        if (start > end) {
            throw new IllegalArgumentException("start must be <= end");
        }
        if (end > position()) {
            throw new IllegalArgumentException("end must be <= position()");
        }
        int bufferStart = 0;
        int bufferEnd = 0;
        for (byte[] buffer : buffers) {
            bufferEnd += buffer.length;
            if (start < bufferEnd) {
                if (end > bufferEnd) {
                    out.write(buffer, start - bufferStart, bufferEnd - start);
                    start += bufferEnd - start;
                } else {
                    out.write(buffer, start - bufferStart, end - start);
                    return;
                }
            }
            bufferStart = bufferEnd;
        }
        throw new Error("Internal error. start=" + start + ", end=" + end +
                ", bufferStart=" + bufferStart + ", bufferEnd=" + bufferEnd);
    }


    /**
     * Copy bytes from this buffer to the specified byte buffer.
     *
     * @param out the byte buffer to write to.
     * @param start the position of the first byte to write
     * @param end the position after the last byte to write
     * @throws BufferOverflowException the byte buffer gets overfull.
     */
    public void copyTo(ByteBuffer out, int start, int end) throws BufferOverflowException {
        if (start == end) {
            return;
        }
        if (start > end) {
            throw new IllegalArgumentException("start must be <= end");
        }
        if (end > position()) {
            throw new IllegalArgumentException("end must be <= position()");
        }
        int bufferStart = 0;
        int bufferEnd = 0;
        for (byte[] buffer : buffers) {
            bufferEnd += buffer.length;
            if (start < bufferEnd) {
                if (end > bufferEnd) {
                    out.put(buffer, start - bufferStart, bufferEnd - start);
                    start += bufferEnd - start;
                } else {
                    out.put(buffer, start - bufferStart, end - start);
                    return;
                }
            }
            bufferStart = bufferEnd;
        }
        throw new Error("Internal error. start=" + start + ", end=" + end +
                ", bufferStart=" + bufferStart + ", bufferEnd=" + bufferEnd);
    }

    /**
     * Copy bytes from this buffer to the specified byte array.
     *
     * @param out the byte array to write to.
     * @param start the position of the first byte to write
     * @param end the position after the last byte to write
     */
    public void copyTo(byte[] out, int start, int end) {
        copyTo(out, start, end, 0);
    }
    /**
     * Copy bytes from this buffer to the specified byte array.
     *
     * @param out the byte array to write to.
     * @param start the position of the first byte to write
     * @param end the position after the last byte to write
     * @param offset offset in the array
     */
    public void copyTo(byte[] out, int start, int end, int offset) {
        if (start == end) {
            return;
        }
        if (start > end) {
            throw new IllegalArgumentException("start must be <= end");
        }
        if (end > position()) {
            throw new IllegalArgumentException("end must be <= position()");
        }
        int bufferStart = 0;
        int bufferEnd = 0;
        for (byte[] buffer : buffers) {
            bufferEnd += buffer.length;
            if (start < bufferEnd) {
                if (end > bufferEnd) {
                    final int srcOffset = start - bufferStart;
                    final int length = bufferEnd - start;
                    System.arraycopy(buffer, srcOffset, out, offset, length);
                    offset += length;
                    start += bufferEnd - start;
                } else {
                    final int srcOffset = start - bufferStart;
                    final int length = end - start;
                    System.arraycopy(buffer, srcOffset, out, offset, length);
                    return;
                }
            }
            bufferStart = bufferEnd;
        }
        throw new Error("Internal error. start=" + start + ", end=" + end +
                ", bufferStart=" + bufferStart + ", bufferEnd=" + bufferEnd);
    }
}
