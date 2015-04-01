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
package com.cinnober.msgcodec.xml;

import org.junit.Test;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.messages.MetaProtocol;

/**
 * @author mikael.brannstrom
 *
 */
public class MetaMessagesTest {

    @Test
    public void test() throws Exception {
        Schema dictionary = MetaProtocol.getSchema();
        System.out.println(dictionary);
        MsgCodec codec = new XmlCodec(dictionary);
        codec.encode(dictionary.toMessage(), System.out);
    }

}
