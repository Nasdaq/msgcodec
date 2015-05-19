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

package com.cinnober.msgcodec.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: javadoc
 * @author mikael.brannstrom
 */
public class ByteSourceInputStream extends InputStream {
    private final ByteSource src;

    public ByteSourceInputStream(ByteSource src) {
        this.src = src;
    }

    // TODO: javadoc, never returns -1, throws EOF instead
    @Override
    public int read() throws IOException {
        return src.read();
    }

    // TODO: javadoc, always returns len
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        src.read(b, off, len);
        return len;
    }

    // TODO: javadoc, always returns len
    @Override
    public int read(byte[] b) throws IOException {
        src.read(b);
        return b.length;
    }

}
