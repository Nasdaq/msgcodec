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

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.blink.BlinkCodecFactory;
import com.cinnober.msgcodec.examples.messages.Hello;
import com.cinnober.msgcodec.json.JsonCodecFactory;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.xml.XmlCodecFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * Basic example of message codec. 
 * Create a schema from java messages, encode and decode using the codecs blink, xml and json.
 *
 * @author Mikael Brannstrom
 *
 */
public class HelloWorld {
    public static void main(String... args) throws Exception {
        // build a schema from Java messages
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(Hello.class);

        // print the protocol schema
        System.out.println("Protocol schema:");
        System.out.println(schema.toString());

        // create a bunch of codecs, they can be reused
        MsgCodec blinkCodec = new BlinkCodecFactory(schema).createCodec();
        MsgCodec jsonCodec = new JsonCodecFactory(schema).createCodec();
        MsgCodec xmlCodec = new XmlCodecFactory(schema).createCodec();

        System.out.println("\n* Blink *");
        encodeAndDecodeSome(blinkCodec, true);

        System.out.println("\n* JSON *");
        encodeAndDecodeSome(jsonCodec, false);

        System.out.println("\n* XML *");
        encodeAndDecodeSome(xmlCodec, false);
    }

    private static void encodeAndDecodeSome(MsgCodec codec, boolean hexDump) throws Exception {
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
