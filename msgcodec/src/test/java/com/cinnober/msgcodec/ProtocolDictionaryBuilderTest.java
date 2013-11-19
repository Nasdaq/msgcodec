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

import com.cinnober.msgcodec.anot.Id;
import org.junit.Test;

import com.cinnober.msgcodec.test.messages.TestProtocol;
import java.util.List;
import static org.junit.Assert.*;


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

    @Test
    public void testTestProtocol() {
        ProtocolDictionary dict = TestProtocol.getProtocolDictionary();
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
    public void testFieldOrder() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(FieldOrderMsg.class);
        System.out.println(dict.toString());

        GroupDef group = dict.getGroup(FieldOrderMsg.class);
        List<FieldDef> fields = group.getFields();
        assertEquals("i1", fields.get(0).getName());
        assertEquals("i4", fields.get(1).getName());
        assertEquals("i2", fields.get(2).getName());
        assertEquals("i5", fields.get(3).getName());
        assertEquals("i3", fields.get(4).getName());
        assertEquals("i6", fields.get(5).getName());
    }


    public static class FieldOrderMsg {
        @Id(1)
        public int i1;
        @Id(3)
        public int i2;
        @Id(5)
        public int i3;
        @Id(2)
        public int i4;
        @Id(4)
        public int i5;
        @Id(6)
        public int i6;
    }
}
