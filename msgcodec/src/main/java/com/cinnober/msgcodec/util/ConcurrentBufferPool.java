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
