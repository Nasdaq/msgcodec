/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
