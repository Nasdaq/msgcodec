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
package com.cinnober.msgcodec.tap;

import com.cinnober.msgcodec.DecodeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import com.cinnober.msgcodec.util.LimitInputStream;
import com.cinnober.msgcodec.util.Pool;
import com.cinnober.msgcodec.util.TempOutputStream;
import java.util.List;

/**
 * The TAP codec can serialize and deserialize Java objects according to the internal Cinnober TAP message format,
 * version 9.
 *
 * <h2>Message structure</h2>
 * A TAP message consists of a header, followed by 3 unknown zero bytes, followed by a polymorphic object
 * (dynamic group).
 *
 * Header:<pre>
 * 'C' 'F' 'T' &lt;header_format&gt; &lt;header_version&gt; &lt;business_version&gt; &lt;spare&gt;
 *
 * &lt;header_format&gt; = 0x01
 * &lt;header_version&gt; = 0x09
 * &lt;business_version&gt; = Protocol specific.
 * </pre>
 *
 * <p>Polymorphic object (dynamic group):<pre>
 * &lt;object_length&gt; &lt;version_mismatch&gt; &lt;class_id_length&gt; &lt;class_id&gt; &lt;fields...&gt;
 *
 * &lt;object_length&gt; = model-encoded length of the object, with the descriptor bit set.
 * &lt;version_mismatch&gt; = unknown data, always 0x00?
 * &lt;class_id_length&gt; = model-encoded length of the class id, with no descriptor bit set.
 * &lt;class_id&gt; = latin1 string, the class id.
 * &lt;fields...&gt; = the field values in the object encoded according to their types
 * </pre>
 *
 * <p>Regular object (static group):<pre>
 * &lt;object_length&gt; &lt;fields...&gt;
 *
 * &lt;object_length&gt; = model-encoded length of the object, with no descriptor bit set.
 * &lt;fields...&gt; = the field values in the object encoded according to their types
 * </pre>
 *
 * TODO: explain how the field data types are encoded.
 *
 *
 * @author mikael.brannstrom
 *
 */
public class TapCodec implements StreamCodec {

    private final GroupTypeAccessor groupTypeAccessor;
    /** The compiled static groups by group type. */
    private final Map<Object, StaticGroupInstruction> groupInstructionsByGroupType;
    /** The compiled static groups by group name. */
    private final Map<String, StaticGroupInstruction> groupInstructionsByName;

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
    private final TapOutputStream internalStream;


    /** Create a Blink codec, with an internal buffer pool of 8192 bytes.
     *
     * @param dictionary the definition of the messages to be understood by the codec.
     */
    public TapCodec(ProtocolDictionary dictionary) {
        this(dictionary, new ConcurrentBufferPool(8192, 1));
    }
    /** Create a Blink codec.
     *
     * @param dictionary the definition of the messages to be understood by the codec.
     * @param bufferPool the buffer pool, needed for temporary storage while <em>encoding</em>.
     */
    public TapCodec(ProtocolDictionary dictionary, Pool<byte[]> bufferPool) {
        if (!dictionary.isBound()) {
            throw new IllegalArgumentException("ProtocolDictionary not bound");
        }
        if (bufferPool != null) {
            this.internalBuffer = new TempOutputStream(bufferPool);
            this.internalStream = new TapOutputStream(internalBuffer);
        } else {
            this.internalBuffer = null;
            this.internalStream = null;
        }

        groupTypeAccessor = dictionary.getBinding().getGroupTypeAccessor();
        groupInstructionsByGroupType = new HashMap<>(dictionary.getGroups().size() * 2);
        groupInstructionsByName = new HashMap<>(dictionary.getGroups().size() * 2);
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
            groupInstructionsByName.put(groupDef.getName(), groupInstruction);
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

    @SuppressWarnings({ "rawtypes" })
    private FieldInstruction createFieldInstruction(ProtocolDictionary dictionary, FieldDef field, TypeDef type) {
        type = dictionary.resolveToType(type, true);
        GroupDef typeGroup = dictionary.resolveToGroup(type);
        boolean required = field == null || field.isRequired();
        if (type instanceof TypeDef.Sequence) {
            // --- SEQUENCE ---
            FieldInstruction elementInstruction =
                    createFieldInstruction(dictionary, null, ((TypeDef.Sequence) type).getComponentType());
            if (field.getJavaClass().isArray()) {
                return new FieldInstruction.ArraySequence(field, elementInstruction);
            } else if (List.class.equals(field.getJavaClass())) {
                return new FieldInstruction.ListSequence(field, elementInstruction);
            } else {
                throw new RuntimeException("Unhandled sequence type: " + field.getJavaClass().getName());
            }
        } else {
            // --- SIMPLE TYPE ---
            switch (type.getType()) {
            case INT8:
            case UINT8:
                if (required) {
                    return new FieldInstruction.Int8(field);
                } else {
                    return optional(new FieldInstruction.Int8(field));
                }
            case INT16:
            case UINT16:
                if (required) {
                    return new FieldInstruction.VarInt16(field);
                } else {
                    return optional(new FieldInstruction.VarInt16(field));
                }
            case INT32:
            case UINT32:
                if (required) {
                    return new FieldInstruction.VarInt32(field);
                } else {
                    return optional(new FieldInstruction.VarInt32(field));
                }
            case INT64:
            case UINT64:
                if (required) {
                    return new FieldInstruction.VarInt64(field);
                } else {
                    return optional(new FieldInstruction.VarInt64(field));
                }
            case FLOAT32:
                if (required) {
                    return new FieldInstruction.Float32(field);
                } else {
                    return optional(new FieldInstruction.Float32(field));
                }
            case FLOAT64:
                if (required) {
                    return new FieldInstruction.Float64(field);
                } else {
                    return optional(new FieldInstruction.Float64(field));
                }
            case DECIMAL:
                if (required) {
                    return new FieldInstruction.Decimal(field);
                } else {
                    return optional(new FieldInstruction.Decimal(field));
                }
            case BOOLEAN:
                if (required) {
                    return new FieldInstruction.Boolean(field);
                } else {
                    return optional(new FieldInstruction.Boolean(field));
                }
            case STRING:
                return new FieldInstruction.StringLatin1Null(field);
            case BINARY:
                return new FieldInstruction.BinaryNull(field);
            case ENUM:
                if (field.getJavaClass().isEnum()) {
                    if (required) {
                        return new FieldInstruction.Enumeration(field, (Enum) type);
                    } else {
                        return optional(new FieldInstruction.Enumeration(field, (Enum) type));
                    }
                } else if (field.getJavaClass().equals(int.class) || field.getJavaClass().equals(Integer.class)) {
                    if (required) {
                        return new FieldInstruction.IntEnumeration(field);
                    } else {
                        return optional(new FieldInstruction.IntEnumeration(field));
                    }
                } else {
                    throw new RuntimeException("Unhandled ENUM java class: " + field.getJavaClass());
                }
            // TODO: case TIME:
            case REFERENCE: // static group
                return new FieldInstruction.StaticGroupNull(field,
                        groupInstructionsByGroupType.get(typeGroup.getGroupType()), this);
            case DYNAMIC_REFERENCE: // dynamic group
                return new FieldInstruction.DynamicGroupNull(field, this);
            default:
                throw new RuntimeException("Unhandled type: " + type.getType());
            }
        }
    }

    private <T> FieldInstruction<T> optional(FieldInstruction<T> instruction) {
        return new FieldInstruction.Null<>(instruction);
    }

    @Override
    public void encode(Object group, OutputStream out) throws IOException {
        if (!(out instanceof TapOutputStream)) {
            out = new TapOutputStream(out);
        }
        encode(group, (TapOutputStream)out);
    }
    @Override
    public Object decode(InputStream in) throws IOException {
        if (!(in instanceof TapInputStream)) {
            in = new TapInputStream(in);
        }
        return decode((TapInputStream)in);
    }

    public void encode(Object group, TapOutputStream out) throws IOException {
        writeHeader(out);
        writeDynamicGroup(group, out);
    }
    public Object decode(TapInputStream in) throws IOException {
        readHeader(in);
        return readDynamicGroup(in);
    }

    private void writeHeader(TapOutputStream out) throws IOException {
        // The header
        out.write('C');
        out.write('F');
        out.write('T');
        out.write(1); // header format
        out.write(9); // header version
        out.write(2); // business version
        out.write(0); // spare
        out.write(0); // spare

        // Unknown stuff
        out.write(0);
        out.write(0);
        out.write(0);
    }

    private void readHeader(TapInputStream in) throws IOException {
        readAssert(in, 'C');
        readAssert(in, 'F');
        readAssert(in, 'T');
        readAssert(in, 1); // header format
        readAssert(in, 9); // header version
        readAssert(in, 2); // business version
        readAssert(in, 0); // spare
        readAssert(in, 0); // spare

        // Unknown stuff
        readAssert(in, 0);
        readAssert(in, 0);
        readAssert(in, 0);
    }

    private void readAssert(TapInputStream in, int expected) throws IOException {
        int b = in.read();
        if (b != expected) {
            throw new DecodeException("Expected " + expected + " read " + b);
        }
    }

    /**
     * @param value
     * @param out
     * @throws IOException
     */
    void writeDynamicGroup(Object value, TapOutputStream out) throws IOException {
        if (internalBuffer == null) {
            throw new UnsupportedOperationException("Encoding is disabled!");
        }

        if (value == null) {
            out.writeNull();
            return;
        }

        Object groupType = groupTypeAccessor.getGroupType(value);
        StaticGroupInstruction groupInstruction = groupInstructionsByGroupType.get(groupType);
        if (groupInstruction == null) {
            throw new IllegalArgumentException("Cannot encode group. Java class not found in protocol dictionary: " +
                    value.getClass().getName());
        }
        Preamble preamble = new Preamble(true);
        if (!preambleStack.isEmpty()) {
            preambleStack.peek().startChild(preamble);
        }
        preambleStack.push(preamble);
        internalStream.write(0); // version mismatch stuff
        groupInstruction.encodeGroupClassId(internalStream);
        groupInstruction.encodeGroup(value, internalStream);
        preambleStack.pop();
        preamble.end();
        if (preambleStack.isEmpty()) {
            preamble.flush(out);
        }
    }

    void writeStaticGroup(Object value, TapOutputStream out, StaticGroupInstruction groupInstruction)
            throws IOException {
        if (internalBuffer == null) {
            throw new UnsupportedOperationException("Encoding is disabled!");
        }

        if (value == null) {
            out.writeNull();
            return;
        }

        Preamble preamble = new Preamble(false);
        if (!preambleStack.isEmpty()) {
            preambleStack.peek().startChild(preamble);
        }
        preambleStack.push(preamble);
        groupInstruction.encodeGroup(value, internalStream);
        preambleStack.pop();
        preamble.end();
        if (preambleStack.isEmpty()) {
            preamble.flush(out);
        }
    }

    /** Read a dynamic group.
     * @param in the stream to read from.
     * @return the group, or null.
     * @throws IOException
     */
    Object readDynamicGroup(TapInputStream in) throws IOException {
        int size = in.readModelLength(true);
        if (size < 0) {
            return null;
        }
        int limit = limit(in, size);

        int versionMismatchModel = in.read();
        if (versionMismatchModel != 0) {
            // PENDING: maybe just parse away this block?
            throw new DecodeException("Expected zero TAP version mismatch descriptor, got: " + versionMismatchModel);
        }
        String groupClassId = readGroupClassId(in);
        StaticGroupInstruction groupInstruction = groupInstructionsByName.get(groupClassId);
        if (groupInstruction == null) {
            throw new DecodeException("Unknown TAP message class id: " + groupClassId);
        }
        Object group = groupInstruction.decodeGroup(in);
        in.skip(in.limit());
        in.limit(limit); // restore old limit
        return group;
    }

    Object readStaticGroup(TapInputStream in, StaticGroupInstruction groupInstruction) throws IOException {
        int size = in.readModelLength(false);
        if (size < 0) {
            return null;
        }
        int limit = limit(in, size);
        Object group = groupInstruction.decodeGroup(in);
        in.skip(in.limit());
        in.limit(limit); // restore old limit
        return group;
    }

    private String readGroupClassId(TapInputStream in) throws IOException {
        int size = in.readModelLength(false);
        if (size < 0) {
            return null;
        }
        int limit = limit(in, size);
        String classId = in.readStringLatin1();
        in.skip(in.limit());
        in.limit(limit); // restore old limit
        return classId;
    }

    private int limit(LimitInputStream in, int size) throws IOException {
        int limit = in.limit();
        if (limit >= 0) {
            if (size > limit) {
                // there is already a limit that is smaller than this message size
                throw new DecodeException("Group size preamble (" + size +
                        ") goes beyond current stream limit (" + limit + ").");
            } else {
                limit -= size;
                in.limit(size);
            }
        }
        return limit;
    }


    private int sizeOfPreamble(int size) {
        return TapOutputStream.sizeOfModelLength(size);
    }

    private class Preamble {
        private final boolean descriptor;
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

        public Preamble(boolean descriptor) {
            startPosition = internalBuffer.position();
            this.descriptor = descriptor;
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
            addedSize += sizeOfPreamble(size);
        }
        private void copyTo(TapOutputStream out) throws IOException {
            // write size preamble
            out.writeModelLength(size, descriptor);
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
        public void flush(TapOutputStream out) throws IOException {
            calculateSize();
            copyTo(out);
            internalBuffer.reset();
        }
    }
}
