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

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.blink.BlinkCodec;
import com.cinnober.msgcodec.blink.BlinkCodecFactory;
import com.cinnober.msgcodec.examples.messages.Hello;
import com.cinnober.msgcodec.json.JsonCodec;
import com.cinnober.msgcodec.json.JsonCodecFactory;
import com.cinnober.msgcodec.tap.TapCodec;
import com.cinnober.msgcodec.tap.TapCodecFactory;
import com.cinnober.msgcodec.util.ByteArrays;
import com.cinnober.msgcodec.xml.XmlCodec;
import com.cinnober.msgcodec.xml.XmlCodecFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * Basic example of message codec. 
 * Create a dictionary from java messages, encode and decode using the codecs blink, xml, json and tap.
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
        StreamCodec blinkCodec = new BlinkCodecFactory(dictionary).createStreamCodec();
        StreamCodec jsonCodec = new JsonCodecFactory(dictionary).createStreamCodec();
        StreamCodec xmlCodec = new XmlCodecFactory(dictionary).createStreamCodec();
        StreamCodec tapCodec = new TapCodecFactory(dictionary).createStreamCodec();

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
