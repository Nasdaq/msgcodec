package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSource;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.io.ByteBufferInputStream;
import com.cinnober.msgcodec.io.ByteBufferOutputStream;
import com.cinnober.msgcodec.io.InputStreamSource;
import com.cinnober.msgcodec.io.OutputStreamSink;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import org.openjdk.jmh.annotations.*;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class BenchmarkOuch42EnterOrderStream {

    @Param({"true"})
    //@Param({"true", "false"})
    public boolean bytecode;

    private Ouch42EnterOrder msg;
    private BlinkCodec codec;
    private int encodedSize;

    private ByteBuffer buf;
    private ByteSink sink;
    private ByteSource source;

    public BenchmarkOuch42EnterOrderStream() {
    }

    @Setup
    public void setup() throws IOException {
        Schema dict = new SchemaBuilder(true).build(Ouch42EnterOrder.class);
        BlinkCodecFactory factory = new BlinkCodecFactory(dict);
        factory.setCodecOption(bytecode ? 
                CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY :
                CodecOption.INSTRUCTION_CODEC_ONLY);
        codec = factory.createCodec();
        buf = ByteBuffer.allocate(1024);
        sink = new OutputStreamSink(new ByteBufferOutputStream(buf));
        source = new InputStreamSource(new ByteBufferInputStream(buf));

        msg = BenchmarkOuch42EnterOrder.createOuch42EnterOrder();

        encodedSize = benchmarkEncode();
        System.out.println("Encoded size: " + encodedSize);
        System.out.println("Encoded hex: " + ByteArrays.toHex(buf.array(), 0, encodedSize, 1, 100, 100));
    }

    @Benchmark
    public Object benchmarkDecode() throws IOException {
        buf.position(0).limit(encodedSize);
        return codec.decode(source);
    }
    @Benchmark
    public int benchmarkEncode() throws IOException {
        buf.clear();
        codec.encode(msg, sink);
        return buf.position();
    }
}
