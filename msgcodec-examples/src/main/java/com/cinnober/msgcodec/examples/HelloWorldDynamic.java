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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import com.cinnober.msgcodec.Group;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.blink.BlinkCodec;
import com.cinnober.msgcodec.examples.messages.Hello;
import com.cinnober.msgcodec.examples.util.Util;
import com.cinnober.msgcodec.json.JsonCodec;
import com.cinnober.msgcodec.messages.MetaProtocol;
import com.cinnober.msgcodec.messages.MetaProtocolDictionary;
import com.cinnober.msgcodec.tap.TapCodec;
import com.cinnober.msgcodec.xml.XmlCodec;

/** This is a somewhat advanced example, that demonstrates the dynamic binding feature of msgcodec,
 * as well as the encoding the protocol dictionary itself.
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
    private static final String TAP = "TAP";

    public static void main(String... args) throws Exception {
        String format = args.length > 0 ? args[0] :  XML;

        // we've got the protocol dictionary in an encoded format, from somewhere
        byte[] encodedDictionary = getEncodedDictionary(format);

        // lets just dump it to see what it looks like
        System.out.println("Encoded dictionary:");
        if (isBinary(format)) {
            System.out.println(Util.toHex(encodedDictionary));
        } else {
            System.out.println(new String(encodedDictionary, UTF8));
        }

        // we need to parse the dictionary
        StreamCodec metaCodec = createCodec(format, MetaProtocol.getProtocolDictionary());
        MetaProtocolDictionary metaMessage =
                (MetaProtocolDictionary) metaCodec.decode(new ByteArrayInputStream(encodedDictionary));
        ProtocolDictionary dictionary = metaMessage.toProtocolDictionary();

        // now we've got a dictionary, but it is not bound to any java messages (e.g. Hello.class)
        // let's assume we do not have any message classes corresponding to this protocol,
        // instead bind it to the generic Group class
        dictionary = Group.bind(dictionary);

        // create a codec
        StreamCodec codec = createCodec(format, dictionary);

        // we've got an encoded message, from somewhere
        byte[] encodedMessage = getEncodedMessage(format);

        // just dump it to see what it looks like
        System.out.println("Encoded message:");
        if (isBinary(format)) {
            System.out.println(Util.toHex(encodedMessage));
        } else {
            System.out.println(new String(encodedMessage, UTF8));
        }

        // decode and print
        Group message = (Group) codec.decode(new ByteArrayInputStream(encodedMessage));
        System.out.println("Decoded message:\n" + message);

        // have a look at the message type
        GroupDef groupDef = dictionary.getGroup(message.getGroupName());
        System.out.println(groupDef.toString());
    }

    private static StreamCodec createCodec(String format, ProtocolDictionary dictionary) throws Exception {
        switch (format) {
        case XML:
            return new XmlCodec(dictionary);
        case JSON:
            return new JsonCodec(dictionary);
        case BLINK:
            return new BlinkCodec(dictionary);
        case TAP:
            return new TapCodec(dictionary);
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
        case TAP:
            return true;
        default:
            throw new Error("Unhandled format: " + format);
        }
    }

    /** Helper to generate an encoded dictionary in the specified format. */
    private static byte[] getEncodedDictionary(String format) throws Exception {
        // Create the dictionary from some source, here we generate it from java messages
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);

        // Obtain the meta dictionary, that can describe a dictionary
        ProtocolDictionary metaDictionary = MetaProtocol.getProtocolDictionary();
        // create a codec for the meta dictionary
        StreamCodec codec = createCodec(format, metaDictionary);

        // encode the dictionary (containing Hello) using the codec
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(dictionary.toMessage(), out);

        return out.toByteArray();
    }

    /** Helper to generate an encoded message in the specified format. */
    private static byte[] getEncodedMessage(String format) throws Exception {
        // Create the dictionary from some source, here we generate it from java messages
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);

        Hello hello = new Hello("I come in peace!");
        StreamCodec codec = createCodec(format, dictionary);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(hello, out);
        return out.toByteArray();

    }

}
