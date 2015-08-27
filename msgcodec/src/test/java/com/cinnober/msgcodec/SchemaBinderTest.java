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

import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Enumeration;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Unsigned;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author mikael.brannstrom
 */
public class SchemaBinderTest {

    public SchemaBinderTest() {
    }

    @Test
    public void testUpgradeOk() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        new SchemaBinder(schema1).bind(schema2, SchemaBinderTest::getDirAtClient);
    }

    @Test(expected = IncompatibleSchemaException.class)
    public void testUpgradeFail() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        new SchemaBinder(schema1).bind(schema2, g -> Direction.BOTH);
    }

    public void testUpgradeBoundToGroupOk() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        schema1 = Group.bind(schema1);
        schema2 = Group.bind(schema2);
        new SchemaBinder(schema1).bind(schema2, g -> Direction.BOTH);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testUpgradeEnumWidening() throws IncompatibleSchemaException {
        Schema original = new SchemaBuilder().addMessages(EnumValueV1.class).build();
        Schema upgraded = new SchemaBuilder().addMessages(EnumValueV2.class).build();
        Schema inbound = new SchemaBinder(upgraded).bind(original.unbind(), g -> Direction.INBOUND);
        Schema outbound = new SchemaBinder(original).bind(upgraded.unbind(), g -> Direction.OUTBOUND);

        assertEquals("Inbound codec has bound enum class", Version2.class, inbound.getGroup("EnumValue").getField("value").getJavaClass());
        assertEquals("Outbound codec has bound enum class", Version1.class, outbound.getGroup("EnumValue").getField("value").getJavaClass());
        
        SymbolMapping mapping;

        // Verify inbound mappings
        mapping = inbound.getGroup("EnumValue").getField("value").getBinding().getSymbolMapping();
        assertEquals(Version2.VALUE1, mapping.lookup(0));
        assertEquals(Version2.VALUE2, mapping.lookup(1));
        assertEquals(Version2.VALUE3, mapping.lookup(2));
        
        assertEquals(Version2.VALUE1, mapping.lookup("VALUE1"));
        assertEquals(Version2.VALUE2, mapping.lookup("VALUE2"));
        assertEquals(Version2.VALUE3, mapping.lookup("VALUE3"));
        
        // Verify outbound mappings
        mapping = outbound.getGroup("EnumValue").getField("value").getBinding().getSymbolMapping();
        assertEquals(Integer.valueOf(2), mapping.getId(Version1.VALUE1));
        assertEquals(Integer.valueOf(1), mapping.getId(Version1.VALUE2));
        assertEquals(Integer.valueOf(3), mapping.getId(Version1.VALUE3));
        
        assertEquals("VALUE1", mapping.getName(Version1.VALUE1));
        assertEquals("VALUE2", mapping.getName(Version1.VALUE2));
        assertEquals("VALUE3", mapping.getName(Version1.VALUE3));
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testUpgradeIntEnumWidening() throws IncompatibleSchemaException {
        Schema original = new SchemaBuilder().addMessages(IntEnumValueV1.class).build();
        Schema upgraded = new SchemaBuilder().addMessages(IntEnumValueV2.class).build();
        Schema inbound = new SchemaBinder(upgraded).bind(original.unbind(), g -> Direction.INBOUND);
        Schema outbound = new SchemaBinder(original).bind(upgraded.unbind(), g -> Direction.OUTBOUND);

        assertEquals("Inbound codec has bound enum class", int.class, inbound.getGroup("EnumValue").getField("value").getJavaClass());
        assertEquals("Outbound codec has bound enum class", int.class, outbound.getGroup("EnumValue").getField("value").getJavaClass());
        
        SymbolMapping mapping;

        // Verify inbound mappings
        mapping = inbound.getGroup("EnumValue").getField("value").getBinding().getSymbolMapping();
        assertEquals(Version2.VALUE1.ordinal(), mapping.lookup(0));
        assertEquals(Version2.VALUE2.ordinal(), mapping.lookup(1));
        assertEquals(Version2.VALUE3.ordinal(), mapping.lookup(2));
        
        assertEquals(Version2.VALUE1.ordinal(), mapping.lookup("VALUE1"));
        assertEquals(Version2.VALUE2.ordinal(), mapping.lookup("VALUE2"));
        assertEquals(Version2.VALUE3.ordinal(), mapping.lookup("VALUE3"));
        
        // Verify outbound mappings
        mapping = outbound.getGroup("EnumValue").getField("value").getBinding().getSymbolMapping();
        assertEquals(Integer.valueOf(2), mapping.getId(Version1.VALUE1.ordinal()));
        assertEquals(Integer.valueOf(1), mapping.getId(Version1.VALUE2.ordinal()));
        assertEquals(Integer.valueOf(3), mapping.getId(Version1.VALUE3.ordinal()));
        
        assertEquals("VALUE1", mapping.getName(Version1.VALUE1.ordinal()));
        assertEquals("VALUE2", mapping.getName(Version1.VALUE2.ordinal()));
        assertEquals("VALUE3", mapping.getName(Version1.VALUE3.ordinal()));
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testUpgradeIntegerEnumWidening() throws IncompatibleSchemaException {
        Schema original = new SchemaBuilder().addMessages(IntegerEnumValueV1.class).build();
        Schema upgraded = new SchemaBuilder().addMessages(IntegerEnumValueV2.class).build();
        Schema inbound = new SchemaBinder(upgraded).bind(original.unbind(), g -> Direction.INBOUND);
        Schema outbound = new SchemaBinder(original).bind(upgraded.unbind(), g -> Direction.OUTBOUND);

        assertEquals("Inbound codec has bound enum class", Integer.class, inbound.getGroup("EnumValue").getField("value").getJavaClass());
        assertEquals("Outbound codec has bound enum class", Integer.class, outbound.getGroup("EnumValue").getField("value").getJavaClass());

        SymbolMapping mapping;

        // Verify inbound mappings
        mapping = inbound.getGroup("EnumValue").getField("value").getBinding().getSymbolMapping();
        assertEquals(Version2.VALUE1.ordinal(), mapping.lookup(0));
        assertEquals(Version2.VALUE2.ordinal(), mapping.lookup(1));
        assertEquals(Version2.VALUE3.ordinal(), mapping.lookup(2));
        
        assertEquals(Version2.VALUE1.ordinal(), mapping.lookup("VALUE1"));
        assertEquals(Version2.VALUE2.ordinal(), mapping.lookup("VALUE2"));
        assertEquals(Version2.VALUE3.ordinal(), mapping.lookup("VALUE3"));
        
        // Verify outbound mappings
        mapping = outbound.getGroup("EnumValue").getField("value").getBinding().getSymbolMapping();
        assertEquals(Integer.valueOf(2), mapping.getId(Version1.VALUE1.ordinal()));
        assertEquals(Integer.valueOf(1), mapping.getId(Version1.VALUE2.ordinal()));
        assertEquals(Integer.valueOf(3), mapping.getId(Version1.VALUE3.ordinal()));
        
        assertEquals("VALUE1", mapping.getName(Version1.VALUE1.ordinal()));
        assertEquals("VALUE2", mapping.getName(Version1.VALUE2.ordinal()));
        assertEquals("VALUE3", mapping.getName(Version1.VALUE3.ordinal()));
    }
    
    enum Version1 {
        VALUE1,
        VALUE2,
        VALUE3,
    }
    
    enum Version2 {
        ADDITIONAL_VALUE,
        VALUE2,
        VALUE1,
        VALUE3,
    }
    
    @Name("EnumValue")
    public static class EnumValueV1 extends MsgObject {
        public Version1 value;
    }

    @Name("EnumValue")
    public static class EnumValueV2 extends MsgObject {
        public Version2 value;
    }
    
    @Name("EnumValue")
    public static class IntEnumValueV1 extends MsgObject {
        @Enumeration(Version1.class)
        public int value;
    }

    @Name("EnumValue")
    public static class IntEnumValueV2 extends MsgObject {
        @Enumeration(Version2.class)
        public int value;
    }
    
    @Name("EnumValue")
    public static class IntegerEnumValueV1 extends MsgObject {
        @Enumeration(Version1.class)
        public Integer value;
    }

    @Name("EnumValue")
    public static class IntegerEnumValueV2 extends MsgObject {
        @Enumeration(Version2.class)
        public Integer value;
    }

    public static Direction getDirAtClient(Annotatable<?> a) {
        String s = a.getAnnotation("dir");
        if (s == null) {
            return Direction.BOTH;
        }
        switch (s) {
            case "c2s":
                return Direction.OUTBOUND;
            case "s2c":
                return Direction.INBOUND;
            default:
                return Direction.BOTH;
        }
    }

    @Annotate("dir=both")
    @Name("Foo")
    public static class FooV1 extends MsgObject {
        public String text;
    }

    @Annotate("dir=c2s")
    @Name("FooReq")
    public static class FooReqV1 extends MsgObject {
        public FooV1 foo;
        @Unsigned
        public long reqId;
    }

    @Annotate("dir=s2c")
    @Name("FooRsp")
    public static class FooRspV1 extends MsgObject {
        public FooV1 foo;
        @Unsigned
        public long reqId;
    }

    @Annotate("dir=both")
    @Name("Foo")
    public static class FooV2 extends MsgObject {
        public String text;
    }

    @Annotate("dir=c2s")
    @Name("FooReq")
    public static class FooReqV2 extends MsgObject {
        public FooV2 foo;
        @Unsigned
        public long reqId;

        public Long newOptionalField;
    }

    @Annotate("dir=s2c")
    @Name("FooRsp")
    public static class FooRspV2 extends MsgObject {
        public FooV2 foo;
        @Unsigned
        public long reqId;

        public long newRequiredField;
    }

}
