/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec;

/**
 * A factory that can create a stream codec.
 * 
 * <p>Example usage:</p>
 * <pre>
 * // init (once)
 * ProtocolDictionary dictionary = ...
 * StreamCodecFactory codecFactory = new XxxCodecFactory(dictionary, ...);
 * 
 * // create codec (for each socket, thread, etc)
 * StreamCodec codec = codecFactory.createStreamCodec();
 * OutputStream out = ...
 * InputStream in = ...
 * 
 * // use codec (for each message to encode/decode)
 * Object message = ...
 * codec.encode(message, out);
 * Object obj = codec.decode(in);
 * </pre>
 * 
 * @author mikael.brannstrom
 */
public interface StreamCodecFactory {
    /**
     * Create a new stream codec.
     * @return a stream codec, not null.
     */
    StreamCodec createStreamCodec();
}
