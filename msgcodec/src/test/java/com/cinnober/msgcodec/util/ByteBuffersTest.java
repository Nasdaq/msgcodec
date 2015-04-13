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

import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikael.brannstrom
 */
public class ByteBuffersTest {

    public ByteBuffersTest() {
    }

    @Test
    public void testCopyAll() {
        ByteBuffer srcBuf = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
        ByteBuffer dstBuf = ByteBuffer.wrap(new byte[] { 5, 6, 7, 8, 9 });

        ByteBuffers.copy(srcBuf, 0, dstBuf, 0, 5);
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 4 }, dstBuf.array());
    }

    @Test
    public void testCopyNone() {
        ByteBuffer srcBuf = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
        ByteBuffer dstBuf = ByteBuffer.wrap(new byte[] { 5, 6, 7, 8, 9 });

        ByteBuffers.copy(srcBuf, 0, dstBuf, 0, 0);
        assertArrayEquals(new byte[] { 5, 6, 7, 8, 9 }, dstBuf.array());
    }

    @Test
    public void testCopyOffsetLength() {
        ByteBuffer srcBuf = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
        ByteBuffer dstBuf = ByteBuffer.wrap(new byte[] { 5, 6, 7, 8, 9 });

        ByteBuffers.copy(srcBuf, 1, dstBuf, 2, 2);
        assertArrayEquals(new byte[]{5, 6, 1, 2, 9}, dstBuf.array());
    }

    @Test
    public void testCopySameLeft() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });

        ByteBuffers.copy(buf, 2, buf, 0, 3);
        assertArrayEquals(new byte[]{2, 3, 4, 3, 4}, buf.array());
    }

    @Test
    public void testCopySameRight() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });

        ByteBuffers.copy(buf, 0, buf, 2, 3);
        assertArrayEquals(new byte[]{0, 1, 0, 1, 2}, buf.array());
    }

    @Test
    public void testToHex1() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        assertEquals("00010203040506070809", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 0, 0, 0));
        assertEquals("00010203040506070809", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 0, 10, 0));
        assertEquals("00010203040506070809", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 0, 10, 10));
        assertEquals("00010203040506070809", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 0, 11, 11));

        assertEquals("00 01 02 03 04 05 06 07 08 09", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 1, 0, 0));
        assertEquals("00 01 02 03 04 05 06 07 08 09", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 1, 10, 10));
        assertEquals("00 01 02 03 04 05 06 07 08 09", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 1, 10, 10));

        assertEquals("00 01 02 03 04 05 06 07  08 09", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 1, 8, 0));
        assertEquals("00 01 02 03 04 05 06 07  08 09", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 1, 8, 10));
        
        assertEquals("0001 0203  0405 0607\n0809", ByteBuffers.toHex(buf, buf.position(), buf.limit(), 2, 4, 8));
    }
}
