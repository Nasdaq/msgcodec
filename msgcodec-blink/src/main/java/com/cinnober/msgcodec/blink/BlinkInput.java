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

import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.io.ByteSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Methods for reading primitive Blink data types.
 *
 * @see BlinkOutput
 * @author mikael.brannstrom
 *
 */
public class BlinkInput {

    private BlinkInput() {
    }


    /**
     * Read a signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte readInt8(ByteSource in) throws IOException {
        return (byte) readSignedVLC(in);
    }
    /**
     * Read a signed 16-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static short readInt16(ByteSource in) throws IOException {
        return (short) readSignedVLC(in);
    }
    /**
     * Read a signed 32-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static int readInt32(ByteSource in) throws IOException {
        return (int) readSignedVLC(in);
    }
    /**
     * Read a signed 64-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static long readInt64(ByteSource in) throws IOException {
        return readSignedVLC(in);
    }

    /**
     * Read an unsigned 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte readUInt8(ByteSource in) throws IOException {
        return (byte) readUnsignedVLC(in);
    }
    /**
     * Read an unsigned 16-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static short readUInt16(ByteSource in) throws IOException {
        return (short) readUnsignedVLC(in);
    }
    /**
     * Read an unsigned 32-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static int readUInt32(ByteSource in) throws IOException {
        return (int) readUnsignedVLC(in);
    }
    /**
     * Read an unsigned 64-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static long readUInt64(ByteSource in) throws IOException {
        return readUnsignedVLC(in);
    }

    /**
     * Read a nullable signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Byte readInt8Null(ByteSource in) throws IOException {
        int b1 = in.read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (byte) readSignedVLC(in, b1);
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
        int b1 = in.read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (short) readSignedVLC(in, b1);
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
        int b1 = in.read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (int) readSignedVLC(in, b1);
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
        return readSignedVLCNull(in);
    }

    /**
     * Read a nullable unsigned 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Byte readUInt8Null(ByteSource in) throws IOException {
        int b1 = in.read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (byte) readUnsignedVLC(in, b1);
        }
    }
    /**
     * Read a nullable unsigned 16-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Short readUInt16Null(ByteSource in) throws IOException {
        int b1 = in.read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (short) readUnsignedVLC(in, b1);
        }
    }
    /**
     * Read a nullable unsigned 32-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Integer readUInt32Null(ByteSource in) throws IOException {
        int b1 = in.read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (int) readUnsignedVLC(in, b1);
        }
    }
    /**
     * Read a nullable unsigned 64-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Long readUInt64Null(ByteSource in) throws IOException {
        return readUnsignedVLCNull(in);
    }

    /**
     * Read a signed big integer.
     * @param in the input stream to read from, not null.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigInteger readBigInt(ByteSource in) throws IOException {
        return readSignedBigVLC(in);
    }
    /**
     * Read a nullable signed big integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigInteger readBigIntNull(ByteSource in) throws IOException {
        return readSignedBigVLCNull(in);
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
        long value = readUInt64(in);
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
        Long value = readUInt64Null(in);
        if (value == null) {
            return null;
        } else {
            return Double.longBitsToDouble(value);
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
        Byte exp = readInt8Null(in);
        if (exp == null) {
            return null;
        } else {
            long mantissa = readInt64(in);
            return BigDecimal.valueOf(mantissa, -exp.intValue());
        }
    }

    /**
     * Read a big decimal number.
     * @param in the input stream to read from, not null.
     * @return the value, not null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigDecimal readBigDecimal(ByteSource in) throws IOException {
        int exp = readInt32(in);
        return readBigDecimalMantissa(in, exp);
    }
    /**
     * Read a nullable big decimal number.
     * @param in the input stream to read from, not null.
     * @return the value, or null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigDecimal readBigDecimalNull(ByteSource in) throws IOException {
        Integer exp = readInt32Null(in);
        if (exp == null) {
            return null;
        } else {
            return readBigDecimalMantissa(in, exp);
        }
    }

    private static BigDecimal readBigDecimalMantissa(ByteSource in, int exp) throws IOException {
        int b1 = in.read();
        if ((0xc0 & b1) == 0xc0 && (0x3f & b1) <= 8) {
            return BigDecimal.valueOf(readSignedVLC(in, b1), -exp);
        } else {
            return new BigDecimal(readSignedBigVLCNull(in, b1), -exp);
        }
    }

    /**
     * Read a presence byte. Assume false if weak error.
     *
     * Backwards compatibility requires us to handle 0 as NULL.
     *
     * @param in the input stream to read from, not null.
     * @return true if the presence byte is 0x01 else false 
     * @throws IOException if the underlying stream throws an exception
     */
    public static boolean readPresenceByte(ByteSource in) throws IOException {
        return in.read() == 0x01;
    }

    /**
     * Read a boolean value.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static boolean readBoolean(ByteSource in) throws IOException {
        return readUInt8(in) != 0;
    }
    /**
     * Read a nullable boolean value.
     * @param in the input stream to read from, not null.
     * @return the value, or null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Boolean readBooleanNull(ByteSource in) throws IOException {
        int b = in.read();
        if (b == 0xc0) {
            return null;
        } else {
            return readUnsignedVLC(in, b) != 0;
        }
    }

    /**
     * Read a unicode string.
     * @param in the input stream to read from, not null.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readStringUTF8(ByteSource in) throws IOException {
        return readStringUTF8(in, -1);
    }
    /**
     * Read a nullable unicode string.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readStringUTF8Null(ByteSource in) throws IOException {
        return readStringUTF8Null(in, -1);
    }
    /**
     * Read a unicode string.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum string length (bytes) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readStringUTF8(ByteSource in, int maxLength) throws IOException {
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
     * Read a nullable unicode string.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum string length (bytes) that is allowed, or -1 for no limit.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readStringUTF8Null(ByteSource in, int maxLength) throws IOException {
        Integer sizeObj = readUInt32Null(in);
        if (sizeObj == null) {
            return null;
        }
        int size = sizeObj.intValue();
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
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readBinary(ByteSource in) throws IOException {
        return readBinary(in, -1);
    }
    /**
     * Read a nullable binary value.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readBinaryNull(ByteSource in) throws IOException {
        return readBinaryNull(in, -1);
    }
    /**
     * Read a binary value.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum binary length (bytes) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readBinary(ByteSource in, int maxLength) throws IOException {
        int size = readUInt32(in);
        if (size < 0) {
            throw new DecodeException("Cannot read binary larger than " + Integer.MAX_VALUE + " bytes.");
        }
        if (size > maxLength && maxLength >= 0) {
            throw new DecodeException("Binary length (" + size + ") exceeds limit (" + maxLength + ")");
        }
        byte[] value = new byte[size];
        in.read(value);
        return value;
    }

    /**
     * Read a nullable binary value.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum binary length (bytes) that is allowed, or -1 for no limit.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readBinaryNull(ByteSource in, int maxLength) throws IOException {
        Integer sizeObj = readUInt32Null(in);
        if (sizeObj == null) {
            return null;
        }
        int size = sizeObj.intValue();
        if (size < 0) {
            throw new DecodeException("Cannot read binary larger than " + Integer.MAX_VALUE + " bytes.");
        }
        if (size > maxLength && maxLength >= 0) {
            throw new DecodeException("Binary length (" + size + ") exceeds limit (" + maxLength + ")");
        }
        byte[] value = new byte[size];
        in.read(value);
        return value;
    }

    /**
     * Read a nullable signed variable-length code value.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Long readSignedVLCNull(ByteSource in) throws IOException {
        int b1 = in.read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return readSignedVLC(in, b1);
        }
    }
    /**
     * Read a signed variable-length code value.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static long readSignedVLC(ByteSource in) throws IOException {
        return readSignedVLC(in, in.read());
    }

    /**
     * Read a signed VLC.
     *
     * @param in the input stream to read from, not null.
     * @param b1 the first byte read.
     * @return the parsed value.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    private static long readSignedVLC(ByteSource in, int b1) throws IOException {
        if ((0x80 & b1) == 0) {
            // single byte
            if ((0x40 & b1) != 0) {
                // negative
                return (-1L << 7) | (0x7fL & b1);
            } else {
                // positive
                return 0x7fL & b1;
            }
        } else if ((0xc0 & b1) == 0x80) {
            // two bytes
            int b2 = in.read();
            if ((b2 & 0x80) != 0) {
                // negative
                return (-1L << 14) | (0x3fL & b1) | ((0xffL & b2) << 6);
            } else {
                // positive
                return (0x3fL & b1) | ((0xffL & b2) << 6);
            }
        } else {
            int size = 0x3f & b1;
            if (size == 0) {
                throw new DecodeException("Found null (0xc0) while parsing a non-nullable VLC integer");
            }
            long value = 0;
            for (int i=0; i<size; i++) {
                value |= (0xffL & in.read()) << (i * 8);
            }
            if (((value >> ((size-1) * 8)) & 0x80) != 0 && // value should be negative
                value > 0) { // but value is not already negative
                // negative
                value |= -1L << (size * 8);
            }
            return value;
        }
    }
    
    /**
     * Read a nullable unsigned big variable-length code value.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigInteger readSignedBigVLC(ByteSource in) throws IOException {
    	BigInteger value = readSignedBigVLCNull(in);
    	if (value == null) {
            throw new DecodeException("Found null (0xc0) while parsing a non-nullable VLC integer");
    	}
    	return value;
    }

    /**
     * Read a nullable unsigned big variable-length code value.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigInteger readSignedBigVLCNull(ByteSource in) throws IOException {
        return readSignedBigVLCNull(in, in.read());
    }
    /**
     * Read a nullable unsigned big variable-length code value.
     * @param in the input stream to read from, not null.
     * @param b1 the first byte read.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    private static BigInteger readSignedBigVLCNull(ByteSource in, int b1) throws IOException {
        if ((0x80 & b1) == 0) {
            // single byte
            if ((0x40 & b1) != 0) {
                // negative
                return BigInteger.valueOf((-1L << 7) | (0x7fL & b1));
            } else {
                // positive
                return BigInteger.valueOf(0x7fL & b1);
            }
        } else if ((0xc0 & b1) == 0x80) {
            // two bytes
            int b2 = in.read();
            if ((b2 & 0x80) != 0) {
                // negative
                return BigInteger.valueOf((-1L << 14) | (0x3fL & b1) | ((0xffL & b2) << 6));
            } else {
                // positive
                return BigInteger.valueOf((0x3fL & b1) | ((0xffL & b2) << 6));
            }
        } else {
            int size = 0x3f & b1;
            if (size == 0) {
                return null;
            } else if (size <= 8) { // optimize: avoid allocation of byte[]
                return BigInteger.valueOf(readSignedVLC(in, b1));
            } else {
                byte[] bytes = new byte[size];
                for (int i = bytes.length - 1; i >= 0; i--) {
                    bytes[i] = (byte) in.read();
                }
                return new BigInteger(bytes);
            }
        }
    }

    /**
     * Read a nullable unsigned variable-length code value.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Long readUnsignedVLCNull(ByteSource in) throws IOException {
        int b1 = in.read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return readUnsignedVLC(in, b1);
        }
    }
    /**
     * Read a signed variable-length code value.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static long readUnsignedVLC(ByteSource in) throws IOException {
        return readUnsignedVLC(in, in.read());
    }

    /**
     * Read an unsigned VLC.
     *
     * @param in the input stream to read from, not null.
     * @param b1 the first byte read.
     * @return the parsed value.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    private static long readUnsignedVLC(ByteSource in, int b1) throws IOException {
        if ((0x80 & b1) == 0) {
            // single byte
            return 0x7fL & b1;
        } else if ((0xc0 & b1) == 0x80) {
            // two bytes
            int b2 = in.read();
            // positive
            return (0x3fL & b1) | ((0xffL & b2) << 6);
        } else {
            int size = 0x3f & b1;
            if (size == 0) {
                throw new DecodeException("Found null (0xc0) while parsing a non-nullable VLC integer");
            }
            long value = 0;
            for (int i=0; i<size; i++) {
                value |= (0xffL & in.read()) << (i * 8);
            }
            return value;
        }
    }

    /**
     * Skip a (nullable) variable-length code value.
     * @param in the input stream to read from, not null.
     * @return true if a non-null value was skipped, otherwise false.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static boolean skipVLC(ByteSource in) throws IOException {
        int b1 = in.read();
        if ((0x80 & b1) == 0) {
            // single byte
            return true;
        } else if ((0xc0 & b1) == 0x80) {
            // two bytes
            in.skip(1);
            return true;
        } else {
            int size = 0x3f & b1;
            if (size == 0) {
                return false;
            } else {
                in.skip(size);
                return true;
            }
        }
    }

    /**
     * Skip a signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt8(ByteSource in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip a signed 16-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt16(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a signed 32-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt32(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a signed 64-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt64(ByteSource in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip an unsigned 8-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt8(ByteSource in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip an unsigned 16-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt16(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip an unsigned 32-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt32(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip an unsigned 64-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt64(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt8Null(ByteSource in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip a nullable signed 16-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt16Null(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable signed 32-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt32Null(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable signed 64-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt64Null(ByteSource in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip a nullable unsigned 8-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt8Null(ByteSource in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip a nullable unsigned 16-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt16Null(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable unsigned 32-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt32Null(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable unsigned 64-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt64Null(ByteSource in) throws IOException {
        skipVLC(in);
    }

    
    /**
     * Skip a signed big integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBigInt(ByteSource in) throws IOException {
        skipVLC(in);
    }
    /**
     * Skip a nullable signed big integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBigIntNull(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a 32-bit floating point number.
     * @param in the input stream to read from, not null.
     * @see #readFloat64(ByteSource)
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipFloat32(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable 32-bit floating point number.
     * @param in the input stream to read from, not null.
     * @see #readFloat64(ByteSource)
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipFloat32Null(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a 64-bit floating point number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipFloat64(ByteSource in) throws IOException {
        skipVLC(in);
    }
    /**
     * Skip a nullable 64-bit floating point number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipFloat64Null(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a decimal number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipDecimal(ByteSource in) throws IOException {
        skipVLC(in);
        skipVLC(in);
    }
    /**
     * Skip a nullable decimal number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipDecimalNull(ByteSource in) throws IOException {
        if (skipVLC(in)) {
            skipVLC(in);
        }
    }

    /**
     * Skip a big decimal number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBigDecimal(ByteSource in) throws IOException {
        skipDecimal(in);
    }
    /**
     * Skip a nullable big decimal number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBigDecimalNull(ByteSource in) throws IOException {
        skipDecimalNull(in);
    }

    /**
     * Skip a boolean value.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBoolean(ByteSource in) throws IOException {
        skipVLC(in);
    }
    /**
     * Skip a nullable boolean value.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBooleanNull(ByteSource in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a unicode string.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipStringUTF8(ByteSource in) throws IOException {
        skipBinary(in);
    }
    /**
     * Skip a nullable unicode string.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipStringUTF8Null(ByteSource in) throws IOException {
        skipBinaryNull(in);
    }
    /**
     * Skip a binary value.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBinary(ByteSource in) throws IOException {
        int size = readUInt32(in);
        if (size < 0) {
            throw new DecodeException("Cannot read binary larger than " + Integer.MAX_VALUE + " bytes.");
        }
        in.skip(size);
    }
    /**
     * Skip a nullable binary value.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBinaryNull(ByteSource in) throws IOException {
        Integer sizeObj = readUInt32Null(in);
        if (sizeObj == null) {
            return;
        }
        int size = sizeObj.intValue();
        if (size < 0) {
            throw new DecodeException("Cannot read binary larger than " + Integer.MAX_VALUE + " bytes.");
        }
        in.skip(size);
    }
    
}
