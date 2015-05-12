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
 * Methods for writing primitive Native Blink data types.
 *
 * @see NativeBlinkInput
 * @author mikael.brannstrom
 *
 */
public class NativeBlinkOutput {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private NativeBlinkOutput() {
    }

    /**
     * Write a signed 8-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt8(ByteSink out, byte value) throws IOException {
        out.write(value & 0xff);
    }
    /**
     * Write a signed 16-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt16(ByteSink out, short value) throws IOException {
        out.write(value & 0xff);
        out.write(value >> 8 & 0xff);
    }
    /**
     * Write a signed 32-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt32(ByteSink out, int value) throws IOException {
        out.writeIntLE(value);
    }
    /**
     * Write a signed 64-bit integer.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInt64(ByteSink out, long value) throws IOException {
        out.writeLongLE(value);
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
            out.write(0);

            out.write(0);
        } else {
            out.write(1);
            writeInt8(out, value);
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
            out.write(0);

            out.write(0);
            out.write(0);
        } else {
            out.write(1);
            writeInt16(out, value);
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
            out.write(0);
            out.writeIntLE(0);
        } else {
            out.write(1);
            out.writeIntLE(value);
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
            out.write(0);
            out.writeLongLE(0);
        } else {
            out.write(1);
            out.writeLongLE(value);
        }
    }


    public static void writeUInt8(ByteSink out, byte value) throws IOException {
        writeInt8(out, value);
    }
    public static void writeUInt8Null(ByteSink out, Byte value) throws IOException {
        writeInt8Null(out, value);
    }
    public static void writeUInt16(ByteSink out, short value) throws IOException {
        writeInt16(out, value);
    }
    public static void writeUInt16Null(ByteSink out, Short value) throws IOException {
        writeInt16Null(out, value);
    }
    public static void writeUInt32(ByteSink out, int value) throws IOException {
        writeInt32(out, value);
    }
    public static void writeUInt32Null(ByteSink out, Integer value) throws IOException {
        writeInt32Null(out, value);
    }
    public static void writeUInt64(ByteSink out, long value) throws IOException {
        writeInt64(out, value);
    }
    public static void writeUInt64Null(ByteSink out, Long value) throws IOException {
        writeInt64Null(out, value);
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
            out.write(0);
            out.writeLongLE(0);
        } else {
            out.write(1);
            writeFloat32(out, value);
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
        out.writeLongLE(Double.doubleToLongBits(value));
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
            out.write(0);
            out.writeLongLE(0);
        } else {
            out.write(1);
            writeFloat64(out, value);
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

        long mantissa = bigMantissa.longValue();
        writeInt8(out, (byte) exp);
        writeInt64(out, mantissa);
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
            out.write(0);
            
            out.write(0);
            out.writeLongLE(0);
        } else {
            out.write(0);
            writeDecimal(out, value);
        }
    }





    
    /**
     * Write a signed big integer.
     *
     * <p>Note: variable length data type. Must be written to the data area.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeDataBigInt(ByteSink out, BigInteger value) throws IOException {
        byte[] bytes = value.toByteArray();

        // convert bytes from BE to LE
        for (int i=0, i2=bytes.length-1; i<i2; i++, i2--) {
            byte b = bytes[i];
            bytes[i] = bytes[i2];
            bytes[i2] = b;
        }
        writeDataBinary(out, bytes);
    }

    /**
     * Write a big decimal number.
     *
     * <p>Note: variable length data type. Must be written to the data area.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public static void writeDataBigDecimal(ByteSink out, BigDecimal value) throws IOException, NullPointerException {
    	int exp = -value.scale();
    	BigInteger bigMantissa = value.unscaledValue();
        writeInt32(out, exp);
        writeDataBigInt(out, bigMantissa);
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
            out.write(0);
            out.write(0);
        } else {
            out.write(1);
            out.write(value ? (byte)1 : (byte)0);
        }
    }

    /**
     * Write a unicode string.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @param maxLength the maximum string length in bytes, in the range [1, 255].
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public static void writeInlineStringUTF8(ByteSink out, String value, int maxLength) throws IOException {
        int len = value.length();
        if (len < maxLength) {
            boolean ascii = true;
            for (int i=0; i<len; i++) {
                if (value.charAt(i) >= 0x80) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                out.write(len);
                for (int i=0; i<len; i++) {
                    out.write(value.charAt(i));
                }
                out.pad(maxLength-len);
            } else {
                byte[] bytes = value.getBytes(UTF8);
                len = bytes.length;
                if (len > maxLength) {
                    throw new IllegalArgumentException("String too long. More than " + maxLength + "  bytes");
                }
                out.write(len);
                out.write(bytes);
                out.pad(maxLength-len);
            }
        } else {
            throw new IllegalArgumentException("String too long. More than " + maxLength + "  bytes");
        }
    }
    
    /**
     * Write a nullable unicode string.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @param maxLength the maximum string length in bytes, in the range [1, 255].
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInlineStringUTF8Null(ByteSink out, String value, int maxLength) throws IOException {
        if (value == null) {
            out.write(0);

            out.pad(1+maxLength);
        } else {
            out.write(1);
            writeInlineStringUTF8Null(out, value, maxLength);
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
    public static void writeDataStringUTF8(ByteSink out, String value) throws IOException {
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
                out.writeIntLE(len);
                for (int i=0; i<len; i++) {
                    out.write(value.charAt(i));
                }
                return;
            }
        }
        byte[] bytes = value.getBytes(UTF8);
        out.writeIntLE(bytes.length);
        out.write(bytes);
    }
    
    /**
     * Write binary data.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written, not null
     * @param maxLength the maximum data length in bytes, in the range [1, 255].
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public static void writeInlineBinary(ByteSink out, byte[] value, int maxLength) throws IOException {
        if (value.length > maxLength) {
            throw new IllegalArgumentException("Binary data too large. More than " + maxLength + "  bytes");
        }
        out.write(value.length);
        out.write(value);
    }
    
    /**
     * Write nullable binary data.
     *
     * @param out the output stream to write to, not null.
     * @param value the value to be written
     * @param maxLength the maximum data length in bytes, in the range [1, 255].
     * @throws IOException if the underlying stream throws an exception
     */
    public static void writeInlineBinaryNull(ByteSink out, byte[] value, int maxLength) throws IOException {
        if (value == null) {
            out.write(0);
            
            out.write(0);
            for(int i=0; i<maxLength; i++) {
                out.write(0); // padding
            }
        } else {
            out.write(1);
            writeInlineBinary(out, value, maxLength);
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
    public static void writeDataBinary(ByteSink out, byte[] value) throws IOException {
        out.writeIntLE(value.length);
        out.write(value);
    }

}
