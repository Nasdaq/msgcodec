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
package com.cinnober.msgcodec.blink;

import static org.junit.Assert.*;
import java.util.Arrays;

/**
 * @author mikael.brannstrom
 *
 */
public class TestUtil {

    private static final char[] HEX_CHAR = new char[]
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static final String toHex(byte[] data) {
        StringBuilder str = new StringBuilder(data.length * 3);

        for (int i = 0; i < data.length; i++) {
            if (i != 0) {
                if (i % 16 == 0) {
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

    public static void assertEquals(byte[] expected, byte[] actual) {
        assertEquals(null, expected, actual);
    }
    public static void assertEquals(String str, byte[] expected, byte[] actual) {
        if(expected == null && actual == null)
            return;
        boolean failed = false;
        if(expected == null || actual == null || expected.length != actual.length) {
            failed = true;
        } else {
            for(int i=0; i<expected.length; i++) {
                if(expected[i] != actual[i]) {
                    failed = true;
                    break;
                }
            }
        }
        if(!failed)
            return;
        fail((str != null ? str : "")+" expected: <"+Arrays.toString(expected)+"> but was: <"+Arrays.toString(actual)+">");
    }

}
