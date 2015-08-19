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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.io.ByteArrayBuf;
import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.io.ByteBufferBuf;

/**
 * @author roland.lidstrom, Cinnober Financial Technology North AB
 *
 */
//@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
//@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
//@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class BenchmarkDynamic {

	public static enum BufferType {
		ARRAY, BUFFER, DIRECT_BUFFER,
	}

	@Param({ "ARRAY", "BUFFER", "DIRECT_BUFFER" })
//	 @Param({"ARRAY"})
//	 @Param({"BUFFER"})
//	 @Param({"DIRECT_BUFFER"})
	public BufferType bufType;

	private DynamicMessage msgDynamic1;
	private DynamicMessage msgDynamic2;
	private DynamicMessage msgDynamic3;
	private DynamicMessage msgDynamic4;
	
	private TheRealMessage msgStaticDynamic;
	private TheRealBigMessage msgStaticManyFieldsLarge;
	private DynamicMessage msgLargeManyFieldsDynamic;
	private ManyDynamicMessage msgFourDynamic;
	private BlinkCodec codec;
	private int encodedSizeDynamic;
	private int encodedSizeNotDynamic;
	private int encodedSizeManyFieldsDynamic;
	private int encodedSizeManyFieldsStatic;
	private int sizeEncodeManyDynamic;
	private ByteBuf buf1;
	private ByteBuf buf2;
	private ByteBuf buf3;

	private ByteBuf buf_fourTypes1;
	private ByteBuf buf_fourTypes2;
	private ByteBuf buf_fourTypes3;
	private ByteBuf buf_fourTypes4;
	public BenchmarkDynamic() {

	}

	/**
	 * @throws IOException
	 */
	@Setup
	public void setup() throws IOException {
		final BlinkCodecFactory factory;
		final Schema dict;
		final int bufferSize = 1024;
//		bufType = BufferType.DIRECT_BUFFER;
		dict = new SchemaBuilder(true)
				.build(DynamicMessage.class, TheMessage.class, TheRealMessage.class, TheRealBigMessage.class,
						ManyDynamicMessage.class, TheRealMessage2.class, TheRealMessage3.class, TheRealMessage4.class)
				.assignGroupIds();

		factory = new BlinkCodecFactory(dict);
		codec = factory.createCodec();

		switch (bufType) {
		case ARRAY:
			buf1 = new ByteArrayBuf(new byte[bufferSize]);
			buf2 = new ByteArrayBuf(new byte[bufferSize]);
			buf3 = new ByteArrayBuf(new byte[bufferSize]);
			
			buf_fourTypes1 = new ByteArrayBuf(new byte[bufferSize]);
			buf_fourTypes2 = new ByteArrayBuf(new byte[bufferSize]);
			buf_fourTypes3 = new ByteArrayBuf(new byte[bufferSize]);
			buf_fourTypes4 = new ByteArrayBuf(new byte[bufferSize]);
			break;
		case BUFFER:
			buf1 = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
			buf2 = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
			buf3 = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
			
			buf_fourTypes1 = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
			buf_fourTypes2 = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
			buf_fourTypes3 = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
			buf_fourTypes4 = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));
			break;
			
		case DIRECT_BUFFER:
			buf1 = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
			buf2 = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
			buf3 = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
			
			buf_fourTypes1 = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
			buf_fourTypes2 = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
			buf_fourTypes3 = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
			buf_fourTypes4 = new ByteBufferBuf(ByteBuffer.allocateDirect(bufferSize));
			break;
		default:
			throw new RuntimeException("Unhandled case: " + bufType);
		}

		msgDynamic1 = createOneReal();
		msgDynamic2 = createTwoReal();
		msgDynamic3 = createThreeReal();
		msgDynamic4 = createFourReal();
		msgLargeManyFieldsDynamic = createDynamicFieldsMsg();
		// msgFourDynamic = createFourRealMsg();

		msgStaticDynamic = createRealMsg();
		msgStaticManyFieldsLarge = createManyFieldsMsg();

		calcBuffertSizes();
		codec.encode(msgDynamic1, buf_fourTypes1);
		buf_fourTypes1.limit(buf_fourTypes1.capacity());
		codec.encode(msgDynamic2, buf_fourTypes1);
		buf_fourTypes1.limit(buf_fourTypes1.capacity());
		codec.encode(msgDynamic3, buf_fourTypes1);
		buf_fourTypes1.limit(buf_fourTypes1.capacity());
		codec.encode(msgDynamic4, buf_fourTypes1);
		
		codec.encode(msgDynamic1, buf_fourTypes2);
		buf_fourTypes2.limit(buf_fourTypes2.capacity());
		codec.encode(msgDynamic2, buf_fourTypes2);
		buf_fourTypes2.limit(buf_fourTypes2.capacity());
		codec.encode(msgDynamic3, buf_fourTypes2);
		buf_fourTypes2.limit(buf_fourTypes2.capacity());
		codec.encode(msgDynamic1, buf_fourTypes2);
		
		codec.encode(msgDynamic1, buf_fourTypes3);
		buf_fourTypes3.limit(buf_fourTypes3.capacity());
		codec.encode(msgDynamic2, buf_fourTypes3);
		buf_fourTypes3.limit(buf_fourTypes3.capacity());
		codec.encode(msgDynamic1, buf_fourTypes3);
		buf_fourTypes3.limit(buf_fourTypes3.capacity());
		codec.encode(msgDynamic1, buf_fourTypes3);
		
		codec.encode(msgDynamic1, buf_fourTypes4);
		buf_fourTypes4.limit(buf_fourTypes4.capacity());
		codec.encode(msgDynamic1, buf_fourTypes4);
		buf_fourTypes4.limit(buf_fourTypes4.capacity());
		codec.encode(msgDynamic1, buf_fourTypes4);
		buf_fourTypes4.limit(buf_fourTypes4.capacity());
		codec.encode(msgDynamic1, buf_fourTypes4);
		
	}

	@Benchmark 
	public void benchmarkDecodeFourDynamicFourTypes(Blackhole bh) throws IOException {
		buf_fourTypes1.position(0);
		bh.consume( codec.decode(buf_fourTypes1));
		bh.consume( codec.decode(buf_fourTypes1));
		bh.consume( codec.decode(buf_fourTypes1));
		bh.consume( codec.decode(buf_fourTypes1));
		
	}
	
	@Benchmark 
	public void benchmarkDecodeFourDynamicThreeTypes(Blackhole bh) throws IOException {
		buf_fourTypes2.position(0);
		bh.consume( codec.decode(buf_fourTypes2));
		bh.consume( codec.decode(buf_fourTypes2));
		bh.consume( codec.decode(buf_fourTypes2));
		bh.consume( codec.decode(buf_fourTypes2));
		
	}
	
	@Benchmark 
	public void benchmarkDecodeFourDynamicTwoTypes(Blackhole bh) throws IOException {
		buf_fourTypes3.position(0);
		bh.consume( codec.decode(buf_fourTypes3));
		bh.consume( codec.decode(buf_fourTypes3));
		bh.consume( codec.decode(buf_fourTypes3));
		bh.consume( codec.decode(buf_fourTypes3));
		
	}
	
	@Benchmark 
	public void benchmarkDecodeFourDynamicOneTypes(Blackhole bh) throws IOException {
		buf_fourTypes4.position(0);
		bh.consume( codec.decode(buf_fourTypes4));
		bh.consume( codec.decode(buf_fourTypes4));
		bh.consume( codec.decode(buf_fourTypes4));
		bh.consume( codec.decode(buf_fourTypes4));
		
	}
	
	
	// --
	// @Benchmark
	// public Object benchmarkDecodeManyDynamic() throws IOException {
	// buf3.position(0).limit(sizeEncodeManyDynamic);
	// return codec.decode(buf3);
	// }

	// @Benchmark
	// public int benchmarkEncodeManyDynamic() throws IOException {
	// buf3.clear();
	// codec.encode(msgFourDynamic, buf3);
	// return buf3.position();
	// }



//	@Benchmark
	public int benchmarkEncodeFourDynamicOneType() throws IOException {
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		return buf1.position();
	}
//	@Benchmark
	public int benchmarkEncodeFourDynamicTwoType() throws IOException {
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		buf1.clear();
		codec.encode(msgDynamic2, buf1);
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		return buf1.position();
	}
//	@Benchmark
	public int benchmarkEncodeFourDynamicThreeType() throws IOException {
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		buf1.clear();
		codec.encode(msgDynamic2, buf1);
		buf1.clear();
		codec.encode(msgDynamic3, buf1);
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		return buf1.position();
	}
	
	
//	@Benchmark
	public int benchmarkEncodeFourDynamicFourTypes() throws IOException {
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		buf1.clear();
		codec.encode(msgDynamic2, buf1);
		buf1.clear();
		codec.encode(msgDynamic3, buf1);
		buf1.clear();
		codec.encode(msgDynamic4, buf1);
		return buf1.position();
	}

	// --
//	@Benchmark 
	public Object benchmarkDecodeDynamic() throws IOException {
		buf1.position(0).limit(encodedSizeDynamic);
		return codec.decode(buf1);
	}

//	@Benchmark
	public int benchmarkEncodeDynamic() throws IOException {
		buf1.clear();
		codec.encode(msgDynamic1, buf1);
		return buf1.position();
	}
	/**
	 * @return the decoded object
	 * @throws IOException
	 */
//	@Benchmark
	public Object benchmarkDecodeStatic() throws IOException {
		buf1.position(0).limit(encodedSizeNotDynamic);
		return codec.decode(buf1);
	}

	/**
	 * @return the size of the encoded data in the buffer,the end position in the buffer.
	 * @throws IOException
	 */
//	@Benchmark
	public int benchmarkEncodeStatic() throws IOException {
		buf1.clear();
		codec.encode(msgStaticDynamic, buf1);
		return buf1.position();
	}

	// --
	/**
	 * @return
	 * @throws IOException
	 */
//	@Benchmark
	public Object benchmarkDecodeDynamicManyFields() throws IOException {
		buf2.position(0).limit(encodedSizeManyFieldsDynamic);
		return codec.decode(buf2);
	}

	/**
	 * @return
	 * @throws IOException
	 */
//	@Benchmark
	public int benchmarkEncodeDynamicManyFields() throws IOException {
		buf2.clear();
		codec.encode(msgLargeManyFieldsDynamic, buf2);
		return buf2.position();
	}

	/**
	 * @return
	 * @throws IOException
	 */
//	@Benchmark
	public Object benchmarkDecodeStaticManyFields() throws IOException {
		buf2.position(0).limit(encodedSizeManyFieldsStatic);
		return codec.decode(buf2);
	}

	/**
	 * @return
	 * @throws IOException
	 */
//	@Benchmark
	public int benchmarkEncodeStaticManyFields() throws IOException {
		buf2.clear();
		codec.encode(msgStaticManyFieldsLarge, buf2);
		return buf2.position();
	}

	/**
	 * @return
	 */
	private TheRealBigMessage createManyFieldsMsg() {
		final TheRealBigMessage msg = new TheRealBigMessage();
		msg.message1 = "qwerty12345678_1";
		msg.message2 = "qwerty12345678_2";
		msg.message3 = "qwerty12345678_3";
		msg.message4 = "qwerty12345678_4";
		msg.message5 = "qwerty12345678_5";
		msg.message6 = "qwerty12345678_6";
		msg.message7 = "qwerty12345678_7";
		msg.message8 = "qwerty12345678_8";
		msg.message9 = "qwerty12345678_9";
		msg.message10 = "qwerty12345678_10";
		msg.message11 = "qwerty12345678_11";
		msg.message12 = "qwerty12345678_12";
		msg.message13 = "qwerty12345678_13";

		return msg;
	}

	/**
	 * @return
	 */
	private DynamicMessage createDynamicFieldsMsg() {
		DynamicMessage msg;
		msg = new DynamicMessage();
		msg.msg = createManyFieldsMsg();
		return msg;
	}

	/**
	 * @return
	 */
	private TheRealMessage createRealMsg() {
		TheRealMessage msg;
		msg = new TheRealMessage();
		msg = new TheRealMessage("qwerty12345678_1");
		return msg;
	}

	/**
	 * @return
	 */
	private DynamicMessage createOneReal() {
		DynamicMessage msg;
		msg = new DynamicMessage();
		msg.msg = new TheRealMessage("qwerty12345678_1");
		return msg;
	}

	/**
	 * @return
	 */
	private DynamicMessage createTwoReal() {
		DynamicMessage msg;
		msg = new DynamicMessage();
		msg.msg = new TheRealMessage2("qwerty12345678_2");
		return msg;
	}

	/**
	 * @return
	 */
	private DynamicMessage createThreeReal() {
		DynamicMessage msg;
		msg = new DynamicMessage();
		msg.msg = new TheRealMessage3("qwerty12345678_3");
		return msg;
	}

	/**
	 * @return
	 */
	private DynamicMessage createFourReal() {
		DynamicMessage msg;
		msg = new DynamicMessage();
		msg.msg = new TheRealMessage4("qwerty12345678_4");
		return msg;
	}

	/**
	 * @throws IOException
	 */
	private void calcBuffertSizes() throws IOException {
		encodedSizeDynamic = benchmarkEncodeDynamic();
		encodedSizeNotDynamic = benchmarkEncodeStatic();

		encodedSizeManyFieldsDynamic = benchmarkEncodeDynamicManyFields();
		encodedSizeManyFieldsStatic = benchmarkEncodeStaticManyFields();

		// sizeEncodeManyDynamic = benchmarkEncodeManyDynamic();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		final BenchmarkDynamic b = new BenchmarkDynamic();
		b.setup();
		// b.benchmarkEncodeManyDynamic();
	}

	// private ManyDynamicMessage createFourRealMsg() {
	// ManyDynamicMessage msg = new ManyDynamicMessage();
	// msg.msg = new TheRealMessage("qwerty12345678_1");
	// msg.msg2 = new TheRealMessage2("qwerty12345678_2");
	// msg.msg3 = new TheRealMessage3("qwerty12345678_3");
	// msg.msg4 = new TheRealMessage4("qwerty12345678_4");
	// return msg;
	// }
}
