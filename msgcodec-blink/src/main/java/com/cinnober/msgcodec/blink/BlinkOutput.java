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

import com.cinnober.msgcodec.io.ByteSink;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * Methods for writing primitive Blink data types.
 *
 * @see BlinkInput
 * @author mikael.brannstrom
 *
 */
public class BlinkOutput {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private BlinkOutput() {
    }

    /**
     * Write the null (0xc0) value.
     *
     * @param out the output stream to write to, not null.
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeNull(ByteSink out) throws IOException {
        out.write(0xc0);
    }

    /**
     * Write a signed 8-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt8(ByteSink out, byte value) throws IOException {
        writeSignedVLC(out, value);
    }
    /**
     * Write a signed 16-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt16(ByteSink out, short value) throws IOException {
        writeSignedVLC(out, value);
    }
    /**
     * Write a signed 32-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt32(ByteSink out, int value) throws IOException {
        writeSignedVLC(out, value);
    }
    /**
     * Write a signed 64-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt64(ByteSink out, long value) throws IOException {
        writeSignedVLC(out, value);
    }
    /**
     * Write an unsigned 8-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUInt8(ByteSink out, byte value) throws IOException {
        writeUnsignedVLC(out, 0xffL & value);
    }
    /**
     * Write an unsigned 16-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUInt16(ByteSink out, short value) throws IOException {
        writeUnsignedVLC(out, 0xffffL & value);
    }
    /**
     * Write an unsigned 32-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUInt32(ByteSink out, int value) throws IOException {
        writeUnsignedVLC(out, 0xffffffffL & value);
    }
    /**
     * Write an unsigned 64-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUInt64(ByteSink out, long value) throws IOException {
        writeUnsignedVLC(out, value);
    }
    /**
     * Write a nullable signed 8-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt8Null(ByteSink out, Byte value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeSignedVLC(out, value.byteValue());
        }
    }
    /**
     * Write a nullable signed 16-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt16Null(ByteSink out, Short value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeSignedVLC(out, value.shortValue());
        }
    }
    /**
     * Write a nullable signed 32-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt32Null(ByteSink out, Integer value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeSignedVLC(out, value.intValue());
        }
    }
    /**
     * Write a nullable signed 64-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt64Null(ByteSink out, Long value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeSignedVLC(out, value.longValue());
        }
    }
    /**
     * Write a nullable unsigned 8-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUInt8Null(ByteSink out, Byte value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeUnsignedVLC(out, 0xffL & value.byteValue());
        }
    }
    /**
     * Write a nullable unsigned 16-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUInt16Null(ByteSink out, Short value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeUnsignedVLC(out, 0xffffL & value.shortValue());
        }
    }
    /**
     * Write a nullable unsigned 32-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUInt32Null(ByteSink out, Integer value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeUnsignedVLC(out, 0xffffffffL & value.intValue());
        }
    }
    /**
     * Write a nullable unsigned 64-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUInt64Null(ByteSink out, Long value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeUnsignedVLC(out, value.longValue());
        }
    }

    /**
     * Write a nullable signed big integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeBigIntNull(ByteSink out, BigInteger value) throws IOException {
    	if (value == null) {
            writeNull(out);
    	} else {
            writeSignedVLC(out, value);
    	}
    }
    /**
     * Write a signed big integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeBigInt(ByteSink out, BigInteger value) throws IOException {
	writeSignedVLC(out, value);
    }

    /**
     * Write a 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is written as a 64-bit floating point number.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     * @see #writeFloat64(ByteSink, double)
     */
    public static void writeFloat32(ByteSink out, float value) throws IOException {
        writeFloat64(out, value);
    }
    /**
     * Write a nullable 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is written as a 64-bit floating point number.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     * @see #writeFloat64Null(ByteSink, Double)
     */
    public static void writeFloat32Null(ByteSink out, Float value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeUnsignedVLC(out, Double.doubleToLongBits(value.doubleValue()));
        }
    }
    /**
     * Write a 64-bit floating point number.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeFloat64(ByteSink out, double value) throws IOException {
        writeUnsignedVLC(out, Double.doubleToLongBits(value));
    }
    /**
     * Write a nullable 64-bit floating point number.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeFloat64Null(ByteSink out, Double value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeUnsignedVLC(out, Double.doubleToLongBits(value.doubleValue()));
        }
    }

    /**
     * Write a decimal number.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public static void writeDecimal(ByteSink out, BigDecimal value) throws IOException, NullPointerException {
        int exp = -value.scale();
        if (exp < -128 || exp > 127) {
            throw new IllegalArgumentException("BigDecimal exponent out of range [-128, 127]: " + exp);
        }
        BigInteger bigMantissa = value.unscaledValue();
        if(bigMantissa.bitLength() > 63) {
            throw new IllegalArgumentException("BigDecimal unscaled value too large: " + bigMantissa);
        }

        writeSignedVLC(out, exp);
        long mantissa = bigMantissa.longValue();
        writeSignedVLC(out, mantissa);
    }
    /**
     * Write a nullable decimal number.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeDecimalNull(ByteSink out, BigDecimal value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeDecimal(out, value);
        }
    }
    /**
     * Write a big decimal number.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public static void writeBigDecimal(ByteSink out, BigDecimal value) throws IOException, NullPointerException {
    	int exp = -value.scale();
    	BigInteger bigMantissa = value.unscaledValue();
    	writeSignedVLC(out, exp);
    	writeSignedVLC(out, bigMantissa);
    }
    /**
     * Write a nullable big decimal number.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeBigDecimalNull(ByteSink out, BigDecimal value) throws IOException {
    	if (value == null) {
            writeNull(out);
        } else {
            writeBigDecimal(out, value);
        }
    }

    /**
     * Write a boolean.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writePresenceByte(ByteSink out, boolean value) throws IOException {
        out.write(value ? (byte)0x01 : (byte)0xc0);
    }

    /**
     * Write a boolean.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeBoolean(ByteSink out, boolean value) throws IOException {
        out.write(value ? (byte)1 : (byte)0);
    }
    /**
     * Write a nullable boolean.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeBooleanNull(ByteSink out, Boolean value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            out.write(value ? (byte)1 : (byte)0);
        }
    }
    /**
     * Write a unicode string.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public static void writeStringUTF8(ByteSink out, String value) throws IOException {
        int len = value.length();
        if (len < 128) {
            boolean ascii = true;
            for (int i=0; i<len; i++) {
                if (value.charAt(i) >= 0x80) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                writeUInt32(out, len);
                for (int i=0; i<len; i++) {
                    out.write(value.charAt(i));
                }
                return;
            }
        }
        byte[] bytes = value.getBytes(UTF8);
        writeUInt32(out, bytes.length);
        out.write(bytes);
    }
    /**
     * Write a nullable unicode string.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeStringUTF8Null(ByteSink out, String value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeStringUTF8(out, value);
        }
    }
    /**
     * Write binary data.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public static void writeBinary(ByteSink out, byte[] value) throws IOException {
        writeUInt32(out, value.length);
        out.write(value);
    }
    /**
     * Write nullable binary data.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeBinaryNull(ByteSink out, byte[] value) throws IOException {
        if (value == null) {
            writeNull(out);
        } else {
            writeUInt32(out, value.length);
            out.write(value);
        }
    }
    /**
     * Write a signed variable-length code value.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeSignedVLC(ByteSink out, BigInteger value) throws IOException {
        if (value.bitLength() <= 63) {
            writeSignedVLC(out, value.longValue());
        } else {
            byte[] bytes = value.toByteArray();
            if (bytes.length <= 2 || bytes.length > 0x3f) {
                throw new IllegalArgumentException("BigInteger is too large. " + bytes.length + " bytes");
            }
            out.write((bytes.length & 0x3f) | 0xc0);
            for (int i = bytes.length - 1; i >= 0; i--) {
                out.write(bytes[i]);
            }
        }
    }

    /**
     * Write a signed variable-length code value.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeSignedVLC(ByteSink out, long value) throws IOException {
        if (value < 0) {
            if (value >= 0xffffffffffffffc0L) {
                writeVLC7(out, value);
            } else if (value >= 0xffffffffffffe000L) { 
                writeVLC14(out, value);
            } else if (value >= 0xffffffffffff8000L) {
                writeVLC16(out, value);
            } else if (value >= 0xffffffffff800000L) {
                writeVLC24(out, value);
            } else if (value >= 0xffffffff80000000L) {
                writeVLC32(out, value);
            } else if (value >= 0xffffff8000000000L) {
                writeVLC40(out, value);
            } else if (value >= 0xffff800000000000L) {
                writeVLC48(out, value);
            } else if (value >= 0xff80000000000000L) {
                writeVLC56(out, value);
            } else {
                writeVLC64(out, value);
            }
        } else {
            if (value < 0x0000000000000040L) {
                writeVLC7(out, value);
            } else if (value < 0x0000000000002000L) {
                writeVLC14(out, value);
            } else if (value < 0x0000000000008000L) {
                writeVLC16(out, value);
            } else if (value < 0x0000000000800000L) {
                writeVLC24(out, value);
            } else if (value < 0x0000000080000000L) {
                writeVLC32(out, value);
            } else if (value < 0x0000008000000000L) {
                writeVLC40(out, value);
            } else if (value < 0x0000800000000000L) {
                writeVLC48(out, value);
            } else if (value < 0x0080000000000000L) {
                writeVLC56(out, value);
            } else {
                writeVLC64(out, value);
            }
        }
    }
    /**
     * Write an unsigned variable-length code value.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUnsignedVLC(ByteSink out, long value) throws IOException {
        if((value & 0xffffffffffffff80L) == 0) {
            writeVLC7(out, value);
        } else if((value & 0xffffffffffffc000L) == 0) {
            writeVLC14(out, value);
        } else if((value & 0xffffffffffff0000L) == 0) {
            writeVLC16(out, value);
        } else if((value & 0xffffffffff000000L) == 0) {
            writeVLC24(out, value);
        } else if((value & 0xffffffff00000000L) == 0) {
            writeVLC32(out, value);
        } else if((value & 0xffffff0000000000L) == 0) {
            writeVLC40(out, value);
        } else if((value & 0xffff000000000000L) == 0) {
            writeVLC48(out, value);
        } else if((value & 0xff00000000000000L) == 0) {
            writeVLC56(out, value);
        } else {
            writeVLC64(out, value);
        }
    }

    static void writeVLC7(ByteSink out, long value) throws IOException {
        out.write((int)value & 0x7f);
    }

    static void writeVLC14(ByteSink out, long value) throws IOException {
        out.write(((int)value & 0x3f) | 0x80);
        out.write((int)(value >> 6) & 0xff);
    }

    static void writeVLC16(ByteSink out, long value) throws IOException {
        out.write(((3-1) & 0x3f) | 0xc0);
        out.write((int)value & 0xff);
        out.write((int)(value >> 8) & 0xff);
    }

    static void writeVLC24(ByteSink out, long value) throws IOException {
        out.write(((4-1) & 0x3f) | 0xc0);
        out.write((int)value & 0xff);
        out.write((int)(value >> 8) & 0xff);
        out.write((int)(value >> 16) & 0xff);
    }

    static void writeVLC32(ByteSink out, long value) throws IOException {
        out.write(((5-1) & 0x3f) | 0xc0);
        out.write((int)value & 0xff);
        out.write((int)(value >> 8) & 0xff);
        out.write((int)(value >> 16) & 0xff);
        out.write((int)(value >> 24) & 0xff);
    }

    static void writeVLC40(ByteSink out, long value) throws IOException {
        out.write(((6-1) & 0x3f) | 0xc0);
        out.write((int)value & 0xff);
        out.write((int)(value >> 8) & 0xff);
        out.write((int)(value >> 16) & 0xff);
        out.write((int)(value >> 24) & 0xff);
        out.write((int)(value >> 32) & 0xff);
    }
    
    static void writeVLC48(ByteSink out, long value) throws IOException {
        out.write(((7-1) & 0x3f) | 0xc0);
        out.write((int)value & 0xff);
        out.write((int)(value >> 8) & 0xff);
        out.write((int)(value >> 16) & 0xff);
        out.write((int)(value >> 24) & 0xff);
        out.write((int)(value >> 32) & 0xff);
        out.write((int)(value >> 40) & 0xff);
    }

    static void writeVLC56(ByteSink out, long value) throws IOException {
        out.write(((8-1) & 0x3f) | 0xc0);
        out.write((int)value & 0xff);
        out.write((int)(value >> 8) & 0xff);
        out.write((int)(value >> 16) & 0xff);
        out.write((int)(value >> 24) & 0xff);
        out.write((int)(value >> 32) & 0xff);
        out.write((int)(value >> 40) & 0xff);
        out.write((int)(value >> 48) & 0xff);
    }

    static void writeVLC64(ByteSink out, long value) throws IOException {
        out.write(((9-1) & 0x3f) | 0xc0);
        out.write((int)value & 0xff);
        out.write((int)(value >> 8) & 0xff);
        out.write((int)(value >> 16) & 0xff);
        out.write((int)(value >> 24) & 0xff);
        out.write((int)(value >> 32) & 0xff);
        out.write((int)(value >> 40) & 0xff);
        out.write((int)(value >> 48) & 0xff);
        out.write((int)(value >> 56) & 0xff);
    }

    /**
     * Write a variable-length code value.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @param size the size in bytes of the value, in the range [1, 9].
     * @throws IOException if the underlying stream throws an exception
     */
    static void writeVLC(ByteSink out, long value, int size) throws IOException {
        switch (size) {
            case 1:
                writeVLC7(out, value);
                break;
            case 2:
                writeVLC14(out, value);
                break;
            case 3:
                writeVLC16(out, value);
                break;
            case 4:
                writeVLC24(out, value);
                break;
            case 5:
                writeVLC32(out, value);
                break;
            case 6:
                writeVLC40(out, value);
                break;
            case 7:
                writeVLC48(out, value);
                break;
            case 8:
                writeVLC56(out, value);
                break;
            case 9:
                writeVLC64(out, value);
                break;
            default:
                throw new IllegalArgumentException("Illegal size: " + size);
        }
    }

    /**
     * Returns the size in bytes of a signed variable-length coded value.
     * @param value the value
     * @return the number of bytes, always in the range [1, 9].
     */
    public static final int sizeOfSignedVLC(long value) {
        if (value < 0) {
            if (value >= 0xffffffffffffffc0L) // -64
                return 1;
            if (value >= 0xffffffffffffe000L) // -8192
                return 2;
            if (value >= 0xffffffffffff8000L)
                return 3; // 2 data bytes = 16 bits = 15 bits + 1 sign
            if (value >= 0xffffffffff800000L)
                return 4; // 3 data bytes = 24 bits = 23 bits + 1 sign
            if (value >= 0xffffffff80000000L)
                return 5; // 4 data bytes = 32 bits = 31 bits + 1 sign
            if (value >= 0xffffff8000000000L)
                return 6;
            if (value >= 0xffff800000000000L)
                return 7;
            if (value >= 0xff80000000000000L)
                return 8;
        } else {
            if (value < 0x0000000000000040L) // 64
                return 1;
            if (value < 0x0000000000002000L) // 8192
                return 2;
            if (value < 0x0000000000008000L)
                return 3; // 2 data bytes = 16 bits = 15 bits + 1 sign
            if (value < 0x0000000000800000L)
                return 4; // 3 data bytes = 24 bits = 23 bits + 1 sign
            if (value < 0x0000000080000000L)
                return 5; // 4 data bytes = 32 bits = 31 bits + 1 sign
            if (value < 0x0000008000000000L)
                return 6;
            if (value < 0x0000800000000000L)
                return 7;
            if (value < 0x0080000000000000L)
                return 8;
        }
        return 9;
    }

    /**
     * Returns the size in bytes of a signed variable-length coded value.
     * @param value the value, not null.
     * @return the number of bytes.
     */
    public static final int sizeOfSignedVLC(BigInteger value) {
        if (value.bitLength() <= 63) {
            return sizeOfSignedVLC(value.longValue());
        } else {
            return 2 + value.bitLength() / 8;
        }
    }

    /**
     * Returns the size in bytes of an unsigned variable-length coded value.
     * @param value the value
     * @return the number of bytes, always in the range [1, 9].
     */
    public static final int sizeOfUnsignedVLC(long value) {
        if((value & 0xffffffffffffff80L) == 0)
            return 1;
        if((value & 0xffffffffffffc000L) == 0)
            return 2;
        if((value & 0xffffffffffff0000L) == 0)
            return 3; // 2 data bytes
        if((value & 0xffffffffff000000L) == 0)
            return 4; // 3 data bytes
        if((value & 0xffffffff00000000L) == 0)
            return 5;
        if((value & 0xffffff0000000000L) == 0)
            return 6;
        if((value & 0xffff000000000000L) == 0)
            return 7;
        if((value & 0xff00000000000000L) == 0)
            return 8;
        return 9;
    }

}
