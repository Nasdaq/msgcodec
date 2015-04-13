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
package com.cinnober.msgcodec.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Concurrent buffer pool. 
 * The pool has a capacity, but when that is exceeded new buffers are created on demand.
 * The pool never blocks.
 * 
 * @author mikael.brannstrom
 *
 */
public class ConcurrentBufferPool implements Pool<byte[]> {
    
    private final BlockingQueue<byte[]> pool; 
    private final int bufferSize;
    
    public ConcurrentBufferPool(int bufferSize, int poolCapacity) {
        this(bufferSize, poolCapacity, 0);
    }
    /** Create a new buffer pool.
     * 
     * @param bufferSize the size of the buffers.
     * @param poolCapacity the number of buffers kept.
     * @param initialPoolSize the initial number of buffers that are pre-allocated.
     */
    public ConcurrentBufferPool(int bufferSize, int poolCapacity, int initialPoolSize) {
        this.pool = new ArrayBlockingQueue<>(poolCapacity);
        this.bufferSize = bufferSize;
        for (int i=0; i<initialPoolSize; i++) {
            pool.offer(newInstance());
        }
    }
    
    private byte[] newInstance() {
        return new byte[bufferSize];
    }
    
    @Override
    public byte[] get() {
         byte[] buf = pool.poll();
         if (buf == null) {
             return newInstance();
         } else {
             return buf;
         }
    }
    @Override
    public void release(byte[] buf) {
        pool.offer(buf);
    }
}
