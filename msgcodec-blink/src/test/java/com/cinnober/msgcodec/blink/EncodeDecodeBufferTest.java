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

import org.junit.Assert;
import org.junit.Test;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.io.ByteBufferBuf;

/**
 * @author roland.lidstrom, Cinnober Financial Technology North AB
 *
 */
public class EncodeDecodeBufferTest {
	 
	 @Test
	public void setup() throws IOException {
		BlinkCodec codec;
		ByteBuf buf3;
		ManyDynamicMessage msg;
		int sizeEncodeManyDynamic;
		final int bufferSize = 1024;

		Schema dict = new SchemaBuilder(true).build( 
				DynamicMessage.class,
				TheMessage.class,
				TheRealMessage.class,
				TheRealBigMessage.class,
				ManyDynamicMessage.class,
				TheRealMessage2.class,
				TheRealMessage3.class,
				TheRealMessage4.class
				).assignGroupIds();
		BlinkCodecFactory factory = new BlinkCodecFactory(dict);
		codec = factory.createCodec();

		buf3 = new ByteBufferBuf(ByteBuffer.allocate(bufferSize));

		msg = new ManyDynamicMessage();
		msg.msg = new TheRealMessage("qwerty12345678_1");
		sizeEncodeManyDynamic = benchmarkEncodeManyDynamic(buf3, codec, msg);
		
		Assert.assertTrue(sizeEncodeManyDynamic>0);
	}

	public int benchmarkEncodeManyDynamic(ByteBuf buf3,BlinkCodec codec,ManyDynamicMessage msg) throws IOException {
		buf3.clear();
		codec.encode(msg, buf3);
		return buf3.position();
	}
}
