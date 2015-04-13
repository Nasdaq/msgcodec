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
import com.cinnober.msgcodec.examples.messages.Carpenter;
import com.cinnober.msgcodec.examples.messages.Numbers;
import com.cinnober.msgcodec.examples.messages.Person;
import com.cinnober.msgcodec.json.JsonCodec;
import com.cinnober.msgcodec.json.JsonCodecFactory;
import com.cinnober.msgcodec.xml.XmlCodec;
import com.cinnober.msgcodec.xml.XmlCodecFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Basic example of message codec.
 * Create a dictionary from java messages, encode and decode using the JSON and XML codecs.
 *
 * @author Mikael Brannstrom
 *
 */
public class MessagesExample {
    public static void main(String... args) throws Exception {
        // build a dictionary from Java messages
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dictionary = builder.build(Person.class, Carpenter.class, Numbers.class);

        // print the protocol specification
        System.out.println("Protocol specification:");
        System.out.println(dictionary.toString());

        // construct messages
        Numbers numbers = new Numbers();
        numbers.bigIntReq = BigInteger.TEN.pow(30);
        numbers.signedReq = -1;
        numbers.unsignedReq = -1; // 2^32 - 1
        numbers.decimal = BigDecimal.valueOf(123456, 3); // 123.456

        Person alice = new Person();
        alice.name = "Alice";

        Carpenter bob = new Carpenter();
        bob.name = "Bob";
        bob.tools = new String[] {"hammer", "screwdriver"};

        Person charlie = new Person();
        charlie.name = "Charlie";
        charlie.mom = alice;
        charlie.dad = bob;
        
        // Create a codec, the codec can be reused
        StreamCodec jsonCodec = new JsonCodecFactory(dictionary).createStreamCodec();
        StreamCodec xmlCodec = new XmlCodecFactory(dictionary).createStreamCodec();

        // encode to system out
        System.out.println("\n* JSON *");
        encode(jsonCodec, numbers, alice, bob, charlie);
        System.out.println("\n* XML *");
        encode(xmlCodec, numbers, alice, bob, charlie);
    }

    private static void encode(StreamCodec codec, Object... messages) throws IOException {
        for (Object message : messages) {
            codec.encode(message, System.out);
            System.out.println();
        }
    }
}
