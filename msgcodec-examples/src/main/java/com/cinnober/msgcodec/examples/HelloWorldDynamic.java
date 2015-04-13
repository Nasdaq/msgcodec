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
package com.cinnober.msgcodec.examples;

import com.cinnober.msgcodec.Group;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.blink.BlinkCodec;
import com.cinnober.msgcodec.blink.BlinkCodecFactory;
import com.cinnober.msgcodec.examples.messages.Hello;
import com.cinnober.msgcodec.json.JsonCodec;
import com.cinnober.msgcodec.json.JsonCodecFactory;
import com.cinnober.msgcodec.messages.MetaProtocol;
import com.cinnober.msgcodec.messages.MetaProtocolDictionary;
import com.cinnober.msgcodec.tap.TapCodec;
import com.cinnober.msgcodec.tap.TapCodecFactory;
import com.cinnober.msgcodec.util.ByteArrays;
import com.cinnober.msgcodec.xml.XmlCodec;
import com.cinnober.msgcodec.xml.XmlCodecFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

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
            System.out.println(ByteArrays.toHex(encodedDictionary));
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
            System.out.println(ByteArrays.toHex(encodedMessage));
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
            return new XmlCodecFactory(dictionary).createStreamCodec();
        case JSON:
            return new JsonCodecFactory(dictionary).createStreamCodec();
        case BLINK:
            return new BlinkCodecFactory(dictionary).createStreamCodec();
        case TAP:
            return new TapCodecFactory(dictionary).createStreamCodec();
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
