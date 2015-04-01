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

package com.cinnober.msgcodec.examples;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.examples.generated.GeneratedProtocol;

/**
 * Example of how to use generated code for messages with msgcodec.
 * See the class GeneratedProtocol for how the protocol dictionary can be built.
 *
 * @author mikael.brannstrom
 */
public class GeneratedCodeExample {
    public static void main(String... args) throws Exception {
        Schema dict = GeneratedProtocol.getInstance();

        System.out.println("The generated protocol:\n" + dict);

        // Now this dict can be used as usual
    }
}
