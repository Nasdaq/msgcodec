package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.util.ByteBufferInputStream;
import com.cinnober.msgcodec.util.ByteBufferOutputStream;
import java.io.IOException;
import java.math.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class BenchmarkOuch42EnterOrder {

    @Param({"10000"}) //, "12340000"})
    public int price;
    @Param({"100"}) //, "10000"})
    public int shares;
    @Param({"true", "false"})
    public boolean bytecode;

    private Ouch42EnterOrder msg;
    private BlinkCodec codec;
    private ByteBuffer buf;
    private ByteBufferOutputStream bufOut;
    private ByteBufferInputStream bufIn;



    public BenchmarkOuch42EnterOrder() {
    }

    @Setup
    public void setup() {
        ProtocolDictionary dict = new ProtocolDictionaryBuilder(true).build(Ouch42EnterOrder.class);
        BlinkCodecFactory factory = new BlinkCodecFactory(dict);
        factory.setCodecOption(bytecode ? 
                CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY :
                CodecOption.INSTRUCTION_CODEC_ONLY);
        codec = factory.createStreamCodec();
        buf = ByteBuffer.allocate(1024);
        bufOut = new ByteBufferOutputStream(buf);
        bufIn = new ByteBufferInputStream(buf);

        msg = new Ouch42EnterOrder();
        msg.token = "qwerty1234";
        msg.buySell = 'B';
        msg.shares = shares;
        msg.stock = "CINN";
        msg.price = price;
        msg.timeInForce = 99998; // market hours
        msg.firm = "AVA";
        msg.display = 'O';
        msg.capacity = 'O';
        msg.intermarketSweep = 'N';
        msg.minimumQuantity = 1;
        msg.crossType = 'N';
        msg.customerType = 'R';
    }

    @Benchmark
    public Object benchmarkEncodeDecode() throws IOException {
        buf.clear();
        codec.encode(msg, bufOut);
        buf.flip();
        return codec.decode(bufIn);
    }
    @Benchmark
    public int benchmarkEncode() throws IOException {
        buf.clear();
        codec.encode(msg, bufOut);
        return buf.position();
    }
}
