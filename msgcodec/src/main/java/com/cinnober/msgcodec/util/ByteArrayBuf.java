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
import java.io.IOException;

/**
 *
 * @author mikael.brannstrom
 */
public class ByteArrayBuf implements ByteBuf {

    private final byte[] data;
    private int pos;
    private int limit;

    public ByteArrayBuf(byte[] data) {
        this.data = data;
    }

    public byte[] array() {
        return data;
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
        limit = 0;
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
        return data[pos++];
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        System.arraycopy(data, pos, b, off, len);
        pos += len;
    }

    @Override
    public String readStringUtf8(int len) throws IOException {
        String s = new String(data, pos, pos+len, UTF8);
        pos += len;
        return s;
    }

    @Override
    public void write(int b) throws IOException {
        data[pos++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        System.arraycopy(b, off, data, pos, len);
        pos += len;
    }

    @Override
    public void write2(int b1, int b2) throws IOException {
        data[pos] = (byte) b1;
        data[pos+1] = (byte) b2;
        pos+=2;
    }

    @Override
    public void write3(int b1, int b2, int b3) throws IOException {
        data[pos] = (byte) b1;
        data[pos+1] = (byte) b2;
        data[pos+2] = (byte) b3;
        pos+=3;
    }

    @Override
    public void write4(int b1, int b2, int b3, int b4) throws IOException {
        data[pos] = (byte) b1;
        data[pos+1] = (byte) b2;
        data[pos+2] = (byte) b3;
        data[pos+3] = (byte) b4;
        pos+=4;
    }

    @Override
    public void write5(int b1, int b2, int b3, int b4, int b5) throws IOException {
        data[pos] = (byte) b1;
        data[pos+1] = (byte) b2;
        data[pos+2] = (byte) b3;
        data[pos+3] = (byte) b4;
        data[pos+4] = (byte) b5;
        pos+=5;
    }

    @Override
    public void write6(int b1, int b2, int b3, int b4, int b5, int b6) throws IOException {
        data[pos] = (byte) b1;
        data[pos+1] = (byte) b2;
        data[pos+2] = (byte) b3;
        data[pos+3] = (byte) b4;
        data[pos+4] = (byte) b5;
        data[pos+5] = (byte) b6;
        pos+=6;
    }

    @Override
    public void write7(int b1, int b2, int b3, int b4, int b5, int b6, int b7) throws IOException {
        data[pos] = (byte) b1;
        data[pos+1] = (byte) b2;
        data[pos+2] = (byte) b3;
        data[pos+3] = (byte) b4;
        data[pos+4] = (byte) b5;
        data[pos+5] = (byte) b6;
        data[pos+6] = (byte) b7;
        pos+=7;
    }

    @Override
    public void write8(int b1, int b2, int b3, int b4, int b5, int b6, int b7, int b8) throws IOException {
        data[pos] = (byte) b1;
        data[pos+1] = (byte) b2;
        data[pos+2] = (byte) b3;
        data[pos+3] = (byte) b4;
        data[pos+4] = (byte) b5;
        data[pos+5] = (byte) b6;
        data[pos+6] = (byte) b7;
        data[pos+7] = (byte) b8;
        pos+=8;
    }


}
