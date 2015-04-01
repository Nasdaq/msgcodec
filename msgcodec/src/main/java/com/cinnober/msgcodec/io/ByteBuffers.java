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
package com.cinnober.msgcodec.io;

import java.nio.ByteBuffer;

/**
 * Utilities for ByteBuffers and binary data.
 *
 * @author mikael.brannstrom
 *
 */
public class ByteBuffers {

    private static final char[] HEX_CHAR = new char[]
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Create a hex dump string of the specified binary data.
     * Default word and group sizes are 1 and 8 bytes respectively.
     *
     * <p>Thread safe: The buffer's position and limit will not be modified.
     *
     * @param data the binary data, not null. Data between position and limit will be used.
     * @return the hex dump string.
     */
    public static String toHex(ByteBuffer data) {
        return toHex(data, data.position(), data.limit());
    }

    /**
     * Create a hex dump string of the specified binary data.
     * Default word and group sizes are 1 and 8 bytes respectively.
     *
     * <p>Thread safe: The buffer's position and limit will not be modified.
     *
     * @param data the binary data, not null. Data between position and limit will be used.
     * @param position the position to use, instead of the position of the buffer.
     * @param limit the limit to use, instead of the limit of the buffer.
     * @return the hex dump string.
     */
    public static String toHex(ByteBuffer data, int position, int limit) {
        return toHex(data, position, limit, 1, 8, 0);
    }

    /**
     * Create a hex dump string of the specified binary data.
     *
     * <p>Thread safe: The buffer's position and limit will not be modified.
     *
     * @param data the binary data, not null. Data between position and limit will be used.
     * @param position the position to use, instead of the position of the buffer.
     * @param limit the limit to use, instead of the limit of the buffer.
     * @param wordSize the size of a "word" in bytes, which will be grouped with a space, or zero for no grouping.
     * @param groupSize the size of a "group" in bytes, which will be grouped with two spaces, or zero for no grouping.
     * @param lineSize the size of a line in bytes, which will be grouped with a new line, or zero for no grouping.
     * @return the hex dump string.
     */
    public static String toHex(ByteBuffer data, int position, int limit, int wordSize, int groupSize, int lineSize) {
        final int length = limit - position;
        StringBuilder str = new StringBuilder(length * 3 + length / 8);
        for (int i = 0; i < length; i++) {
            appendHex(data.get(position++), i, str, wordSize, groupSize, lineSize);
        }
        return str.toString();
    }

    static void validateHexFormatSizes(int wordSize, int groupSize, int lineSize) {
        if (lineSize < 0) {
            throw new IllegalArgumentException("Illegal lineSize; negative number");
        }
        if (groupSize < 0) {
            throw new IllegalArgumentException("Illegal groupSize; negative number");
        }
        if (wordSize < 0) {
            throw new IllegalArgumentException("Illegal wordSize; negative number");
        }
        if (lineSize != 0) {
            if (groupSize != 0 && lineSize % groupSize != 0) {
                throw new IllegalArgumentException("Illegal lineSize; must be a multiple of groupSize");
            }
            if (wordSize != 0 && lineSize % wordSize != 0) {
                throw new IllegalArgumentException("Illegal lineSize; must be a multiple of wordSize");
            }
        }
        if (groupSize != 0) {
            if (wordSize != 0 && groupSize % wordSize != 0) {
                throw new IllegalArgumentException("Illegal groupSize; must be a multiple of wordSize");
            }
        }
    }

    /**
     * Append hex to the string builder.
     *
     * @param b the binary data
     * @param index the index of the binary data (zero based)
     * @param appendTo the string builder to append hex string to
     * @param wordSize the size of a "word" in bytes, which will be grouped with a space, or zero for no grouping.
     * @param groupSize the size of a "group" in bytes, which will be grouped with two spaces, or zero for no grouping.
     * @param lineSize the size of a line in bytes, which will be grouped with a new line, or zero for no grouping.
     */
    static void appendHex(byte b, int index, StringBuilder appendTo, int wordSize, int groupSize, int lineSize) {
        if (index != 0) {
            if (lineSize != 0 && index % lineSize == 0) {
                appendTo.append('\n');
            } else if (groupSize != 0 && index % groupSize == 0) {
                appendTo.append("  ");
            } else if (wordSize != 0 && index % wordSize == 0) {
                appendTo.append(' ');
            }
        }
        appendTo.append((HEX_CHAR[(b & 0xf0) >> 4]))
           .append((HEX_CHAR[b & 0xf]));
    }

    /**
     * Copy bytes from a byte buffer to a byte array.
     *
     * <p><b>Note:</b> Position and limit in the buffers are modified in relative put/get operations.
     * This is not thread safe. To be safe against position and limit modifications, first duplicate the buffer:
     * <pre>
     * copy(srcBuf.duplicate(), srcIndex, dstArr, dstIndex, length);
     * </pre>
     *
     * @param srcBuf the buffer to read from, not null.
     * @param srcIndex the index in the source buffer
     * @param dstArr where bytes will be written, not null.
     * @param dstIndex the index int the destination byte array
     * @param length the number of bytes to copy
     */
    public static void copy(ByteBuffer srcBuf, int srcIndex, byte[] dstArr, int dstIndex, int length) {
        srcBuf.limit(srcIndex+length).position(srcIndex);
        srcBuf.get(dstArr, dstIndex, length);
    }

    /**
     * Copy bytes from a byte array to a byte buffer.
     *
     * <p><b>Note:</b> Position and limit in the buffers are modified in relative put/get operations.
     * This is not thread safe. To be safe against position and limit modifications, first duplicate the buffer:
     * <pre>
     * copy(srcArr, srcIndex, dstBuf.duplicate(), dstIndex, length);
     * </pre>
     *
     * @param srcArr the byte array to read from, not null.
     * @param srcIndex the index in the source byte array.
     * @param dstBuf where bytes will be written, not null.
     * @param dstIndex the index int the destination buffer.
     * @param length the number of bytes to copy
     */
    public static void copy(byte[] srcArr, int srcIndex, ByteBuffer dstBuf, int dstIndex, int length) {
        dstBuf.limit(dstIndex+length).position(dstIndex);
        dstBuf.put(srcArr, srcIndex, length);
    }

    /**
     * Copy bytes between two buffers.
     *
     * <p><b>Note:</b> Position and limit in the buffers are modified in relative put/get operations.
     * This is not thread safe. To be safe against position and limit modifications, first duplicate the buffers:
     * <pre>
     * copy(srcBuf.duplicate(), srcIndex, dstBuf.duplicate(), dstIndex, length);
     * </pre>
     *
     * @param srcBuf buffer to copy bytes from, not null.
     * @param srcIndex offset in srcBuf
     * @param dstBuf buffer to copy bytes to, not null. May refer to the same buffer as srcBuf.
     * @param dstIndex offset in dstBuf
     * @param length number of bytes to copy
     */
    public static void copy(ByteBuffer srcBuf, int srcIndex, ByteBuffer dstBuf, int dstIndex, int length) {
        ByteBuffer src = srcBuf;
        ByteBuffer dst = dstBuf == srcBuf ? dstBuf.duplicate() : dstBuf;
        src.limit(srcIndex+length).position(srcIndex);
        dst.limit(dstIndex+length).position(dstIndex);
        dst.put(src);
    }

    /**
     * Shift (copy) bytes in the buffer to the right.
     *
     * <p>Equivalent of <code>copy(buffer, index, buffer, index+distance, length)</code>.
     *
     * <p><b>Note:</b> Position and limit in the buffers are modified in relative put/get operations.
     * This is not thread safe. To be safe against position and limit modifications, first duplicate the buffers:
     * <pre>
     * shiftRight(buffer.duplicate(), index, length, distance);
     * </pre>
     *
     * @param buffer the buffer, not null
     * @param index the start index of the bytes to be copied
     * @param length the number of bytes to be copied
     * @param distance the number of positions to move the bytes to the right.
     */
    public static void shiftRight(ByteBuffer buffer, int index, int length, int distance) {
        copy(buffer, index, buffer, index+distance, length);
    }

    /**
     * Shift (copy) bytes in the buffer to the left.
     *
     * <p>Equivalent of <code>copy(buffer, index, buffer, index-distance, length)</code>.
     *
     * <p><b>Note:</b> Position and limit in the buffers are modified in relative put/get operations.
     * This is not thread safe. To be safe against position and limit modifications, first duplicate the buffers:
     * <pre>
     * shiftLeft(buffer.duplicate(), index, length, distance);
     * </pre>
     *
     * @param buffer the buffer, not null
     * @param index the start index of the bytes to be copied
     * @param length the number of bytes to be copied
     * @param distance the number of positions to move the bytes to the left.
     */
    public static void shiftLeft(ByteBuffer buffer, int index, int length, int distance) {
        copy(buffer, index, buffer, index-distance, length);
    }
}
