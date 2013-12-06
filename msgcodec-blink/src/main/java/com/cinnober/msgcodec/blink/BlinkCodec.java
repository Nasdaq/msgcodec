/*
 * Copyright (c) 2013 Cinnober Financial Technology AB, Stockholm,
 * Sweden. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 *
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */
package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.DecodeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.GroupTypeAccessor;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Enum;
import com.cinnober.msgcodec.util.ConcurrentBufferPool;
import com.cinnober.msgcodec.util.Pool;
import com.cinnober.msgcodec.util.TempOutputStream;
import java.util.List;

/**
 * The Blink codec can serialize and deserialize Java objects according to
 * the Blink compact binary encoding format.
 *
 * Null values are supported in encode and decode.
 *
 * <p>The Blink Codec understands the annotation named "maxLength" for strings, binaries and sequences.
 * If present, this limit will be used as the maximum number of chars, bytes and elements for
 * strings, binaries and sequences respectively.
 *
 * <p>See the <a href="http://blinkprotocol.org/s/BlinkSpec-beta2.pdf">Blink Specification beta2 - 2013-02-05.</a>
 *
 * @author mikael.brannstrom
 *
 */
public class BlinkCodec implements StreamCodec {

    private final GroupTypeAccessor groupTypeAccessor;
    /** The compiled static groups by group type. */
    private final Map<Object, StaticGroupInstruction> groupInstructionsByGroupType;
    /** The compiled static groups by group id. */
    private final Map<Integer, StaticGroupInstruction> groupInstructionsById;

    /** The size preambles. The top of the stack refers to the
     * dynamic group that is currently being encoded.
     * The stack is only non-empty while encoding a message.
     */
    private final Stack<Preamble> preambleStack = new Stack<>();
    /** The internal buffer used for temporary storage of encoded dynamic groups.
     * This is needed in order to know how large an encoded dynamic group is, which
     * is written in the preamble.
     */
    private final TempOutputStream internalBuffer;
    /** Blink output stream wrapped around the {@link #internalBuffer}. */
    private final BlinkOutputStream internalStream;


    /** Create a Blink codec, with an internal buffer pool of 8192 bytes.
     *
     * @param dictionary the definition of the messages to be understood by the codec.
     */
    public BlinkCodec(ProtocolDictionary dictionary) {
        this(dictionary, new ConcurrentBufferPool(8192, 1));
    }
    /** Create a Blink codec.
     *
     * @param dictionary the definition of the messages to be understood by the codec.
     * @param bufferPool the buffer pool, needed for temporary storage while <em>encoding</em>.
     */
    public BlinkCodec(ProtocolDictionary dictionary, Pool<byte[]> bufferPool) {
        if (!dictionary.isBound()) {
            throw new IllegalArgumentException("ProtocolDictionary not bound");
        }
        if (bufferPool != null) {
            this.internalBuffer = new TempOutputStream(bufferPool);
            this.internalStream = new BlinkOutputStream(internalBuffer);
        } else {
            this.internalBuffer = null;
            this.internalStream = null;
        }
        groupTypeAccessor = dictionary.getBinding().getGroupTypeAccessor();
        groupInstructionsByGroupType = new HashMap<>(dictionary.getGroups().size() * 2);
        groupInstructionsById = new HashMap<>(dictionary.getGroups().size() * 2);
        // first store place holders for group instructions,
        // since they might be needed when creating field instructions
        for (GroupDef groupDef : dictionary.getGroups()) {
            StaticGroupInstruction superGroupInstruction = null;
            if (groupDef.getSuperGroup() != null) {
                GroupDef superGroup = dictionary.getGroup(groupDef.getSuperGroup());
                superGroupInstruction = groupInstructionsByGroupType.get(superGroup.getGroupType());
                if (superGroupInstruction == null) {
                    throw new RuntimeException("I think I found a bug in ProtocolDictionary");
                }
            }

            StaticGroupInstruction groupInstruction = new StaticGroupInstruction(groupDef, superGroupInstruction);
            groupInstructionsByGroupType.put(groupDef.getGroupType(), groupInstruction);
            groupInstructionsById.put(groupDef.getId(), groupInstruction);
        }
        // create field instructions for all groups
        for (GroupDef groupDef : dictionary.getGroups()) {
            StaticGroupInstruction groupInstruction = groupInstructionsByGroupType.get(groupDef.getGroupType());
            int index = 0;
            for (FieldDef fieldDef : groupDef.getFields()) {
                @SuppressWarnings("rawtypes")
                FieldInstruction fieldInstruction = createFieldInstruction(dictionary, fieldDef, fieldDef.getType());
                groupInstruction.initFieldInstruction(index++, fieldInstruction);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private FieldInstruction createFieldInstruction(ProtocolDictionary dictionary, FieldDef field, TypeDef type) {
        type = dictionary.resolveToType(type, true);
        GroupDef typeGroup = dictionary.resolveToGroup(type);
        boolean required = field == null || field.isRequired();
        if (type instanceof TypeDef.Sequence) {
            // --- SEQUENCE ---
            FieldInstruction elementInstruction =
                    createFieldInstruction(dictionary, null, ((TypeDef.Sequence) type).getComponentType());
            if (field.getJavaClass().isArray()) {
                if (required) {
                    return new FieldInstruction.ArraySequence(field, elementInstruction);
                } else {
                    return new FieldInstruction.ArraySequenceNull(field, elementInstruction);
                }
            } else if (List.class.equals(field.getJavaClass())) {
                if (required) {
                    return new FieldInstruction.ListSequence(field, elementInstruction);
                } else {
                    return new FieldInstruction.ListSequenceNull(field, elementInstruction);
                }
            } else {
                throw new RuntimeException("Unhandled sequence type: " + field.getJavaClass().getName());
            }
        } else {
            // --- SIMPLE TYPE ---
            switch (type.getType()) {
            case INT8:
                if (required) {
                    return new FieldInstruction.Int8(field);
                } else {
                    return new FieldInstruction.Int8Null(field);
                }
            case INT16:
                if (required) {
                    return new FieldInstruction.Int16(field);
                } else {
                    return new FieldInstruction.Int16Null(field);
                }
            case INT32:
                if (required) {
                    return new FieldInstruction.Int32(field);
                } else {
                    return new FieldInstruction.Int32Null(field);
                }
            case INT64:
                if (required) {
                    return new FieldInstruction.Int64(field);
                } else {
                    return new FieldInstruction.Int64Null(field);
                }
            case UINT8:
                if (required) {
                    return new FieldInstruction.UInt8(field);
                } else {
                    return new FieldInstruction.UInt8Null(field);
                }
            case UINT16:
                if (required) {
                    return new FieldInstruction.UInt16(field);
                } else {
                    return new FieldInstruction.UInt16Null(field);
                }
            case UINT32:
                if (required) {
                    return new FieldInstruction.UInt32(field);
                } else {
                    return new FieldInstruction.UInt32Null(field);
                }
            case UINT64:
                if (required) {
                    return new FieldInstruction.UInt64(field);
                } else {
                    return new FieldInstruction.UInt64Null(field);
                }
            case FLOAT32:
                if (required) {
                    return new FieldInstruction.Float32(field);
                } else {
                    return new FieldInstruction.Float32Null(field);
                }
            case FLOAT64:
                if (required) {
                    return new FieldInstruction.Float64(field);
                } else {
                    return new FieldInstruction.Float64Null(field);
                }
            case DECIMAL:
                if (required) {
                    return new FieldInstruction.Decimal(field);
                } else {
                    return new FieldInstruction.DecimalNull(field);
                }
            case BIGDECIMAL:
                if (required) {
                    return new FieldInstruction.BigDecimal(field);
                } else {
                    return new FieldInstruction.BigDecimalNull(field);
                }
            case BIGINT:
                if (required) {
                    return new FieldInstruction.BigInt(field);
                } else {
                    return new FieldInstruction.BigIntNull(field);
                }
            case BOOLEAN:
                if (required) {
                    return new FieldInstruction.Boolean(field);
                } else {
                    return new FieldInstruction.BooleanNull(field);
                }
            case STRING:
                if (required) {
                    return new FieldInstruction.StringUTF8(field);
                } else {
                    return new FieldInstruction.StringUTF8Null(field);
                }
            case BINARY:
                if (required) {
                    return new FieldInstruction.Binary(field);
                } else {
                    return new FieldInstruction.BinaryNull(field);
                }
            case ENUM:
                if (field.getJavaClass().isEnum()) {
                    if (required) {
                        return new FieldInstruction.Enumeration(field, (Enum) type);
                    } else {
                        return new FieldInstruction.EnumerationNull(field, (Enum) type);
                    }
                } else if (field.getJavaClass().equals(int.class) || field.getJavaClass().equals(Integer.class)) {
                    if (required) {
                        return new FieldInstruction.IntEnumeration(field);
                    } else {
                        return new FieldInstruction.IntEnumerationNull(field);
                    }
                } else {
                    throw new RuntimeException("Unhandled ENUM java type: " + field.getJavaClass());
                }
            case TIME:
                if (field.getJavaClass().equals(Date.class)) {
                    if (required) {
                        return new FieldInstruction.DateTime(field);
                    } else {
                        return new FieldInstruction.DateTimeNull(field);
                    }
                } else if (field.getJavaClass().equals(long.class) || field.getJavaClass().equals(Long.class)) {
                    if (required) {
                        return new FieldInstruction.Int64(field);
                    } else {
                        return new FieldInstruction.Int64Null(field);
                    }
                } else if (field.getJavaClass().equals(int.class) || field.getJavaClass().equals(Integer.class)) {
                    if (required) {
                        return new FieldInstruction.Int32(field);
                    } else {
                        return new FieldInstruction.Int32Null(field);
                    }
                } else {
                    throw new RuntimeException("Unhandled time type: "+field.getJavaClass().getName());
                }
            case REFERENCE: // static group
                if (required) {
                    return new FieldInstruction.StaticGroup(field, groupInstructionsByGroupType.get(typeGroup.getGroupType()));
                } else {
                    return new FieldInstruction.StaticGroupNull(field, groupInstructionsByGroupType.get(typeGroup.getGroupType()));
                }
            case DYNAMIC_REFERENCE: // dynamic group
                if (required) {
                    return new FieldInstruction.DynamicGroup(field, this);
                } else {
                    return new FieldInstruction.DynamicGroupNull(field, this);
                }
            default:
                throw new RuntimeException("Unhandled type: " + type.getType());
            }
        }
    }

    @Override
    public void encode(Object group, OutputStream out) throws IOException {
        if (out instanceof BlinkOutputStream) {
            writeDynamicGroup(group, (BlinkOutputStream)out);
        } else {
            writeDynamicGroup(group, new BlinkOutputStream(out));
        }
    }
    @Override
    public Object decode(InputStream in) throws IOException {
        if (in instanceof BlinkInputStream) {
            return readDynamicGroup((BlinkInputStream)in);
        } else {
            return readDynamicGroup(new BlinkInputStream(in));
        }
    }

    public void encode(Object group, BlinkOutputStream out) throws IOException {
        writeDynamicGroupNull(group, out);
    }
    public Object decode(BlinkInputStream in) throws IOException {
        return readDynamicGroupNull(in);
    }

    /**
     * @param value
     * @param out
     * @throws IOException
     */
    void writeDynamicGroup(Object value, BlinkOutputStream out) throws IOException {
        if (internalBuffer == null) {
            throw new UnsupportedOperationException("Encoding is disabled!");
        }
        writeDynamicGroup(value, out, false);
    }
    /**
     * @param value
     * @param out
     * @throws IOException
     */
    void writeDynamicGroupNull(Object value, BlinkOutputStream out) throws IOException {
        if (value == null) {
            out.writeUInt32Null(null);
        } else {
            writeDynamicGroup(value, out, true);
        }
    }
    private void writeDynamicGroup(Object value, BlinkOutputStream out, boolean nullable) throws IOException {
        Object groupType = groupTypeAccessor.getGroupType(value);
        StaticGroupInstruction groupInstruction = groupInstructionsByGroupType.get(groupType);
        if (groupInstruction == null) {
            throw new IllegalArgumentException("Cannot encode group. Group type not found in protocol dictionary: " +
                    groupType);
        }
        Preamble preamble = new Preamble(nullable);
        if (!preambleStack.isEmpty()) {
            preambleStack.peek().startChild(preamble);
        }
        preambleStack.push(preamble);
        groupInstruction.encodeGroupId(internalStream);
        groupInstruction.encodeGroup(value, internalStream);
        preambleStack.pop();
        preamble.end();
        if (preambleStack.isEmpty()) {
            preamble.flush(out);
        }
    }


    /** Read a dynamic group.
     * @param in the stream to read from.
     * @return the group, not null.
     * @throws IOException
     */
    Object readDynamicGroup(BlinkInputStream in) throws IOException {
        int size = in.readUInt32();
        return readDynamicGroup(size, in);
    }
    /** Read a nullable dynamic group.
     * @param in the stream to read from.
     * @return the group, or null.
     * @throws IOException
     */
    Object readDynamicGroupNull(BlinkInputStream in) throws IOException {
        Integer sizeObj = in.readUInt32Null();
        if (sizeObj == null) {
            return null;
        }
        int size = sizeObj.intValue();
        return readDynamicGroup(size, in);
    }

    private Object readDynamicGroup(int size, BlinkInputStream in) throws IOException {
        int limit = in.limit();
        if (limit >= 0) {
            if (size > limit) {
                // there is already a limit that is smaller than this message size
                throw new DecodeException("Dynamic group size preamble (" + size +
                        ") goes beyond current stream limit (" + limit + ").");
            } else {
                limit -= size;
                in.limit(size);
            }
        }
        int groupId = in.readUInt32();
        StaticGroupInstruction groupInstruction = groupInstructionsById.get(groupId);
        if (groupInstruction == null) {
            throw new DecodeException("Unknown group id: " + groupId);
        }

        Object group = groupInstruction.decodeGroup(in);
        in.skip(in.limit());
        in.limit(limit); // restore old limit
        return group;
    }

    private int sizeOfPreamble(int size, boolean nullable) {
        return BlinkOutputStream.sizeOfUnsignedVLC(size);
    }

    private class Preamble {
        /** True iff this group is nullable (optional). */
        private final boolean nullable;
        /** Linked list stuff. */
        private Preamble firstChild;
        /** Linked list stuff. */
        private Preamble lastChild;
        /** Linked list stuff. */
        private Preamble nextSibling;
        /** The position of the first byte in this group. */
        private final int startPosition;
        /** The position of the first byte after this group. */
        private int endPosition;

        /** The size of this group, excluding the preamble of this group.
         * I.e. the size value to be written in the preamble. */
        private int size;
        /** The number of bytes added by the preamble(s) of this group and all child groups. */
        private int addedSize;

        public Preamble(boolean nullable) {
            this.nullable = nullable;
            startPosition = internalBuffer.position();
        }
        void startChild(Preamble preamble) {
            if (firstChild == null) {
                firstChild = preamble;
            }
            if (lastChild != null) {
                lastChild.nextSibling = preamble;
            }
            lastChild = preamble;
        }
        void end() {
            endPosition = internalBuffer.position();
        }
        private int getAddedSize() {
            return addedSize;
        }
        private void calculateSize() {
            size = endPosition - startPosition;
            addedSize = 0;
            for (Preamble child = firstChild; child != null; child = child.nextSibling) {
                child.calculateSize();
                addedSize += child.getAddedSize();
            }
            size += addedSize;
            addedSize += sizeOfPreamble(size, nullable);
        }
        private void copyTo(BlinkOutputStream out) throws IOException {
            // write size preamble
            if (nullable) {
                out.writeUInt32Null(size);
            } else {
                out.writeUInt32(size);
            }
            int position = startPosition;
            for (Preamble child = firstChild; child != null; child = child.nextSibling) {
                internalBuffer.copyTo(out, position, child.startPosition);
                child.copyTo(out);
                position = child.endPosition;
            }
            internalBuffer.copyTo(out, position, endPosition);
        }
        /**
         * Flush the temporary encoded group, add size preambles and finally reset the internal buffer.
         */
        public void flush(BlinkOutputStream out) throws IOException {
            calculateSize();
            copyTo(out);
            internalBuffer.reset();
        }
    }
}
