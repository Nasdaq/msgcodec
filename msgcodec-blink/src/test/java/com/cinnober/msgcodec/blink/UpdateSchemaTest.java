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

public class UpdateSchemaTest {


	
	public void printStream(ByteArrayOutputStream stream) {
        byte[] arr = stream.toByteArray();
        
        for(int i=0;i<arr.length;i++) {
        	System.out.print(arr[i] & 0xFF);
        	System.out.print(" ");
        }
    	System.out.println("");
	}
	
    @Test
    public void testUpdateNoSchemaConverter() throws IOException {
        Schema schema = new SchemaBuilder().build(Version1.class);
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec.encode(new Version1(124, EnumV1.VALUE1), bout);

        Schema schema2 = new SchemaBuilder().build(Version2.class);
        MsgCodec codec2 = new BlinkCodecFactory(schema2).createCodec();
        Version2 msg = (Version2) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(124L, msg.number);
        assertEquals(EnumV2.VALUE2, msg.enumeration);  // wrong enum constant expected (ordinal = 1)
    }
	
	
    @Test
    public void testUpdateInbound() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema2 = new SchemaBuilder().build(Version2.class);
        Schema schema = new SchemaBinder(schema2).bind(schema1, g -> Direction.INBOUND);

        MsgCodec codec1 = new BlinkCodecFactory(schema1).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new Version1(24, EnumV1.VALUE1), bout);

        MsgCodec codec2 = new BlinkCodecFactory(schema).createCodec();
        Version2 msg = (Version2) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(24, msg.number);
        assertEquals(EnumV2.VALUE1, msg.enumeration);
    }
	

    @Test
    public void testUpdateOutbound() throws IOException, IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().build(Version1.class);
        Schema schema2 = new SchemaBuilder().build(Version2.class);
        Schema schema = new SchemaBinder(schema1).bind(schema2, g -> Direction.OUTBOUND);

        MsgCodec codec1 = new BlinkCodecFactory(schema2).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new Version2(24, EnumV2.VALUE1), bout);

        MsgCodec codec2 = new BlinkCodecFactory(schema).createCodec();
        Version1 msg = (Version1) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(24, msg.number);
        assertEquals(EnumV1.VALUE1, msg.enumeration);
    }
    
    
    public static enum EnumV1 {
    	VALUE3,
    	VALUE1,
    	VALUE2,
    }
    public static enum EnumV2 {
    	VALUE1,
    	VALUE2,
    	VALUE3,
    	ADDITIONAL_VALUE,
    }
    
    @Name("Payload")
    @Id(1)
    public static class Version1 extends MsgObject {
    	public int number;
    	public EnumV1 enumeration;
    	
    	public Version1() {}
    	public Version1(int value, EnumV1 eValue) {
    		number = value;
    		enumeration = eValue; 
    	}
    }

    @Name("Payload")
    @Id(1)
    public static class Version2 extends MsgObject {
    	public long number;
    	public EnumV2 enumeration;
    	public Version2() {}
    	
    	public Version2(long value, EnumV2 eValue) {
    		number = value;
    		enumeration = eValue; 
    	}
    }
    
}
