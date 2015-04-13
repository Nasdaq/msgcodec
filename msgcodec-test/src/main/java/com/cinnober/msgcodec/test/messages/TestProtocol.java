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
package com.cinnober.msgcodec.test.messages;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;

/**
 * @author mikael.brannstrom
 *
 */
public class TestProtocol {
    public static ProtocolDictionary getProtocolDictionary() {
        return new ProtocolDictionaryBuilder().build(
            new Class<?>[] {
                Hello.class,
                Person.class,
                Employee.class,
                DatesMessage.class,
                DecimalsMessage.class,
                EnumsMessage.class,
                FloatsMessage.class,
                IntegersMessage.class,
                MiscMessage.class,
                SequencesMessage.class,
                StringsMessage.class,
                GenericEmployeeMessage.class,
            });
    }


    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "Dates.zero" or "Decimals.border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, Object> createMessages() {
        Map<String, Object> messages = new LinkedHashMap<>();

        putAll(messages, "Dates.", DatesMessage.createMessages());
        putAll(messages, "Decimals.", DecimalsMessage.createMessages());
        putAll(messages, "Sequences.", SequencesMessage.createMessages());
        putAll(messages, "Enums.", EnumsMessage.createMessages());
        putAll(messages, "Integers.", IntegersMessage.createMessages());
        putAll(messages, "GenericEmployees.", GenericEmployeeMessage.createMessages());

        return messages;
    }

    private static void putAll(Map<String, Object> result, String prefix, Map<String, ? extends Object> messages) {
        for (Map.Entry<String, ? extends Object> entry : messages.entrySet()) {
            result.put(prefix + entry.getKey(), entry.getValue());
        }
    }
}
