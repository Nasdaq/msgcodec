/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
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

package com.cinnober.msgcodec;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author mikael.brannstrom
 */
public interface ByteSource {
    static final Charset UTF8 = Charset.forName("UTF-8");

    int read() throws IOException;
    default void read(byte[] b, int off, int len) throws IOException {
        for (int i=off; i<len; i++) {
            b[i] = (byte) read();
        }
    }
    default void read(byte[] b) throws IOException {
        read(b, 0, b.length);
    }

    default String readStringUtf8(int len) throws IOException {
        byte[] data = new byte[len];
        read(data);
        return new String(data, UTF8);
    }
    default void skip(int len) throws IOException {
        for (int i=0; i<len; i++) {
            read();
        }
    }
}
