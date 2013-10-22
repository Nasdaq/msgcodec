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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.examples.messages.Person;
import com.cinnober.msgcodec.examples.messages.Numbers;
import com.cinnober.msgcodec.examples.messages.Carpenter;
import com.cinnober.msgcodec.json.JsonCodec;
import com.cinnober.msgcodec.xml.XmlCodec;

/** Basic example of message codec. Create a dictionary from java messages, encode and decode using the
 * JSON and XML codecs.
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
        StreamCodec jsonCodec = new JsonCodec(dictionary);
        StreamCodec xmlCodec = new XmlCodec(dictionary);

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
