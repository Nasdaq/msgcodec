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
package com.cinnober.msgcodec.messages;

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author mikael.brannstrom
 *
 */
public class MetaProtocol {
    private static final Collection<Class<?>> protocolMessageClasses = Collections.unmodifiableCollection(Arrays.asList(
        (Class<?>)MetaProtocolDictionary.class,
        MetaAnnotated.class,
        MetaAnnotation.class,
        MetaNamedType.class,
        MetaGroupDef.class,
        MetaFieldDef.class,
        MetaTypeDef.class,
        MetaTypeDef.MetaRef.class,
        MetaTypeDef.MetaDynRef.class,
        MetaTypeDef.MetaInt8.class,
        MetaTypeDef.MetaInt16.class,
        MetaTypeDef.MetaInt32.class,
        MetaTypeDef.MetaInt64.class,
        MetaTypeDef.MetaUInt8.class,
        MetaTypeDef.MetaUInt16.class,
        MetaTypeDef.MetaUInt32.class,
        MetaTypeDef.MetaUInt64.class,
        MetaTypeDef.MetaFloat32.class,
        MetaTypeDef.MetaFloat64.class,
        MetaTypeDef.MetaDecimal.class,
        MetaTypeDef.MetaBoolean.class,
        MetaTypeDef.MetaTime.class,
        MetaTypeDef.MetaBigInt.class,
        MetaTypeDef.MetaBigDecimal.class,
        MetaTypeDef.MetaString.class,
        MetaTypeDef.MetaBinary.class,
        MetaTypeDef.MetaSequence.class,
        MetaTypeDef.MetaEnum.class,
        MetaTypeDef.MetaSymbol.class
    ));
    private static final ProtocolDictionary protocolDictionary = createProtocolDictionary();


    private MetaProtocol() { }

    /**
     * @return the protocol dictionary
     */
    private static ProtocolDictionary createProtocolDictionary() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder(true);
        ProtocolDictionary dictionary =  builder.build(protocolMessageClasses);
        return dictionary;
    }

    /**
     * @return the protocolDictionary
     */
    public static ProtocolDictionary getProtocolDictionary() {
        return protocolDictionary;
    }

    /**
     * Returns all protocol messages classes in the protocol.
     *
     * The protocol dictionary can be created like this:
     * <pre>
     * ProtocolDictionary dictionary = new ProtocolDictionaryBuilder(true).build(getprotocolMessageClasses());
     * </pre>
     *
     * @return all protocol messages classes in the protocol.
     */
    public static Collection<Class<?>> getProtocolMessageClasses() {
        return protocolMessageClasses;
    }

}
