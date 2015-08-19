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
package com.cinnober.msgcodec;

import com.cinnober.msgcodec.anot.Id;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;


/**
 * @author mikael.brannstrom
 *
 */
public class SchemaBuilderTest {

    @Test
    public void testFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(FooMessage.class);
        System.out.println(schema.toString());
    }
    @Test
    public void testFooWithAddMessageStep() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.addMessages(FooMessage.class).build();
        System.out.println(schema.toString());
    }
    @Test
    public void testBar() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(BarMessage.class, Thing.class);
        System.out.println(schema.toString());
    }
    @Test
    public void testFooBarWithAddMessageStep() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.addMessages(BarMessage.class, Thing.class)
                .addMessages(FooMessage.class).build();
        System.out.println(schema.toString());
    }
    @Test
    public void testFooBar() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(FooMessage.class, BarMessage.class, Thing.class);
        System.out.println(schema.toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testWrappedFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(WrappedFoo.class);
        final GroupDef groupDef = schema.getGroup("WrappedFoo");
        assertEquals("FooMessage", groupDef.getField("wrapped").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testWrappedWrappedFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(WrappedWrappedFoo.class);
        final GroupDef groupDef = schema.getGroup("WrappedWrappedFoo");
        assertEquals("FooMessage", groupDef.getField("wrapped").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testArrayOnlyWrappedFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schmea = builder.build(ArrayOnlyWrappedFoo.class);
        final GroupDef groupDef = schmea.getGroup("ArrayOnlyWrappedFoo");
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testListOnlyWrappedFoo() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(ListOnlyWrappedFoo.class);
        final GroupDef groupDef = schema.getGroup("ListOnlyWrappedFoo");
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
    }

    @Test
    public void testFieldOrder() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(FieldOrderMsg.class);
        System.out.println(schema.toString());

        GroupDef group = schema.getGroup(FieldOrderMsg.class);
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
        builder.addAnnotationMapper(CustomAnnotation.class, a -> "custom=" + a.value());
        Schema schema = builder.build(FooMessage.class);
        System.out.println("annotationMapper: \n" + schema.toString());
        assertEquals("FooMessage", schema.getGroup("FooMessage").getAnnotation("custom"));
        assertEquals("myByte", schema.getGroup("FooMessage").getField("myByte").getAnnotation("custom"));
    }

    @Test
    public void testPrivate() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(SecretMessage.class);
        System.out.println(schema.toString());
        assertNotNull(schema.getGroup("SecretMessage").getFactory().newInstance());
    }
    @Test
    public void testPrivate2() {
        SchemaBuilder builder = new SchemaBuilder();
        try {
            builder.build(SecretMessage2.class);
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
