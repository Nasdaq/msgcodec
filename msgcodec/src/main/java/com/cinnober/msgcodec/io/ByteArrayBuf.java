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
    public void skip(int len) throws IOException {
        pos += len;
    }

    @Override
    public String readStringUtf8(int len) throws IOException {
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
                pos += len;
                return new String(chars);
            }
        }


        String s = new String(data, pos, len, UTF8);
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
