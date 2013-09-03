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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.cinnober.msgcodec.util.Pool;

/**
 * Internal byte buffer where temporary dynamic groups are encoded.
 *
 * @author mikael.brannstrom
 *
 */
class InternalBlinkBuffer extends OutputStream {

    private final Pool<byte[]> bufferPool;

    /** All buffers currently in use. */
    private List<byte[]> buffers = new ArrayList<byte[]>();
    /** The current (last) buffer. */
    private byte[] currentBuffer;
    /** The next byte to write in currentBuffer. */
    private int currentPosition;
    /** The number of bytes before currentBuffer. */
    private int currentOffset;

    public InternalBlinkBuffer(Pool<byte[]> bufferPool) {
        this.bufferPool = bufferPool;
    }

    /** Returns the number of bytes written to this buffer. */
    public int position() {
        return currentPosition + currentOffset;
    }

    /** Resets this buffer.
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

    /** Allocate a buffer
     */
    protected void allocate() throws IOException {
        currentOffset += currentPosition;
        currentPosition = 0;
        currentBuffer = bufferPool.get();
        buffers.add(currentBuffer);
    }

    @Override
    public void write(int b) throws IOException {
        if (currentBuffer == null || currentPosition == currentBuffer.length) {
            allocate();
        }
        currentBuffer[currentPosition++] = (byte)b;
    }

    @Override
    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int offset, int length) throws IOException {
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

    /** Copy bytes from this buffer to the specified output stream.
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
        }
        throw new IOException("Internal error. start=" + start + ", end=" + end +
                ", bufferStart=" + bufferStart + ", bufferEnd=" + bufferEnd);
    }

}
