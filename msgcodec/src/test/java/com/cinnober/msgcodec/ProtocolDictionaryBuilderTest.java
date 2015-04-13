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
public class ProtocolDictionaryBuilderTest {

    @Test
    public void testFoo() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(FooMessage.class);
        System.out.println(dict.toString());
    }
    @Test
    public void testFooWithAddMessageStep() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.addMessages(FooMessage.class).build();
        System.out.println(dict.toString());
    }
    @Test
    public void testBar() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(BarMessage.class, Thing.class);
        System.out.println(dict.toString());
    }
    @Test
    public void testFooBarWithAddMessageStep() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.addMessages(BarMessage.class, Thing.class)
                .addMessages(FooMessage.class).build();
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
        final GroupDef groupDef = dict.getGroup("WrappedFoo");
        assertEquals("FooMessage", groupDef.getField("wrapped").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testWrappedWrappedFoo() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(WrappedWrappedFoo.class);
        final GroupDef groupDef = dict.getGroup("WrappedWrappedFoo");
        assertEquals("FooMessage", groupDef.getField("wrapped").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testArrayOnlyWrappedFoo() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(ArrayOnlyWrappedFoo.class);
        final GroupDef groupDef = dict.getGroup("ArrayOnlyWrappedFoo");
        assertEquals("FooMessage[]", groupDef.getField("wrappedArray").getType().toString());
    }

    /** Test of generic class parameters, as well as recursive add of referred components.
     */
    @Test
    public void testListOnlyWrappedFoo() {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dict = builder.build(ListOnlyWrappedFoo.class);
        final GroupDef groupDef = dict.getGroup("ListOnlyWrappedFoo");
        assertEquals("FooMessage[]", groupDef.getField("wrappedList").getType().toString());
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
