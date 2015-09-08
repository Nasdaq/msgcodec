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
package com.cinnober.msgcodec.test.messages;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.io.ByteArrayBuf;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.io.ByteBufferBuf;
import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ReallocatingArray;
import com.cinnober.msgcodec.io.ReallocatingByteBuf;

import org.junit.Assert;

/**
 * Base class for standard test messages for testing a codec implementation.
 *
 * <p>Usage (assuming your codec is called Foo):
 * <pre>
 * public class FooTestMessagesSuiteImpl extends TestMessagesSuite {
 *     public FooTestMessagesSuiteImpl(Class&lt;?&gt; rootClass) {
 *         super(rootClass, createCodec());
 *     }
 *     private static StreamCodec createCodec() {
 *         return new FooCodec(TestProtocol.getSchema());
 *     }
 * }
 *
 * {@literal @}RunWith(FooTestMessagesSuiteImpl.class)
 * public class FooTestMessagesSuite {}
 * </pre>
 *
 * @author mikael.brannstrom
 *
 */
public abstract class TestMessagesSuite extends Suite {

    protected TestMessagesSuite(Class<?> rootClass, MsgCodec codec) throws InitializationError {
        super(rootClass, createRunners(rootClass, codec));
    }

    private static List<Runner> createRunners(Class<?> rootClass, MsgCodec codec) {
        List<Runner> runners = new ArrayList<>();
        for (Map.Entry<String, Object> messageEntry : TestProtocol.createMessages().entrySet()) {
            runners.add(new EncodeDecodeTest(rootClass, codec, messageEntry.getKey(), messageEntry.getValue()));
        }
        return runners;
    }

    public static class EncodeDecodeTest extends Runner {

        private final MsgCodec codec;
        private final String label;
        private final Object message;
        private final Description description;
        private final Class<?> rootClass;

        private EncodeDecodeTest(Class<?> rootClass, MsgCodec codec, String label, Object message) {
            this.codec = codec;
            this.label = label;
            this.message = message;
            this.description = Description.createTestDescription(EncodeDecodeTest.class, label);
            this.rootClass = rootClass;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public Description getDescription() {
            return description;
        }

        
        public void runByteBuf(RunNotifier notifier, Object message, ByteBuf buffer) {
            try {
                codec.encode(message, buffer);

                buffer.clear();
                Object object = codec.decode(buffer);
                if (object == null) {
                    fail("Decoded message is null");
                } else if (!object.getClass().equals(message.getClass())) {
                    fail("Decoded message is not same type as encoded message");
                }
                Assert.assertEquals(message, object);
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            }

        }
        

        @Override
        public void run(RunNotifier notifier) {
            notifier.fireTestStarted(description);

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                codec.encode(message, out);

                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                Object object = codec.decode(in);
                
                if (object == null) {
                    fail("Decoded message is null");
                } else if (!object.getClass().equals(message.getClass())) {
                    fail("Decoded message is not same type as encoded message");
                }
                Assert.assertEquals(message, object);
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            }
            
            
            if (!rootClass.getName().contains("XmlTestMessagesSuite")) {

                // array implementations
                try {
                    ByteArrayBuf arr = new ByteArrayBuf(4096);
                    codec.encode(message, arr);

//                    System.out.println("******Encoded hex: " + ByteArrays.toHex(((ByteArrayBuf)arr).array(), 0, arr.position(), 1, 100, 100));
                    arr.clear();
                    Object object = codec.decode(arr);
                    
                    if (object == null) {
                        fail("Decoded message is null");
                    } else if (!object.getClass().equals(message.getClass())) {
                        fail("Decoded message is not same type as encoded message");
                    }
                    Assert.assertEquals(message, object);
                } catch (Throwable e) {
                    notifier.fireTestFailure(new Failure(description, e));
                }
                
                try {
                    ReallocatingArray arr = new ReallocatingArray(4096, 4096);
                    codec.encode(message, arr);
                    //                System.out.println("******Encoded hex: " + ByteArrays.toHex(((ReallocatingArray)arr).getBuffer().array(), 0, arr.position(), 1, 100, 100));
                    arr.clear();
                    Object object = codec.decode(arr);

                    if (object == null) {
                        fail("Decoded message is null");
                    } else if (!object.getClass().equals(message.getClass())) {
                        fail("Decoded message is not same type as encoded message");
                    }
                    Assert.assertEquals(message, object);
                } catch (Throwable e) {
                    notifier.fireTestFailure(new Failure(description, e));
                }
            
            // Ugly ignore hack, since xml parser is not compatible with buffer read that requires exact read sizes
                runByteBuf(notifier, message, new ByteBufferBuf(ByteBuffer.wrap(new byte[4096])));
                runByteBuf(notifier, message, new ByteBufferBuf(ByteBuffer.allocateDirect(65536)));
                
                runByteBuf(notifier, message, new ReallocatingByteBuf(4096, 4096, ByteBuffer::allocate));
                runByteBuf(notifier, message, new ReallocatingByteBuf(4096, 4096, ByteBuffer::allocateDirect));
            }
            
            notifier.fireTestFinished(description);
        }

    }
}
