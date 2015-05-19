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
import com.cinnober.msgcodec.anot.Required;
import java.io.IOException;

/**
 * @author Mikael Brannstrom
 *
 */
public class JsonCodecTest {

    @Test
    public void test() throws Exception {
        SchemaBuilder builder = new SchemaBuilder();
        Schema schema = builder.build(Hello.class);
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

    public static class Hello {
        @Required
        private String greeting;
        public Hello() {
        }
        public Hello(String greeting) {
            this.greeting = greeting;
        }
        public String getGreeting() {
            return greeting;
        }
        public void setGreeting(String greeting) {
            this.greeting = greeting;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((greeting == null) ? 0 : greeting.hashCode());
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Hello other = (Hello) obj;
            if (greeting == null) {
                if (other.greeting != null)
                    return false;
            } else if (!greeting.equals(other.greeting))
                return false;
            return true;
        }


    }
}
