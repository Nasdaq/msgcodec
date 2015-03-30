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

}
