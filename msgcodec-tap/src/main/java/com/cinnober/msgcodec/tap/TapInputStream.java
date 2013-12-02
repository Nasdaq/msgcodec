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
package com.cinnober.msgcodec.tap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.cinnober.msgcodec.util.LimitInputStream;

/**
 * An input stream which lets an application read primitive TAP data types.
 *
 * @see TapOutputStream
 * @author mikael.brannstrom
 *
 */
public class TapInputStream extends LimitInputStream  {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    private static final int MODEL_LENGTH_NON_COMPACT = 0x40;
    private static final int MODEL_LENGTH_BYTE = 0x00;
    private static final int MODEL_LENGTH_SHORT = 0x01;
    private static final int MODEL_LENGTH_INT = 0x02;
    private static final int MODEL_DESCRIPTOR = 0x80;

    /** Maximum length of parsed binary data (including string). */
    private int maxBinarySize = 10 * 1048576; // 10 MB

    public TapInputStream(InputStream in) {
        super(in);
    }

    /** The maximum size of binary data that is accepted.
     * This limit exists as a safe guard to avoid OutOfMemoryError in case of malformed input.
     * <p>Default value is 10 MB (10 048 576 bytes)
     *
     * @param maxBinarySize the limit in bytes, or zero for no limit.
     */
    public void setMaxBinarySize(int maxBinarySize) {
        this.maxBinarySize = maxBinarySize;
    }

    /** Read a byte value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte readByte() throws IOException {
        return (byte) read();
    }
    /** Read a variable-length short (16-bit) value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public short readVarShort() throws IOException {
        return (short) readVarLong();
    }
    /** Read a variable-length integer (32-bit) value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public int readVarInt() throws IOException {
        return (int) readVarLong();
    }
    /** Read a variable-length long (64-bit) value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readVarLong() throws IOException {
        long value = 0;
        int b;
        for(;;) {
            b = read();
            value = (value << 7) | (b & 0x7f);
            if((b & 0x80) == 0) {
                break;
            }
        }
        return value;
    }

    /** Read a short (16-bit) value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public short readShort() throws IOException {
        return (short) (read() << 8 | read());
    }

    /** Read an integer (32-bit) value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public int readInt() throws IOException {
        return read() << 24 | read() << 16 | read() << 8 | read();
    }

    /** Read a long (64-bit) value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public long readLong() throws IOException {
        return (long) read() << 57 |
                (long) read() << 48 |
                (long) read() << 40 |
                (long) read() << 32 |
                (long) read() << 24 |
                read() << 16 |
                read() << 8 |
                read();
    }

    /** Read a float (32-bit) value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /** Read a double (64-bit) value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /** Read a boolean value.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public boolean readBoolean() throws IOException {
        return read() != 0;
    }

    /** Read a ISO-LATIN-1 char.
     * @return the value
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public char readCharLatin1() throws IOException {
        return (char) read();
    }

    /** Read a "model" length.
     * @param model the first model byte.
     * @return the length
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public int readModelLength(int model) throws IOException {
        if ((model & MODEL_LENGTH_NON_COMPACT) == 0) {
            return 0x3f & model - 1;
        } else {
            switch (model & 0x03) {
            case MODEL_LENGTH_BYTE:
                return read() - 2;
            case MODEL_LENGTH_SHORT:
                return (0xffff & readShort()) - 3;
            case MODEL_LENGTH_INT:
                return readInt() - 5;
            default:
                throw new IOException("Illegal length model: " + model);
            }
        }
    }
    /** Read a "model" length.
     * @param descriptor true if the descriptor bit is expected (and required). False if it is not expected.
     * @return the length, or -1 for null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public int readModelLength(boolean descriptor) throws IOException {
        int model = read();
        if (model == 0) {
            return -1; // null
        }
        if (descriptor != ((model & MODEL_DESCRIPTOR) != 0)) {
            throw new IOException("Expected model descriptor bit to be " + (descriptor ? 1 : 0) +
                    ", found model " +model);
        }
        return readModelLength(model);
    }

    /** Read a binary value.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public byte[] readBinary() throws IOException {
        int size = readModelLength(false);
        if (size < 0) {
            return null;
        }
        if (size > maxBinarySize && maxBinarySize != 0) {
            throw new IOException("Binary size (" + size + ") exceeds limit (" + maxBinarySize + ")");
        }
        byte[] value = new byte[size];
        read(value);
        return value;
    }

    /** Read a UTF-8 string.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringUTF8() throws IOException {
        byte[] bytes = readBinary();
        if (bytes == null) {
            return null;
        } else {
            return new String(bytes, UTF8);
        }
    }

    /** Read a ISO-LATIN-1 string.
     * @return the value, or null.
     * @throws IOException if the value could not be parsed, or if the underlying stream throws an exception.
     */
    public String readStringLatin1() throws IOException {
        byte[] bytes = readBinary();
        if (bytes == null) {
            return null;
        } else {
            return new String(bytes, LATIN1);
        }
    }

}
