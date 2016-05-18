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
package com.cinnober.msgcodec.json;

import com.cinnober.msgcodec.DecodeException;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.junit.Test;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Required;
import java.io.IOException;

/**
 * @author Mikael Brannstrom
 *
 */
public class JsonCodecTest {

    @Test
    public void testHello() throws Exception {
        Schema schema = new SchemaBuilder().build(Hello.class);
        MsgCodec codec = new JsonCodec(schema, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Hello msg = new Hello("Hello world!");
        codec.encode(msg, out);
        codec.encode(msg, System.out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Object msg2 = codec.decode(in);
        assertEquals(msg, msg2);

        in = new ByteArrayInputStream(
                "{\"$type\":\"Hello\", \"greeting\":\"Hello world!\"}".getBytes(Charset.forName("UTF8")));
        msg2 = codec.decode(in);
        assertEquals(msg, msg2);
    }

    @Test
    public void testStaticHello() throws Exception {
        Schema schema = new SchemaBuilder().build(Hello.class);
        JsonCodec codec = new JsonCodec(schema, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Hello msg = new Hello("Hello world!");
        codec.encodeStatic(msg, out);
        codec.encodeStatic(msg, System.out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Object msg2 = codec.decodeStatic("Hello", in);
        assertEquals(msg, msg2);

        in = new ByteArrayInputStream(out.toByteArray());
        msg2 = codec.decodeStatic(Hello.class, in);
        assertEquals(msg, msg2);

        in = new ByteArrayInputStream(
                "{\"greeting\":\"Hello world!\"}".getBytes(Charset.forName("UTF8")));
        msg2 = codec.decodeStatic("Hello", in);
        assertEquals(msg, msg2);
    }


    @Test
    public void testDecodeHelloTypeOutOfOrder() throws Exception {
        Schema schema = new SchemaBuilder().build(Hello.class);
        MsgCodec codec = new JsonCodec(schema, false);

        ByteArrayInputStream in = new ByteArrayInputStream(
                "{\"greeting\":\"Hello world!\", \"$type\":\"Hello\"}".getBytes(Charset.forName("UTF8")));
        assertEquals(new Hello("Hello world!"), codec.decode(in));
    }

    @Test
    public void testCharacterMessage() throws Exception {
        Schema schema = new SchemaBuilder().build(CharacterMessage.class);
        MsgCodec codec = new JsonCodec(schema, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CharacterMessage msg = new CharacterMessage('a', 'B', null);
        codec.encode(msg, out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Object msg2 = codec.decode(in);
        assertEquals(msg, msg2);

        final int chara = new Character('a').charValue();
        final int charB = new Character('B').charValue();
        final String data = "{\"$type\":\"CharacterMessage\", \"data1\":" + chara + ", \"data2\":" + charB + "}";
        in = new ByteArrayInputStream(data.getBytes(Charset.forName("UTF8")));
        msg2 = codec.decode(in);
        assertEquals(msg, msg2);
    }

    @Test
    public void testDecodeNestedTypeOutOfOrder1() throws Exception {
        Schema schema = new SchemaBuilder().build(Nested.class, Hello.class);
        MsgCodec codec = new JsonCodec(schema, false);

        String json = "{\"i\":123, \"$type\":\"Nested\", \"payload\": "
                + "{\"$type\":\"Hello\", \"greeting\":\"Hello world!\"}"
                + "}";
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF8")));
        assertEquals(new Nested(123, new Hello("Hello world!")), codec.decode(in));
    }
    @Test
    public void testDecodeNestedTypeOutOfOrder2() throws Exception {
        Schema schema = new SchemaBuilder().build(Nested.class, Hello.class);
        MsgCodec codec = new JsonCodec(schema, false);

        String json = "{\"i\":123, \"$type\":\"Nested\", \"payload\": "
                + "{\"greeting\":\"Hello world!\", \"$type\":\"Hello\"}"
                + "}";
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF8")));
        assertEquals(new Nested(123, new Hello("Hello world!")), codec.decode(in));
    }
    @Test
    public void testDecodeNestedTypeOutOfOrder3() throws Exception {
        Schema schema = new SchemaBuilder().build(Nested.class, Hello.class);
        MsgCodec codec = new JsonCodec(schema, false);

        String json = "{\"payload\": "
                + "{\"$type\":\"Hello\", \"greeting\":\"Hello world!\"}"
                + ", \"i\":123, \"$type\":\"Nested\"}";
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF8")));
        assertEquals(new Nested(123, new Hello("Hello world!")), codec.decode(in));
    }

    @Test(expected = DecodeException.class)
    public void testDecodeMissingRequiredField() throws IOException {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(Hello.class);
        MsgCodec codec = new JsonCodec(schema, false);

        ByteArrayInputStream in = new ByteArrayInputStream(
                "{\"$type\":\"Hello\"}".getBytes(Charset.forName("UTF8")));
        codec.decode(in);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeMissingRequiredField() throws IOException {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(Hello.class);
        MsgCodec codec = new JsonCodec(schema, false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Hello msg = new Hello(null);
        codec.encode(msg, out);
    }

    @Test
    public void testEncodeDecodeBinary() throws IOException {
        Schema schema = new SchemaBuilder().build(BinaryMessage.class);
        JsonCodec codec = new JsonCodec(schema, false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryMessage msg1 = new BinaryMessage(new byte[] {1,2,3});
        codec.encode(msg1, out);
        byte[] data = out.toByteArray();
        System.out.write(data);
        
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BinaryMessage msg2 = (BinaryMessage) codec.decode(in);
        assertEquals(msg1, msg2);
    }
    @Test
    public void testDecodeBinaryTypeOutOfOrder() throws IOException {
        Schema schema = new SchemaBuilder().build(BinaryMessage.class);
        JsonCodec codec = new JsonCodec(schema, false);

        String json = "{\"data\":\"AQID\",\"$type\":\"BinaryMessage\"}";
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF8")));
        BinaryMessage msg = (BinaryMessage) codec.decode(in);
        assertEquals(new BinaryMessage(new byte[] {1,2,3}), msg);
    }

    @Test(expected = DecodeException.class)
    public void testFailDecodeAbstractMessage() throws IOException {
        Schema schema = new SchemaBuilder().build(AbstractMessage.class);
        JsonCodec codec = new JsonCodec(schema, false);
        
        String json = "{\"$type\":\"AbstractMessage\"}";
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF8")));
        codec.decode(in);
    }

    public static class Hello extends MsgObject {
        @Required
        public String greeting;
        public Hello() {
        }
        public Hello(String greeting) {
            this.greeting = greeting;
        }
    }
    public static class Nested extends MsgObject {
        public int i;
        @Dynamic
        public Object payload;
        public Nested() {
        }
        public Nested(int i, Object payload) {
            this.i = i;
            this.payload = payload;
        }
    }
    public static class BinaryMessage extends MsgObject {
        public byte[] data;
        public BinaryMessage() {
        }
        public BinaryMessage(byte[] data) {
            this.data = data;
        }
    }
    public static class CharacterMessage extends MsgObject {
        public char data1;
        public Character data2;
        public Character data3;
        public CharacterMessage() {
        }
        public CharacterMessage(char data1, Character data2, Character data3) {
            this.data1 = data1;
            this.data2 = data2;
            this.data3 = data3;
        }
    }
    public static abstract class AbstractMessage extends MsgObject {
    }
}
