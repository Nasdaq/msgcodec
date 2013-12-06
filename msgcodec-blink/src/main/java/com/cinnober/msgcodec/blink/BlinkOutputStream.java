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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * An output stream which lets an application write primitive Blink data types.
 *
 * @see BlinkInputStream
 * @author mikael.brannstrom
 *
 */
public class BlinkOutputStream extends FilterOutputStream {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public BlinkOutputStream(OutputStream out) {
        super(out);
    }

    /** Write the null (0xc0) value.
     *
     * @throws IOException if the underlying stream throws an exception
     */
    private void writeNull() throws IOException {
        out.write(0xc0);
    }

    /** Write a signed 8-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt8(byte value) throws IOException {
        writeSignedVLC(value);
    }
    /** Write a signed 16-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt16(short value) throws IOException {
        writeSignedVLC(value);
    }
    /** Write a signed 32-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt32(int value) throws IOException {
        writeSignedVLC(value);
    }
    /** Write a signed 64-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt64(long value) throws IOException {
        writeSignedVLC(value);
    }
    /** Write an unsigned 8-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt8(byte value) throws IOException {
        writeUnsignedVLC(0xffL & value);
    }
    /** Write an unsigned 16-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt16(short value) throws IOException {
        writeUnsignedVLC(0xffffL & value);
    }
    /** Write an unsigned 32-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt32(int value) throws IOException {
        writeUnsignedVLC(0xffffffffL & value);
    }
    /** Write an unsigned 64-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt64(long value) throws IOException {
        writeUnsignedVLC(value);
    }
    /** Write a nullable signed 8-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt8Null(Byte value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeSignedVLC(value.byteValue());
        }
    }
    /** Write a nullable signed 16-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt16Null(Short value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeSignedVLC(value.shortValue());
        }
    }
    /** Write a nullable signed 32-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt32Null(Integer value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeSignedVLC(value.intValue());
        }
    }
    /** Write a nullable signed 64-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt64Null(Long value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeSignedVLC(value.longValue());
        }
    }
    /** Write a nullable unsigned 8-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt8Null(Byte value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeUnsignedVLC(0xffL & value.byteValue());
        }
    }
    /** Write a nullable unsigned 16-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt16Null(Short value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeUnsignedVLC(0xffffL & value.shortValue());
        }
    }
    /** Write a nullable unsigned 32-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt32Null(Integer value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeUnsignedVLC(0xffffffffL & value.intValue());
        }
    }
    /** Write a nullable unsigned 64-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt64Null(Long value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeUnsignedVLC(value.longValue());
        }
    }

    /** Write a nullable signed big integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBigIntNull(BigInteger value) throws IOException {
    	if (value == null) {
    		writeNull();
    	} else {
    		writeSignedBigVLC(value);
    	}
    }
    /** Write a signed big integer.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBigInt(BigInteger value) throws IOException {
		writeSignedBigVLC(value);
    }

    /** Write a 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is written as a 64-bit floating point number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     * @see #writeFloat64(double)
     */
    public void writeFloat32(float value) throws IOException {
        writeFloat64(value);
    }
    /** Write a nullable 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is written as a 64-bit floating point number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     * @see #writeFloat64Null(Double)
     */
    public void writeFloat32Null(Float value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeUnsignedVLC(Double.doubleToLongBits(value.doubleValue()));
        }
    }
    /** Write a 64-bit floating point number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeFloat64(double value) throws IOException {
        writeUnsignedVLC(Double.doubleToLongBits(value));
    }
    /** Write a nullable 64-bit floating point number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeFloat64Null(Double value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeUnsignedVLC(Double.doubleToLongBits(value.doubleValue()));
        }
    }

    /** Write a decimal number.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public void writeDecimal(BigDecimal value) throws IOException, NullPointerException {
        int exp = -value.scale();
        if (exp < -128 || exp > 127) {
            throw new IllegalArgumentException("BigDecimal exponent out of range [-128, 127]: " + exp);
        }
        BigInteger bigMantissa = value.unscaledValue();
        if(bigMantissa.bitLength() > 63) {
            throw new IllegalArgumentException("BigDecimal unscaled value too large: " + bigMantissa);
        }

        writeSignedVLC(exp);
        long mantissa = bigMantissa.longValue();
        writeSignedVLC(mantissa);
    }
    /** Write a nullable decimal number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeDecimalNull(BigDecimal value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeDecimal(value);
        }
    }
    /** Write a big decimal number.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public void writeBigDecimal(BigDecimal value) throws IOException, NullPointerException {
    	int exp = -value.scale();
    	BigInteger bigMantissa = value.unscaledValue();
    	writeSignedVLC(exp);
    	writeSignedBigVLC(bigMantissa);
    }
    /** Write a nullable big decimal number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBigDecimalNull(BigDecimal value) throws IOException {
    	if (value == null) {
    		writeNull();
    	} else {
    		writeBigDecimal(value);
    	}
    }

    /** Write a boolean.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBoolean(boolean value) throws IOException {
        out.write(value ? (byte)1 : (byte)0);
    }
    /** Write a nullable boolean.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBooleanNull(Boolean value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            out.write(value ? (byte)1 : (byte)0);
        }
    }
    /** Write a unicode string.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public void writeStringUTF8(String value) throws IOException {
        byte[] bytes = value.getBytes(UTF8);
        writeUInt32(bytes.length);
        out.write(bytes);
    }
    /** Write a nullable unicode string.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeStringUTF8Null(String value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeStringUTF8(value);
        }
    }
    /** Write binary data.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public void writeBinary(byte[] value) throws IOException {
        writeUInt32(value.length);
        write(value);
    }
    /** Write nullable binary data.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBinaryNull(byte[] value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeUInt32(value.length);
            write(value);
        }
    }
    /** Write a signed big variable-length code value.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeSignedBigVLC(BigInteger value) throws IOException {
    	if (value.bitLength() <= 63) {
    		writeSignedVLC(value.longValue());
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

    /** Write a signed variable-length code value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeSignedVLC(long value) throws IOException {
        writeVLC(value, sizeOfSignedVLC(value));
    }
    /** Write an unsigned variable-length code value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUnsignedVLC(long value) throws IOException {
        writeVLC(value, sizeOfUnsignedVLC(value));
    }

    /** Write a variable-length code value.
     *
     * @param value the value to be written
     * @param size the size in bytes of the value, in the range [1, 9].
     * @throws IOException if the underlying stream throws an exception
     */
    private void writeVLC(long value, int size) throws IOException {
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

    /** Returns the size in bytes of a signed variable-length coded value.
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

    /** Returns the size in bytes of an unsigned variable-length coded value.
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
