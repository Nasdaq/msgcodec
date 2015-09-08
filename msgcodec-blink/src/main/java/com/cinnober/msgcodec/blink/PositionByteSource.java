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
import com.cinnober.msgcodec.io.ByteSource;
import java.io.IOException;

/**
 * Wrapper of a ByteSource with a position.
 * The position is accessed via the {@link #position()} method. All other method in ByteBuf are unsupported.
 *
 * @author mikael.brannstrom
 */
class PositionByteSource implements ByteBuf {

    private final ByteSource src;
    private int position;

    public PositionByteSource(ByteSource src) {
        this.src = src;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public int read() throws IOException {
        int b = src.read();
        position++;
        return b;
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        src.read(b, off, len);
        position += len;
    }

    @Override
    public int readIntLE() throws IOException {
        int v = src.readIntLE();
        position += 4;
        return v;
    }

    @Override
    public long readLongLE() throws IOException {
        long v = src.readLongLE();
        position += 8;
        return v;
    }

    @Override
    public String readStringUtf8(int len) throws IOException {
        String v = src.readStringUtf8(len);
        position += len;
        return v;
    }

    @Override
    public void skip(int len) throws IOException {
        src.skip(len);
        position += len;
    }

    @Override
    public ByteBuf position(int position) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int limit() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ByteBuf limit(int limit) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int capacity() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public int allocation() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public ByteBuf clear() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ByteBuf flip() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void shift(int position, int length, int distance) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
