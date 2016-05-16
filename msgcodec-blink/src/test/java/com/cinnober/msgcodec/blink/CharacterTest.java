/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 The MsgCodec Authors
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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.cinnober.msgcodec.IncompatibleSchemaException;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;

public class CharacterTest {

    public void printStream(ByteArrayOutputStream stream) {
        byte[] arr = stream.toByteArray();

        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] & 0xFF);
            System.out.print(" ");
        }
        System.out.println("");
    }

    @Test
    public void testCharacter() throws IOException, IncompatibleSchemaException {
        Schema schema = new SchemaBuilder().build(Version1.class);

        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec.encode(new Version1('a', null), bout);

        printStream(bout);

        Version1 msg = (Version1) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals('a', msg.char1);
        assertEquals(null, msg.char2);
    }

    @Test
    public void testCharacter2() throws IOException, IncompatibleSchemaException {
        Schema schema = new SchemaBuilder().build(Version1.class);

        MsgCodec codec = new BlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec.encode(new Version1('a', 'B'), bout);

        printStream(bout);

        Version1 msg = (Version1) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals('a', msg.char1);
        assertEquals((Character) 'B', msg.char2);
    }

    @Name("Payload")
    @Id(1)
    public static class Version1 extends MsgObject {
        public char char1;
        public Character char2;

        public Version1() {
        }

        public Version1(char v1, Character v2) {
            char1 = v1;
            char2 = v2;
        }
    }
}
