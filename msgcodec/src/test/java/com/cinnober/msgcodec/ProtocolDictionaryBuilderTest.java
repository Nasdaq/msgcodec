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
import java.util.List;
import static org.junit.Assert.*;


/**
 * @author mikael.brannstrom
 *
 */
public class ProtocolDictionaryBuilderTest {

    @Test
    public void testFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(FooMessage.class);
        System.out.println(dict.toString());
    }
    @Test
    public void testFooWithAddMessageStep() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.addMessages(FooMessage.class).build();
        System.out.println(dict.toString());
    }
    @Test
    public void testBar() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(BarMessage.class, Thing.class);
        System.out.println(dict.toString());
    }
    @Test
    public void testFooBarWithAddMessageStep() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.addMessages(BarMessage.class, Thing.class)
                .addMessages(FooMessage.class).build();
        System.out.println(dict.toString());
    }
    @Test
    public void testFooBar() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(FooMessage.class, BarMessage.class, Thing.class);
        System.out.println(dict.toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testWrappedFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(WrappedFoo.class);
        final GroupDef groupDef = dict.getGroup("WrappedFoo");
        assertEquals("FooMessage", groupDef.getField("wrapped").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testWrappedWrappedFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(WrappedWrappedFoo.class);
        final GroupDef groupDef = dict.getGroup("WrappedWrappedFoo");
        assertEquals("FooMessage", groupDef.getField("wrapped").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testArrayOnlyWrappedFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(ArrayOnlyWrappedFoo.class);
        final GroupDef groupDef = dict.getGroup("ArrayOnlyWrappedFoo");
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testListOnlyWrappedFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(ListOnlyWrappedFoo.class);
        final GroupDef groupDef = dict.getGroup("ListOnlyWrappedFoo");
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
    }

    @Test
    public void testFieldOrder() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(FieldOrderMsg.class);
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

    @Test
    public void testAnnotationMapper() {
        SchemaBuilder builder = new SchemaBuilder();
        builder.addAnnotationMapper(CustomAnnotation.class, new AnnotationMapper<CustomAnnotation>() {
            @Override
            public String map(CustomAnnotation annotation) {
                return "custom=" + annotation.value();
            }
        });
        Schema dict = builder.build(FooMessage.class);
        System.out.println("annotationMapper: \n" + dict.toString());
        assertEquals("FooMessage", dict.getGroup("FooMessage").getAnnotation("custom"));
        assertEquals("myByte", dict.getGroup("FooMessage").getField("myByte").getAnnotation("custom"));
    }

    @Test
    public void testPrivate() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema dict = builder.build(SecretMessage.class);
        System.out.println(dict.toString());
        assertNotNull(dict.getGroup("SecretMessage").getFactory().newInstance());
    }
    @Test
    public void testPrivate2() {
        SchemaBuilder builder = new SchemaBuilder();
        try {
            Schema dict = builder.build(SecretMessage2.class);
            fail("Expected exception: no default constructor");
        } catch (IllegalArgumentException e) {}
    }

    public static class FieldOrderMsg extends MsgObject {
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
