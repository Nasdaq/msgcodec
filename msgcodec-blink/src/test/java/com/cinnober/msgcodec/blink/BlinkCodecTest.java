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
package com.cinnober.msgcodec.blink;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.Group;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.io.ByteBufferBuf;
import com.cinnober.msgcodec.messages.MetaProtocol;

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
        Schema schema = new SchemaBuilder().build(Hello.class);
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec.encode(new Hello("Hello World"), bout);
        assertArrayEquals("Encoded Hello World", expected, bout.toByteArray());

        // ensure that the message can be parsed as well
        Hello msg = (Hello) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Hello greeting", "Hello World", msg.getGreeting());
    }

    /** Example from the Blink Specification beta2 - 2013-02-05, chapter 1.
     * Here the schema is bound to Group objects.
     */
    @Test
    public void testHelloExample2() throws IOException {
        byte[] expected = new byte[]
                { 0x0d, 0x01, 0x0b, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64 };
        Schema schema = new SchemaBuilder().build(Hello.class);
        schema = Group.bind(schema);
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Group hello = new Group(schema, "Hello");
        hello.set("greeting", "Hello World");
        codec.encode(hello, bout);
        assertArrayEquals("Encoded Hello World", expected, bout.toByteArray());

        // ensure that the message can be parsed as well
        Group msg = (Group) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Hello greeting", "Hello World", msg.get("greeting"));
    }

    @Test
    public void testBrokenHelloEncode() throws Exception {
        Schema schema = new SchemaBuilder().build(Hello.class);
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        Hello msg1 = new Hello(); // missing required field
        IllegalArgumentException msg1Error = null;
        try {
            codec.encode(msg1, bout);
        } catch (IllegalArgumentException e) {
            msg1Error = e;
        }
        assertNotNull("Msg1 encoding error", msg1Error);
        bout.reset();

        Hello msg2 = new Hello("123");
        codec.encode(msg2, bout);

        Hello msg2dec = (Hello) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(new Hello("123"), msg2dec);
    }

    @Test
    public void testBrokenHelloDecodeNull() throws Exception {
        Schema schema = new SchemaBuilder().build(Hello.class);
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();

        byte[] dataNull = new byte[]
                { 0x0d, // length
            0x01, // group id
            (byte)0xc0 // null, i.e. missing required value
        };

        ByteArrayInputStream in = new ByteArrayInputStream(dataNull);

        DecodeException decodeError = null;
        try {
            codec.decode(in);
        } catch (DecodeException e) {
            decodeError = e;
            //e.printStackTrace(System.out);
        }
        assertNotNull("Decoding error", decodeError);
    }

    @Test
    public void testBrokenHelloDecodeEof() throws Exception {
        Schema schema = new SchemaBuilder().build(Hello.class);
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();

        byte[] dataNull = new byte[]
                { 0x0d, // length
            0x01 // group id
        };

        ByteArrayInputStream in = new ByteArrayInputStream(dataNull);

        DecodeException decodeError = null;
        try {
            codec.decode(in);
        } catch (DecodeException e) {
            decodeError = e;
            //e.printStackTrace(System.out);
        }
        assertNotNull("Decoding error", decodeError);
    }

    @Test
    public void testDynamicGroups() throws IOException {
        Schema schema = new SchemaBuilder().build(Foo.class, Bar.class);
        System.out.println("Schema:\n" + schema);
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();
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
        System.out.println("HEX:\n" + ByteArrays.toHex(bout.toByteArray()));
        assertArrayEquals(expected, bout.toByteArray());

        // test that we can parse the object
        Bar bar5Decoded = (Bar) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Decodec bar5", bar5, bar5Decoded);

        // make sure we can repeat this, i.e. that there are not any corrupt data left in the codec
        bout.reset();
        codec.encode(bar5, bout);
        assertArrayEquals(expected, bout.toByteArray());

    }
    
    @Test
    public void testMultipleDynamics() throws IllegalArgumentException, IOException {
        Schema schema = new SchemaBuilder().build(DynamicMsgs.class, IntMsg.class);
        byte[] byteData = new byte[1024];
        ByteBuf buffer = new ByteBufferBuf(ByteBuffer.wrap(byteData));
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();

        codec.encode(new DynamicMsgs(new IntMsg(42), null, null), buffer);
        
        buffer.clear();
        codec.encode(new DynamicMsgs(new IntMsg(42), new IntMsg(1), new IntMsg(2)), buffer);
    }

    @Test
    public void testDates1() throws IOException {
        Schema schema = new SchemaBuilder().build(DateMsg.class);
        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();

        long dayInMillis = 24 * 3600 * 1000;

        DateMsg d1 = new DateMsg();
        d1.days1970 = new Date(9 * dayInMillis);

        byte[] exp1 = new byte[] {
            // DateMsg
            0x05, // size
            0x04, // Msg type: DateMsg has ID 4
            0x09, // days1970=9
            (byte)0xc0, // days2000=null
            (byte)0xc0, // seconds1970=null
            (byte)0xc0, // seconds2000=null
        };

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec.encode(d1, bout);
        System.out.println("HEX:\n" + ByteArrays.toHex(bout.toByteArray()));
        assertArrayEquals(exp1, bout.toByteArray());

        // test that we can parse the object
        DateMsg d1Decoded = (DateMsg) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Decoded", d1, d1Decoded);
    }

    @Test
    public void testMetaProtocolEncodeDecode() throws IOException {
        Schema classSchema = MetaProtocol.getSchema();
        MsgCodec classCodec = new BlinkCodecFactory(classSchema).createCodec();

        Schema groupSchema = Group.bind(classSchema.unbind());
        MsgCodec groupCodec = new BlinkCodecFactory(groupSchema).createCodec();

        Object classMsg = classSchema.toMessage();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        classCodec.encode(classMsg, bout);

        Object decodedClassMsg = classCodec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(classMsg, decodedClassMsg);

        Group decodedGroupMsg = (Group) groupCodec.decode(new ByteArrayInputStream(bout.toByteArray()));
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        groupCodec.encode(decodedGroupMsg, bout2);

        assertArrayEquals(bout.toByteArray(), bout2.toByteArray());
    }

    @Test(expected = DecodeException.class)
    public void testFailDecodeAbstractMessage() throws IOException {
        Schema schema = new SchemaBuilder().build(AbstractMessage.class);
        BlinkCodec codec = new BlinkCodecFactory(schema).createCodec();

        byte[] blink = new byte[]{0x01, 0x05}; // size, type
        ByteArrayInputStream in = new ByteArrayInputStream(blink);
        try {
            codec.decode(in);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @Id(1)
    public static class Hello extends MsgObject {
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
    }

    @Id(2)
    public static class Foo extends MsgObject {
        @Id(1)
        private int id;
        @Id(2)
        @Dynamic
        private Foo foo;
        public Foo() {}
        public Foo(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public Foo getFoo() {
            return foo;
        }
        public void setFoo(Foo foo) {
            this.foo = foo;
        }
    }

    @Id(3)
    public static class Bar extends Foo {
        @Id(3)
        private int data;
        public Bar() {}
        public Bar(int id) {
            super(id);
        }
        public int getData() {
            return data;
        }
        public void setData(int data) {
            this.data = data;
        }
    }

    @Id(4)
    public static class DateMsg extends MsgObject {
        @Id(1)
        @Time(unit = TimeUnit.DAYS, epoch = Epoch.UNIX, timeZone = "")
        public Date days1970;

        @Id(2)
        @Time(unit = TimeUnit.DAYS, epoch = Epoch.Y2K, timeZone = "")
        public Date days2000;

        @Id(3)
        @Time(unit = TimeUnit.SECONDS, epoch = Epoch.UNIX, timeZone = "")
        public Date seconds1970;

        @Id(4)
        @Time(unit = TimeUnit.SECONDS, epoch = Epoch.Y2K, timeZone = "")
        public Date seconds2000;
    }

    @Id(5)
    public static abstract class AbstractMessage extends MsgObject {
    }
    
    @Id(7)
    public static class DynamicMsgs {
        @Dynamic
        AbstractMessage first;
        @Dynamic
        AbstractMessage second;
        @Dynamic
        AbstractMessage third;
        
        public DynamicMsgs() {
        }
        
        public DynamicMsgs(AbstractMessage first, AbstractMessage second, AbstractMessage third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
    
    @Id(8)
    public static class IntMsg extends AbstractMessage {
        int value;
        
        public IntMsg() {
        }
        
        public IntMsg(int value) {
            this.value = value;
        }
    }
}
