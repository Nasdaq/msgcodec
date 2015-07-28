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