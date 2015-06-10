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

import com.cinnober.msgcodec.io.ByteSource;
import com.cinnober.msgcodec.DecodeException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Methods for reading primitive Native Blink data types.
 *
 * <p>Read methods for variable size data types come in two flavors:
 * <code>readInlineXxx</code> and <code>readDataXxx</code> for inline and data area respectively.
 *
 * @see BlinkOutput
 * @author mikael.brannstrom
 *
 */
public class NativeBlinkInput {

    private NativeBlinkInput() {
    }


    /**
     * Read a signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte readInt8(ByteSource in) throws IOException {
        return (byte) in.read();
    }
    /**
     * Read a signed 16-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static short readInt16(ByteSource in) throws IOException {
        return (short)(in.read() | in.read() << 8);
    }
    /**
     * Read a signed 32-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static int readInt32(ByteSource in) throws IOException {
        return in.readIntLE();
    }
    /**
     * Read a signed 64-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static long readInt64(ByteSource in) throws IOException {
        return in.readLongLE();
    }

    /**
     * Read a nullable signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Byte readInt8Null(ByteSource in) throws IOException {
        if (in.read() != 0) {
            return (byte) in.read();
        } else {
            in.skip(1);
            return null;
        }
    }
    /**
     * Read a nullable signed 16-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Short readInt16Null(ByteSource in) throws IOException {
        if (in.read() != 0) {
            return readInt16(in);
        } else {
            in.skip(2);
            return null;
        }
    }
    /**
     * Read a nullable signed 32-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Integer readInt32Null(ByteSource in) throws IOException {
        if (in.read() != 0) {
            return in.readIntLE();
        } else {
            in.skip(4);
            return null;
        }
    }
    /**
     * Read a nullable signed 64-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Long readInt64Null(ByteSource in) throws IOException {
        if (in.read() != 0) {
            return in.readLongLE();
        } else {
            in.skip(8);
            return null;
        }
    }

    public static byte readUInt8(ByteSource in) throws IOException {
        return readInt8(in);
    }
    public static Byte readUInt8Null(ByteSource in) throws IOException {
        return readInt8Null(in);
    }
    public static short readUInt16(ByteSource in) throws IOException {
        return readInt16(in);
    }
    public static Short readUInt16Null(ByteSource in) throws IOException {
        return readInt16Null(in);
    }
    public static int readUInt32(ByteSource in) throws IOException {
        return readInt32(in);
    }
    public static Integer readUInt32Null(ByteSource in) throws IOException {
        return readInt32Null(in);
    }
    public static long readUInt64(ByteSource in) throws IOException {
        return readInt64(in);
    }
    public static Long readUInt64Null(ByteSource in) throws IOException {
        return readInt64Null(in);
    }


    /**
     * Read a signed big integer.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum bigint length (bytes) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigInteger readDataBigInt(ByteSource in, int maxLength) throws IOException {
        byte[] bytes = readDataBinary(in, maxLength);
        if (bytes.length == 0) {
            throw new DecodeException("Cannot decode BigInt from zero length data");
        }

        // convert bytes from LE to BE
        for (int i=0, i2=bytes.length-1; i<i2; i++, i2--) {
            byte b = bytes[i];
            bytes[i] = bytes[i2];
            bytes[i2] = b;
        }
        return new BigInteger(bytes);
    }

    /**
     * Read a 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is read as a 64-bit floating point number.
     *
     * @param in the input stream to read from, not null.
     * @return the value
     * @see #readFloat64(ByteSource)
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static float readFloat32(ByteSource in) throws IOException {
        return (float) readFloat64(in);
    }

    /**
     * Read a nullable 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is read as a 64-bit floating point number.
     *
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @see #readFloat64(ByteSource)
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Float readFloat32Null(ByteSource in) throws IOException {
        Double value = readFloat64Null(in);
        if (value == null) {
            return null;
        } else {
            return value.floatValue();
        }
    }

    /**
     * Read a 64-bit floating point number.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static double readFloat64(ByteSource in) throws IOException {
        long value = in.readLongLE();
        return Double.longBitsToDouble(value);
    }
    /**
     * Read a nullable 64-bit floating point number.
     * @param in the input stream to read from, not null.
     * @return the value, or null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Double readFloat64Null(ByteSource in) throws IOException {
        if (in.read() != 0) {
            return readFloat64(in);
        } else {
            in.skip(8);
            return null;
        }
    }

    /**
     * Read a decimal number.
     * @param in the input stream to read from, not null.
     * @return the value, not null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigDecimal readDecimal(ByteSource in) throws IOException {
        int exp = readInt8(in);
        long mantissa = readInt64(in);
        return BigDecimal.valueOf(mantissa, -exp);
    }
    /**
     * Read a nullable decimal number.
     * @param in the input stream to read from, not null.
     * @return the value, or null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigDecimal readDecimalNull(ByteSource in) throws IOException {
        if (in.read() != 0) {
            return readDecimal(in);
        } else {
            in.skip(9);
            return null;
        }
    }

    /**
     * Read a big decimal number.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum data length (bytes) that is allowed for the mantissa, or -1 for no limit.
     * @return the value, not null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigDecimal readDataBigDecimal(ByteSource in, int maxLength) throws IOException {
        int exp = readInt32(in);
        BigInteger mantissa = readDataBigInt(in, maxLength);
        return new BigDecimal(mantissa, -exp);
    }

    /**
     * Read a boolean value.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static boolean readBoolean(ByteSource in) throws IOException {
        return in.read() != 0;
    }
    /**
     * Read a nullable boolean value.
     * @param in the input stream to read from, not null.
     * @return the value, or null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Boolean readBooleanNull(ByteSource in) throws IOException {
        if (in.read() != 0) {
            return in.read() != 0;
        } else {
            in.skip(0);
            return null;
        }
    }

    /**
     * Read a unicode string.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum string length (bytes), in the range [1, 255].
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readInlineStringUTF8(ByteSource in, int maxLength) throws IOException {
        int size = in.read();
        if (size > maxLength) {
            throw new DecodeException("String length (" + size + ") exceeds limit (" + maxLength + ")");
        }
        String str = in.readStringUtf8(size);
        in.skip(maxLength - size);
        return str;
    }
    /**
     * Read a nullable unicode string.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum string length (bytes), in the range [1, 255].
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readInlineStringUTF8Null(ByteSource in, int maxLength) throws IOException {
        if (in.read() != 0) {
            return readInlineStringUTF8(in, maxLength);
        } else {
            in.skip(1 + maxLength);
            return null;
        }
    }
    /**
     * Read a unicode string.
     * @param in the input stream to read from, not null.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readDataStringUTF8(ByteSource in) throws IOException {
        return readDataStringUTF8(in, -1);
    }
    /**
     * Read a unicode string.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum string length (bytes) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readDataStringUTF8(ByteSource in, int maxLength) throws IOException {
        int size = readUInt32(in);
        if (size < 0) {
            throw new DecodeException("Cannot read string larger than " + Integer.MAX_VALUE + " bytes.");
        }
        if (size > maxLength && maxLength >= 0) {
            throw new DecodeException("String length (" + size + ") exceeds limit (" + maxLength + ")");
        }
        return in.readStringUtf8(size);
    }
    /**
     * Read a binary value.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum binary length (bytes), in the range [1, 255].
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readInlineBinary(ByteSource in, int maxLength) throws IOException {
        int size = in.read();
        if (size > maxLength) {
            throw new DecodeException("Binary length (" + size + ") exceeds limit (" + maxLength + ")");
        }
        byte[] value = new byte[size];
        in.read(value);
        return value;
    }

    /**
     * Read a nullable binary value.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum binary length (bytes), in the range [1, 255].
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readInlineBinaryNull(ByteSource in, int maxLength) throws IOException {
        if (in.read() != 0) {
            return readInlineBinary(in, maxLength);
        } else {
            in.skip(1 + maxLength);
            return null;
        }
    }

    /**
     * Read binary data.
     *
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum binary length (bytes) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readDataBinary(ByteSource in, int maxLength) throws IOException {
        int size = readUInt32(in);
        if (size < 0) {
            throw new DecodeException("Cannot read binary larger than " + Integer.MAX_VALUE + " bytes.");
        }
        if (size > maxLength && maxLength >= 0) {
            throw new DecodeException("Binary length (" + size + ") exceeds limit (" + maxLength + ")");
        }
        byte[] data = new byte[size];
        in.read(data);
        return data;
    }
    
}
