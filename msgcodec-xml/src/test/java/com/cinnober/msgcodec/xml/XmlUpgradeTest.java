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
package com.cinnober.msgcodec.xml;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinder;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.test.upgrade.PairedTestProtocols.PairedMessages;
import com.cinnober.msgcodec.test.upgrade.UpgradeAddRemoveFieldMessages.EnumV1;
import com.cinnober.msgcodec.test.upgrade.UpgradeAddRemoveFieldMessages.EnumV2;
import com.cinnober.msgcodec.test.upgrade.UpgradeAddRemoveFieldMessages.Version1;
import com.cinnober.msgcodec.test.upgrade.UpgradeAddRemoveFieldMessages.Version2;
import com.cinnober.msgcodec.IncompatibleSchemaException;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgObject;

public class XmlUpgradeTest {

    @Test
    public void testUpdateInbound() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema2 = new SchemaBuilder().build(Version2.class);
        Schema schema = new SchemaBinder(schema2).bind(schema1, g -> Direction.INBOUND);

        MsgCodec codec1 = new XmlCodecFactory(schema1).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new Version1(24, EnumV1.VALUE1, 1.2f), bout);

        MsgCodec codec2 = new XmlCodecFactory(schema).createCodec();
        Version2 msg = (Version2) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(24, msg.number);
    }

    @Test
    public void testUpdateOutbound() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema2 = new SchemaBuilder().build(Version2.class);
        Schema schema = new SchemaBinder(schema1).bind(schema2, g -> Direction.OUTBOUND);

        MsgCodec codec1 = new XmlCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new Version1(24, EnumV1.VALUE1, 1.0f), bout);
        
        System.out.println("Encoded stuff: " + bout);
        
        assertEquals(true, bout.toString().contains("number"));
        assertEquals(true, bout.toString().contains("newInt"));
        assertEquals(true, bout.toString().contains("newDouble"));

        MsgCodec codec2 = new XmlCodecFactory(schema2).createCodec();
        Version2 msg = (Version2) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(24, msg.number);
        assertEquals((byte) 0, msg.newByte1);
        assertEquals(null, msg.newByte2);
        assertEquals((short) 0, msg.newShort1);
        assertEquals(null, msg.newShort2);
        assertEquals(0, msg.newInt1);
        assertEquals(null, msg.newInt2);
        assertEquals(0.0f, msg.newFloat1, 1e-8);
        assertEquals(null, msg.newFloat2);
        assertEquals(0.0, msg.newDouble1, 1e-8);
        assertEquals(null, msg.newDouble2);
        assertEquals(null, msg.newEnum);
        assertEquals(false, msg.newBoolean1);
        assertEquals(null, msg.newBoolean2);
        assertEquals(null, msg.newString);
        assertEquals(null, msg.newBinary);
        assertEquals(null, msg.newBigDecimal);
        assertEquals(null, msg.newBigInt);
    }
    
    @Test
    public void testUpdateOutboundGroupBound() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema2 = new SchemaBuilder().build(Version2.class);
        Schema schema = new SchemaBinder(schema1).bind(schema2.unbind(), g -> Direction.OUTBOUND);

        MsgCodec codec1 = new XmlCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new Version1(24, EnumV1.VALUE1, 1.0f), bout);
        
        System.out.println("Encoded stuff: " + bout);
        
        assertEquals(true, bout.toString().contains("number"));
        assertEquals(true, bout.toString().contains("newInt"));
        assertEquals(true, bout.toString().contains("newDouble"));

        MsgCodec codec2 = new XmlCodecFactory(schema2).createCodec();
        Version2 msg = (Version2) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(24, msg.number);
        assertEquals((byte) 0, msg.newByte1);
        assertEquals(null, msg.newByte2);
        assertEquals((short) 0, msg.newShort1);
        assertEquals(null, msg.newShort2);
        assertEquals(0, msg.newInt1);
        assertEquals(null, msg.newInt2);
        assertEquals(0.0f, msg.newFloat1, 1e-8);
        assertEquals(null, msg.newFloat2);
        assertEquals(0.0, msg.newDouble1, 1e-8);
        assertEquals(null, msg.newDouble2);
        assertEquals(null, msg.newEnum);
        assertEquals(false, msg.newBoolean1);
        assertEquals(null, msg.newBoolean2);
        assertEquals(null, msg.newString);
        assertEquals(null, msg.newBinary);
        assertEquals(null, msg.newBigDecimal);
        assertEquals(null, msg.newBigInt);

    }
    
    public static enum EnumV1 {
        VALUE3, VALUE1, VALUE2,
    }

    public static enum EnumV2 {
        VALUE1, VALUE2, VALUE3, ADDITIONAL_VALUE,
    }
    
    @Name("Payload")
    @Id(1)
    public static class Version1 extends MsgObject {
        public int number;
        public EnumV1 enumeration;
        public float decimal;

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
        public long number;
        public byte newByte1;
        public Byte newByte2;
        public short newShort1;
        public Short newShort2;
        public int newInt1;
        public Integer newInt2;
        public float newFloat1;
        public Float newFloat2;
        public double newDouble1;
        public Double newDouble2;
        public EnumV2 newEnum;
        public boolean newBoolean1;
        public Boolean newBoolean2;
        public String newString;
        public byte[] newBinary;
        public BigDecimal newBigDecimal;
        public BigInteger newBigInt;
        
        public Version2() {
        }
        
        public Version2(long v1, EnumV2 v4) {
            number = v1;
            newEnum = v4;
        }

    }
        
    
    
    
    
    
    
}
