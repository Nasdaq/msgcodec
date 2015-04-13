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
package com.cinnober.msgcodec.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

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

    /**
     * Create a new temporary output stream.
     * @param bufferPool the buffer pool, not null.
     */
    public TempOutputStream(Pool<byte[]> bufferPool) {
        this.bufferPool = Objects.requireNonNull(bufferPool);
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

    /**
     * Returns an input stream view which can be used to read bytes from this output stream.
     * 
     * @param start the position of the first byte to read
     * @return the input stream, not null.
     * @throws IOException if the start position is beyond the end of this temp output stream.
     */
    public InputStream getInputStream(int start) throws IOException {
        return new InputStreamView(buffers.iterator(), start);
    }

    private static class InputStreamView extends InputStream {
        private static final byte[] EMPTY_BUFFER = new byte[0];
        private final Iterator<byte[]> buffers;
        /** The current (last) buffer. */
        private byte[] currentBuffer;
        /** The next byte to read in currentBuffer. */
        private int currentPosition;

        public InputStreamView(Iterator<byte[]> buffers, int start) throws EOFException {
            this.buffers = buffers;
            this.currentBuffer = buffers.next();
            this.currentPosition = start;
            drainBuffers();
        }
        
        @Override
        public long skip(long n) throws IOException {
            currentPosition += n;
            drainBuffers();
            return n;
        }

        /**
         * Consumes buffers up to the currentPosition.
         * @return true if there are additional bytes remaining.
         * @throws EOFException if the bytes could not be consumed;
         */
        private boolean drainBuffers() throws EOFException {
            while (currentPosition >= currentBuffer.length) {
                currentPosition -= currentBuffer.length;
                if (buffers.hasNext()) {
                    currentBuffer = buffers.next();
                } else {
                    currentBuffer = EMPTY_BUFFER;
                    if (currentPosition > 0) {
                        throw new EOFException();
                    }
                }
            }
            return currentPosition < currentBuffer.length;
        }

        @Override
        public int read() throws IOException {
            if (!drainBuffers()) {
                return -1;
            }

            return 0xff & currentBuffer[currentPosition++];
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (!drainBuffers()) {
                return -1;
            }

            len = Math.min(available(), len);
            System.arraycopy(currentBuffer, currentPosition, b, off, len);
            currentPosition += len;
            return len;
        }

        @Override
        public int available() throws IOException {
            drainBuffers();
            return currentBuffer.length - currentPosition;
        }



    }
}
