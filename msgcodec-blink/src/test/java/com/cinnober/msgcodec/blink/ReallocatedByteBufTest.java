package com.cinnober.msgcodec.blink;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.io.ReallocatingByteBuf;

public class ReallocatedByteBufTest {

    @Id(1)
    public static class InternalMessageObject {
        public int id;
        public String text;
        public boolean flag;

        public InternalMessageObject() {}

        public InternalMessageObject(int id, String text, boolean flag) {}
    }
    
    
    @Test
    public void testEncodeAndDecodeWithMsgCodecToReallocatedByteBuf() throws IOException {
        InternalMessageObject obj = new InternalMessageObject();
        obj.id = 42;
        obj.text = "Hello World!";
        obj.flag = true;

        Schema schema = new SchemaBuilder().build(obj.getClass());

        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();

        ReallocatingByteBuf buf = new ReallocatingByteBuf(8, 1024, ByteBuffer::allocate);
        codec.encode(obj, buf);
        obj.id = 100043;
        codec.encode(obj, buf);

        buf.flip();
        InternalMessageObject output1 = (InternalMessageObject) codec.decode(buf);
        InternalMessageObject output2 = (InternalMessageObject) codec.decode(buf);

        assertEquals(42, output1.id);
        assertEquals(100043, output2.id);
    }

}
