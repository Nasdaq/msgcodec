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
package com.cinnober.msgcodec;

import org.junit.Test;
import static org.junit.Assert.*;

//import com.cinnober.msgcodec.test.messages.TestProtocol;


/**
 * @author mikael.brannstrom
 *
 */
public class ProtocolDictionaryBuilderTest {

    @Test
    public void testFoo() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(FooMessage.class);
        System.out.println(dict.toString());
    }
    @Test
    public void testBar() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(BarMessage.class, Thing.class);
        System.out.println(dict.toString());
    }
    @Test
    public void testFooBar() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(FooMessage.class, BarMessage.class, Thing.class);
        System.out.println(dict.toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testWrappedFoo() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(WrappedFoo.class);
        System.out.println(dict.toString());
    }

    @Test
    public void testAnnotationMapper() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        builder.addAnnotationMapper(CustomAnnotation.class, new AnnotationMapper<CustomAnnotation>() {
            @Override
            public String map(CustomAnnotation annotation) {
                return "custom=" + annotation.value();
            }
        });
        ProtocolDictionary dict = builder.build(FooMessage.class);
        System.out.println("annotationMapper: \n" + dict.toString());
        assertEquals("FooMessage", dict.getGroup("FooMessage").getAnnotation("custom"));
        assertEquals("myByte", dict.getGroup("FooMessage").getField("myByte").getAnnotation("custom"));
    }

    @Test
    public void testPrivate() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(SecretMessage.class);
        System.out.println(dict.toString());
        assertNotNull(dict.getGroup("SecretMessage").getFactory().newInstance());
    }
    @Test
    public void testPrivate2() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        try {
            ProtocolDictionary dict = builder.build(SecretMessage2.class);
            fail("Expected exception: no default constructor");
        } catch (IllegalArgumentException e) {}
    }
}
