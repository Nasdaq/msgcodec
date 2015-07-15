# MsgCodec Changelog

## 3.0.0

Changed requirement from JDK 7 to JDK 8.

Removed TAP format support (msgcodec-tap).

### msgcodec
Renamed 'ProtocolDictionary' to 'Schema':

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
 - com.cinnober.msgcodec.util.LimitInputStream
 - com.cinnober.msgcodec.util.LimitException
 
Added visitor package:

 - com.cinnober.msgcodec.visitor

Schema related changes:

 - Schema change: Added maxSize to String and Binary types. New annotation MaxSize.
 - Schame change: Group type in GroupBinding is now optional (required for encoding).
 - Message classes can now be abstract. Factory.newInstance can now throw ObjectInstantiationException.
 - Added SchemaBinder with support for schema upgrades.
 - Added SchemaParser that can parse the Schema.toString format into a Schema.

### msgcodec-blink

 - Improved performance.
 - Added experimental support for the Native Blink format.
 - Removed legacy 'instruction based' codec. The ASM-based dynamic bytecode generation codec is faster and stable.
 - Upgraded from Blink spec beta2 to beta4.
 - Removed com.cinnober.msgcodec.blink.Blink{Input|Output}Stream. Use com.cinnober.msgcodec.blink.Blink{Input|Output} instead.

### msgcodec-json

 - Added support for JavaScript safe mode (default), where large numbers are encoded as strings.
 - The "$type" field in dynamic groups, is no longer required to be first. However, having it first is best for performance.
 - Added support to encode/decode static groups in JsonCodec, i.e. JSON without the "$type" field.
