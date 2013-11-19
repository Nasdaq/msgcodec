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
package com.cinnober.msgcodec.test.messages;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import com.cinnober.msgcodec.StreamCodec;
import org.junit.Assert;

/**
 * Base class for standard test messages for testing a codec implementation.
 *
 * <p>Usage (assuming your codec is called Foo):
 * <pre>
 * public class FooTestMessagesSuiteImpl extends TestMessagesSuite {
 *     public FooTestMessagesSuiteImpl(Class<?> rootClass) {
 *         super(rootClass, createCodec());
 *     }
 *     private static StreamCodec createCodec() {
 *         return new FooCodec(TestProtocol.getProtocolDictionary());
 *     }
 * }
 *
 * {@literal}RunWith(FooTestMessagesSuiteImpl.class)
 * public class FooTestMessagesSuite {}
 * </pre>
 *
 * @author mikael.brannstrom
 *
 */
public abstract class TestMessagesSuite extends Suite {

    /**
     * @throws InitializationError
     *
     */
    protected TestMessagesSuite(Class<?> rootClass, StreamCodec codec) throws InitializationError {
        super(rootClass, createRunners(codec));
    }

    private static List<Runner> createRunners(StreamCodec codec) {
        List<Runner> runners = new ArrayList<>();
        for (Map.Entry<String, Object> messageEntry : TestProtocol.createMessages().entrySet()) {
            runners.add(new EncodeDecodeTest(codec, messageEntry.getKey(), messageEntry.getValue()));
        }
        return runners;
    }

    public static class EncodeDecodeTest extends Runner {

        private final StreamCodec codec;
        private final String label;
        private final Object message;
        private final Description description;

        private EncodeDecodeTest(StreamCodec codec, String label, Object message) {
            this.codec = codec;
            this.label = label;
            this.message = message;
            this.description = Description.createTestDescription(EncodeDecodeTest.class, label);
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public Description getDescription() {
            return description;
        }

        @Override
        public void run(RunNotifier notifier) {
            notifier.fireTestStarted(description);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                codec.encode(message, out);

                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                Object object = codec.decode(in);
                if (object == null) {
                    fail("Decoded message is null");
                } else if (!object.getClass().equals(message.getClass())) {
                    fail("Decodec message is not same type as encoded message");
                }
                Assert.assertEquals(message, object);
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            }

            notifier.fireTestFinished(description);
        }

    }
}
