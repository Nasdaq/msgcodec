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

/**
 *
 * @author mikael.brannstrom
 */
public interface ByteSink {
    void write(int b) throws IOException;

    default void write(byte[] b, int off, int len) throws IOException {
        for (int i=off; i<len; i++) {
            write(b[i]);
        }
    }
    default void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    default void write2(int b1, int b2) throws IOException {
        write(b1);
        write(b2);
    }
    default void write3(int b1, int b2, int b3) throws IOException {
        write(b1);
        write(b2);
        write(b3);
    }
    default void write4(int b1, int b2, int b3, int b4) throws IOException {
        write(b1);
        write(b2);
        write(b3);
        write(b4);
    }
    default void write5(int b1, int b2, int b3, int b4, int b5) throws IOException {
        write(b1);
        write(b2);
        write(b3);
        write(b4);
        write(b5);
    }
    default void write6(int b1, int b2, int b3, int b4, int b5, int b6) throws IOException {
        write(b1);
        write(b2);
        write(b3);
        write(b4);
        write(b5);
        write(b6);
    }
    default void write7(int b1, int b2, int b3, int b4, int b5, int b6, int b7) throws IOException {
        write(b1);
        write(b2);
        write(b3);
        write(b4);
        write(b5);
        write(b6);
        write(b7);
    }
    default void write8(int b1, int b2, int b3, int b4, int b5, int b6, int b7, int b8) throws IOException {
        write(b1);
        write(b2);
        write(b3);
        write(b4);
        write(b5);
        write(b6);
        write(b7);
        write(b8);
    }
}
