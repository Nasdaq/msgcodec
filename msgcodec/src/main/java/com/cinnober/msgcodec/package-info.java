/**
 * The msgcodec package provides classes for meta data of protocol messages
 * and interfaces for encoding and decoding messages.
 *
 * <p>Msgcodec is an API for how to describe protocol messages and their fields with meta data. The actual
 * coding is left to specific msgcodec implementations, such as JSON, XML, Blink or TAP message formats.
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
