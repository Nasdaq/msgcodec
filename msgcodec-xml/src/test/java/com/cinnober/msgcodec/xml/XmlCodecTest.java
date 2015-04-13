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
