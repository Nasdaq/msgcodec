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
import com.cinnober.msgcodec.anot.Name;

public class SchemaUpgradeTest {

	
    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@Test
    public void testUpgradeEnumWidening() throws IncompatibleSchemaException {

    	FooReqV1 msg1 = new FooReqV1();
        FooReqV2 msg2 = new FooReqV2();
        
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        Schema schema = new SchemaBinder(schema2).bind(schema1, g -> Direction.INBOUND);
        
        Accessor a1 = schema.getGroup("FooReq").getField("req").getBinding().getAccessor();
        a1.setValue(msg2, new Integer(13));
        assertEquals(13, ((Integer)a1.getValue(msg2)).intValue());

        Accessor a2 = schema.getGroup("FooReq").getField("value").getBinding().getAccessor();
        a1.setValue(msg2, 1);
        assertEquals(1, ((Integer)a1.getValue(msg2)).intValue());
        
        //TODO: how to verify the internal enum mapping?
        
    }

    
    enum Version1 {
    	VALUE1,
    	VALUE2,
    	VALUE3,
    }
    enum Version2 {
    	VALUE1,
    	VALUE2,
    	VALUE3,
    	ADDITIONAL_VALUE,
    }
    
    @Annotate("dir=c2s")
    @Name("FooReq")
    public static class FooReqV1 extends MsgObject {
        public Version1 value;
    	public int req;
    }

    @Annotate("dir=s2c")
    @Name("FooRsp")
    public static class FooRspV1 extends MsgObject {
        public Version1 value;
    	public double resp;
    }

    @Annotate("dir=c2s")
    @Name("FooReq")
    public static class FooReqV2 extends MsgObject {
        public Version2 value;
    	public long req;
    }
 
    @Annotate("dir=s2c")
    @Name("FooRsp")
    public static class FooRspV2 extends MsgObject {
        public Version2 value;
    	public double resp;
    }
    
}
