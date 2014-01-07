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
    private static final ProtocolDictionary protocolDictionary = createProtocolDictionary();
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
     * @return
     */
    public static Collection<Class<?>> getProtocolMessageClasses() {
        return protocolMessageClasses;
    }

}
