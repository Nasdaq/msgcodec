package com.cinnober.msgcodec.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

public class ByteBufTest {

    
    public void testReadWrite(ByteBuf buf) throws IOException {
        buf.write(123);
        buf.writeIntLE(123123);
        buf.writeLongLE(123123123L);
        buf.write(Byte.MIN_VALUE);
        buf.write(Byte.MAX_VALUE);
        buf.writeIntLE(Integer.MIN_VALUE);
        buf.writeIntLE(Integer.MAX_VALUE);
        buf.writeLongLE(Long.MIN_VALUE);
        buf.writeLongLE(Long.MAX_VALUE);
        buf.write(new byte[]{5,4,3});
        buf.write(new byte[]{5,4,3,2,1}, 1, 3);
        buf.pad(2);
        buf.write(new byte[]{1,2,3,4});
        
        
        System.out.format("%-54s", buf.getClass());
        System.out.println(ByteBuffers.toHex(buf.getByteBuffer(), 0, buf.position(), 1, 100, 100));
            
        buf.flip();
        
        assertEquals(123, buf.read());
        assertEquals(123123, buf.readIntLE());
        assertEquals(123123123L, buf.readLongLE());
        int vByte1 = buf.read();
        int vByte2 = buf.read();
        
        assertEquals(Byte.MIN_VALUE, (byte) vByte1);
        assertEquals(Byte.MAX_VALUE, (byte) vByte2);
        assertEquals(128, vByte1);
        assertEquals(Byte.MAX_VALUE, vByte2);
        
        assertEquals(Integer.MIN_VALUE, buf.readIntLE());
        assertEquals(Integer.MAX_VALUE, buf.readIntLE());
        
        assertEquals(Long.MIN_VALUE, buf.readLongLE());
        assertEquals(Long.MAX_VALUE, buf.readLongLE());
        byte[] data1 = new byte[3];
        buf.read(data1);
        assertArrayEquals(new byte[]{5,4,3}, data1);
        
        byte[] data2 = new byte[3];
        buf.read(data2);
        assertArrayEquals(new byte[]{4,3,2}, data2);
        assertEquals(0, buf.read());
        assertEquals(0, buf.read());
        
        byte[] data3 = new byte[5];
        buf.read(data3, 1, 4);
        assertArrayEquals(new byte[]{0,1,2,3,4}, data3);
        
        buf.clear();
        buf.write(new byte[]{0,1,2,3,4,0});
        buf.shift(1, 4, 1);
        buf.flip();
        buf.limit(6);
        byte[] data6 = new byte[6];
        buf.read(data6);
        assertArrayEquals(new byte[]{0,1,1,2,3,4}, data6);
        
        buf.clear();
        buf.write(new byte[]{0,1,2,3,4,0});
        buf.shift(1, 4, -1);
        buf.flip();
        buf.limit(5);
        byte[] data5 = new byte[5];
        buf.read(data5);
        assertArrayEquals(new byte[]{1,2,3,4,4}, data5);
    }
    
    
    
    @Test
    public void testBuffer() throws IOException {
        ByteBuf buf = new ByteBufferBuf(ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN));
        testReadWrite(buf);
    }
    
    @Test
    public void testDirectBuffer() throws IOException {
        ByteBuf buf = new ByteBufferBuf(ByteBuffer.allocateDirect(4096).order(ByteOrder.LITTLE_ENDIAN));
        testReadWrite(buf);
    }
    
    @Test
    public void testArray() throws IOException {
        ByteBuf buf = new ByteArrayBuf(4096);
        testReadWrite(buf);
    }
    
    @Test
    public void testReallocatingBuffer() throws IOException {
        ByteBuf buf = new ReallocatingByteBuf(8, 4096, ByteBuffer::allocate);
        testReadWrite(buf);
    }
    
    @Test
    public void testReallocatingDirectBuffer() throws IOException {
        ByteBuf buf = new ReallocatingByteBuf(8, 4096, ByteBuffer::allocateDirect);
        testReadWrite(buf);
    }
    
    @Test
    public void testReallocatingArray() throws IOException {
        ByteBuf buf = new ReallocatingArray(8, 4096);
        testReadWrite(buf);
    }
    
}
