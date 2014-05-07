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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.cinnober.msgcodec.util.LimitInputStream;

/**
 * An input stream which lets an application read primitive Blink data types.
 *
 * @see BlinkOutputStream
 * @author mikael.brannstrom
 *
 */
public class BlinkInputStream extends LimitInputStream  {

    /** Maximum length of parsed binary data (including string). */
    private int maxBinarySize = 10 * 1048576; // 10 MB

    public BlinkInputStream(InputStream in) {
        super(in);
    }

    private int sizeLimit(int maxLength) {
        if (maxLength < 0) {
            return maxBinarySize;
        } else if (maxBinarySize < 0) {
            return maxLength;
        } else {
            return maxBinarySize < maxLength ? maxBinarySize : maxLength;
        }
    }

    /**
     * The maximum size of binary data that is accepted.
     * This limit exists as a safe guard to avoid OutOfMemoryError in case of malformed input.
     * <p>Default value is 10 MB (10 048 576 bytes)
     *
     * @param maxBinarySize the limit in bytes, or -1 for no limit.
     */
    public void setMaxBinarySize(int maxBinarySize) {
        this.maxBinarySize = maxBinarySize;
    }

    /**
     * Read a signed 8-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte readInt8() throws IOException {
        return BlinkInput.readInt8(in);
    }
    /**
     * Read a signed 16-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public short readInt16() throws IOException {
        return BlinkInput.readInt16(in);
    }
    /**
     * Read a signed 32-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public int readInt32() throws IOException {
        return BlinkInput.readInt32(in);
    }
    /**
     * Read a signed 64-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readInt64() throws IOException {
        return BlinkInput.readInt64(in);
    }

    /**
     * Read an unsigned 8-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte readUInt8() throws IOException {
        return BlinkInput.readUInt8(in);
    }
    /**
     * Read an unsigned 16-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public short readUInt16() throws IOException {
        return BlinkInput.readUInt16(in);
    }
    /**
     * Read an unsigned 32-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public int readUInt32() throws IOException {
        return BlinkInput.readUInt32(in);
    }
    /**
     * Read an unsigned 64-bit integer.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readUInt64() throws IOException {
        return BlinkInput.readUInt64(in);
    }

    /**
     * Read a nullable signed 8-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Byte readInt8Null() throws IOException {
        return BlinkInput.readInt8Null(in);
    }
    /**
     * Read a nullable signed 16-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Short readInt16Null() throws IOException {
        return BlinkInput.readInt16Null(in);
    }
    /**
     * Read a nullable signed 32-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Integer readInt32Null() throws IOException {
        return BlinkInput.readInt32Null(in);
    }
    /**
     * Read a nullable signed 64-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Long readInt64Null() throws IOException {
        return BlinkInput.readInt64Null(in);
    }

    /**
     * Read a nullable unsigned 8-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Byte readUInt8Null() throws IOException {
        return BlinkInput.readUInt8Null(in);
    }
    /**
     * Read a nullable unsigned 16-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Short readUInt16Null() throws IOException {
        return BlinkInput.readUInt16Null(in);
    }
    /**
     * Read a nullable unsigned 32-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Integer readUInt32Null() throws IOException {
        return BlinkInput.readUInt32Null(in);
    }
    /**
     * Read a nullable unsigned 64-bit integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Long readUInt64Null() throws IOException {
        return BlinkInput.readUInt64Null(in);
    }

    /**
     * Read a signed big integer.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigInteger readBigInt() throws IOException {
        return BlinkInput.readBigInt(in);
    }
    /**
     * Read a nullable signed big integer.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigInteger readBigIntNull() throws IOException {
        return BlinkInput.readBigIntNull(in);
    }

    /**
     * Read a 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is read as a 64-bit floating point number.
     *
     * @return the value
     * @see #readFloat64()
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public float readFloat32() throws IOException {
        return BlinkInput.readFloat32(in);
    }

    /**
     * Read a nullable 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is read as a 64-bit floating point number.
     *
     * @return the value, or null.
     * @see #readFloat64()
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Float readFloat32Null() throws IOException {
        return BlinkInput.readFloat32Null(in);
    }

    /**
     * Read a 64-bit floating point number.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public double readFloat64() throws IOException {
        return BlinkInput.readFloat64(in);
    }
    /**
     * Read a nullable 64-bit floating point number.
     * @return the value, or null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Double readFloat64Null() throws IOException {
        return BlinkInput.readFloat64Null(in);
    }

    /**
     * Read a decimal number.
     * @return the value, not null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigDecimal readDecimal() throws IOException {
        return BlinkInput.readDecimal(in);
    }
    /**
     * Read a nullable decimal number.
     * @return the value, or null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigDecimal readDecimalNull() throws IOException {
        return BlinkInput.readDecimalNull(in);
    }

    /**
     * Read a big decimal number.
     * @return the value, not null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigDecimal readBigDecimal() throws IOException {
        return BlinkInput.readBigDecimal(in);
    }
    /**
     * Read a nullable big decimal number.
     * @return the value, or null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigDecimal readBigDecimalNull() throws IOException {
        return BlinkInput.readBigDecimalNull(in);
    }

    /**
     * Read a boolean value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public boolean readBoolean() throws IOException {
        return BlinkInput.readBoolean(in);
    }
    /**
     * Read a nullable boolean value.
     * @return the value, or null
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Boolean readBooleanNull() throws IOException {
        return BlinkInput.readBooleanNull(in);
    }

    /**
     * Read a unicode string.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8() throws IOException {
        return BlinkInput.readStringUTF8(in, maxBinarySize);
    }
    /**
     * Read a nullable unicode string.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8Null() throws IOException {
        return BlinkInput.readStringUTF8Null(in, maxBinarySize);
    }
    /**
     * Read a unicode string.
     * @param maxLength the maximum string length (bytes) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8(int maxLength) throws IOException {
        return BlinkInput.readStringUTF8(in, sizeLimit(maxLength));
    }
    /**
     * Read a nullable unicode string.
     * @param maxLength the maximum string length (characters) that is allowed, or -1 for no limit.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8Null(int maxLength) throws IOException {
        return BlinkInput.readStringUTF8Null(in, sizeLimit(maxLength));
    }
    /**
     * Read a binary value.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinary() throws IOException {
        return BlinkInput.readBinary(in, maxBinarySize);
    }
    /**
     * Read a nullable binary value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinaryNull() throws IOException {
        return BlinkInput.readBinaryNull(in, maxBinarySize);
    }
    /**
     * Read a binary value.
     * @param maxLength the maximum binary length (bytes) that is allowed, or -1 for no limit.
     * @return the value, not null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinary(int maxLength) throws IOException {
        return BlinkInput.readBinary(in, sizeLimit(maxLength));
    }

    /**
     * Read a nullable binary value.
     * @param maxLength the maximum binary length (bytes) that is allowed, or -1 for no limit.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinaryNull(int maxLength) throws IOException {
        return BlinkInput.readBinaryNull(in, sizeLimit(maxLength));
    }

    /**
     * Read a nullable signed variable-length code value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Long readSignedVLCNull() throws IOException {
        return BlinkInput.readSignedVLCNull(in);
    }
    /**
     * Read a signed variable-length code value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readSignedVLC() throws IOException {
        return BlinkInput.readSignedVLC(in);
    }

    /**
     * Read a nullable unsigned big variable-length code value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public BigInteger readSignedBigVLC() throws IOException {
        return BlinkInput.readSignedBigVLC(in);
    }

    /**
     * Read a nullable unsigned big variable-length code value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
   public BigInteger readSignedBigVLCNull() throws IOException {
       return BlinkInput.readSignedBigVLCNull(in);
   }

    /**
     * Read a nullable unsigned variable-length code value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public Long readUnsignedVLCNull() throws IOException {
        return BlinkInput.readUnsignedVLCNull(in);
    }
    /**
     * Read a signed variable-length code value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readUnsignedVLC() throws IOException {
        return BlinkInput.readUnsignedVLC(in);
    }
}
