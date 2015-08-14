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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Enumeration;
import com.cinnober.msgcodec.anot.Name;

public class SchemaUpgradeTest {

	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
    public void testUpgradeEnumWidening() throws IncompatibleSchemaException {

    	EnumValueV1 originalMsg = new EnumValueV1();
    	EnumValueV2 upgradedMsg = new EnumValueV2();
        
        Schema original = new SchemaBuilder().addMessages(EnumValueV1.class).build();
        Schema upgraded = new SchemaBuilder().addMessages(EnumValueV2.class).build();
        Schema inbound = new SchemaBinder(upgraded).bind(original.unbind(), g -> Direction.INBOUND);
        Schema outbound = new SchemaBinder(original).bind(upgraded.unbind(), g -> Direction.OUTBOUND);

        Accessor inboundAccessor = inbound.getGroup("EnumValue").getField("value").getBinding().getAccessor();
        Accessor outboundAccessor = outbound.getGroup("EnumValue").getField("value").getBinding().getAccessor();

        // Verify that inbound values are transformed when set in the message
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE1.name(), Version1.VALUE1.ordinal()));
        assertEquals(Version2.VALUE1, upgradedMsg.value);
        
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE2.name(), Version1.VALUE2.ordinal()));
        assertEquals(Version2.VALUE2, upgradedMsg.value);
        
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE3.name(), Version1.VALUE3.ordinal()));
        assertEquals(Version2.VALUE3, upgradedMsg.value);
       
        inboundAccessor.setValue(upgradedMsg, null);
        assertEquals(null, upgradedMsg.value);

        // Verify that outbound values are transformed when retrieved from the message
        originalMsg.value = Version1.VALUE1;
        assertEquals(new TypeDef.Symbol(Version2.VALUE1.name(), 2), outboundAccessor.getValue(originalMsg));

        originalMsg.value = Version1.VALUE2;
        assertEquals(new TypeDef.Symbol(Version2.VALUE2.name(), 1), outboundAccessor.getValue(originalMsg));
        
        originalMsg.value = Version1.VALUE3;
        assertEquals(new TypeDef.Symbol(Version2.VALUE3.name(), 3), outboundAccessor.getValue(originalMsg));
        
        originalMsg.value = null;
        assertEquals(null, outboundAccessor.getValue(originalMsg));
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testUpgradeIntEnumWidening() throws IncompatibleSchemaException {

        IntEnumValueV1 originalMsg = new IntEnumValueV1();
        IntEnumValueV2 upgradedMsg = new IntEnumValueV2();
        
        Schema original = new SchemaBuilder().addMessages(IntEnumValueV1.class).build();
        Schema upgraded = new SchemaBuilder().addMessages(IntEnumValueV2.class).build();
        Schema inbound = new SchemaBinder(upgraded).bind(original.unbind(), g -> Direction.INBOUND);
        Schema outbound = new SchemaBinder(original).bind(upgraded.unbind(), g -> Direction.OUTBOUND);

        Accessor inboundAccessor = inbound.getGroup("EnumValue").getField("value").getBinding().getAccessor();
        Accessor outboundAccessor = outbound.getGroup("EnumValue").getField("value").getBinding().getAccessor();

        // Verify that inbound values are transformed when set in the message

        // Version2.VALUE1
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE1.name(), Version1.VALUE1.ordinal()));
        assertEquals(2, upgradedMsg.value);
        
        // Version2.VALUE2
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE2.name(), Version1.VALUE2.ordinal()));
        assertEquals(1, upgradedMsg.value);
        
        // Version2.VALUE3
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE3.name(), Version1.VALUE3.ordinal()));
        assertEquals(3, upgradedMsg.value);
       
        // Verify that outbound values are transformed when retrieved from the message
        originalMsg.value = Version1.VALUE1.ordinal();
        assertEquals(new TypeDef.Symbol(Version2.VALUE1.name(), 2), outboundAccessor.getValue(originalMsg));

        originalMsg.value = Version1.VALUE2.ordinal();
        assertEquals(new TypeDef.Symbol(Version2.VALUE2.name(), 1), outboundAccessor.getValue(originalMsg));
        
        originalMsg.value = Version1.VALUE3.ordinal();
        assertEquals(new TypeDef.Symbol(Version2.VALUE3.name(), 3), outboundAccessor.getValue(originalMsg));
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testUpgradeIntegerEnumWidening() throws IncompatibleSchemaException {

        IntegerEnumValueV1 originalMsg = new IntegerEnumValueV1();
        IntegerEnumValueV2 upgradedMsg = new IntegerEnumValueV2();
        
        Schema original = new SchemaBuilder().addMessages(IntegerEnumValueV1.class).build();
        Schema upgraded = new SchemaBuilder().addMessages(IntegerEnumValueV2.class).build();
        Schema inbound = new SchemaBinder(upgraded).bind(original.unbind(), g -> Direction.INBOUND);
        Schema outbound = new SchemaBinder(original).bind(upgraded.unbind(), g -> Direction.OUTBOUND);

        Accessor inboundAccessor = inbound.getGroup("EnumValue").getField("value").getBinding().getAccessor();
        Accessor outboundAccessor = outbound.getGroup("EnumValue").getField("value").getBinding().getAccessor();

        // Verify that inbound values are transformed when set in the message

        // Version2.VALUE1
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE1.name(), Version1.VALUE1.ordinal()));
        assertEquals(Integer.valueOf(2), upgradedMsg.value);
        
        // Version2.VALUE2
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE2.name(), Version1.VALUE2.ordinal()));
        assertEquals(Integer.valueOf(1), upgradedMsg.value);
        
        // Version2.VALUE3
        inboundAccessor.setValue(upgradedMsg, new TypeDef.Symbol(Version1.VALUE3.name(), Version1.VALUE3.ordinal()));
        assertEquals(Integer.valueOf(3), upgradedMsg.value);
       
        inboundAccessor.setValue(upgradedMsg, null);
        assertEquals(null, upgradedMsg.value);

        // Verify that outbound values are transformed when retrieved from the message
        originalMsg.value = Version1.VALUE1.ordinal();
        assertEquals(new TypeDef.Symbol(Version2.VALUE1.name(), 2), outboundAccessor.getValue(originalMsg));

        originalMsg.value = Version1.VALUE2.ordinal();
        assertEquals(new TypeDef.Symbol(Version2.VALUE2.name(), 1), outboundAccessor.getValue(originalMsg));
        
        originalMsg.value = Version1.VALUE3.ordinal();
        assertEquals(new TypeDef.Symbol(Version2.VALUE3.name(), 3), outboundAccessor.getValue(originalMsg));
        
        originalMsg.value = null;
        assertEquals(null, outboundAccessor.getValue(originalMsg));
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
}
