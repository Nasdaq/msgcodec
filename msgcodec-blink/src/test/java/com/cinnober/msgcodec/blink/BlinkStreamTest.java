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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

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

    /**
     * Testcases that triggers bug TEDEV-13982.
     * @throws IOException
     */
    @Test
    public void testLargeNegativeSignedVLC() throws IOException {
        testEncodeDecodeSignedVLC(-1L<<55,
                new byte[] {(byte)0xc7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x80});
        testEncodeDecodeSignedVLC(-1L<<56,
                new byte[] {(byte)0xc8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xff});
        testEncodeDecodeSignedVLC(-1L<<57,
                new byte[] {(byte)0xc8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xfe});
        testEncodeDecodeSignedVLC(-1L<<58,
                new byte[] {(byte)0xc8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xfc});
        testEncodeDecodeSignedVLC(-1L<<59,
                new byte[] {(byte)0xc8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xf8});
        testEncodeDecodeSignedVLC(-1L<<60,
                new byte[] {(byte)0xc8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xf0});
        testEncodeDecodeSignedVLC(-1L<<61,
                new byte[] {(byte)0xc8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xe0});
        testEncodeDecodeSignedVLC(-1L<<62,
                new byte[] {(byte)0xc8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xc0});
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
        assertArrayEquals("Encoded value", encoded, actualEncoded);

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

    @Test
    public void testSizeOfSignedVLC() {
        // positive values
        assertEquals(1, BlinkOutput.sizeOfSignedVLC(0));
        assertEquals(1, BlinkOutput.sizeOfSignedVLC((1<<6)-1)); // 63
        assertEquals(2, BlinkOutput.sizeOfSignedVLC( 1<<6)); // 64
        assertEquals(2, BlinkOutput.sizeOfSignedVLC((1<<13)-1));
        assertEquals(3, BlinkOutput.sizeOfSignedVLC( 1<<13));
        assertEquals(3, BlinkOutput.sizeOfSignedVLC((1<<15)-1));
        assertEquals(4, BlinkOutput.sizeOfSignedVLC( 1<<15));
        assertEquals(4, BlinkOutput.sizeOfSignedVLC((1<<23)-1));
        assertEquals(5, BlinkOutput.sizeOfSignedVLC( 1<<23));
        assertEquals(5, BlinkOutput.sizeOfSignedVLC((1L<<31)-1));
        assertEquals(6, BlinkOutput.sizeOfSignedVLC( 1L<<31));
        assertEquals(6, BlinkOutput.sizeOfSignedVLC((1L<<39)-1));
        assertEquals(7, BlinkOutput.sizeOfSignedVLC( 1L<<39));
        assertEquals(7, BlinkOutput.sizeOfSignedVLC((1L<<47)-1));
        assertEquals(8, BlinkOutput.sizeOfSignedVLC( 1L<<47));
        assertEquals(8, BlinkOutput.sizeOfSignedVLC((1L<<55)-1));
        assertEquals(9, BlinkOutput.sizeOfSignedVLC( 1L<<55));
        assertEquals(9, BlinkOutput.sizeOfSignedVLC((1L<<63)-1));

        // negative values
        assertEquals(1, BlinkOutput.sizeOfSignedVLC(-(1<<6))); // -64
        assertEquals(2, BlinkOutput.sizeOfSignedVLC(-(1<<6)-1)); // -65
        assertEquals(2, BlinkOutput.sizeOfSignedVLC(-(1<<13)));
        assertEquals(3, BlinkOutput.sizeOfSignedVLC(-(1<<13)-1));
        assertEquals(3, BlinkOutput.sizeOfSignedVLC(-(1<<15)));
        assertEquals(4, BlinkOutput.sizeOfSignedVLC(-(1<<15)-1));
        assertEquals(4, BlinkOutput.sizeOfSignedVLC(-(1<<23)));
        assertEquals(5, BlinkOutput.sizeOfSignedVLC(-(1<<23)-1));
        assertEquals(5, BlinkOutput.sizeOfSignedVLC(-(1L<<31)));
        assertEquals(6, BlinkOutput.sizeOfSignedVLC(-(1L<<31)-1));
        assertEquals(6, BlinkOutput.sizeOfSignedVLC(-(1L<<39)));
        assertEquals(7, BlinkOutput.sizeOfSignedVLC(-(1L<<39)-1));
        assertEquals(7, BlinkOutput.sizeOfSignedVLC(-(1L<<47)));
        assertEquals(8, BlinkOutput.sizeOfSignedVLC(-(1L<<47)-1));
        assertEquals(8, BlinkOutput.sizeOfSignedVLC(-(1L<<55)));
        assertEquals(9, BlinkOutput.sizeOfSignedVLC(-(1L<<55)-1));
        assertEquals(9, BlinkOutput.sizeOfSignedVLC(-(1L<<63)));

        // big integers
        assertEquals(1, BlinkOutput.sizeOfSignedVLC(BigInteger.ZERO));
        assertEquals(9, BlinkOutput.sizeOfSignedVLC((BigInteger.ONE.shiftLeft(8*8-1).subtract(BigInteger.ONE))));
        assertEquals(9, BlinkOutput.sizeOfSignedVLC((BigInteger.ONE.shiftLeft(8*8-1).negate())));

        assertEquals(10, BlinkOutput.sizeOfSignedVLC(BigInteger.ONE.shiftLeft(9*8-1).subtract(BigInteger.ONE)));
        assertEquals(11, BlinkOutput.sizeOfSignedVLC(BigInteger.ONE.shiftLeft(9*8-1)));
        
        assertEquals(10, BlinkOutput.sizeOfSignedVLC(BigInteger.ONE.shiftLeft(9*8-1).negate()));
        assertEquals(11, BlinkOutput.sizeOfSignedVLC(BigInteger.ONE.shiftLeft(9*8-1).negate().subtract(
                BigInteger.ONE)));
    }

}
