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

import org.junit.runners.model.InitializationError;

import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.test.messages.TestMessagesSuite;
import com.cinnober.msgcodec.test.messages.TestProtocol;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * @author mikael.brannstrom
 *
 */
public class XmlTestMessagesSuiteImpl extends TestMessagesSuite {

    public XmlTestMessagesSuiteImpl(Class<?> rootClass)
            throws InitializationError {
        super(rootClass, createCodec());
    }

    private static MsgCodec createCodec() {
        try {
            return new XmlCodec(TestProtocol.getProtocolDictionary());
        } catch (ParserConfigurationException | SAXException ex) {
            throw new RuntimeException(ex);
        }
    }
}
