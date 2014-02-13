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

import com.cinnober.msgcodec.DecodeException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

import java.io.EOFException;

/**
 * Methods for reading primitive Blink data types.
 *
 * @see BlinkOutput
 * @author mikael.brannstrom
 *
 */
public class BlinkInput {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int UTF8_MAX_CHARS_PER_BYTE = 4;

    private BlinkInput() {
    }

    /**
     * Read the next byte from the specified input stream.
     * @param in the input stream to read from, not null.
     * @return the next byte (never -1).
     * @throws EOFException if there is no more data.
     */
    private static int read(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }

    /**
     * Read bytes from the specified input stream.
     *
     * @param in the input stream to read from, not null.
     * @param buf the buffer into which the data is read
     *
     * @return the number of bytes read, always bytes.length.
     * @throws EOFException if there is no more data.
     */
    private static int read(InputStream in, byte[] buf) throws IOException {
        return read(in, buf, 0, buf.length);
    }

    /**
     * Read bytes from the specified input stream.
     *
     * @param in the input stream to read from, not null.
     * @param buf the buffer into which the data is read
     * @param offset the start offset in the buffer
     * @param length the number of bytes to read
     *
     * @return the number of bytes read, always length.
     * @throws EOFException if there is no more data.
     */
    private static int read(InputStream in, byte[] buf, int offset, int length) throws IOException {
        final int totalRead = length;
        while (length > 0) {
            int read = in.read(buf, offset, length);
            if (read == -1) {
                throw new EOFException();
            }
            offset += read;
            length -= read;
        }
        return totalRead;
    }


    /**
     * Read a signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte readInt8(InputStream in) throws IOException {
        return (byte) readSignedVLC(in);
    }
    /**
     * Read a signed 16-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static short readInt16(InputStream in) throws IOException {
        return (short) readSignedVLC(in);
    }
    /**
     * Read a signed 32-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static int readInt32(InputStream in) throws IOException {
        return (int) readSignedVLC(in);
    }
    /**
     * Read a signed 64-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static long readInt64(InputStream in) throws IOException {
        return readSignedVLC(in);
    }

    /**
     * Read an unsigned 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte readUInt8(InputStream in) throws IOException {
        return (byte) readUnsignedVLC(in);
    }
    /**
     * Read an unsigned 16-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static short readUInt16(InputStream in) throws IOException {
        return (short) readUnsignedVLC(in);
    }
    /**
     * Read an unsigned 32-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static int readUInt32(InputStream in) throws IOException {
        return (int) readUnsignedVLC(in);
    }
    /**
     * Read an unsigned 64-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static long readUInt64(InputStream in) throws IOException {
        return readUnsignedVLC(in);
    }

    /**
     * Read a nullable signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Byte readInt8Null(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static Short readInt16Null(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static Integer readInt32Null(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static Long readInt64Null(InputStream in) throws IOException {
        return readSignedVLCNull(in);
    }

    /**
     * Read a nullable unsigned 8-bit integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Byte readUInt8Null(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static Short readUInt16Null(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static Integer readUInt32Null(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static Long readUInt64Null(InputStream in) throws IOException {
        return readUnsignedVLCNull(in);
    }

    /**
     * Read a signed big integer.
     * @param in the input stream to read from, not null.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigInteger readBigInt(InputStream in) throws IOException {
        return readSignedBigVLC(in);
    }
    /**
     * Read a nullable signed big integer.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigInteger readBigIntNull(InputStream in) throws IOException {
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
     * @see #readFloat64(InputStream)
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static float readFloat32(InputStream in) throws IOException {
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
     * @see #readFloat64(InputStream)
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Float readFloat32Null(InputStream in) throws IOException {
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
    public static double readFloat64(InputStream in) throws IOException {
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
    public static Double readFloat64Null(InputStream in) throws IOException {
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
    public static BigDecimal readDecimal(InputStream in) throws IOException {
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
    public static BigDecimal readDecimalNull(InputStream in) throws IOException {
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
    public static BigDecimal readBigDecimal(InputStream in) throws IOException {
        int exp = readInt32(in);
        BigInteger mantissa = readBigInt(in);
        return new BigDecimal(mantissa, -exp);
    }
    /**
     * Read a nullable big decimal number.
     * @param in the input stream to read from, not null.
     * @return the value, or null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static BigDecimal readBigDecimalNull(InputStream in) throws IOException {
        Integer exp = readInt32Null(in);
        if (exp == null) {
            return null;
        } else {
        	BigInteger mantissa = readBigInt(in);
            return new BigDecimal(mantissa, -exp.intValue());
        }
    }

    /**
     * Read a boolean value.
     * @param in the input stream to read from, not null.
     * @return the value
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static boolean readBoolean(InputStream in) throws IOException {
        return readUInt8(in) != 0;  // TODO: is it byte or VLC?
    }
    /**
     * Read a nullable boolean value.
     * @param in the input stream to read from, not null.
     * @return the value, or null
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Boolean readBooleanNull(InputStream in) throws IOException {
        int b = read(in);
        if (b == 0xc0) {
            return null;
        } else {
            return b != 0; // TODO: is it byte or VLC?
        }
    }

    /**
     * Read a unicode string.
     * @param in the input stream to read from, not null.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readStringUTF8(InputStream in) throws IOException {
        return readStringUTF8(in, -1);
    }
    /**
     * Read a nullable unicode string.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readStringUTF8Null(InputStream in) throws IOException {
        return readStringUTF8Null(in, -1);
    }
    /**
     * Read a unicode string.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum string length (characters) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readStringUTF8(InputStream in, int maxLength) throws IOException {
        final int maxBinaryLength = maxLength < 0 ? -1 : maxLength * UTF8_MAX_CHARS_PER_BYTE;
        byte[] bytes = readBinary(in, maxBinaryLength);
        String value = new String(bytes, UTF8); // TODO: use a cache?
        if (maxLength >= 0 && value.length() > maxLength) {
            throw new DecodeException("String length (" + value.length() + ") exceeds limit (" + maxLength + ")");
        }
        return value;
    }
    /**
     * Read a nullable unicode string.
     * @param in the input stream to read from, not null.
     * @param maxLength the maximum string length (characters) that is allowed, or -1 for no limit.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static String readStringUTF8Null(InputStream in, int maxLength) throws IOException {
        final int maxBinaryLength = maxLength < 0 ? -1 : maxLength * UTF8_MAX_CHARS_PER_BYTE;
        byte[] bytes = readBinaryNull(in, maxBinaryLength);
        if (bytes == null) {
            return null;
        } else {
            String value = new String(bytes, UTF8); // TODO: use a cache?
            if (maxLength >= 0 && value.length() > maxLength) {
                throw new DecodeException("String length (" + value.length() + ") exceeds limit (" + maxLength + ")");
            }
            return value;
        }
    }
    /**
     * Read a binary value.
     * @param in the input stream to read from, not null.
     * @return the value, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readBinary(InputStream in) throws IOException {
        return readBinary(in, -1);
    }
    /**
     * Read a nullable binary value.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static byte[] readBinaryNull(InputStream in) throws IOException {
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
    public static byte[] readBinary(InputStream in, int maxLength) throws IOException {
        int size = readUInt32(in);
        if (size < 0) {
            throw new DecodeException("Cannot read binary larger than " + Integer.MAX_VALUE + " bytes.");
        }
        if (size > maxLength && maxLength >= 0) {
            throw new DecodeException("Binary length (" + size + ") exceeds limit (" + maxLength + ")");
        }
        byte[] value = new byte[size];
        read(in, value);
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
    public static byte[] readBinaryNull(InputStream in, int maxLength) throws IOException {
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
        read(in, value);
        return value;
    }

    /**
     * Read a nullable signed variable-length code value.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Long readSignedVLCNull(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static long readSignedVLC(InputStream in) throws IOException {
        return readSignedVLC(in, read(in));
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
    private static long readSignedVLC(InputStream in, int b1) throws IOException {
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
            int b2 = read(in);
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
                value |= (0xffL & read(in)) << (i * 8);
            }
            if (((value >> ((size-1) * 8)) & 0x80) != 0) {
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
    public static BigInteger readSignedBigVLC(InputStream in) throws IOException {
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
    public static BigInteger readSignedBigVLCNull(InputStream in) throws IOException {
	   int b1 = read(in);
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
           int b2 = read(in);
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
           }
           byte [] bytes = new byte[size];
           for (int i = bytes.length - 1; i >= 0; i--) {
                bytes[i] = (byte) read(in);
           }
           return new BigInteger(bytes);
       }
   }

    /**
     * Read a nullable unsigned variable-length code value.
     * @param in the input stream to read from, not null.
     * @return the value, or null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static Long readUnsignedVLCNull(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static long readUnsignedVLC(InputStream in) throws IOException {
        return readUnsignedVLC(in, read(in));
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
    private static long readUnsignedVLC(InputStream in, int b1) throws IOException {
        if ((0x80 & b1) == 0) {
            // single byte
            return 0x7fL & b1;
        } else if ((0xc0 & b1) == 0x80) {
            // two bytes
            int b2 = read(in);
            // positive
            return (0x3fL & b1) | ((0xffL & b2) << 6);
        } else {
            int size = 0x3f & b1;
            if (size == 0) {
                throw new DecodeException("Found null (0xc0) while parsing a non-nullable VLC integer");
            }
            long value = 0;
            for (int i=0; i<size; i++) {
                value |= (0xffL & read(in)) << (i * 8);
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
    public static boolean skipVLC(InputStream in) throws IOException {
        int b1 = read(in);
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
    public static void skipInt8(InputStream in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip a signed 16-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt16(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a signed 32-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt32(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a signed 64-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt64(InputStream in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip an unsigned 8-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt8(InputStream in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip an unsigned 16-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt16(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip an unsigned 32-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt32(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip an unsigned 64-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt64(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable signed 8-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt8Null(InputStream in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip a nullable signed 16-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt16Null(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable signed 32-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt32Null(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable signed 64-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipInt64Null(InputStream in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip a nullable unsigned 8-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt8Null(InputStream in) throws IOException {
        skipVLC(in);
    }
    
    /**
     * Skip a nullable unsigned 16-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt16Null(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable unsigned 32-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt32Null(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable unsigned 64-bit integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipUInt64Null(InputStream in) throws IOException {
        skipVLC(in);
    }

    
    /**
     * Skip a signed big integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBigInt(InputStream in) throws IOException {
        skipVLC(in);
    }
    /**
     * Skip a nullable signed big integer.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBigIntNull(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a 32-bit floating point number.
     * @param in the input stream to read from, not null.
     * @see #readFloat64(InputStream)
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipFloat32(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a nullable 32-bit floating point number.
     * @param in the input stream to read from, not null.
     * @see #readFloat64(InputStream)
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipFloat32Null(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a 64-bit floating point number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipFloat64(InputStream in) throws IOException {
        skipVLC(in);
    }
    /**
     * Skip a nullable 64-bit floating point number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipFloat64Null(InputStream in) throws IOException {
        skipVLC(in);
    }

    /**
     * Skip a decimal number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipDecimal(InputStream in) throws IOException {
        skipVLC(in);
        skipVLC(in);
    }
    /**
     * Skip a nullable decimal number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipDecimalNull(InputStream in) throws IOException {
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
    public static void skipBigDecimal(InputStream in) throws IOException {
        skipDecimal(in);
    }
    /**
     * Skip a nullable big decimal number.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBigDecimalNull(InputStream in) throws IOException {
        skipDecimalNull(in);
    }

    /**
     * Skip a boolean value.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBoolean(InputStream in) throws IOException {
        in.skip(1); // TODO: is it byte or VLC?
    }
    /**
     * Skip a nullable boolean value.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBooleanNull(InputStream in) throws IOException {
        in.skip(1); // TODO: is it byte or VLC?
    }

    /**
     * Skip a unicode string.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipStringUTF8(InputStream in) throws IOException {
        skipBinary(in);
    }
    /**
     * Skip a nullable unicode string.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipStringUTF8Null(InputStream in) throws IOException {
        skipBinaryNull(in);
    }
    /**
     * Skip a binary value.
     * @param in the input stream to read from, not null.
     * @throws IOException if the input stream throws an exception.
     * @throws DecodeException if the value could not be parsed.
     */
    public static void skipBinary(InputStream in) throws IOException {
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
    public static void skipBinaryNull(InputStream in) throws IOException {
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
