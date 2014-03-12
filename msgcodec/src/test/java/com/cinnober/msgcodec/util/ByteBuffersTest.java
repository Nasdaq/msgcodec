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
}
