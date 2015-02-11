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


import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.Group;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Time;
import static com.cinnober.msgcodec.blink.TestUtil.*;
import com.cinnober.msgcodec.messages.MetaProtocol;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author mikael.brannstrom
 *
 */
public class BlinkCodecTest {

    @Test
    public void testHelloExampleBytecode() throws IOException {
        testHelloExample(CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY);
    }
    @Test
    public void testHelloExampleInstruction() throws IOException {
        testHelloExample(CodecOption.INSTRUCTION_CODEC_ONLY);
    }

    /** Example from the Blink Specification beta2 - 2013-02-05, chapter 1.
     */
    private void testHelloExample(CodecOption codecOption) throws IOException {
        byte[] expected = new byte[]
                { 0x0d, 0x01, 0x0b, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64 };
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);
        StreamCodec codec = new BlinkCodecFactory(dictionary).setCodecOption(codecOption).createStreamCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec.encode(new Hello("Hello World"), bout);
        assertEquals("Encoded Hello World", expected, bout.toByteArray());

        // ensure that the message can be parsed as well
        Hello msg = (Hello) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Hello greeting", "Hello World", msg.getGreeting());
    }

    @Test
    public void testHelloExample2Bytecode() throws IOException {
        testHelloExample2(CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY);
    }
    @Test
    public void testHelloExample2Instruction() throws IOException {
        testHelloExample2(CodecOption.INSTRUCTION_CODEC_ONLY);
    }

    /** Example from the Blink Specification beta2 - 2013-02-05, chapter 1.
     * Here the dictionary is bound to Group objects.
     */
    private void testHelloExample2(CodecOption codecOption) throws IOException {
        byte[] expected = new byte[]
                { 0x0d, 0x01, 0x0b, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64 };
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);
        dictionary = Group.bind(dictionary);
        StreamCodec codec = new BlinkCodecFactory(dictionary).setCodecOption(codecOption).createStreamCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Group hello = new Group(dictionary, "Hello");
        hello.set("greeting", "Hello World");
        codec.encode(hello, bout);
        assertEquals("Encoded Hello World", expected, bout.toByteArray());

        // ensure that the message can be parsed as well
        Group msg = (Group) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Hello greeting", "Hello World", msg.get("greeting"));
    }

    @Test
    public void testBrokenHelloBytecode() throws Exception {
        testBrokenHello(CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY);
    }
    @Test
    public void testBrokenHelloInstruction() throws Exception {
        testBrokenHello(CodecOption.INSTRUCTION_CODEC_ONLY);
    }

    private void testBrokenHello(CodecOption codecOption) throws Exception {
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);
        StreamCodec codec = new BlinkCodecFactory(dictionary).setCodecOption(codecOption).createStreamCodec();
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
    public void testDynamicGroupsBytecode() throws IOException {
        testDynamicGroups(CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY);
    }
    @Test
    public void testDynamicGroupsInstruction() throws IOException {
        testDynamicGroups(CodecOption.INSTRUCTION_CODEC_ONLY);
    }
    
    private void testDynamicGroups(CodecOption codecOption) throws IOException {
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Foo.class, Bar.class);
        System.out.println("Dictionary:\n" + dictionary);
        StreamCodec codec = new BlinkCodecFactory(dictionary).setCodecOption(codecOption).createStreamCodec();
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

    @Test
    public void testDates1Bytecode() throws IOException {
        testDates1(CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY);
    }
    @Test
    public void testDates1Instruction() throws IOException {
        testDates1(CodecOption.INSTRUCTION_CODEC_ONLY);
    }

    private void testDates1(CodecOption codecOption) throws IOException {
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(DateMsg.class);
        StreamCodec codec = new BlinkCodecFactory(dictionary).setCodecOption(codecOption).createStreamCodec();

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
        System.out.println("HEX:\n" + TestUtil.toHex(bout.toByteArray()));
        assertEquals(exp1, bout.toByteArray());

        // test that we can parse the object
        DateMsg d1Decoded = (DateMsg) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals("Decoded", d1, d1Decoded);
    }

    @Test
    public void testMetaProtocolEncodeDecodeBytecode() throws IOException {
        testMetaProtocolEncodeDecode(CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY);
    }
    @Test
    public void testMetaProtocolEncodeDecodeInstruction() throws IOException {
        testMetaProtocolEncodeDecode(CodecOption.INSTRUCTION_CODEC_ONLY);
    }

    private void testMetaProtocolEncodeDecode(CodecOption codecOption) throws IOException {
        ProtocolDictionary classDict = MetaProtocol.getProtocolDictionary();
        StreamCodec classCodec = new BlinkCodecFactory(classDict).setCodecOption(codecOption).createStreamCodec();

        ProtocolDictionary groupDict = Group.bind(classDict.unbind());
        StreamCodec groupCodec = new BlinkCodecFactory(groupDict).setCodecOption(codecOption).createStreamCodec();

        Object classMsg = classDict.toMessage();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        classCodec.encode(classMsg, bout);

        Object decodedClassMsg = classCodec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(classMsg, decodedClassMsg);

        Group decodedGroupMsg = (Group) groupCodec.decode(new ByteArrayInputStream(bout.toByteArray()));
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        groupCodec.encode(decodedGroupMsg, bout2);

        assertArrayEquals(bout.toByteArray(), bout2.toByteArray());
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


}
