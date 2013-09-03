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
package com.cinnober.msgcodec.examples.util;


/**
 * @author mikael.brannstrom
 *
 */
public class Util {

    private static final char[] HEX_CHAR = new char[]
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static final String toHex(byte[] data) {
        StringBuilder str = new StringBuilder(data.length * 3);

        for (int i = 0; i < data.length; i++) {
            if (i != 0) {
                if (i % 8 == 0) {
                    str.append("\n");
                } else {
                    str.append(" ");
                }
            }
            str.append((HEX_CHAR[(data[i] & 0xf0) >> 4]))
               .append((HEX_CHAR[data[i] & 0xf]));
        }

        return str.toString();
    }
}
