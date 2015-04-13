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

import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.blink.rtcmessages.EnterDeal;
import com.cinnober.msgcodec.blink.rtcmessages.IncomingTradeSide;
import com.cinnober.msgcodec.blink.rtcmessages.Request;
import com.cinnober.msgcodec.blink.rtcmessages.SessionToken;
import com.cinnober.msgcodec.blink.rtcmessages.TradeDestination;
import com.cinnober.msgcodec.blink.rtcmessages.TradeExternalData;
import com.cinnober.msgcodec.util.ByteArrays;
import com.cinnober.msgcodec.util.ByteBufferInputStream;
import com.cinnober.msgcodec.util.ByteBufferOutputStream;
import com.cinnober.msgcodec.util.TimeFormat;
import java.io.IOException;
import java.math.*;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.concurrent.*;
import org.openjdk.jmh.annotations.*;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class BenchmarkRtcEnterDeal {

    public static enum BufferType {
        ARRAY,
        BUFFER,
        DIRECT_BUFFER,
    }

    @Param({"true"})
    //@Param({"true", "false"})
    public boolean bytecode;

    private EnterDeal msg;
    private BlinkCodec codec;
    private ByteBuffer buf;
    private int encodedSize;
    private ByteBufferOutputStream bufOut;
    private ByteBufferInputStream bufIn;


    @Setup
    public void setup() throws IOException {
        ProtocolDictionary dict = new ProtocolDictionaryBuilder(true).build(
                EnterDeal.class,
                Request.class,
                IncomingTradeSide.class,
                SessionToken.class,
                TradeDestination.class,
                TradeExternalData.class
        ).assignGroupIds();
        BlinkCodecFactory factory = new BlinkCodecFactory(dict);
        factory.setCodecOption(bytecode ?
                CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY :
                CodecOption.INSTRUCTION_CODEC_ONLY);
        codec = factory.createStreamCodec();
        buf = ByteBuffer.allocate(1024);
        bufOut = new ByteBufferOutputStream(buf);
        bufIn = new ByteBufferInputStream(buf);

        msg = createRtcEnterDeal();

        encodedSize = benchmarkEncode();
        System.out.println("Encoded size: " + encodedSize);
        System.out.println("Encoded hex: " + ByteArrays.toHex(buf.array(), 0, encodedSize, 1, 100, 100));
    }

    public static EnterDeal createRtcEnterDeal() {
        EnterDeal msg = new EnterDeal();
        msg.requestId = 123456;
        msg.clientDealId = "qwerty";
        msg.instrumentKey = 1000_001;
        msg.price = BigDecimal.valueOf(1234, 2);
        msg.quantity = BigDecimal.valueOf(10_000);
        try {
            msg.tradeBusinessDate = (int) TimeFormat.getTimeFormat(TimeUnit.DAYS, Epoch.UNIX).parse("2015-03-30");
        } catch (ParseException ex) {
            throw new RuntimeException("Should not happen", ex);
        }
        msg.buy = createTradeSide(msg.quantity);
        msg.sell = createTradeSide(msg.quantity);
        msg.tradeExternalData = null;
        return msg;
    }

    private static IncomingTradeSide createTradeSide(BigDecimal quantity) {
        IncomingTradeSide side = new IncomingTradeSide();
        side.clientOrderId = "abc-123";
        TradeDestination alloc = new TradeDestination(456789, quantity);
        side.allocations = Arrays.asList(alloc);
        return side;
    }

    @Benchmark
    public Object benchmarkDecode() throws IOException {
        buf.position(0).limit(encodedSize);
        return codec.decode(bufIn);
    }
    @Benchmark
    public int benchmarkEncode() throws IOException {
        buf.clear();
        codec.encode(msg, bufOut);
        return buf.position();
    }
}
