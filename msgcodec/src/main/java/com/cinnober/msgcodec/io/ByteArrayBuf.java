/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
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

package com.cinnober.msgcodec.io;

import java.io.IOException;
import java.util.Objects;

/**
 * A ByteBuf backed by a byte array.
 */
// TODO: limit checks
public class ByteArrayBuf implements ByteBuf {

    private final byte[] data;
    private int pos;
    private int limit;

    /**
     * Create a new byte array buffer.
     * @param size the size of the buffer in bytes.
     */
    public ByteArrayBuf(int size) {
        this(new byte[size]);
    }

    /**
     * Create a new byte array buffer.
     * @param data the byte array, not null.
     */
    public ByteArrayBuf(byte[] data) {
        this.data = Objects.requireNonNull(data);
        this.limit = data.length;
    }

    /**
     * Returns the underlying byte array.
     * @return the underlying byte array, not null.
     */
    public byte[] array() {
        return data;
    }

    /**
     * Copy the content of this buffer to the specified byte sink.
     * The data between position and limit are copied.
     * @param out the byte sink to write to, not null.
     * @throws IOException if data cannot be written to the byte sink.
     */
    public void copyTo(ByteSink out) throws IOException {
        out.write(data, pos, limit-pos);
    }

    @Override
    public int capacity() {
        return data.length;
    }

    @Override
    public int position() {
        return pos;
    }
    @Override
    public ByteArrayBuf position(int position) {
        this.pos = position;
        return this;
    }
    @Override
    public int limit() {
        return limit;
    }

    @Override
    public ByteArrayBuf limit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public ByteArrayBuf clear() {
        pos = 0;
        limit = data.length;
        return this;
    }

    @Override
    public ByteArrayBuf flip() {
        limit = pos;
        pos = 0;
        return this;
    }

    @Override
    public int read() throws IOException {
        return 0xff & data[pos++];
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        System.arraycopy(data, pos, b, off, len);
        pos += len;
    }

    @Override
    public String readStringUtf8(int len) throws IOException {
        if (len < 128) {
            boolean ascii = true;
            int end = pos+len;
            for (int i=pos; i<end; i++) {
                if(data[i] >= 0x80) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                char[] chars = new char[len];
                for (int i=0; i<len; i++) {
                    chars[i] = (char) data[pos+i];
                }
                pos += len;
                return new String(chars);
            }
        }


        String s = new String(data, pos, pos+len, UTF8);
        pos += len;
        return s;
    }

    @Override
    public void write(int b) throws IOException {
        if (pos >= limit) {
            throw new IOException("Insufficient space");
        }
        data[pos++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (pos + len > limit) {
            throw new IOException("Insufficient space");
        }
        System.arraycopy(b, off, data, pos, len);
        pos += len;
    }

    @Override
    public void shift(int position, int length, int distance) {
        if (position + distance < 0) {
            throw new IllegalArgumentException("Cannot shift left beyond 0");
        } else if (position + distance + length > limit) {
            throw new IllegalArgumentException("Cannot shift right beyond limit");
        }
        System.arraycopy(data, position, data, position+distance, length);
    }

    @Override
    public void writeIntLE(int v) throws IOException {
        if (pos + 4 > limit) {
            throw new IOException("Insufficient space");
        }
        data[pos] = (byte) v;
        data[pos+1] = (byte) (v >> 8);
        data[pos+2] = (byte) (v >> 16);
        data[pos+3] = (byte) (v >> 24);
        pos += 4;
    }

    @Override
    public void writeLongLE(long v) throws IOException {
        if (pos + 8 > limit) {
            throw new IOException("Insufficient space");
        }
        data[pos] = (byte) v;
        data[pos+1] = (byte) (v >> 8);
        data[pos+2] = (byte) (v >> 16);
        data[pos+3] = (byte) (v >> 24);
        data[pos+4] = (byte) (v >> 32);
        data[pos+5] = (byte) (v >> 40);
        data[pos+6] = (byte) (v >> 48);
        data[pos+7] = (byte) (v >> 56);
        pos += 8;
    }

}
