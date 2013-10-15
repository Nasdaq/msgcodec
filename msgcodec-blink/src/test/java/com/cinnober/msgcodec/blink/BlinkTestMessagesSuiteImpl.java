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
package com.cinnober.msgcodec.blink;

import org.junit.runners.model.InitializationError;

import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.test.messages.TestMessagesSuite;
import com.cinnober.msgcodec.test.messages.TestProtocol;

/**
 * @author mikael.brannstrom
 *
 */
public class BlinkTestMessagesSuiteImpl extends TestMessagesSuite {

    public BlinkTestMessagesSuiteImpl(Class<?> rootClass)
            throws InitializationError {
        super(rootClass, createCodec());
    }

    private static StreamCodec createCodec() {
        return new BlinkCodec(TestProtocol.getProtocolDictionary());
    }
}
