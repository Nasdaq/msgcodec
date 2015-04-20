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

import com.cinnober.msgcodec.io.ByteArrayBuf;
import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.io.ByteBufferBuf;
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
public class BenchmarkIntegerCoding {

    public static enum BufferType {
        ARRAY,
        BUFFER,
        DIRECT_BUFFER,
    }

    @Param({"BUFFER", "DIRECT_BUFFER", "ARRAY"})
    public BufferType bufType;

    private ByteBuf vlcBuf;
    private ByteBuffer nioBuf;
    private int[] intValues;
    private long[] longValues;

    public BenchmarkIntegerCoding() {
    }

    @Setup
    public void setup() throws IOException {
        final int bufferSize = 1024;
        switch (bufType) {
            case ARRAY:
                    nioBuf = ByteBuffer.allocate(bufferSize); // same as buffer
                vlcBuf = new ByteArrayBuf(new byte[bufferSize]);
                break;
            case BUFFER:
                nioBuf = ByteBuffer.allocate(bufferSize);
                vlcBuf = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
                break;
            case DIRECT_BUFFER:
                nioBuf = ByteBuffer.allocateDirect(bufferSize);
                vlcBuf = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
                break;
            default:
                throw new RuntimeException("Unhandled case: " + bufType);
        }
        
        intValues = new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 20, 30, 40, 50, 60, 70, 80, 90,
            100, 200, 300, 400, 500, 600, 700, 800, 900,
            1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000,
            10_000, 100_000, 1000_000
        };
        System.out.println("Integer values:");
        System.out.println("  Num: " + intValues.length);
        int sizeOfVlcInts = writeVlcUInt32();
        System.out.println("  VLC Size: " + sizeOfVlcInts);
        int sizeOfNioInts = writePutUInt32();
        System.out.println("  NIO Size: " + sizeOfNioInts);
    }

    @Benchmark
    public int writeVlcUInt32() throws IOException {
        vlcBuf.clear();
        for (int i=0; i<intValues.length; i++) {
            BlinkOutput.writeUInt32(vlcBuf, intValues[i]);
        }
        return vlcBuf.position();
    }
    @Benchmark
    public int readVlcUInt32() throws IOException {
        vlcBuf.clear();
        int sum = 0;
        for (int i=0; i<intValues.length; i++) {
            int value = BlinkInput.readUInt32(vlcBuf);
            sum += value;
        }
        return sum;
    }
    @Benchmark
    public int writePutUInt32() throws IOException {
        nioBuf.clear();
        for (int i=0; i<intValues.length; i++) {
            nioBuf.putInt(intValues[i]);
        }
        return nioBuf.position();
    }
    @Benchmark
    public int readGetUInt32() throws IOException {
        nioBuf.clear();
        int sum = 0;
        for (int i=0; i<intValues.length; i++) {
            int value = nioBuf.getInt();
            sum += value;
        }
        return sum;
    }
    
}
