/*
 * Copyright (c) 2014 Cinnober Financial Technology AB, Stockholm,
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

/**
 * Utilities for byte arrays and binary data.
 *
 * @author mikael.brannstrom
 */
public class ByteArrays {
    /**
     * Create a hex dump string of the specified binary data.
     *
     * @param data the binary data, not null.
     * @return the hex dump string.
     */
    public static String toHex(byte[] data) {
        return toHex(data, 0, data.length);
    }

    /**
     * Create a hex dump string of the specified binary data.
     *
     * @param data the binary data, not null.
     * @param offset start of bytes in the byte array.
     * @param length number of bytes to use in the byte array.
     * @return the hex dump string.
     */
    public static String toHex(byte[] data, int offset, int length) {
        StringBuilder str = new StringBuilder(length * 3 + length / 8);
        for (int i = 0; i < length; i++) {
            ByteBuffers.appendHex(data[offset++], i, str);
        }
        return str.toString();
    }
}
