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

package com.cinnober.msgcodec.util;

import com.cinnober.msgcodec.ByteBuf;
import static com.cinnober.msgcodec.ByteSource.UTF8;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO: javadoc
 * TODO: limit checks
 * @author mikael.brannstrom
 */
public class ByteBufferBuf implements ByteBuf {

    private final ByteBuffer buf;

    public ByteBufferBuf(ByteBuffer buf) {
        this.buf = buf;
    }

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
        return 0xff & buf.get();
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        buf.get(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        buf.put((byte) b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buf.put(b, off, len);
    }

    @Override
    public String readStringUtf8(int len) throws IOException {
        if (buf.hasArray()) {
            final byte[] data = buf.array();
            final int pos = buf.arrayOffset();
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
                    buf.position(buf.position()+len);
                    return new String(chars);
                }
            }
            String s = new String(data, pos, pos+len, UTF8);
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
}
