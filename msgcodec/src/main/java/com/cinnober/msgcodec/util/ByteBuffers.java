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
package com.cinnober.msgcodec.util;

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
     *
     * @param data the binary data, not null. Data between position and limit will be used.
     * @return the hex dump string.
     */
    public static String toHex(ByteBuffer data) {
        final int length = data.remaining();
        StringBuilder str = new StringBuilder(length * 3 + length / 8);
        for (int i = 0; i < length; i++) {
            appendHex(data.get(data.position() + i), i, str);
        }
        return str.toString();
    }
    /**
     * Create a hex dump string of the specified binary data.
     *
     * @param data the binary data, not null.
     * @return the hex dump string.
     */
    public static String toHex(byte[] data) {
        final int length = data.length;
        StringBuilder str = new StringBuilder(length * 3 + length / 8);
        for (int i = 0; i < length; i++) {
            appendHex(data[i], i, str);
        }
        return str.toString();
    }
    /**
     * Append hex to the string builder.
     *
     * @param b the binary data
     * @param index the index of the binary data (zero based)
     * @param appendTo the string builder to append hex string to
     */
    private static void appendHex(byte b, int index, StringBuilder appendTo) {
        if (index != 0) {
            if (index % 16 == 0) {
                appendTo.append('\n');
            } else if (index % 8 == 0) {
                appendTo.append("  ");
            } else {
                appendTo.append(' ');
            }
        }
        appendTo.append((HEX_CHAR[(b & 0xf0) >> 4]))
           .append((HEX_CHAR[b & 0xf]));
    }
}
