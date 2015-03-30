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

import com.cinnober.msgcodec.ByteSink;
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
        writeVLC(out, value, sizeOfSignedVLC(value));
    }
    /**
     * Write an unsigned variable-length code value.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeUnsignedVLC(ByteSink out, long value) throws IOException {
        writeVLC(out, value, sizeOfUnsignedVLC(value));
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
        if (size == 1) {
            out.write((int)value & 0x7f);
        } else if (size == 2) {
            out.write(((int)value & 0x3f) | 0x80);
            out.write((int)(value >> 6) & 0xff);
        } else {
            out.write(((size-1) & 0x3f) | 0xc0);
            switch (size) {
                case 3:
                    out.write((int)value & 0xff);
                    out.write((int)(value >> 8) & 0xff);
                    break;
                case 4:
                    out.write((int)value & 0xff);
                    out.write((int)(value >> 8) & 0xff);
                    out.write((int)(value >> 16) & 0xff);
                    break;
                case 5:
                    out.write((int)value & 0xff);
                    out.write((int)(value >> 8) & 0xff);
                    out.write((int)(value >> 16) & 0xff);
                    out.write((int)(value >> 24) & 0xff);
                    break;
                case 6:
                    out.write((int)value & 0xff);
                    out.write((int)(value >> 8) & 0xff);
                    out.write((int)(value >> 16) & 0xff);
                    out.write((int)(value >> 24) & 0xff);
                    out.write((int)(value >> 32) & 0xff);
                    break;
                case 7:
                    out.write((int)value & 0xff);
                    out.write((int)(value >> 8) & 0xff);
                    out.write((int)(value >> 16) & 0xff);
                    out.write((int)(value >> 24) & 0xff);
                    out.write((int)(value >> 32) & 0xff);
                    out.write((int)(value >> 40) & 0xff);
                    break;
                case 8:
                    out.write((int)value & 0xff);
                    out.write((int)(value >> 8) & 0xff);
                    out.write((int)(value >> 16) & 0xff);
                    out.write((int)(value >> 24) & 0xff);
                    out.write((int)(value >> 32) & 0xff);
                    out.write((int)(value >> 40) & 0xff);
                    out.write((int)(value >> 48) & 0xff);
                    break;
                case 9:
                    out.write((int)value & 0xff);
                    out.write((int)(value >> 8) & 0xff);
                    out.write((int)(value >> 16) & 0xff);
                    out.write((int)(value >> 24) & 0xff);
                    out.write((int)(value >> 32) & 0xff);
                    out.write((int)(value >> 40) & 0xff);
                    out.write((int)(value >> 48) & 0xff);
                    out.write((int)(value >> 56) & 0xff);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal size: " + size);
            }
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
