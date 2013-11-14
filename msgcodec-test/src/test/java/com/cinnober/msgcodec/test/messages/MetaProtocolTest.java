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
package com.cinnober.msgcodec.test.messages;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.messages.MetaProtocol;
import com.cinnober.msgcodec.messages.MetaProtocolDictionary;

/**
 * @author mikael.brannstrom
 *
 */
public class MetaProtocolTest {

    /** Test that the protocol dictionary can be built (does not throw any exceptions). */
    @Test
    public void testProtocolDictionary() {
        ProtocolDictionary protocolDictionary = MetaProtocol.getProtocolDictionary();
        String protocolDictionaryString = protocolDictionary.toString();
        System.out.println(protocolDictionaryString);

        // convert to meta messages and back
        MetaProtocolDictionary metaProtocolDictionary = protocolDictionary.toMessage();
        ProtocolDictionary protocolDictionary2 = metaProtocolDictionary.toProtocolDictionary();
        String protocolDictionaryString2 = protocolDictionary2.toString();
        System.out.println(protocolDictionaryString2);

        assertEquals(protocolDictionaryString, protocolDictionaryString2);
    }

}
