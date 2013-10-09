/**
 * The msgcodec package provides classes for meta data of protocol messages
 * and interfaces for encoding and decoding messages.
 *
 * <p>Msgcodec is an API for how to describe protocol messages and their fields with meta data. The actual
 * coding is left to specific msgcodec implementations, such as JSON, XML, Blink or TAP message formats.
 *
 * <p>The protocol messages are described in a {@link com.cinnober.msgcodec.ProtocolDictionary}.
 * The protocol dictionary can be created by hand or generated. Based on the protocol dictionary a specific
 * {@link com.cinnober.msgcodec.StreamCodec} implementation can be instantiated.
 *
 * <p>There is a {@link com.cinnober.msgcodec.ProtocolDictionaryBuilder} that can build a
 * {@link com.cinnober.msgcodec.ProtocolDictionary} based on annotated plain old Java objects.
 *
 * <p>Example usage:<pre>
 * ProtocolDictionaryBuilder builder = new ProtocolDictionaryBuilder();
 * ProtocolDictionary protocolDictionary = builder.build(MyPingMessage.class,
 *                                                       MyPongMessage.class,
 *                                                       SomeOtherMessage.class,
 *                                                       ...);
 * StreamCodec codec = new XxxStreamCodec(protocolDictionary);
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
