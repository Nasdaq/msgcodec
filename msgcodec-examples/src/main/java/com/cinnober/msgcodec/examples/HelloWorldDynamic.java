/*
 * Copyright (c) 2013 Cinnober Financial Technology AB, Stockholm,
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
package com.cinnober.msgcodec.examples;

import com.cinnober.msgcodec.Group;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.blink.BlinkCodecFactory;
import com.cinnober.msgcodec.examples.messages.Hello;
import com.cinnober.msgcodec.json.JsonCodecFactory;
import com.cinnober.msgcodec.messages.MetaProtocol;
import com.cinnober.msgcodec.messages.MetaSchema;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.xml.XmlCodecFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/** This is a somewhat advanced example, that demonstrates the dynamic binding feature of msgcodec,
 * as well as the encoding the protocol schema itself.
 *
 * <p>This example might be useful for writing applications like:
 * <ul>
 * <li>Generic message format conversion, from e.g. binary blink to json (and back).
 * <li>Generic message log parser, both from file or maybe even as a wireshark plugin.
 * </ul>
 *
 * @author Mikael Brannstrom
 *
 */
public class HelloWorldDynamic {
    private static final Charset UTF8 = Charset.forName("UTF8");

    private static final String JSON = "JSON";
    private static final String BLINK = "Blink";
    private static final String XML = "XML";

    public static void main(String... args) throws Exception {
        String format = args.length > 0 ? args[0] :  XML;

        // we've got the protocol schema in an encoded format, from somewhere
        byte[] encodedSchema = getEncodedSchema(format);

        // lets just dump it to see what it looks like
        System.out.println("Encoded schema:");
        if (isBinary(format)) {
            System.out.println(ByteArrays.toHex(encodedSchema));
        } else {
            System.out.println(new String(encodedSchema, UTF8));
        }

        // we need to parse the schema
        MsgCodec metaCodec = createCodec(format, MetaProtocol.getSchema());
        MetaSchema metaMessage =
                (MetaSchema) metaCodec.decode(new ByteArrayInputStream(encodedSchema));
        Schema schema = metaMessage.toSchema();

        // now we've got a dictionary, but it is not bound to any java messages (e.g. Hello.class)
        // let's assume we do not have any message classes corresponding to this protocol,
        // instead bind it to the generic Group class
        schema = Group.bind(schema);

        // create a codec
        MsgCodec codec = createCodec(format, schema);

        // we've got an encoded message, from somewhere
        byte[] encodedMessage = getEncodedMessage(format);

        // just dump it to see what it looks like
        System.out.println("Encoded message:");
        if (isBinary(format)) {
            System.out.println(ByteArrays.toHex(encodedMessage));
        } else {
            System.out.println(new String(encodedMessage, UTF8));
        }

        // decode and print
        Group message = (Group) codec.decode(new ByteArrayInputStream(encodedMessage));
        System.out.println("Decoded message:\n" + message);

        // have a look at the message type
        GroupDef groupDef = schema.getGroup(message.getGroupName());
        System.out.println(groupDef.toString());
    }

    private static MsgCodec createCodec(String format, Schema schema) throws Exception {
        switch (format) {
        case XML:
            return new XmlCodecFactory(schema).createCodec();
        case JSON:
            return new JsonCodecFactory(schema).createCodec();
        case BLINK:
            return new BlinkCodecFactory(schema).createCodec();
        default:
            throw new Error("Unhandled format: " + format);
        }
    }

    private static boolean isBinary(String format) {
        switch (format) {
        case XML:
        case JSON:
            return false;
        case BLINK:
            return true;
        default:
            throw new Error("Unhandled format: " + format);
        }
    }

    /** Helper to generate an encoded schema in the specified format. */
    private static byte[] getEncodedSchema(String format) throws Exception {
        // Create the schema from some source, here we generate it from java messages
        Schema schema = new SchemaBuilder().build(Hello.class);

        // Obtain the meta schema, that can describe a dictionary
        Schema metaSchema = MetaProtocol.getSchema();
        // create a codec for the meta schema
        MsgCodec codec = createCodec(format, metaSchema);

        // encode the schema (containing Hello) using the codec
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(schema.toMessage(), out);

        return out.toByteArray();
    }

    /** Helper to generate an encoded message in the specified format. */
    private static byte[] getEncodedMessage(String format) throws Exception {
        // Create the schema from some source, here we generate it from java messages
        Schema schema = new SchemaBuilder().build(Hello.class);

        Hello hello = new Hello("I come in peace!");
        MsgCodec codec = createCodec(format, schema);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(hello, out);
        return out.toByteArray();

    }

}
