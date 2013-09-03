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
package com.cinnober.msgcodec.blink;

import static com.cinnober.msgcodec.blink.TestUtil.assertEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.junit.Test;

/**
 * @author mikael.brannstrom
 *
 */
public class BlinkStreamTest {

    /** Examples from the Blink Specification beta2 - 2013-02-05, chapter 3.1. 
     * @throws IOException 
     */
    @Test
    public void testIntegerExamples() throws IOException {
        testEncodeDecodeUnsignedVLC(64, new byte[] { 0x40 });
        testEncodeDecodeSignedVLC(64, new byte[] { (byte)0x80, 0x01 });
        testEncodeDecodeSignedVLC(4711, new byte[] { (byte)0xa7, 0x49 });
        testEncodeDecodeUnsignedVLC(4711, new byte[] { (byte)0xa7, 0x49 });
        testEncodeDecodeUnsignedVLC(4294967295L,
            new byte[] { (byte)0xc4, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff });
        testEncodeDecodeSignedVLC(-64, new byte[] { 0x40 });
        testEncodeDecodeSignedVLC(-4711, new byte[] { (byte)0x99, (byte)0xb6 });
        testEncodeDecodeSignedVLC(-2147483648, new byte[] { (byte)0xc4, 0x00, 0x00, 0x00, (byte)0x80 });
    }

    /** Examples from the Blink Specification beta2 - 2013-02-05, chapter 3.2. 
     * @throws IOException 
     */
    @Test
    public void testStringExamples() throws IOException {
        testEncodeDecodeStringUTF8("Hello", new byte[] { 0x05, 0x48, 0x65, 0x6c, 0x6c, 0x6f });
        
        testEncodeDecodeStringUTF8("Räksmörgås",
            new byte[] { 0x0d, 0x52, (byte)0xc3, (byte)0xa4, 0x6b, 0x73,
            0x6d, (byte)0xc3, (byte)0xb6, 0x72, 0x67, (byte)0xc3, (byte)0xa5, 0x73});

        testEncodeDecodeStringUTF8("", new byte[] { 0x00 });
    }   

    /** Examples from the Blink Specification beta2 - 2013-02-05, chapter 3.5. 
     * @throws IOException 
     */
    @Test
    public void testDecimalExamples() throws IOException {
        testEncodeDecodeDecimal(BigDecimal.valueOf(10000, 2), // 100.00
                new byte[] { 0x7e, (byte)0xc2, 0x10, 0x27 });
    }   

    /** Examples from the Blink Specification beta2 - 2013-02-05, chapter 3.6. 
     * @throws IOException 
     */
    @Test
    public void testFloatExamples() throws IOException {
        testEncodeDecodeFloat64(1.23456789,
            new byte[] { (byte)0xc8, 0x1b, (byte)0xde, (byte)0x83, 0x42, 
            (byte)0xca, (byte)0xc0, (byte)0xf3, 0x3f });
        
        testEncodeDecodeFloat64(Double.POSITIVE_INFINITY,
            new byte[] { (byte)0xc8, 0x00, 0x00, 0x00, 0x00, 
            0x00, 0x00, (byte)0xf0, 0x7f });
    }   
    
    
    
    
    private void testEncodeDecodeUnsignedVLC(long value, byte[] encoded) throws IOException {
        testEncodeDecode(value, encoded, new StreamOp<Long>() {
            @Override
            public void writeValue(Long value, BlinkOutputStream out) throws IOException {
                out.writeUnsignedVLC(value);
            }
            @Override
            public Long readValue(BlinkInputStream in) throws IOException {
                return in.readUnsignedVLC();
            }
        });
    }
    private void testEncodeDecodeSignedVLC(long value, byte[] encoded) throws IOException {
        testEncodeDecode(value, encoded, new StreamOp<Long>() {
            @Override
            public void writeValue(Long value, BlinkOutputStream out) throws IOException {
                out.writeSignedVLC(value);
            }
            @Override
            public Long readValue(BlinkInputStream in) throws IOException {
                return in.readSignedVLC();
            }
        });
    }
    private void testEncodeDecodeStringUTF8(String value, byte[] encoded) throws IOException {
        testEncodeDecode(value, encoded, new StreamOp<String>() {
            @Override
            public void writeValue(String value, BlinkOutputStream out) throws IOException {
                out.writeStringUTF8(value);
            }
            @Override
            public String readValue(BlinkInputStream in) throws IOException {
                return in.readStringUTF8();
            }
        });
    }
    private void testEncodeDecodeDecimal(BigDecimal value, byte[] encoded) throws IOException {
        testEncodeDecode(value, encoded, new StreamOp<BigDecimal>() {
            @Override
            public void writeValue(BigDecimal value, BlinkOutputStream out) throws IOException {
                out.writeDecimal(value);
            }
            @Override
            public BigDecimal readValue(BlinkInputStream in) throws IOException {
                return in.readDecimal();
            }
        });
    }
    private void testEncodeDecodeFloat64(Double value, byte[] encoded) throws IOException {
        testEncodeDecode(value, encoded, new StreamOp<Double>() {
            @Override
            public void writeValue(Double value, BlinkOutputStream out) throws IOException {
                out.writeFloat64(value);
            }
            @Override
            public Double readValue(BlinkInputStream in) throws IOException {
                return in.readFloat64();
            }
        });
    }
    
    private <V> void testEncodeDecode(V value, byte[] encoded, StreamOp<V> streamOp) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        BlinkOutputStream out = new BlinkOutputStream(bout);

        // encode
        bout.reset();
        streamOp.writeValue(value, out);
        byte[] actualEncoded = bout.toByteArray();
        assertEquals("Encoded value", encoded, actualEncoded);

        // decode
        ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        BlinkInputStream in = new BlinkInputStream(bin);
        V decodedValue = streamOp.readValue(in);
        assertEquals("Decoded value", value, decodedValue);
        
        assertEquals("Remaining bytes after decode", 0, in.available());
    }   
    
    private interface StreamOp<V> {
        void writeValue(V value, BlinkOutputStream out) throws IOException;
        V readValue(BlinkInputStream in) throws IOException;
    }
    
}
