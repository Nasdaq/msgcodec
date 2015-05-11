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
import java.nio.ByteOrder;
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
                nioBuf = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN); // same as buffer
                vlcBuf = new ByteArrayBuf(new byte[bufferSize]);
                break;
            case BUFFER:
                nioBuf = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN);
                vlcBuf = new ByteBufferBuf(ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN));
                break;
            case DIRECT_BUFFER:
                nioBuf = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.LITTLE_ENDIAN);
                vlcBuf = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.LITTLE_ENDIAN));
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
        long[] additionalLongValues = new long[] {
            100_000, 200_000, 300_000, 400_000, 500_000, 600_000, 700_000, 800_000, 900_000,
            1000_000, 2000_000, 3000_000, 4000_000, 5000_000, 6000_000, 7000_000, 8000_000, 9000_000,
            10_000_000, 20_000_000, 30_000_000, 40_000_000, 50_000_000, 60_000_000, 70_000_000, 80_000_000, 90_000_000,
            1L<<50, 1L<<51, 1L<<52, 1L<<53, 1L<<54, 1L<<55, 1L<<56, 1L<<57, 1L<<58, 1L<<59,
            1L<<60, 1L<<61, 1L<<62, Long.MAX_VALUE, -1L
        };
        longValues = new long[intValues.length+additionalLongValues.length];
        for (int i=0; i<intValues.length; i++) {
            longValues[i] = intValues[i];
        }
        System.arraycopy(additionalLongValues, 0, longValues, intValues.length, additionalLongValues.length);

        System.out.println("Integer values:");
        System.out.println("  Num: " + intValues.length);
        int sizeOfVlcInts = writeVlcUInt32();
        System.out.println("  VLC Size: " + sizeOfVlcInts);
        int sizeOfNioInts = writePutUInt32();
        System.out.println("  NIO Size: " + sizeOfNioInts);

        System.out.println("Long values:");
        System.out.println("  Num: " + longValues.length);
        int sizeOfVlcLongs = writeVlcUInt64();
        System.out.println("  VLC Size: " + sizeOfVlcLongs);
        int sizeOfNioLongs = writePutUInt64();
        System.out.println("  NIO Size: " + sizeOfNioLongs);
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

    @Benchmark
    public int writeVlcUInt64() throws IOException {
        vlcBuf.clear();
        for (int i=0; i<longValues.length; i++) {
            BlinkOutput.writeUInt64(vlcBuf, longValues[i]);
        }
        return vlcBuf.position();
    }
    @Benchmark
    public long readVlcUInt64() throws IOException {
        vlcBuf.clear();
        long sum = 0;
        for (int i=0; i<longValues.length; i++) {
            long value = BlinkInput.readUInt64(vlcBuf);
            sum += value;
        }
        return sum;
    }
    @Benchmark
    public int writePutUInt64() throws IOException {
        nioBuf.clear();
        for (int i=0; i<longValues.length; i++) {
            nioBuf.putLong(longValues[i]);
        }
        return nioBuf.position();
    }
    @Benchmark
    public long readGetUInt64() throws IOException {
        nioBuf.clear();
        long sum = 0;
        for (int i=0; i<longValues.length; i++) {
            long value = nioBuf.getLong();
            sum += value;
        }
        return sum;
    }

}
