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

package com.cinnober.msgcodec.examples;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.SmallDecimal;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;
import com.cinnober.msgcodec.blink.BlinkCodec;
import com.cinnober.msgcodec.blink.BlinkCodecFactory;
import com.cinnober.msgcodec.io.ByteBufferInputStream;
import com.cinnober.msgcodec.io.ByteBufferOutputStream;
import com.cinnober.msgcodec.util.LimitInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

/**
 *
 * @author mikael.brannstrom
 */
public class Benchmark {

    private final MsgCodec codec;
    private final Object[] messages;

    private OutputStream out;
    private InputStream in;
    private ByteBuffer buf;

    public Benchmark(MsgCodec codec, ArrayList<Object> messages) {
        this.codec = codec;
        this.messages = messages.toArray();

        buf = ByteBuffer.allocateDirect(1_00_000_000);
        out = new ByteBufferOutputStream(buf);
        in = new LimitInputStream(new ByteBufferInputStream(buf));
    }

    public void run(int iterations) throws IOException {
        buf.clear();
        long time;
        time = System.nanoTime();
        for (int i=0; i<iterations; i++) {
            for (Object msg : messages) {
                codec.encode(msg, out);
            }
        }
        time = System.nanoTime() - time;
        System.out.println("Encode");
        System.out.format("  %,d messages.\n",
                iterations*messages.length);
        System.out.format("  time %.2f s.\n", 1e-9*time);
        System.out.format("  nanos/message %.2f.\n",
                1.0*time/(iterations*messages.length));
        System.out.format("  bytes %,d\n", buf.position());
        System.out.format("  bytes/message %.2f.\n",
                1.0*buf.position()/(iterations*messages.length));
        System.out.println();

        buf.flip();
        time = System.nanoTime();
        for (int i=0; i<iterations; i++) {
            for (Object msg : messages) {
                codec.decode(in);
            }
        }
        time = System.nanoTime() - time;
        System.out.println("Decode");
        System.out.format("  %d messages.\n",
                iterations*messages.length);
        System.out.format("  time %.2f s.\n", 1e-9*time);
        System.out.format("  nanos/message %.2f.\n",
                1.0*time/(iterations*messages.length));
        System.out.println();
    }

    public static void main(String... args) throws Exception {
        System.clearProperty("java.util.logging.config.class");
        System.setProperty("java.util.logging.config.file", "../msgcodec-blink/logging.properties");
        LogManager.getLogManager().readConfiguration();

        Random rnd = new Random(12345678);

        ArrayList<Object> messages = new ArrayList<>(100);
        for (int i=0; i<100; i++) {
            messages.add(createEnterDoubleSidedTrade(rnd));
            if (i==0) {
                System.out.println("messages: " + messages);
            }
        }

        Schema dict = new SchemaBuilder(true).build(EnterDoubleSidedTrade.class);
        System.out.println("dict: \n" + dict);

        BlinkCodec codec = new BlinkCodecFactory(dict).createCodec();

        Benchmark benchmark = new Benchmark(codec, messages);

        benchmark.run(10000);
        benchmark.run(10000);
        benchmark.run(10000);
        benchmark.run(10000);
        benchmark.run(10000);

    }

    private static long randomId(Random rnd, double num) {
        return (long) Math.abs(rnd.nextGaussian() * num/2 + num/2);
    }
    private static String randomStringId(Random rnd, double num) {
        return Long.toString(randomId(rnd, num), Character.MAX_RADIX);
    }

    public static EnterDoubleSidedTrade createEnterDoubleSidedTrade(Random rnd) {
        EnterDoubleSidedTrade msg = new EnterDoubleSidedTrade();
        msg.requestId = randomId(rnd, 1e6);
        msg.clientDoubleSidedTradeId = randomStringId(rnd, 1e5);
        msg.instrumentKey = randomId(rnd, 1e4) + 1000;
        msg.tradeBusinessDate = 16000 + (rnd.nextInt() & 0x3ff);
        msg.quantity = BigDecimal.valueOf((long) Math.abs(rnd.nextGaussian() * 1e6 + 1e6));
        msg.price = BigDecimal.valueOf((long) Math.abs(rnd.nextGaussian() * 1e2 + 1e2));
        msg.buy = new IncomingTradeSide(randomStringId(rnd, 1e5),
                new TradeDestination(randomId(rnd, 1e7), msg.quantity));
        msg.sell = new IncomingTradeSide(randomStringId(rnd, 1e5),
                new TradeDestination(randomId(rnd, 1e7),
                        msg.quantity.multiply(BigDecimal.valueOf(0.5))),
                new TradeDestination(randomId(rnd, 1e7),
                        msg.quantity.subtract(msg.quantity.multiply(BigDecimal.valueOf(0.5))))
        );

        return msg;
    }


    @Id(1)
    public static class EnterDoubleSidedTrade extends MsgObject {

        @Unsigned
        public long requestId;
        /**
         * Client assigned double-sided trade id, e.g. the market place assigned trade id.
         */
        @Required
        public String clientDoubleSidedTradeId;

        /**
         * The instrument the trade applies to.
         */
        @Unsigned
        public long instrumentKey;

        /**
         * The trade price.
         */
        @Required
        @SmallDecimal
        public BigDecimal price;

        /**
         * The trade quantity.
         */
        @Required
        public BigDecimal quantity;

        /**
         * The business date the trade occurred.
         */
        @Time(unit=TimeUnit.DAYS, timeZone="")
        public int tradeBusinessDate;

        /**
         * The buy side.
         */
        @Required
        public IncomingTradeSide buy;

        /**
         * The sell side.
         */
        @Required
        public IncomingTradeSide sell;
    }

    public static class IncomingTradeSide extends MsgObject {

        /**
         * A optional reference set by the trading member to backtrack the trade to an order at the trading venue.
         */
        public String clientOrderId;

        /**
         * The allocations to accounts. The allocated quantities must sum up to the trade quantity.
         */
        @Required
        @Sequence(TradeDestination.class)
        public List<TradeDestination> allocations;

        public IncomingTradeSide() {
        }

        public IncomingTradeSide(String clientOrderId, TradeDestination ... allocations) {
            this.clientOrderId = clientOrderId;
            this.allocations = Arrays.asList(allocations);
        }
    }

    public static class TradeDestination extends MsgObject {

        /**
         * The account to book a Trade to.
         */
        @Unsigned
        public long accountId;

        /**
         * The quantity to book in the account.
         */
        @Required
        public BigDecimal quantity;

        /**
         * An optional free text field
         */
        public String reference;

        /**
         * Specifies whether this allocation is meant to open a new position or close an old position. Returns the
         * openOrClose.
         *
         * Determines if the position should be opened or closed. Note: Only open is currently supported.
         */
        public OpenOrClose openOrClose;

        public TradeDestination() {
        }

        public TradeDestination(long accountId, BigDecimal quantity) {
            this.accountId = accountId;
            this.quantity = quantity;
            this.openOrClose = OpenOrClose.OPEN;
        }

    }

    public static enum OpenOrClose {
        OPEN, CLOSE
    }
}
