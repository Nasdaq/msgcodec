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

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.blink.BlinkCodec;
import com.cinnober.msgcodec.examples.messages.Hello;
import com.cinnober.msgcodec.json.JsonCodec;
import com.cinnober.msgcodec.tap.TapCodec;
import com.cinnober.msgcodec.util.ByteArrays;
import com.cinnober.msgcodec.xml.XmlCodec;

/** Basic example of message codec. Create a dictionary from java messages, encode and decode using the
 * codecs blink, xml, json and tap.
 *
 * @author Mikael Brannstrom
 *
 */
public class HelloWorld {
    public static void main(String... args) throws Exception {
        // build a dictionary from Java messages
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dictionary = builder.build(Hello.class);

        // print the protocol specification
        System.out.println("Protocol specification:");
        System.out.println(dictionary.toString());

        // create a bunch of codecs, they can be reused
        StreamCodec blinkCodec = new BlinkCodec(dictionary);
        StreamCodec jsonCodec = new JsonCodec(dictionary);
        StreamCodec xmlCodec = new XmlCodec(dictionary);
        StreamCodec tapCodec = new TapCodec(dictionary);

        System.out.println("\n* Blink *");
        encodeAndDecodeSome(blinkCodec, true);

        System.out.println("\n* JSON *");
        encodeAndDecodeSome(jsonCodec, false);

        System.out.println("\n* XML *");
        encodeAndDecodeSome(xmlCodec, false);

        System.out.println("\n* TAP *");
        encodeAndDecodeSome(tapCodec, true);
    }

    private static void encodeAndDecodeSome(StreamCodec codec, boolean hexDump) throws Exception {
        // encode a message
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Hello hello = new Hello("Hello world!");
        codec.encode(hello, out);
        System.out.println("Encode message: " + hello);
        if (hexDump) {
            System.out.println(ByteArrays.toHex(out.toByteArray()));
        } else {
            System.out.println(new String(out.toByteArray(), Charset.forName("UTF8")));
        }

        // decode a message
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Hello message = (Hello) codec.decode(in);
        System.out.println("Decode message: " + message);
        System.out.println("Equals: " + hello.equals(message));
    }
}
