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

import com.cinnober.msgcodec.io.InputStreams;
import com.cinnober.msgcodec.io.ByteSource;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: javadoc
 * @author mikael.brannstrom
 */
public class InputStreamSource implements ByteSource {

    private final InputStream in;

    public InputStreamSource(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b < 0) {
            throw new EOFException();
        }
        return b;
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        InputStreams.readFully(in, b, off, len);
    }

    @Override
    public void read(byte[] b) throws IOException {
        InputStreams.readFully(in, b);
    }

}
