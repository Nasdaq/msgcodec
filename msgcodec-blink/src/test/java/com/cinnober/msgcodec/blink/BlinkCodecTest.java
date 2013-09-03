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
package com.cinnober.msgcodec.blink;

import static org.junit.Assert.*;
import static com.cinnober.msgcodec.blink.TestUtil.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.cinnober.msgcodec.Annotations;
import com.cinnober.msgcodec.Group;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;

/**
 * @author mikael.brannstrom
 *
 */
public class BlinkCodecTest {

    /** Example from the Blink Specification beta2 - 2013-02-05, chapter 1.
     */
    @Test
    public void testHelloExample() throws IOException {
        byte[] expected = new byte[]
                { 0x0d, 0x01, 0x0b, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64 };
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);
        StreamCodec codec = new BlinkCodec(dictionary);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec.encode(new Hello("Hello World"), bout);
        assertEquals("Encoded Hello World", expected, bout.toByteArray());

        // ensure that the message can be parsed as well
        Hello msg = (Hello) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Hello greeting", "Hello World", msg.getGreeting());
    }

    /** Example from the Blink Specification beta2 - 2013-02-05, chapter 1.
     * Here the dictionary is bound to Group objects.
     */
    @Test
    public void testHelloExample2() throws IOException {
        byte[] expected = new byte[]
                { 0x0d, 0x01, 0x0b, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64 };
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);
        dictionary = Group.bind(dictionary);
        StreamCodec codec = new BlinkCodec(dictionary);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Group hello = new Group("Hello");
        hello.put("greeting", "Hello World");
        codec.encode(hello, bout);
        assertEquals("Encoded Hello World", expected, bout.toByteArray());

        // ensure that the message can be parsed as well
        Group msg = (Group) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Hello greeting", "Hello World", msg.get("greeting"));
    }

    @Test
    public void testHelloExampleMaxLength() throws IOException {
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);
        Annotations annotations = new Annotations();
        annotations.path("Hello", "greeting").put("maxLength", "5");
        ProtocolDictionary dictionary5 = dictionary.addAnnotations(annotations);
        annotations.path("Hello", "greeting").put("maxLength", "11");
        ProtocolDictionary dictionary11 = dictionary.addAnnotations(annotations);

        StreamCodec codec = new BlinkCodec(dictionary);
        StreamCodec codec5 = new BlinkCodec(dictionary5);
        StreamCodec codec11 = new BlinkCodec(dictionary11);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream bout5 = new ByteArrayOutputStream();
        ByteArrayOutputStream bout11 = new ByteArrayOutputStream();
        Hello msg = new Hello("Hello Wörld");

        // encode, unlimit
        codec.encode(msg, bout); // OK
        // encode, 11 chars
        codec11.encode(msg, bout11); // OK

        // encode, 5 chars
        try {
            codec5.encode(msg, bout5); // should fail
            fail("Expected exception");
        } catch(IOException e) {}

        // decode, unlimit
        codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        // decode, 11 chars
        codec11.decode(new ByteArrayInputStream(bout.toByteArray()));
        // decode, 5 chars
        try {
            codec5.decode(new ByteArrayInputStream(bout.toByteArray()));
            fail("Expected exception");
        } catch(IOException e) {}
    }

    @Test
    public void testDynamicGroups() throws IOException {
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Foo.class, Bar.class);
        System.out.println("Dictionary:\n" + dictionary);
        StreamCodec codec = new BlinkCodec(dictionary);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        Foo foo1 = new Foo(1);
        Foo foo2 = new Foo(2);
        Foo foo3 = new Foo(3);
        Bar bar4 = new Bar(4);
        bar4.setData(9);
        Bar bar5 = new Bar(5);
        bar5.setData(10);

        foo1.setFoo(foo2);
        foo2.setFoo(bar4);
        bar4.setFoo(foo3);
        bar5.setFoo(foo1);

        // Bar5
        //  foo=Foo1
        //       foo=Foo2
        //            foo=Bar4
        //                 foo=Foo3
        //                      foo=null
        //                 data=9
        //  data=10

        byte[] expected = new byte[] {
            // Bar5
            0x11, // size
            0x03, // Msg type: Bar has ID 3
            0x05, // id=5
            // Bar5.foo=Foo1
            0x0d, // size
            0x02, // Msg type: Foo has ID 2
            0x01, // id=1
            // Foo1.foo=Foo2
            0x0a, // size
            0x02, // Msg type: Foo has ID 2
            0x02, // id=1
            // Foo2.foo=Bar4
            0x07, // size
            0x03, // Msg type: Bar has ID 3
            0x04, // id=4
            // Bar4.foo=Foo3
            0x03, // size
            0x02, // Msg type: Foo has ID 2
            0x03, // id=3
            (byte)0xc0, // Foo3.foo=null
            0x09, // Bar4.data=9
            0x0a, // Bar5.data=10
        };
        codec.encode(bar5, bout);
        System.out.println("HEX:\n" + TestUtil.toHex(bout.toByteArray()));
        assertEquals(expected, bout.toByteArray());

        // test that we can parse the object
        Bar bar5Decoded = (Bar) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Decodec bar5", bar5, bar5Decoded);

        // make sure we can repeat this, i.e. that there are not any corrupt data left in the codec
        bout.reset();
        codec.encode(bar5, bout);
        assertEquals(expected, bout.toByteArray());

    }

    @Id(1)
    public static class Hello {
        private String greeting;
        public Hello() {}
        public Hello(String greeting) {
            this.greeting = greeting;
        }
        @Required
        public String getGreeting() {
            return greeting;
        }
        public void setGreeting(String greeting) {
            this.greeting = greeting;
        }
    }

    @Id(2)
    public static class Foo {
        private int id;
        private Foo foo;
        public Foo() {}
        public Foo(int id) {
            this.id = id;
        }
        @Id(1)
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        @Id(2)
        public Foo getFoo() {
            return foo;
        }
        public void setFoo(Foo foo) {
            this.foo = foo;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Foo other = (Foo) obj;
            if (foo == null) {
                if (other.foo != null)
                    return false;
            } else if (!foo.equals(other.foo))
                return false;
            if (id != other.id)
                return false;
            return true;
        }
    }

    @Id(3)
    public static class Bar extends Foo {
        private int data;
        public Bar() {}
        public Bar(int id) {
            super(id);
        }
        public int getData() {
            return data;
        }
        @Id(3)
        public void setData(int data) {
            this.data = data;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            Bar other = (Bar) obj;
            if (data != other.data)
                return false;
            return true;
        }
    }



}
