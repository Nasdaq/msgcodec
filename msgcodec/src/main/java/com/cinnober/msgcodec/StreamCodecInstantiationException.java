/*
 * Copyright (c) 2014 Cinnober Financial Technology AB, Stockholm,
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

/**
 * Thrown when a StreamCodec could not be instantiated.
 *
 * @author mikael.brannstrom
 */
public class StreamCodecInstantiationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new CodecInstantiationException.
     */
    public StreamCodecInstantiationException() {
    }

    /**
     * Create a new CodecInstantiationException.
     *
     * @param message the detail message.
     */
    public StreamCodecInstantiationException(String message) {
        super(message);
    }

    /**
     * Create a new CodecInstantiationException.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public StreamCodecInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new CodecInstantiationException.
     * 
     * @param cause the cause.
     */
    public StreamCodecInstantiationException(Throwable cause) {
        super(cause);
    }


}
