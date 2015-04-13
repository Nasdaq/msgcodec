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

package com.cinnober.msgcodec;

/**
 * A factory that can create a message codec.
 * 
 * <p>Example usage:</p>
 * <pre>
 * // init (once)
 * Schema schema = ...
 * MsgCodecFactory codecFactory = new XxxCodecFactory(schema);
 * codecFactory.setSomeParameter(...); // optionally configure
 * 
 * // create codec (for each socket, thread, etc)
 * MsgCodec codec = codecFactory.createCodec();
 * OutputStream out = ...
 * InputStream in = ...
 * 
 * // use codec (for each message to encode/decode)
 * Object message = ...
 * codec.encode(message, out);
 * Object obj = codec.decode(in);
 * </pre>
 *
 * <p>It is recommended that implementations have parameter setter methods that returns the
 * instance itself to allow for method chaining. For example:
 * <pre>
 * MsgCodec codec = new XxxCodecFactory(schema).setFoo(a).setBar(b).createCodec();
 * </pre>
 *
 * @author mikael.brannstrom
 */
public interface MsgCodecFactory {
    /**
     * Create a new message codec.
     * @return a message codec, not null.
     * @throws MsgCodecInstantiationException if the codec could not be instantiated,
     * for example due to a factory configuration error.
     */
    MsgCodec createCodec() throws MsgCodecInstantiationException;
}
