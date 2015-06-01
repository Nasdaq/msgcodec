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

package com.cinnober.msgcodec.examples.upgrade;

import com.cinnober.msgcodec.Group;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinder;
import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.blink.BlinkCodecFactory;
import com.cinnober.msgcodec.io.ByteArrayBuf;
import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.messages.MetaProtocol;
import com.cinnober.msgcodec.messages.MetaSchema;

/**
 * Example of a "file"
 * @author mikael.brannstrom
 */
public class SchemaUpgradeWithGroup {

    public static void main(String[] args) throws Exception {
        MsgCodec metaCodec = new BlinkCodecFactory(MetaProtocol.getSchema()).createCodec();
        ByteBuf buf1 = new ByteArrayBuf(new byte[1000_000]);

        // == PHASE 1: Some application writes some Pets to a file (here buffer)
        Schema schema1 = new SchemaBuilder().build(Pet1.class);
        MsgCodec codec1 = new BlinkCodecFactory(schema1).createCodec();
        // write schema using MetaProtocol, so the Pets can be decoded later on
        metaCodec.encode(schema1.toMessage(), buf1);
        // then write 3 pets
        codec1.encode(new Pet1("Buster"), buf1);
        codec1.encode(new Pet1("Rosa"), buf1);
        codec1.encode(new Pet1("Felix"), buf1);
        buf1.flip();

        // == PHASE 2: A "schema upgrade application" upgrades the contents of the file,
        //             reads buf1 stores the result in buf2

        // Schema2 is the new schema, here bound to Group
        Schema schema2 = Group.bind(new SchemaBuilder().build(Pet2.class));
        // parse old schema from buf1
        Schema oldSchema = ((MetaSchema)metaCodec.decode(buf1)).toSchema();
        // downgrade schema2 to be able to read the old schema
        Schema downgradedSchema2 = new SchemaBinder(schema2).bind(oldSchema, g -> Direction.INBOUND);
        MsgCodec downgradedCodec2 = new BlinkCodecFactory(downgradedSchema2).createCodec();
        MsgCodec codec2 = new BlinkCodecFactory(schema2).createCodec();

        ByteBuf buf2 = new ByteArrayBuf(new byte[1000_000]);
        metaCodec.encode(schema2.toMessage(), buf2);
        while (buf1.position() != buf1.limit()) {
            Group g = (Group) downgradedCodec2.decode(buf1);
            System.out.println("Read group: " + g);
            codec2.encode(g, buf2);
        }
        buf2.flip();
        // now buf1 and buf2 are equivalent, with the only difference in schema used.

        System.out.println("Buf1 size: " + buf1.limit());
        System.out.println("Buf2 size: " + buf2.limit());
    }

    @Id(1)
    @Name("Pet")
    public static class Pet1 extends MsgObject {
        String name;

        public Pet1() {}
        public Pet1(String name) {
            this.name = name;
        }
    }

    @Id(1)
    @Name("Pet")
    public static class Pet2 extends MsgObject {
        String name;
        Double weight;
    }

}
