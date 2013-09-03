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
package com.cinnober.msgcodec.json;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.junit.Test;

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.StreamCodec;

/**
 * @author Mikael Brannstrom
 *
 */
public class JsonCodecTest {

    @Test
    public void test() throws Exception {
        ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
        ProtocolDictionary dictionary = builder.build(Hello.class);
        StreamCodec codec = new JsonCodec(dictionary);
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


    public static class Hello {
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
