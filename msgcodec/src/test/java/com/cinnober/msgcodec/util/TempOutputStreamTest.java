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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikael.brannstrom
 */
public class TempOutputStreamTest {

    public TempOutputStreamTest() {
    }

    @Test
    public void testCopyToStream() throws Exception {
        Pool<byte[]> pool = new ConcurrentBufferPool(10, 10);
        TempOutputStream tmpOut = new TempOutputStream(pool);
        final int length = 100;

        for (int i=0; i<length; i++) {
            tmpOut.write(i);
        }

        for (int start=0; start<length; start++) {
            for (int end=start; end<length; end++) {
                try {
                    final int expNumBytes = end-start;
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    tmpOut.copyTo(out, start, end);

                    byte[] array = out.toByteArray();
                    assertEquals("array.length (start="+start+",end="+end+")", end-start, array.length);
                    byte[] expArray = new byte[expNumBytes];
                    for (int i=0; i<expArray.length; i++) {
                        expArray[i] = (byte) (i+start);
                    }
                    assertArrayEquals("array (start="+start+",end="+end+")", expArray, array);
                } catch (Throwable e) {
                    System.err.println("Exception (start="+start+",end="+end+")");
                    throw e;
                }
            }
        }
    }

    @Test
    public void testCopyToByteBuffer() throws Exception {
        Pool<byte[]> pool = new ConcurrentBufferPool(10, 10);
        TempOutputStream tmpOut = new TempOutputStream(pool);
        final int length = 100;

        for (int i=0; i<length; i++) {
            tmpOut.write(i);
        }

        for (int start=0; start<length; start++) {
            for (int end=start; end<length; end++) {
                try {
                    final int expNumBytes = end-start;
                    ByteBuffer out = ByteBuffer.allocate(expNumBytes);
                    tmpOut.copyTo(out, start, end);
                    assertEquals(expNumBytes, out.position());
                    out.flip();

                    byte[] array = out.array();
                    
                    assertEquals("array.length (start="+start+",end="+end+")", end-start, array.length);
                    byte[] expArray = new byte[expNumBytes];
                    for (int i=0; i<expArray.length; i++) {
                        expArray[i] = (byte) (i+start);
                    }
                    assertArrayEquals("array (start="+start+",end="+end+")", expArray, array);
                } catch (Throwable e) {
                    System.err.println("Exception (start="+start+",end="+end+")");
                    throw e;
                }
            }
        }
    }


    @Test
    public void testCopyToByteArray() throws Exception {
        Pool<byte[]> pool = new ConcurrentBufferPool(10, 10);
        TempOutputStream tmpOut = new TempOutputStream(pool);
        final int length = 100;

        for (int i=0; i<length; i++) {
            tmpOut.write(i);
        }

        for (int start=0; start<length; start++) {
            for (int end=start; end<length; end++) {
                try {
                    final int expNumBytes = end-start;
                    byte[] out = new byte[expNumBytes];
                    tmpOut.copyTo(out, start, end);

                    byte[] array = out;

                    assertEquals("array.length (start="+start+",end="+end+")", end-start, array.length);
                    byte[] expArray = new byte[expNumBytes];
                    for (int i=0; i<expArray.length; i++) {
                        expArray[i] = (byte) (i+start);
                    }
                    assertArrayEquals("array (start="+start+",end="+end+")", expArray, array);
                } catch (Throwable e) {
                    System.err.println("Exception (start="+start+",end="+end+")");
                    throw e;
                }
            }
        }
    }

}
