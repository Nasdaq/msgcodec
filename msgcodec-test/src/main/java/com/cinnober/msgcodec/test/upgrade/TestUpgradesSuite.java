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
package com.cinnober.msgcodec.test.upgrade;

import com.cinnober.msgcodec.Group;
import com.cinnober.msgcodec.IncompatibleSchemaException;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinder;
import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.test.upgrade.PairedTestProtocols.PairedMessages;
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.fail;

/**
 * Base class for schema upgrade tests that can be run in any codec implementation.
 *
 * <p>Usage (assuming your codec is called Foo):
 * <pre>
 * public class FooTestUpgradesSuiteImpl extends TestUpgradesSuite {
 *     public FooTestUpgradesSuiteImpl(Class&lt;?&gt; rootClass) {
 *         super(rootClass, s -&gt; new FooCodec(s));
 *     }
 * }
 *
 * {@literal @}RunWith(FooTestUpgradesSuiteImpl.class)
 * public class FooTestUpgradesSuite {}
 * </pre>
 *
 * @author Tommy Norling
 *
 */
public abstract class TestUpgradesSuite extends Suite {

    protected TestUpgradesSuite(Class<?> rootClass, Function<Schema, MsgCodec> codecFactory) throws InitializationError {
        super(rootClass, createRunners(codecFactory));
    }

    private static List<Runner> createRunners(Function<Schema, MsgCodec> codecFactory) throws InitializationError {
        List<Runner> runners = new ArrayList<>();
        
        Schema originalSchema = PairedTestProtocols.getOriginalSchema();
        Schema upgradedSchema = PairedTestProtocols.getUpgradedSchema();

        try {
            for (Map.Entry<String, PairedMessages> messageEntry : PairedTestProtocols.createMessages().entrySet()) {
                runners.add(new InboundTest(originalSchema, upgradedSchema, codecFactory, "InboundTest." + messageEntry.getKey(), 
                        messageEntry.getValue().originalMessage, messageEntry.getValue().upgradedMessage));
                runners.add(new OutboundTest(originalSchema, upgradedSchema, codecFactory,"OutboundTest." + messageEntry.getKey(), 
                        messageEntry.getValue().originalMessage, messageEntry.getValue().upgradedMessage));
                runners.add(new InboundGroupTest(originalSchema, upgradedSchema, codecFactory, "InboundGroupTest." + messageEntry.getKey(),
                        messageEntry.getValue().originalMessage, messageEntry.getValue().upgradedMessage));
                runners.add(new OutboundGroupTest(originalSchema, upgradedSchema, codecFactory,"OutboundGroupTest." + messageEntry.getKey(),
                        messageEntry.getValue().originalMessage, messageEntry.getValue().upgradedMessage));
            }
        } catch (IncompatibleSchemaException e) {
            throw new InitializationError(e);
        }

        return runners;
    }


    private static Group toGroup(Schema javaSchema, Schema groupSchema, Object javaEntity, Function<Schema, MsgCodec> codecFactory)  {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            codecFactory.apply(javaSchema).encode(javaEntity, outputStream);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return (Group) codecFactory.apply(groupSchema).decode(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert to Group!", e);
        }
    }

    private static Object fromGroup(Schema javaSchema, Schema groupSchema, Object groupEntity, Function<Schema, MsgCodec> codecFactory)  {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            codecFactory.apply(groupSchema).encode(groupEntity, outputStream);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return codecFactory.apply(javaSchema).decode(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert to Group!", e);
        }
    }

    public static class InboundGroupTest extends Runner {
        private final MsgCodec originalCodec;
        private final MsgCodec upgradeCodec;
        private final String label;
        private final Object originalMessage;
        private final Object upgradedMessage;
        private final Description description;
        private final Schema originalJavaSchema;
        private final Schema upgradedJavaSchema;
        private final Schema originalGroupBound;
        private final Schema upgradedGroupBound;
        private Function<Schema, MsgCodec> codecFactory;

        private InboundGroupTest(Schema originalJavaSchema, Schema upgradedJavaSchema, Function<Schema, MsgCodec> codecFactory,
                            String label, Object originalMessage, Object upgradedMessage) throws IncompatibleSchemaException {
            originalGroupBound = Group.bind(originalJavaSchema.unbind());
            upgradedGroupBound = Group.bind(upgradedJavaSchema.unbind());
            this.originalJavaSchema = originalJavaSchema;
            this.upgradedJavaSchema = upgradedJavaSchema;
            this.codecFactory = codecFactory;

            Schema inboundSchema = new SchemaBinder(upgradedGroupBound).bind(originalGroupBound.unbind(), g -> Direction.INBOUND);

            this.originalCodec = codecFactory.apply(originalGroupBound);
            this.upgradeCodec = codecFactory.apply(inboundSchema);

            this.label = label;
            this.originalMessage = toGroup(originalJavaSchema, originalGroupBound, originalMessage, codecFactory);
            this.upgradedMessage = toGroup(upgradedJavaSchema, upgradedGroupBound, upgradedMessage, codecFactory);
            this.description = Description.createTestDescription(OutboundTest.class, label);
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
                // Encode using the original codec
                originalCodec.encode(originalMessage, out);

                // Decode using the normal upgraded codec, should result in an upgraded version of the original entity
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                Object object = upgradeCodec.decode(in);
                if (object == null) {
                    fail("Decoded message is null");
                }
                Assert.assertEquals(fromGroup(upgradedJavaSchema, upgradedGroupBound, upgradedMessage, codecFactory),
                        fromGroup(upgradedJavaSchema, upgradedGroupBound, object, codecFactory));
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            }

            notifier.fireTestFinished(description);
        }
    }

    public static class OutboundGroupTest extends Runner {
        private final MsgCodec upgradeCodec;
        private final MsgCodec upgradedCodec;
        private final String label;
        private final Object originalMessage;
        private final Object upgradedMessage;
        private final Description description;
        private final Schema originalJavaSchema;
        private final Schema upgradedJavaSchema;
        private final Schema originalGroupBound;
        private final Schema upgradedGroupBound;
        private Function<Schema, MsgCodec> codecFactory;

        private OutboundGroupTest(Schema originalJavaSchema, Schema upgradedJavaSchema, Function<Schema, MsgCodec> codecFactory,
                             String label, Object originalMessage, Object upgradedMessage) throws IncompatibleSchemaException {
            originalGroupBound = Group.bind(originalJavaSchema.unbind());
            upgradedGroupBound = Group.bind(upgradedJavaSchema.unbind());
            this.originalJavaSchema = originalJavaSchema;
            this.upgradedJavaSchema = upgradedJavaSchema;
            this.codecFactory = codecFactory;

            Schema outboundSchema = new SchemaBinder(originalGroupBound).bind(upgradedGroupBound.unbind(), g -> Direction.OUTBOUND);

            this.upgradeCodec = codecFactory.apply(outboundSchema);
            this.upgradedCodec = codecFactory.apply(upgradedGroupBound);

            this.label = label;
            this.originalMessage = toGroup(originalJavaSchema, originalGroupBound, originalMessage, codecFactory);
            this.upgradedMessage = toGroup(upgradedJavaSchema, upgradedGroupBound, upgradedMessage, codecFactory);
            this.description = Description.createTestDescription(OutboundTest.class, label);
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
                // Encode using the upgrade codec
                upgradeCodec.encode(originalMessage, out);

                // Decode using the normal upgraded codec, should result in an upgraded version of the original entity
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                Object object = upgradedCodec.decode(in);
                if (object == null) {
                    fail("Decoded message is null");
                }

                Assert.assertEquals(fromGroup(upgradedJavaSchema, upgradedGroupBound, upgradedMessage, codecFactory),
                        fromGroup(upgradedJavaSchema, upgradedGroupBound, object, codecFactory));
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            }

            notifier.fireTestFinished(description);
        }
    }

    public static class InboundTest extends Runner {
        private final MsgCodec originalCodec;
        private final MsgCodec upgradeCodec;
        private final String label;
        private final Object originalMessage;
        private final Object upgradedMessage;
        private final Description description;

        private InboundTest(Schema originalSchema, Schema upgradedSchema, Function<Schema, MsgCodec> codecFactory, 
                String label, Object originalMessage, Object upgradedMessage) throws IncompatibleSchemaException {
            Schema inboundSchema = new SchemaBinder(upgradedSchema).bind(originalSchema.unbind(), g -> Direction.INBOUND);
            
            this.originalCodec = codecFactory.apply(originalSchema);
            this.upgradeCodec = codecFactory.apply(inboundSchema);
            
            this.label = label;
            this.originalMessage = originalMessage;
            this.upgradedMessage = upgradedMessage;
            this.description = Description.createTestDescription(OutboundTest.class, label);
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
                // Encode using the original codec
                originalCodec.encode(originalMessage, out);

                // Decode using the normal upgraded codec, should result in an upgraded version of the original entity
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                Object object = upgradeCodec.decode(in);
                if (object == null) {
                    fail("Decoded message is null");
                }
                Assert.assertEquals(upgradedMessage, object);
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            }

            notifier.fireTestFinished(description);
        }
    }
    
    public static class OutboundTest extends Runner {
        private final MsgCodec upgradeCodec;
        private final MsgCodec upgradedCodec;
        private final String label;
        private final Object originalMessage;
        private final Object upgradedMessage;
        private final Description description;

        private OutboundTest(Schema originalSchema, Schema upgradedSchema, Function<Schema, MsgCodec> codecFactory, 
                String label, Object originalMessage, Object upgradedMessage) throws IncompatibleSchemaException {
            Schema outboundSchema = new SchemaBinder(originalSchema).bind(upgradedSchema.unbind(), g -> Direction.OUTBOUND);
            
            this.upgradeCodec = codecFactory.apply(outboundSchema);
            this.upgradedCodec = codecFactory.apply(upgradedSchema);
            
            this.label = label;
            this.originalMessage = originalMessage;
            this.upgradedMessage = upgradedMessage;
            this.description = Description.createTestDescription(OutboundTest.class, label);
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
                // Encode using the upgrade codec
                upgradeCodec.encode(originalMessage, out);

                // Decode using the normal upgraded codec, should result in an upgraded version of the original entity
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                Object object = upgradedCodec.decode(in);
                if (object == null) {
                    fail("Decoded message is null");
                }
                Assert.assertEquals(upgradedMessage, object);
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            }

            notifier.fireTestFinished(description);
        }
    }
}
