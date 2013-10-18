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
package com.cinnober.msgcodec.xml;

import org.junit.Assert;
import org.junit.Test;

import com.cinnober.msgcodec.Annotations;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Required;

/**
 * @author mikael.brannstrom
 *
 */
public class XmlCodecTest {

    @Test
    public void testDecodeHello() throws Exception {
        System.out.println("--- testDecodeHello ---");
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);

        Annotations annot = new Annotations();
        annot.path("Hello", "greeting").put("xml:field", "element");
        dictionary = dictionary.replaceAnnotations(annot);

        StreamCodec codec = new XmlCodec(dictionary);

        Hello msg = (Hello) codec.decode(XmlCodecTest.class.getResourceAsStream("hello1.xml"));
        Assert.assertEquals("Hello world!", msg.getGreeting());

        codec.encode(msg, System.out);
    }

    @Test
    public void testDecodeHelloAttribute() throws Exception {
        System.out.println("--- testDecodeHelloAttribute ---");
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);

        Annotations annot = new Annotations();
        annot.path("Hello", "greeting").put("xml:field", "attribute");
        dictionary = dictionary.replaceAnnotations(annot);

        StreamCodec codec = new XmlCodec(dictionary);

        Hello msg = (Hello) codec.decode(XmlCodecTest.class.getResourceAsStream("hello2.xml"));
        Assert.assertEquals("Hello world!", msg.getGreeting());

        codec.encode(msg, System.out);
    }

    @Test
    public void testDecodeMyMessage() throws Exception {
        System.out.println("--- testDecodeMyMessage ---");
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(MyMessage.class, Hello.class);

        Annotations annot = new Annotations();
        annot.path("Hello", "greeting").put("xml:field", "element");
        annot.path("MyMessage", "person1").put("xml:field", "inline");
        dictionary = dictionary.replaceAnnotations(annot);

        StreamCodec codec = new XmlCodec(dictionary);

        MyMessage msg = (MyMessage) codec.decode(XmlCodecTest.class.getResourceAsStream("hello3.xml"));
        System.out.println("Message: " + msg);

        codec.encode(msg, System.out);
    }

    @Test
    public void testDecodeMyMessageAttribute() throws Exception {
        System.out.println("--- testDecodeMyMessageAttribute ---");
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(MyMessage.class, Hello.class);

        Annotations annot = new Annotations();
        annot.path("Hello", "greeting").put("xml:field", "attribute");
        annot.path("MyMessage", "person1").put("xml:field", "inline");
        dictionary = dictionary.replaceAnnotations(annot);

        StreamCodec codec = new XmlCodec(dictionary);

        MyMessage msg = (MyMessage) codec.decode(XmlCodecTest.class.getResourceAsStream("hello4.xml"));
        System.out.println("Message: " + msg);

        codec.encode(msg, System.out);
    }
    @Test

    public void testDecodeMyMessageAttributeInlineDynamic() throws Exception {
        System.out.println("--- testDecodeMyMessageAttributeInlineDynamic ---");
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(MyMessage.class, Hello.class);

        Annotations annot = new Annotations();
        annot.path("Hello", "greeting").put("xml:field", "attribute");
        annot.path("MyMessage", "person1").put("xml:field", "inline");
        annot.path("MyMessage", "person2").put("xml:field", "inline");
        dictionary = dictionary.replaceAnnotations(annot);

        StreamCodec codec = new XmlCodec(dictionary);

        MyMessage msg = (MyMessage) codec.decode(XmlCodecTest.class.getResourceAsStream("hello5.xml"));
        System.out.println("Message: " + msg);

        codec.encode(msg, System.out);
    }

    public static class Hello {
        @Required
        private String greeting;
        public Hello() {}
        public Hello(String greeting) {
            this.greeting = greeting;
        }
        public String getGreeting() {
            return greeting;
        }
        public void setGreeting(String greeting) {
            this.greeting = greeting;
        }
        @Override
        public String toString() {
            return "Hello [greeting=" + greeting + "]";
        }
    }

    public static class MyMessage {
        private Hello person1;
        @Dynamic
        private Hello person2;
        /**
         * @return the person1
         */
        public Hello getPerson1() {
            return person1;
        }
        /**
         * @param person1 the person1 to set
         */
        public void setPerson1(Hello person1) {
            this.person1 = person1;
        }
        /**
         * @return the person2
         */
        public Hello getPerson2() {
            return person2;
        }
        /**
         * @param person2 the person2 to set
         */
        public void setPerson2(Hello person2) {
            this.person2 = person2;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "MyMessage [person1=" + person1 + ", person2=" + person2
                    + "]";
        }


    }

}
