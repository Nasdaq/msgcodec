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
/**
 * The msgcodec package provides classes for meta data of protocol messages
 * and interfaces for encoding and decoding messages.
 *
 * <p>Msgcodec is an API for how to describe protocol messages and their fields with meta data. The actual
 * coding is left to specific msgcodec implementations, such as JSON, XML or Blink message formats.
 *
 * <p>The protocol messages are described in a {@link com.cinnober.msgcodec.Schema}.
 * The schema can be created by hand or generated. Based on the schema a specific
 * {@link com.cinnober.msgcodec.MsgCodec} implementation can be instantiated.
 *
 * <p>There is a {@link com.cinnober.msgcodec.SchemaBuilder} that can build a
 * {@link com.cinnober.msgcodec.Schema} based on annotated plain old Java objects.
 *
 * <p>Example usage:<pre>
 * SchemaBuilder builder = new SchemaBuilder();
 * Schema schema = builder.build(
 *     MyPingMessage.class,
 *     MyPongMessage.class,
 *     SomeOtherMessage.class,
 *     ...);
 * MsgCodec codec = new XxxCodecFactory(schema).createCodec();
 * OutputStream out = ...;
 * InputStrem in = ...;
 * Object message = codec.decode(in);
 * if (message instanceof MyPingMessage) {
 *     MyPingMessage ping = (MyPingMessage) message;
 *     MyPongMessage reply = new MyPongMessage(ping.getText());
 *     codec.encode(reply, out);
 * }
 * </pre>
 */
package com.cinnober.msgcodec;
