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

import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.io.ByteArrayBuf;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.io.ByteBufferBuf;
import com.cinnober.msgcodec.io.ByteBufferInputStream;
import com.cinnober.msgcodec.io.ByteBufferOutputStream;
import com.cinnober.msgcodec.io.ByteBuffers;
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

    public static enum BufferType {
        ARRAY,
        BUFFER,
        DIRECT_BUFFER,
    }

    @Param({"true"})
    //@Param({"true", "false"})
    public boolean bytecode;

    @Param({"ARRAY", "BUFFER", "DIRECT_BUFFER"})
    public BufferType bufType;

    private Ouch42EnterOrder msg;
    private BlinkCodec codec;
    private int encodedSize;

    private ByteBuf buf;

    public BenchmarkOuch42EnterOrder() {
    }

    @Setup
    public void setup() throws IOException {
        Schema dict = new SchemaBuilder(true).build(Ouch42EnterOrder.class);
        BlinkCodecFactory factory = new BlinkCodecFactory(dict);
        factory.setCodecOption(bytecode ? 
                CodecOption.DYNAMIC_BYTECODE_CODEC_ONLY :
                CodecOption.INSTRUCTION_CODEC_ONLY);
        codec = factory.createCodec();
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

        msg = createOuch42EnterOrder();

        encodedSize = benchmarkEncode();
        System.out.println("Encoded size: " + encodedSize);
        if (bufType == BufferType.ARRAY) {
            System.out.println("Encoded hex: " + ByteArrays.toHex(((ByteArrayBuf)buf).array(), 0, encodedSize, 1, 100, 100));
        } else {
            System.out.println("Encoded hex: " + ByteBuffers.toHex(((ByteBufferBuf)buf).buffer(), 0, encodedSize, 1, 100, 100));
        }
    }

    public static Ouch42EnterOrder createOuch42EnterOrder() {
        Ouch42EnterOrder msg = new Ouch42EnterOrder();
        msg.token = "qwerty1234";
        msg.buySell = 'B';
        msg.shares = 100;
        msg.stock = "CINN";
        msg.price = 10000;
        msg.timeInForce = 99998; // market hours
        msg.firm = "AVA";
        msg.display = 'O';
        msg.capacity = 'O';
        msg.intermarketSweep = 'N';
        msg.minimumQuantity = 1;
        msg.crossType = 'N';
        msg.customerType = 'R';
        return msg;
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
