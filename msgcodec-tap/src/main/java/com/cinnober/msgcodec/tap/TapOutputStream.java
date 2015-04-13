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
package com.cinnober.msgcodec.tap;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * An output stream which lets an application write primitive TAP data types.
 *
 * @see TapInputStream
 * @author mikael.brannstrom
 *
 */
public class TapOutputStream extends FilterOutputStream {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    private static final int MODEL_LENGTH_NON_COMPACT = 0x40;
    private static final int MODEL_LENGTH_BYTE = 0x00;
    private static final int MODEL_LENGTH_SHORT = 0x01;
    private static final int MODEL_LENGTH_INT = 0x02;
    private static final int MODEL_DESCRIPTOR = 0x80;


    public TapOutputStream(OutputStream out) {
        super(out);
    }

    /** Write the null (0x00) value.
     *
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeNull() throws IOException {
        out.write(0x00);
    }

    /** Write a byte (8-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeByte(byte value) throws IOException {
        out.write(0xff & value);
    }

    /** Write a variable-length short (16-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeVarShort(short value) throws IOException {
        writeVarLong(value, sizeOfVarShort(value));
    }

    /** Write a variable-length integer (32-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeVarInt(int value) throws IOException {
        writeVarLong(value, sizeOfVarInt(value));
    }

    /** Write a variable-length long (64-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeVarLong(long value) throws IOException {
        writeVarLong(value, sizeOfVarLong(value));
    }

    /** Write a variable-length long (64-bit) value with the specified length.
     *
     * @param value the value to be written
     * @param size the number of bytes written in the range [1, 10].
     * @throws IOException if the underlying stream throws an exception
     */
    @SuppressWarnings("fallthrough")
    private void writeVarLong(long value, int size) throws IOException {
        switch (size) {
        case 10:
            out.write(((int) (value >>> 63) & 0x7F) | 0x80);
        case 9:
            out.write(((int) (value >>> 56) & 0x7F) | 0x80);
        case 8:
            out.write(((int) (value >>> 49) & 0x7F) | 0x80);
        case 7:
            out.write(((int) (value >>> 42) & 0x7F) | 0x80);
        case 6:
            out.write(((int) (value >>> 35) & 0x7F) | 0x80);
        case 5:
            out.write(((int) (value >>> 28) & 0x7F) | 0x80);
        case 4:
            out.write(((int) (value >>> 21) & 0x7F) | 0x80);
        case 3:
            out.write(((int) (value >>> 14) & 0x7F) | 0x80);
        case 2:
            out.write(((int) (value >>> 7) & 0x7F) | 0x80);
        case 1:
            out.write((int) (value & 0x7F));
            break;
        default:
            throw new IllegalArgumentException("Illegal size: " + size);
        }
    }
    /** Returns the size of a variable-length encoded long (64-bit) value.
     *
     * @param value the value to be encoded
     * @return the number of bytes, a number in the range [1, 10].
     */
    public static int sizeOfVarLong(long value) {
        if (value < 0) {
            return 10;
        } else if (value < 1 << 7) {
            return 1;
        } else if (value < 1 << 14) {
            return 2;
        } else if (value < 1 << 21) {
            return 3;
        } else if (value < 1L << 28) {
            return 4;
        } else if (value < 1L << 35) {
            return 5;
        } else if (value < 1L << 42) {
            return 6;
        } else if (value < 1L << 49) {
            return 7;
        } else if (value < 1L << 56) {
            return 8;
        } else {
            return 9;
        }
    }
    /** Returns the size of a variable-length encoded short (16-bit) value.
     *
     * @param value the value to be encoded
     * @return the number of bytes, a number in the range [1, 3].
     */
    public static int sizeOfVarShort(short value) {
        return sizeOfVarLong(0xffffL & value);
    }
    /** Returns the size of a variable-length encoded integer (32-bit) value.
    *
    * @param value the value to be encoded
    * @return the number of bytes, a number in the range [1, 5].
    */
    public static int sizeOfVarInt(int value) {
        return sizeOfVarLong(0xffffffffL & value);
    }

    /** Write a boolean value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBoolean(boolean value) throws IOException {
        out.write(value ? 1 : 0);
    }
    /** Write a short (16-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeShort(short value) throws IOException {
        out.write((value >>> 8) & 0xFF);
        out.write((value >>> 0) & 0xFF);
    }
    /** Write a integer (32-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeInt(int value) throws IOException {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write((value >>> 0) & 0xFF);
    }
    /** Write a long (64-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeLong(long value) throws IOException {
        out.write((int)(value >>> 56) & 0xFF);
        out.write((int)(value >>> 48) & 0xFF);
        out.write((int)(value >>> 40) & 0xFF);
        out.write((int)(value >>> 32) & 0xFF);
        out.write((int)(value >>> 24) & 0xFF);
        out.write((int)(value >>> 16) & 0xFF);
        out.write((int)(value >>> 8) & 0xFF);
        out.write((int)(value >>> 0) & 0xFF);
    }

    /** Write a float (32-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeFloat(float value) throws IOException {
        writeInt(Float.floatToIntBits(value));
    }

    /** Write a double (64-bit) value.
     *
     * @param value the value to be written
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeDouble(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }
    /** Write a ISO Latin-1 char.
    *
    * @param value the value to be written
    * @throws IOException if the underlying stream throws an exception
    */
    public void writeCharLatin1(char value) throws IOException {
        out.write(0xff & value);
    }

    /** Write a "model" length value.
     *
     * @param length the length to be written
     * @param descriptor true if the descriptor bit should be set, otherwise false.
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeModelLength(int length, boolean descriptor) throws IOException {
        if (length < 0) {
            writeNull();
            return;
        }
        int descriptorBit = descriptor ? MODEL_DESCRIPTOR : 0;
        if (length + 1 < 0x3f) { // NOTE: it should be <= 0x3f, but we want to mimic original TAP
            out.write(length + 1 | descriptorBit);
        } else if (length + 2 < 0xff) {
            // NOTE: +2 is somewhat odd, but we want to mimic original TAP
            out.write(MODEL_LENGTH_BYTE | MODEL_LENGTH_NON_COMPACT | descriptorBit);
            out.write((length + 2) & 0xff);
        } else if (length + 3 < Short.MAX_VALUE) {
            // NOTE: +3 is somewhat odd, but we want to mimic original TAP
            out.write(MODEL_LENGTH_SHORT | MODEL_LENGTH_NON_COMPACT | descriptorBit);
            writeShort((short)(length + 3));
        } else {
            // NOTE: +5 is somewhat odd, but we want to mimic original TAP
            out.write(MODEL_LENGTH_INT | MODEL_LENGTH_NON_COMPACT | descriptorBit);
            writeInt((length + 5));
        }
    }

    /** Returns the size of a "model" encoded length in bytes.
     *
     * @param length the length to be encoded.
     * @return the number of bytes, in the range [1, 5].
     */
    public static int sizeOfModelLength(int length) {
        if (length < 0) {
            return 1; // null
        } else if (length + 1 < 0x3f) {
            return 1;
        } else if (length + 2 < 0xff) {
            return 2;
        } else if (length + 3 < Short.MAX_VALUE) {
            return 3;
        } else {
            return 5;
        }
    }


    /** Write a binary value.
     *
     * @param value the value to be written, or null
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeBinary(byte[] value) throws IOException {
        if (value == null) {
            writeNull();
        } else {
            writeModelLength(value.length, false);
            out.write(value);
        }
    }

    /** Write a ISO-LATIN-1 string value.
     *
     * @param value the value to be written, or null
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeStringLatin1(String value) throws IOException {
        writeBinary(value == null ? null : value.getBytes(LATIN1));
    }
    /** Write UTF-8 string value.
     *
     * @param value the value to be written, or null
     * @throws IOException if the underlying stream throws an exception
     */
    public void writeStringUTF8(String value) throws IOException {
        writeBinary(value == null ? null : value.getBytes(UTF8));
    }

}
