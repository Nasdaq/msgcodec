# MsgCodec Changelog

## 3.0.0

Changed requirement from JDK 7 to JDK 8.

Renamed 'ProtocolDictionary' into 'Schema':

 - com.cinnober.msgcodec.{ProtocolDictionary => Schema}
 - com.cinnober.msgcodec.{ProtocolDictionaryBuilder => SchemaBuilder}
 - com.cinnober.msgcodec.meta.{MetaProtocolDictionary => MetaSchema}

Added new abstractions for byte sink and source in addition to
output stream and input stream:

 - com.cinnober.msgcodec.io.ByteSink
 - com.cinnober.msgcodec.io.ByteSource

Moved some of the I/O utilities to a new package:

 - com.cinnober.msgcodec.{util => io}.ByteBuffers
 - com.cinnober.msgcodec.{util => io}.ByteArrays
 - com.cinnober.msgcodec.{util => io}.InputStreams
 - com.cinnober.msgcodec.{util => io}.ByteBufferInputStream
 - com.cinnober.msgcodec.{util => io}.ByteBufferOutputStream

Renamed 'StreamCodec' into 'MsgCodec' to reflect the change
to byte sink and source:

 - com.cinnober.msgcodec.{StreamCodec => MsgCodec}
 - com.cinnober.msgcodec.{StreamCodecFactory => MsgCodecFactory}

Removed utilities:

 - com.cinnober.msgcodec.util.TempOutputStream
 
Added visitor package:

 - com.cinnober.msgcodec.visitor

Removed TAP format support (msgcodec-tap).

Schema changes:

 - Added maxSize to String and Binary types. New annotation MaxSize.
 
Blink:

 - Added experimental support for the Native Blink format.
