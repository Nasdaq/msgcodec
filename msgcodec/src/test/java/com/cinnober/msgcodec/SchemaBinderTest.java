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
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Unsigned;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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


    @Ignore()
    @Test(expected = IncompatibleSchemaException.class)
    public void testUpgradeFail() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        new SchemaBinder(schema1).bind(schema2, g -> Direction.BOTH);
    }

    @Test
    public void testUpgradeBoundToGroupOk() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        schema1 = Group.bind(schema1);
        schema2 = Group.bind(schema2);
        new SchemaBinder(schema1).bind(schema2, SchemaBinderTest::getDirAtClient);
    }

    @Ignore()
    @Test(expected = IncompatibleSchemaException.class)
    public void testUpgradeBoundToGroupFail() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooRspV2.class).build();
        schema1 = Group.bind(schema1);
        schema2 = Group.bind(schema2);
        new SchemaBinder(schema1).bind(schema2, g -> Direction.INBOUND);
    }

    @Ignore()
    @Test(expected = IncompatibleSchemaException.class)
    public void testUpgradeBoundToGroupFail2() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooRspV2.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooRspV1.class).build();
        schema1 = Group.bind(schema1);
        schema2 = Group.bind(schema2);
        new SchemaBinder(schema1).bind(schema2, g -> Direction.OUTBOUND);
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

    private void testWideningAndNarrowingSchemaBuild(Class<?>... classes) throws IncompatibleSchemaException {
        for (int i=0;i<classes.length-1;i++) {
            for (int j=i+1;j<classes.length;j++) {
                Schema schemaNarrow = new SchemaBuilder().build(classes[i]);
                Schema schemaWide = new SchemaBuilder().build(classes[j]);

                boolean exceptionThrown = false;
                try {
                    new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.OUTBOUND);
                } catch (IncompatibleSchemaException e) {
                    exceptionThrown = true;
                }
                assertTrue("No IncompatibleSchemaException thrown for "+classes[j]+"->"+classes[i]+"!",exceptionThrown);

                new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.INBOUND);

                exceptionThrown = false;
                try {
                    new SchemaBinder(schemaNarrow).bind(schemaWide, g -> Direction.INBOUND);
                } catch (IncompatibleSchemaException e) {
                    exceptionThrown = true;
                }
                assertTrue("No IncompatibleSchemaException thrown for "+classes[i]+"<-"+classes[j]+"!",exceptionThrown);

                new SchemaBinder(schemaNarrow).bind(schemaWide, g -> Direction.OUTBOUND);

                exceptionThrown = false;
                try {
                    new SchemaBinder(schemaNarrow).bind(schemaWide, g -> Direction.BOTH);
                } catch (IncompatibleSchemaException e) {
                    exceptionThrown = true;
                }
                assertTrue("No IncompatibleSchemaException thrown for "+classes[i]+"<->"+classes[j]+"!",exceptionThrown);
            }
        }
    }

    @Test
    public void testWideningAndNarrowing() throws IOException, IncompatibleSchemaException {
        // wider direction --->
        testWideningAndNarrowingSchemaBuild(DecimalNarrow.class, DecimalWide.class);
        testWideningAndNarrowingSchemaBuild(ByteNum.class, OptByteNum.class);
        testWideningAndNarrowingSchemaBuild(ByteNum.class, ShortNum.class,
                IntNum.class,LongNum.class);
        testWideningAndNarrowingSchemaBuild(EnumEntNarrow.class, EnumEntWide.class);
        testWideningAndNarrowingSchemaBuild(ReqTestEntityWithRequiredYear.class, ReqTestEntity.class);
    }

    @Test(expected = IncompatibleSchemaException.class)
    public void testSameNameDifferentIdOutbound() throws IncompatibleSchemaException {
        Schema schemaNarrow = new SchemaBuilder().build(SameNameDifferentId1.class);
        Schema schemaWide = new SchemaBuilder().build(SameNameDifferentId2.class);
        new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.OUTBOUND);
    }

    @Test(expected = IncompatibleSchemaException.class)
    public void testSameNameDifferentIdInbound() throws IncompatibleSchemaException {
        Schema schemaNarrow = new SchemaBuilder().build(SameNameDifferentId1.class);
        Schema schemaWide = new SchemaBuilder().build(SameNameDifferentId2.class);
        new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.INBOUND);
    }

    @Test(expected = IncompatibleSchemaException.class)
    public void testDifferentNameSameIdInbound() throws IncompatibleSchemaException {
        Schema schemaNarrow = new SchemaBuilder().build(DifferentNameSameId1.class);
        Schema schemaWide = new SchemaBuilder().build(DifferentNameSameId2.class);
        new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.INBOUND);
    }

    @Test(expected = IncompatibleSchemaException.class)
    public void testDifferentNameSameIdOutbound() throws IncompatibleSchemaException {
        Schema schemaNarrow = new SchemaBuilder().build(DifferentNameSameId1.class);
        Schema schemaWide = new SchemaBuilder().build(DifferentNameSameId2.class);
        new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.OUTBOUND);
    }


    @Name("SameNameDifferentId")
    @Id(1000)
    public static class SameNameDifferentId1 extends MsgObject {
        public String data;
        public SameNameDifferentId1() {
        }
    }

    @Name("SameNameDifferentId")
    @Id(1001)
    public static class SameNameDifferentId2 extends MsgObject {
        public String data;
        public SameNameDifferentId2() {
        }
    }

    @Name("DifferentNameSameId")
    @Id(1002)
    public static class DifferentNameSameId1 extends MsgObject {
        public String data;
        public DifferentNameSameId1() {
        }
    }

    @Name("DifferentNameSameIdDiff")
    @Id(1002)
    public static class DifferentNameSameId2 extends MsgObject {
        public String data;
        public DifferentNameSameId2() {
        }
    }


    @Name("Decimal")
    @Id(2)
    public static class DecimalNarrow extends MsgObject {
        public float decimal;

        public DecimalNarrow() {
        }

        public DecimalNarrow(float decimal) {
            this.decimal=decimal;
        }
    }

    @Name("Decimal")
    @Id(2)
    public static class DecimalWide extends MsgObject {
        public double decimal;

        public DecimalWide() {
        }

        public DecimalWide(double decimal) {
            this.decimal=decimal;
        }

        public boolean equals(Object o) {
            return (o instanceof DecimalWide) && Math.abs(((DecimalWide) o).decimal-decimal)<0.0001;
        }
    }

    @Name("Number")
    @Id(3)
    public static class OptByteNum extends MsgObject {
        public Byte n;

        public OptByteNum() {}

        public OptByteNum(Byte n) { this.n = n; }


    }

    @Name("Number")
    @Id(3)
    public static class ByteNum extends MsgObject {
        public byte n;

        public ByteNum() {}

        public ByteNum(byte n) { this.n = n; }
    }

    @Name("Number")
    @Id(3)
    public static class ShortNum extends MsgObject {
        public short n;

        public ShortNum() {}
        public ShortNum(short n) { this.n = n; }

    }

    @Name("Number")
    @Id(3)
    public static class IntNum extends MsgObject {
        public int n;

        public IntNum() {}
        public IntNum(int n) { this.n = n; }
    }

    @Name("Number")
    @Id(3)
    public static class LongNum extends MsgObject {
        public long n;


        public LongNum() {}
        public LongNum(long n) { this.n = n; }
    }

    public enum EnumNarrow {
        VALUE3, VALUE1, VALUE2,
    }

    public enum EnumWide {
        DUMMY_1, VALUE1, VALUE2, VALUE3, ADDITIONAL_VALUE,
    }

    @Name("EnumEnt")
    @Id(1)
    public static class EnumEntNarrow extends MsgObject {
        public EnumNarrow enumeration;

        public EnumEntNarrow() {
        }

        public EnumEntNarrow(EnumNarrow eValue) {
            enumeration = eValue;
        }
    }

    @Name("EnumEnt")
    @Id(1)
    public static class EnumEntWide extends MsgObject {
        public EnumWide enumeration;

        public EnumEntWide() {
        }

        public EnumEntWide(EnumWide eValue) {
            enumeration = eValue;
        }
    }

    @Name("ReqTestEntity")
    @Id(10)
    public static class ReqTestEntity extends MsgObject {
        public String number;

        public ReqTestEntity() {
            number = "ReqTestEntity " + System.nanoTime();
        }
    }

    @Name("ReqTestEntity")
    @Id(10)
    public static class ReqTestEntityWithRequiredYear extends MsgObject {
        public String number;
        public int year;

        public ReqTestEntityWithRequiredYear() {
            number = "ReqTestEntity " + System.nanoTime();
            year = (int)(System.nanoTime()%3000);
        }
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

        public Long newOptionalField;
    }
}
