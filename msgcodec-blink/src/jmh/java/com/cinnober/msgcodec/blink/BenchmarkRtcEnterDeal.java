package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.ByteBuf;
import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.blink.rtcmessages.EnterDeal;
import com.cinnober.msgcodec.blink.rtcmessages.IncomingTradeSide;
import com.cinnober.msgcodec.blink.rtcmessages.Request;
import com.cinnober.msgcodec.blink.rtcmessages.SessionToken;
import com.cinnober.msgcodec.blink.rtcmessages.TradeDestination;
import com.cinnober.msgcodec.blink.rtcmessages.TradeExternalData;
import com.cinnober.msgcodec.util.ByteArrayBuf;
import com.cinnober.msgcodec.util.ByteArrays;
import com.cinnober.msgcodec.util.ByteBufferBuf;
import com.cinnober.msgcodec.util.ByteBuffers;
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
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
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

//    @Param({"ARRAY", "BUFFER", "DIRECT_BUFFER"})
    @Param({"ARRAY"})
    public BufferType bufType;

    private EnterDeal msg;
    private BlinkCodec codec;
    private int encodedSize;

    private ByteBuf buf;

    public BenchmarkRtcEnterDeal() {
    }

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
        final int bufferSize = 1024;
        switch (bufType) {
            case ARRAY:
                buf = new ByteArrayBuf(new byte[bufferSize]);
                break;
            case BUFFER:
                buf = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
                break;
            case DIRECT_BUFFER:
                buf = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
                break;
            default:
                throw new RuntimeException("Unhandled case: " + bufType);
        }

        msg = createRtcEnterDeal();

        encodedSize = benchmarkEncode();
        System.out.println("Encoded size: " + encodedSize);
        if (bufType == BufferType.ARRAY) {
            System.out.println("Encoded hex: " + ByteArrays.toHex(((ByteArrayBuf)buf).array(), 0, encodedSize, 1, 100, 100));
        } else {
            System.out.println("Encoded hex: " + ByteBuffers.toHex(((ByteBufferBuf)buf).buffer(), 0, encodedSize, 1, 100, 100));
        }
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
        return codec.decode(buf);
    }
    @Benchmark
    public int benchmarkEncode() throws IOException {
        buf.clear();
        codec.encode(msg, buf);
        return buf.position();
    }
}
