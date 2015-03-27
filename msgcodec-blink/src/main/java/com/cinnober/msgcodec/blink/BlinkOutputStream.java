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
import com.cinnober.msgcodec.util.OutputStreamSink;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * An output stream which lets an application write primitive Blink data types.
 *
 * @see BlinkInputStream
 * @author mikael.brannstrom
 *
 */
public class BlinkOutputStream extends FilterOutputStream {

    private final ByteSink sink;
    public BlinkOutputStream(OutputStream out) {
        super(out);
        sink = new OutputStreamSink(out);
    }

    ByteSink sink() {
        return sink;
    }

    /**
     * Write the null (0xc0) value.
     *
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeNull() throws IOException {
        BlinkOutput.writeNull(sink);
    }

    /**
     * Write a signed 8-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt8(byte value) throws IOException {
        BlinkOutput.writeInt8(sink, value);
    }
    /**
     * Write a signed 16-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt16(short value) throws IOException {
        BlinkOutput.writeInt16(sink, value);
    }
    /**
     * Write a signed 32-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt32(int value) throws IOException {
        BlinkOutput.writeInt32(sink, value);
    }
    /**
     * Write a signed 64-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt64(long value) throws IOException {
        BlinkOutput.writeInt64(sink, value);
    }
    /**
     * Write an unsigned 8-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt8(byte value) throws IOException {
        BlinkOutput.writeUInt8(sink, value);
    }
    /**
     * Write an unsigned 16-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt16(short value) throws IOException {
        BlinkOutput.writeUInt16(sink, value);
    }
    /**
     * Write an unsigned 32-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt32(int value) throws IOException {
        BlinkOutput.writeUInt32(sink, value);
    }
    /**
     * Write an unsigned 64-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt64(long value) throws IOException {
        BlinkOutput.writeUInt64(sink, value);
    }
    /**
     * Write a nullable signed 8-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt8Null(Byte value) throws IOException {
        BlinkOutput.writeInt8Null(sink, value);
    }
    /**
     * Write a nullable signed 16-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt16Null(Short value) throws IOException {
        BlinkOutput.writeInt16Null(sink, value);
    }
    /**
     * Write a nullable signed 32-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt32Null(Integer value) throws IOException {
        BlinkOutput.writeInt32Null(sink, value);
    }
    /**
     * Write a nullable signed 64-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt64Null(Long value) throws IOException {
        BlinkOutput.writeInt64Null(sink, value);
    }
    /**
     * Write a nullable unsigned 8-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt8Null(Byte value) throws IOException {
        BlinkOutput.writeUInt8Null(sink, value);
    }
    /**
     * Write a nullable unsigned 16-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt16Null(Short value) throws IOException {
        BlinkOutput.writeUInt16Null(sink, value);
    }
    /**
     * Write a nullable unsigned 32-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt32Null(Integer value) throws IOException {
        BlinkOutput.writeUInt32Null(sink, value);
    }
    /**
     * Write a nullable unsigned 64-bit integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUInt64Null(Long value) throws IOException {
        BlinkOutput.writeUInt64Null(sink, value);
    }

    /**
     * Write a nullable signed big integer.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBigIntNull(BigInteger value) throws IOException {
        BlinkOutput.writeBigIntNull(sink, value);
    }
    /**
     * Write a signed big integer.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBigInt(BigInteger value) throws IOException {
        BlinkOutput.writeBigInt(sink, value);
    }

    /**
     * Write a 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is written as a 64-bit floating point number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     * @see #writeFloat64(double)
     */
    public void writeFloat32(float value) throws IOException {
        BlinkOutput.writeFloat32(sink, value);
    }
    /**
     * Write a nullable 32-bit floating point number.
     *
     * <p>Since the Blink protocol does not support 32-bit floating point numbers,
     * the value is written as a 64-bit floating point number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     * @see #writeFloat64Null(Double)
     */
    public void writeFloat32Null(Float value) throws IOException {
        BlinkOutput.writeFloat32Null(sink, value);
    }
    /**
     * Write a 64-bit floating point number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeFloat64(double value) throws IOException {
        BlinkOutput.writeFloat64(sink, value);
    }
    /**
     * Write a nullable 64-bit floating point number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeFloat64Null(Double value) throws IOException {
        BlinkOutput.writeFloat64Null(sink, value);
    }

    /**
     * Write a decimal number.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public void writeDecimal(BigDecimal value) throws IOException, NullPointerException {
        BlinkOutput.writeDecimal(sink, value);
    }
    /**
     * Write a nullable decimal number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeDecimalNull(BigDecimal value) throws IOException {
        BlinkOutput.writeDecimalNull(sink, value);
    }
    /**
     * Write a big decimal number.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public void writeBigDecimal(BigDecimal value) throws IOException, NullPointerException {
        BlinkOutput.writeBigDecimal(sink, value);
    }
    /**
     * Write a nullable big decimal number.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBigDecimalNull(BigDecimal value) throws IOException {
        BlinkOutput.writeBigDecimalNull(sink, value);
    }

    /**
     * Write a boolean.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBoolean(boolean value) throws IOException {
        BlinkOutput.writeBoolean(sink, value);
    }
    /**
     * Write a nullable boolean.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBooleanNull(Boolean value) throws IOException {
        BlinkOutput.writeBooleanNull(sink, value);
    }
    /**
     * Write a unicode string.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public void writeStringUTF8(String value) throws IOException {
        BlinkOutput.writeStringUTF8(sink, value);
    }
    /**
     * Write a nullable unicode string.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeStringUTF8Null(String value) throws IOException {
        BlinkOutput.writeStringUTF8Null(sink, value);
    }
    /**
     * Write binary data.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     * @throws NullPointerException if value is null
     */
    public void writeBinary(byte[] value) throws IOException {
        BlinkOutput.writeBinary(sink, value);
    }
    /**
     * Write nullable binary data.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBinaryNull(byte[] value) throws IOException {
        BlinkOutput.writeBinaryNull(sink, value);
    }
    /**
     * Write a signed big variable-length code value.
     *
     * @param value the value to be written, not null
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeSignedBigVLC(BigInteger value) throws IOException {
        BlinkOutput.writeSignedVLC(sink, value);
    }

    /**
     * Write a signed variable-length code value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeSignedVLC(long value) throws IOException {
        BlinkOutput.writeSignedVLC(sink, value);
    }
    /**
     * Write an unsigned variable-length code value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeUnsignedVLC(long value) throws IOException {
        BlinkOutput.writeUnsignedVLC(sink, value);
    }

    /**
     * Returns the size in bytes of a signed variable-length coded value.
     * @param value the value
     * @return the number of bytes, always in the range [1, 9].
     */
    public static final int sizeOfSignedVLC(long value) {
        return BlinkOutput.sizeOfSignedVLC(value);
    }

    /**
     * Returns the size in bytes of an unsigned variable-length coded value.
     * @param value the value
     * @return the number of bytes, always in the range [1, 9].
     */
    public static final int sizeOfUnsignedVLC(long value) {
        return BlinkOutput.sizeOfUnsignedVLC(value);
    }

}
