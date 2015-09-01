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

package com.cinnober.msgcodec.blink;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.cinnober.msgcodec.IncompatibleSchemaException;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinder;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Unsigned;

public class NativeBlinkUpdateSchemaTest {

    public void printStream(ByteArrayOutputStream stream) {
        byte[] arr = stream.toByteArray();

        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] & 0xFF);
            System.out.print(" ");
        }
        System.out.println("");
    }


    @Test
    public void testUpdateInbound() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema2 = new SchemaBuilder().build(Version2.class);
        Schema schema = new SchemaBinder(schema2).bind(schema1, g -> Direction.INBOUND);

        MsgCodec codec1 = new NativeBlinkCodecFactory(schema1).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new Version1(24, EnumV1.VALUE1, 1.2f), bout);

        MsgCodec codec2 = new NativeBlinkCodecFactory(schema).createCodec();
        Version2 msg = (Version2) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(24, msg.number);
        assertEquals(EnumV2.VALUE1, msg.enumeration);
    }

    @Test
    public void testUpdateOutbound() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema2 = new SchemaBuilder().build(Version2.class);
        Schema schema = new SchemaBinder(schema1).bind(schema2, g -> Direction.OUTBOUND);

        MsgCodec codec1 = new NativeBlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new Version1(24, EnumV1.VALUE1, 1.0f), bout);

        MsgCodec codec2 = new NativeBlinkCodecFactory(schema2).createCodec();
        Version2 msg = (Version2) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(24, msg.number);
    }

    
    @Test
    public void testRemovedAndAddedFields() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema3 = new SchemaBuilder().build(Version3.class);
        Schema schema = new SchemaBinder(schema3).bind(schema1, g -> Direction.INBOUND);

        MsgCodec codec1 = new NativeBlinkCodecFactory(schema1).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        codec1.encode(new Version1(24, EnumV1.VALUE2, 2.0f), bout);

        MsgCodec codec2 = new NativeBlinkCodecFactory(schema).createCodec();
        Version3 msg = (Version3) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(0L, msg.newfield1);
        assertEquals(0.0f, msg.newfield2, 1e-8);
        assertEquals(null, msg.newEnum);
        assertEquals(24L, msg.number);
    }
    
    @Test
    public void testRemovedAndAddedFields2() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema3 = new SchemaBuilder().build(Version3.class);
        Schema schema = new SchemaBinder(schema1).bind(schema3, g -> Direction.OUTBOUND);

        MsgCodec codec1 = new NativeBlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        codec1.encode(new Version1(24, EnumV1.VALUE1, 1.0f), bout);

        MsgCodec codec2 = new NativeBlinkCodecFactory(schema3).createCodec();
        Version3 msg = (Version3) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(24, msg.number);
    }

    @Test (expected = IncompatibleSchemaException.class)
    public void testRemovedAndAddedFieldsNarrow() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema3 = new SchemaBuilder().build(Version3.class);
        new SchemaBinder(schema1).bind(schema3, g -> Direction.INBOUND);
    }

    @Test (expected = IncompatibleSchemaException.class)
    public void testRemovedAndAddedFieldsNarrow2() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema3 = new SchemaBuilder().build(Version3.class);
        new SchemaBinder(schema3).bind(schema1, g -> Direction.OUTBOUND);
    }
    
    @Test (expected = IncompatibleSchemaException.class)
    public void testRemovedAndAddedFieldsNarrow3() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema3 = new SchemaBuilder().build(Version3.class);
        new SchemaBinder(schema1).bind(schema3, g -> Direction.BOTH);
    }

    public static enum EnumV1 {
        VALUE3, VALUE1, VALUE2,
    }

    public static enum EnumV2 {
        DUMMY_1, VALUE1, VALUE2, VALUE3, ADDITIONAL_VALUE,
    }

    @Name("Payload")
    @Id(1)
    public static class Version1 extends MsgObject {
        public float decimal;
        public EnumV1 enumeration;
        public int number;

        public Version1() {
        }

        public Version1(int value, EnumV1 eValue, float d) {
            number = value;
            enumeration = eValue;
            decimal = d;
        }
    }

    @Name("Payload")
    @Id(1)
    public static class Version2 extends MsgObject {
        public double decimal;
        
        public byte dummy_11byte;
        @Unsigned
        public byte dummy_12byte;
        public Byte dummy_13byte;
        @Unsigned
        public Byte dummy_14byte;
        
        public short dummy_21short;
        @Unsigned
        public short dummy_22short;
        public Short dummy_23short;
        @Unsigned
        public Short dummy_24short;
        
        public int dummy_31int;
        @Unsigned
        public int dummy_32int;
        public Integer dummy_33int;
        @Unsigned
        public Integer dummy_34int;

        public float dummy_41float;
        public Float dummy_42float;
        
        
        public EnumV2 enumeration;
        public long number;

        public Version2() {
        }

        public Version2(long value, EnumV2 eValue, double d) {
            number = value;
            enumeration = eValue;
            decimal = d;
        }
    }

    @Name("Payload")
    @Id(1)
    public static class Version3 extends MsgObject {
        public long number;
        public short newfield1;
        public double newfield2;
        public EnumV2 newEnum;
        
        public Version3() {
        }
        
        public Version3(long v1, short v2, double v3, EnumV2 v4) {
            number = v1;
            newfield1 = v2;
            newfield2 = v3;
            newEnum = v4;
        }

    }
    

}
