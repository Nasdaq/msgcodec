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

import com.cinnober.msgcodec.anot.Enumeration;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Sequence;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Tests of the Group class which can be bound instead of actual user classes
 * when working with dynamic schemas.
 * 
 * @author Tommy.Norling
 */
public class GroupTest {
    @Test
    public void testSymbolMappingForGroup() {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(EnumMessage.class);
        
        schema = Group.bind(schema.unbind());
        
        
        GroupDef group = schema.getGroup(1);
        
        SymbolMapping<Integer> mapping = 
                new SymbolMapping.IdentityIntegerEnumMapping(
                        (TypeDef.Enum) schema.getNamedTypes().stream()
                        .filter(t -> t.getName().equals("TestEnum")).findAny().get().getType());
        
        assertNull("No mapping unless an enum", group.getField("notEnum").getBinding().getSymbolMapping());
        
        assertEquals("Enum has identity mapping", mapping, group.getField("e1").getBinding().getSymbolMapping());
        
        assertEquals("Integer enum has identity mapping", 
                mapping, group.getField("e2").getBinding().getSymbolMapping());

        assertEquals("Enum list has identity mapping", 
                mapping, group.getField("e3").getBinding().getSymbolMapping());

        assertEquals("Enum array has identity mapping", 
                mapping, group.getField("e4").getBinding().getSymbolMapping());
    }

    public static enum TestEnum {
        V1,
        V2,
        V3
    }
    
    @Id(1)
    public static class EnumMessage {
        public int notEnum;
        
        public TestEnum e1;
        
        @Enumeration(TestEnum.class)
        public int e2;
        
        @Sequence(TestEnum.class)
        public List<TestEnum> e3;
        
        public TestEnum[] e4;
    }
}
