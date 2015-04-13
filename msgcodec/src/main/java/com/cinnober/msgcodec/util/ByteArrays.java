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

package com.cinnober.msgcodec.util;

/**
 * Utilities for byte arrays and binary data.
 *
 * @author mikael.brannstrom
 */
public class ByteArrays {
    /**
     * Create a hex dump string of the specified binary data.
     * Default word and group sizes are 1 and 8 bytes respectively.
     *
     * @param data the binary data, not null.
     * @return the hex dump string.
     */
    public static String toHex(byte[] data) {
        return toHex(data, 0, data.length);
    }

    /**
     * Create a hex dump string of the specified binary data.
     * Default word and group sizes are 1 and 8 bytes respectively.
     *
     * @param data the binary data, not null.
     * @param offset start of bytes in the byte array.
     * @param length number of bytes to use in the byte array.
     * @return the hex dump string.
     */
    public static String toHex(byte[] data, int offset, int length) {
        return toHex(data, offset, length, 1, 8, 0);
    }


    /**
     * Create a hex dump string of the specified binary data.
     *
     * @param data the binary data, not null.
     * @param offset start of bytes in the byte array.
     * @param length number of bytes to use in the byte array.
     * @param wordSize the size of a "word" in bytes, which will be grouped with a space, or zero for no grouping.
     * @param groupSize the size of a "group" in bytes, which will be grouped with two spaces, or zero for no grouping.
     * @param lineSize the size of a line in bytes, which will be grouped with a new line, or zero for no grouping.
     * @return the hex dump string.
     */
    public static String toHex(byte[] data, int offset, int length, int wordSize, int groupSize, int lineSize) {
        StringBuilder str = new StringBuilder(length * 3 + length / 8);
        for (int i = 0; i < length; i++) {
            ByteBuffers.appendHex(data[offset++], i, str, wordSize, groupSize, lineSize);
        }
        return str.toString();
    }

}
