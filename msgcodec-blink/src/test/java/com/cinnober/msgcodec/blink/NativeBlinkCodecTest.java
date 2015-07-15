/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
 * Sweden. All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 * 
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */
package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Unsigned;
import com.cinnober.msgcodec.io.ByteArrayBuf;
import com.cinnober.msgcodec.io.ByteBuf;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikael.brannstrom
 */
public class NativeBlinkCodecTest {

    public NativeBlinkCodecTest() {
    }

    @Test
    public void testEncodeDecodeIntMessage() throws IOException {
        Schema schema = new SchemaBuilder().build(IntMessage.class);
        NativeBlinkCodec codec = new NativeBlinkCodecFactory(schema).createCodec();

        ByteBuf buf = new ByteArrayBuf(1024);
        Object msg1 = new IntMessage(1,2,3,4);
        codec.encode(msg1, buf);

        buf.flip();
        Object msg2 = codec.decode(buf);
        assertEquals(msg1, msg2);
    }


    @Id(1)
    public static class IntMessage extends MsgObject {
        public int a;
        @Unsigned
        public int b;
        public long c;
        @Unsigned
        public long d;

        public IntMessage() {
        }

        public IntMessage(int a, int b, long c, long d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
    }
}
