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

import com.cinnober.msgcodec.util.LimitInputStream;

/**
 * An input stream which lets an application read primitive Blink data types.
 *
 * @see BlinkOutputStream
 * @author mikael.brannstrom
 *
 */
public class BlinkInputStream extends LimitInputStream  {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int UTF8_MAX_CHARS_PER_BYTE = 4;

    /** Maximum length of parsed binary data (including string). */
    private int maxBinarySize = 10 * 1048576; // 10 MB

    public BlinkInputStream(InputStream in) {
        super(in);
    }

    /** The maximum size of binary data that is accepted.
     * This limit exists as a safe guard to avoid OutOfMemoryError in case of malformed input.
     * <p>Default value is 10 MB (10 048 576 bytes)
     *
     * @param maxBinarySize the limit in bytes, or -1 for no limit.
     */
    public void setMaxBinarySize(int maxBinarySize) {
        this.maxBinarySize = maxBinarySize;
    }

    /** Read a signed 8-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte readInt8() throws IOException {
        return (byte) readSignedVLC();
    }
    /** Read a signed 16-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public short readInt16() throws IOException {
        return (short) readSignedVLC();
    }
    /** Read a signed 32-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public int readInt32() throws IOException {
        return (int) readSignedVLC();
    }
    /** Read a signed 64-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readInt64() throws IOException {
        return readSignedVLC();
    }

    /** Read an unsigned 8-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte readUInt8() throws IOException {
        return (byte) readUnsignedVLC();
    }
    /** Read an unsigned 16-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public short readUInt16() throws IOException {
        return (short) readUnsignedVLC();
    }
    /** Read an unsigned 32-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public int readUInt32() throws IOException {
        return (int) readUnsignedVLC();
    }
    /** Read an unsigned 64-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readUInt64() throws IOException {
        return readUnsignedVLC();
    }

    /** Read a nullable signed 8-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Byte readInt8Null() throws IOException {
        int b1 = read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (byte) readSignedVLC(b1);
        }
    }
    /** Read a nullable signed 16-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Short readInt16Null() throws IOException {
        int b1 = read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (short) readSignedVLC(b1);
        }
    }
    /** Read a nullable signed 32-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Integer readInt32Null() throws IOException {
        int b1 = read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (int) readSignedVLC(b1);
        }
    }
    /** Read a nullable signed 64-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Long readInt64Null() throws IOException {
        return readSignedVLCNull();
    }

    /** Read a nullable unsigned 8-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Byte readUInt8Null() throws IOException {
        int b1 = read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (byte) readUnsignedVLC(b1);
        }
    }
    /** Read a nullable unsigned 16-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Short readUInt16Null() throws IOException {
        int b1 = read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (short) readUnsignedVLC(b1);
        }
    }
    /** Read a nullable unsigned 32-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Integer readUInt32Null() throws IOException {
        int b1 = read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return (int) readUnsignedVLC(b1);
        }
    }
    /** Read a nullable unsigned 64-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Long readUInt64Null() throws IOException {
        return readUnsignedVLCNull();
    }

    /** Read a signed big integer.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigInteger readBigInt() throws IOException {
        return readSignedBigVLC();
    }
    /** Read a nullable signed big integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigInteger readBigIntNull() throws IOException {
        return readSignedBigVLCNull();
    }

    /** Read a 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is read as a 64-bit floating point number.
     *
     * @return the value
     * @see #readFloat64()
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public float readFloat32() throws IOException {
        return (float) readFloat64();
    }

    /** Read a nullable 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is read as a 64-bit floating point number.
     *
     * @return the value, or null.
     * @see #readFloat64()
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Float readFloat32Null() throws IOException {
        Double value = readFloat64Null();
        if (value == null) {
            return null;
        } else {
            return value.floatValue();
        }
    }

    /** Read a 64-bit floating point number.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public double readFloat64() throws IOException {
        long value = readUInt64();
        return Double.longBitsToDouble(value);
    }
    /** Read a nullable 64-bit floating point number.
     * @return the value, or null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Double readFloat64Null() throws IOException {
        Long value = readUInt64Null();
        if (value == null) {
            return null;
        } else {
            return Double.longBitsToDouble(value);
        }
    }

    /** Read a decimal number.
     * @return the value, not null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigDecimal readDecimal() throws IOException {
        int exp = readInt8();
        long mantissa = readInt64();
        return BigDecimal.valueOf(mantissa, -exp);
    }
    /** Read a nullable decimal number.
     * @return the value, or null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigDecimal readDecimalNull() throws IOException {
        Byte exp = readInt8Null();
        if (exp == null) {
            return null;
        } else {
            long mantissa = readInt64();
            return BigDecimal.valueOf(mantissa, -exp.intValue());
        }
    }

    /** Read a big decimal number.
     * @return the value, not null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigDecimal readBigDecimal() throws IOException {
        int exp = readInt32();
        BigInteger mantissa = readBigInt();
        return new BigDecimal(mantissa, -exp);
    }
    /** Read a nullable big decimal number.
     * @return the value, or null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigDecimal readBigDecimalNull() throws IOException {
        Integer exp = readInt32Null();
        if (exp == null) {
            return null;
        } else {
        	BigInteger mantissa = readBigInt();
            return new BigDecimal(mantissa, -exp.intValue());
        }
    }

    /** Read a boolean value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public boolean readBoolean() throws IOException {
        return readUInt8() != 0;
    }
    /** Read a nullable boolean value.
     * @return the value, or null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Boolean readBooleanNull() throws IOException {
        int b = read();
        if (b == 0xc0) {
            return null;
        } else {
            return b != 0;
        }
    }

    /** Read a unicode string.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8() throws IOException {
        return readStringUTF8(-1);
    }
    /** Read a nullable unicode string.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8Null() throws IOException {
        return readStringUTF8Null(-1);
    }
    /** Read a unicode string.
     * @param maxLength the maximum string length (characters) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8(int maxLength) throws IOException {
        final int maxBinaryLength = maxLength < 0 ? -1 : maxLength * UTF8_MAX_CHARS_PER_BYTE;
        byte[] bytes = readBinary(maxBinaryLength);
        String value = new String(bytes, UTF8); // TODO: use a cache?
        if (maxLength >= 0 && value.length() > maxLength) {
            throw new DecodeException("String length (" + value.length() + ") exceeds limit (" + maxLength + ")");
        }
        return value;
    }
    /** Read a nullable unicode string.
     * @param maxLength the maximum string length (characters) that is allowed, or -1 for no limit.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8Null(int maxLength) throws IOException {
        final int maxBinaryLength = maxLength < 0 ? -1 : maxLength * UTF8_MAX_CHARS_PER_BYTE;
        byte[] bytes = readBinaryNull(maxBinaryLength);
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
    /** Read a binary value.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinary() throws IOException {
        return readBinary(-1);
    }
    /** Read a nullable binary value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinaryNull() throws IOException {
        return readBinaryNull(-1);
    }
    /** Read a binary value.
     * @param maxLength the maximum binary length (bytes) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinary(int maxLength) throws IOException {
        int size = readUInt32();
        if (size < 0) {
            throw new DecodeException("Cannot read binary larger than " + Integer.MAX_VALUE + " bytes.");
        }
        if (size > maxLength && maxLength >= 0) {
            throw new DecodeException("Binary length (" + size + ") exceeds limit (" + maxLength + ")");
        }
        if (size > maxBinarySize && maxBinarySize >= 0) {
            throw new DecodeException("Binary size (" + size + ") exceeds limit (" + maxBinarySize + ")");
        }
        byte[] value = new byte[size];
        read(value);
        return value;
    }

    /** Read a nullable binary value.
     * @param maxLength the maximum binary length (bytes) that is allowed, or -1 for no limit.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinaryNull(int maxLength) throws IOException {
        Integer sizeObj = readUInt32Null();
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
        if (size > maxBinarySize && maxBinarySize >= 0) {
            throw new DecodeException("Binary size (" + size + ") exceeds limit (" + maxBinarySize + ")");
        }
        byte[] value = new byte[size];
        read(value);
        return value;
    }

    /** Read a nullable signed variable-length code value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Long readSignedVLCNull() throws IOException {
        int b1 = read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return readSignedVLC(b1);
        }
    }
    /** Read a signed variable-length code value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readSignedVLC() throws IOException {
        return readSignedVLC(read());
    }

    /** Read a signed VLC.
     *
     * @param b1 the first byte read.
     * @return the parsed value.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    private long readSignedVLC(int b1) throws IOException {
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
            int b2 = read();
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
                value |= (0xffL & read()) << (i * 8);
            }
            if (((value >> ((size-1) * 8)) & 0x80) != 0) {
                // negative
                value |= -1L << (size * 8);
            }
            return value;
        }
    }

    /** Read a nullable unsigned big variable-length code value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigInteger readSignedBigVLC() throws IOException {
    	BigInteger value = readSignedBigVLCNull();
    	if (value == null) {
            throw new DecodeException("Found null (0xc0) while parsing a non-nullable VLC integer");
    	}
    	return value;
    }

    /** Read a nullable unsigned big variable-length code value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
   public BigInteger readSignedBigVLCNull() throws IOException {
	   int b1 = read();
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
           int b2 = read();
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
                bytes[i] = (byte) read();
           }
           return new BigInteger(bytes);
       }
   }

    /** Read a nullable unsigned variable-length code value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Long readUnsignedVLCNull() throws IOException {
        int b1 = read();
        if (b1 == 0xc0) {
            return null;
        } else {
            return readUnsignedVLC(b1);
        }
    }
    /** Read a signed variable-length code value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readUnsignedVLC() throws IOException {
        return readUnsignedVLC(read());
    }

    /** Read an unsigned VLC.
     *
     * @param b1 the first byte read.
     * @return the parsed value.
     * @throws IOException
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    private long readUnsignedVLC(int b1) throws IOException {
        if ((0x80 & b1) == 0) {
            // single byte
            return 0x7fL & b1;
        } else if ((0xc0 & b1) == 0x80) {
            // two bytes
            int b2 = read();
            // positive
            return (0x3fL & b1) | ((0xffL & b2) << 6);
        } else {
            int size = 0x3f & b1;
            if (size == 0) {
                throw new DecodeException("Found null (0xc0) while parsing a non-nullable VLC integer");
            }
            long value = 0;
            for (int i=0; i<size; i++) {
                value |= (0xffL & read()) << (i * 8);
            }
            return value;
        }
    }

}
