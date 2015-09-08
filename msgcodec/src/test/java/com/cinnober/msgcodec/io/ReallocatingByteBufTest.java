/*
 *
 *  * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
 *  * Sweden. All rights reserved.
 *  *
 *  * This software is the confidential and proprietary information of
 *  * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 *  * disclose such Confidential Information and shall use it only in
 *  * accordance with the terms of the license agreement you entered into
 *  * with Cinnober.
 *  *
 *  * Cinnober makes no representations or warranties about the suitability
 *  * of the software, either expressed or implied, including, but not limited
 *  * to, the implied warranties of merchantibility, fitness for a particular
 *  * purpose, or non-infringement. Cinnober shall not be liable for any
 *  * damages suffered by licensee as a result of using, modifying, or
 *  * distributing this software or its derivatives.
 *
 *
 */

package com.cinnober.msgcodec.io;

import static org.junit.Assert.assertEquals;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.cinnober.msgcodec.EncodeBufferOverflowException;
import com.cinnober.msgcodec.EncodeBufferUnderflowException;
import com.cinnober.msgcodec.anot.Id;

/**
 */
public class ReallocatingByteBufTest {

    
    public static void writeInt32BE(ByteBuf out, int value) throws IOException {
        out.write(0xff & (value >> 24));
        out.write(0xff & (value >> 16));
        out.write(0xff & (value >> 8));
        out.write(0xff & (value >> 0));
    }
    
    public static int readInt32BE(ByteBuf in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }
    
    
    // expects a buffer with initial size 8 and max size 22
    public void testByteBufGrowing(ByteBuf buf) throws IOException {
        assertEquals(8 , buf.allocation());
        writeInt32BE(buf, 1);
        writeInt32BE(buf, 2);
        writeInt32BE(buf, 3);
        assertEquals(16, buf.allocation());
        writeInt32BE(buf, 4);
        assertEquals(16, buf.allocation());
        writeInt32BE(buf, 5);
        assertEquals(22, buf.allocation());

        buf.flip();

        assertEquals(1, readInt32BE(buf));
        assertEquals(2, readInt32BE(buf));
        assertEquals(3, readInt32BE(buf));
        assertEquals(4, readInt32BE(buf));
        assertEquals(5, readInt32BE(buf));
    }

    // expects a buffer with initial size 8 and max size 16
    public void testByteBufLimitOverflow(ByteBuf buf) throws IOException {
        
        buf.limit(8);
        assertEquals(8 , buf.allocation());
        writeInt32BE(buf, 1);
        writeInt32BE(buf, 2);
        writeInt32BE(buf, 3);
    }

    // expects a buffer with initial size 8 and max size 8
    public void testByteBufOverflow(ByteBuf buf) throws IOException {
        
        assertEquals(8 , buf.allocation());
        writeInt32BE(buf, 1);
        writeInt32BE(buf, 2);
        writeInt32BE(buf, 3);
    }
    
    
    // expects a buffer with initial size 8 and max size 8
    public void testByteBufLimitUnderflow(ByteBuf buf) throws IOException {
        
        assertEquals(8 , buf.allocation());
        writeInt32BE(buf, 1);
        writeInt32BE(buf, 2);
        assertEquals(8 , buf.allocation());

        buf.flip();

        assertEquals(1, readInt32BE(buf));
        assertEquals(2, readInt32BE(buf));
        assertEquals(3, readInt32BE(buf));
    }
    
    
    @Id(1)
    public static class InternalMessageObject {
        public int id;
        public String text;
        public boolean flag;

        public InternalMessageObject() {}

        public InternalMessageObject(int id, String text, boolean flag) {}
    }
    
    
    @Test
    public void testReallocatingBuffer() throws IOException {
        testByteBufGrowing(new ReallocatingByteBuf(8, 22, ByteBuffer::allocate));
    }

    @Test
    public void testReallocatingDirectBuffer() throws IOException {
        testByteBufGrowing(new ReallocatingByteBuf(8, 22, ByteBuffer::allocateDirect));
    }
    
    @Test
    public void testReallocatingArray() throws IOException {
        testByteBufGrowing(new ReallocatingArray(8, 22));
    }
    
    @Test(expected = EncodeBufferOverflowException.class)
    public void testReallocatingBufferLimitOverflow() throws IOException {
        testByteBufLimitOverflow(new ReallocatingByteBuf(8, 16, ByteBuffer::allocate));
    }
    
    @Test(expected = EncodeBufferOverflowException.class)
    public void testReallocatingDirectBufferLimitOverflow() throws IOException {
        testByteBufLimitOverflow(new ReallocatingByteBuf(8, 16, ByteBuffer::allocateDirect));
    }
    
    @Test(expected = EncodeBufferOverflowException.class)
    public void testReallocatingArrayrLimitOverflow() throws IOException {
        testByteBufLimitOverflow(new ReallocatingArray(8, 16));
    }
    
    
    @Test(expected = EncodeBufferOverflowException.class)
    public void testReallocatingBufferOverflow() throws IOException {
        testByteBufOverflow(new ReallocatingByteBuf(8, 8, ByteBuffer::allocate));
    }
    
    @Test(expected = EncodeBufferOverflowException.class)
    public void testReallocatingDirectBufferOverflow() throws IOException {
        testByteBufOverflow(new ReallocatingByteBuf(8, 8, ByteBuffer::allocateDirect));
    }
    
    @Test(expected = EncodeBufferOverflowException.class)
    public void testReallocatingArrayrOverflow() throws IOException {
        testByteBufOverflow(new ReallocatingArray(8, 8));
    }
    
    
    @Test(expected = EncodeBufferUnderflowException.class)
    public void testReallocatingBufferUnderflow() throws IOException {
        testByteBufLimitUnderflow(new ReallocatingByteBuf(8, 8, ByteBuffer::allocate));
    }
    
    @Test(expected = EncodeBufferUnderflowException.class)
    public void testReallocatingDirectBufferUnderflow() throws IOException {
        testByteBufLimitUnderflow(new ReallocatingByteBuf(8, 8, ByteBuffer::allocateDirect));
    }
    
    @Test(expected = EncodeBufferUnderflowException.class)
    public void testReallocatingArrayrUnderflow() throws IOException {
        testByteBufLimitUnderflow(new ReallocatingArray(8, 8));
    }
    
}
