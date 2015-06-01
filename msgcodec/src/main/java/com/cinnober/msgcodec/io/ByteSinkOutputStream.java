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
import java.io.OutputStream;
import java.util.Objects;

/**
 * An output stream wrapoer for a byte sink.
 * 
 * @author mikael.brannstrom
 */
public class ByteSinkOutputStream extends OutputStream {
    private final ByteSink sink;

    /**
     * Create a new byte sink output stream.
     * @param sink the wrapped byte sink to write to, not null.
     */
    public ByteSinkOutputStream(ByteSink sink) {
        this.sink = Objects.requireNonNull(sink);
    }

    @Override
    public void write(int b) throws IOException {
        sink.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        sink.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        sink.write(b);
    }
}
