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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSource;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.GroupTypeAccessor;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.TypeDef;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A codec for a specific schema, which use FieldInstruction for encoding/decoding.
 *
 * @author mikael.brannstrom
 */
class InstructionCodec extends GeneratedCodec {
    private final GroupTypeAccessor groupTypeAccessor;
    private final Map<Object, StaticGroupInstruction> groupInstructionsByGroupType;
    /** The compiled static groups by group id. */
    private final Map<Integer, StaticGroupInstruction> groupInstructionsById;

    public InstructionCodec(BlinkCodec codec, Schema schema) {
        super(codec);
        groupTypeAccessor = schema.getBinding().getGroupTypeAccessor();
        groupInstructionsByGroupType = new HashMap<>(schema.getGroups().size() * 2);
        groupInstructionsById = new HashMap<>(schema.getGroups().size() * 2);
        // first store place holders for group instructions,
        // since they might be needed when creating field instructions
        for (GroupDef groupDef : schema.getGroups()) {
            StaticGroupInstruction superGroupInstruction = null;
            if (groupDef.getSuperGroup() != null) {
                GroupDef superGroup = schema.getGroup(groupDef.getSuperGroup());
                superGroupInstruction = groupInstructionsByGroupType.get(superGroup.getGroupType());
                if (superGroupInstruction == null) {
                    throw new RuntimeException("I think I found a bug in Schema");
                }
            }

            StaticGroupInstruction groupInstruction = new StaticGroupInstruction(groupDef, superGroupInstruction);
            groupInstructionsByGroupType.put(groupDef.getGroupType(), groupInstruction);
            groupInstructionsById.put(groupDef.getId(), groupInstruction);
        }
        // create field instructions for all groups
        for (GroupDef groupDef : schema.getGroups()) {
            StaticGroupInstruction groupInstruction = groupInstructionsByGroupType.get(groupDef.getGroupType());
            int index = 0;
            for (FieldDef fieldDef : groupDef.getFields()) {
                @SuppressWarnings("rawtypes")
                FieldInstruction fieldInstruction = createFieldInstruction(schema, fieldDef, fieldDef.getType(),
                        fieldDef.getJavaClass(), fieldDef.getComponentJavaClass());
                groupInstruction.initFieldInstruction(index++, fieldInstruction);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private FieldInstruction createFieldInstruction(Schema schema, FieldDef field, TypeDef type,
            Class<?> javaClass, Class<?> componentJavaClass) {
        type = schema.resolveToType(type, true);
        GroupDef typeGroup = schema.resolveToGroup(type);
        boolean required = field == null || field.isRequired();
        if (type instanceof TypeDef.Sequence) {
            // --- SEQUENCE ---
            FieldInstruction elementInstruction =
                    createFieldInstruction(schema, null, ((TypeDef.Sequence) type).getComponentType(),
                            componentJavaClass, null);
            if (javaClass.isArray()) {
                if (required) {
                    return new FieldInstruction.ArraySequence(field, elementInstruction);
                } else {
                    return new FieldInstruction.ArraySequenceNull(field, elementInstruction);
                }
            } else if (List.class.equals(javaClass)) {
                if (required) {
                    return new FieldInstruction.ListSequence(field, elementInstruction);
                } else {
                    return new FieldInstruction.ListSequenceNull(field, elementInstruction);
                }
            } else {
                throw new RuntimeException("Unhandled sequence type: " + javaClass.getName());
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
                if (javaClass.isEnum()) {
                    if (required) {
                        return new FieldInstruction.Enumeration(field, (TypeDef.Enum) type, javaClass);
                    } else {
                        return new FieldInstruction.EnumerationNull(field, (TypeDef.Enum) type, javaClass);
                    }
                } else if (javaClass.equals(int.class) || javaClass.equals(Integer.class)) {
                    if (required) {
                        return new FieldInstruction.IntEnumeration(field);
                    } else {
                        return new FieldInstruction.IntEnumerationNull(field);
                    }
                } else {
                    throw new RuntimeException("Unhandled ENUM java type: " + javaClass);
                }
            case TIME:
                if (javaClass.equals(Date.class)) {
                    if (required) {
                        return new FieldInstruction.DateTime(field);
                    } else {
                        return new FieldInstruction.DateTimeNull(field);
                    }
                } else if (javaClass.equals(long.class) || javaClass.equals(Long.class)) {
                    if (required) {
                        return new FieldInstruction.Int64(field);
                    } else {
                        return new FieldInstruction.Int64Null(field);
                    }
                } else if (javaClass.equals(int.class) || javaClass.equals(Integer.class)) {
                    if (required) {
                        return new FieldInstruction.Int32(field);
                    } else {
                        return new FieldInstruction.Int32Null(field);
                    }
                } else {
                    throw new RuntimeException("Unhandled time type: "+javaClass.getName());
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
    protected void writeStaticGroupWithId(ByteSink out, Object value) throws IOException, IllegalArgumentException {
        Object groupType = groupTypeAccessor.getGroupType(value);
        StaticGroupInstruction groupInstruction = groupInstructionsByGroupType.get(groupType);
        if (groupInstruction == null) {
            throw new IllegalArgumentException("Cannot encode group. Group type not found in schema: " +
                    groupType);
        }
        groupInstruction.encodeGroupId(out);
        groupInstruction.encodeGroup(value, out);
    }

    @Override
    protected Object readStaticGroup(int groupId, ByteSource in) throws IOException, DecodeException {
        StaticGroupInstruction groupInstruction = groupInstructionsById.get(groupId);
        if (groupInstruction == null) {
            throw new DecodeException("Unknown group id: " + groupId);
        }

//        int limit = in.limit();
        Object group = groupInstruction.decodeGroup(in);
//        in.skip(in.limit());
//        in.limit(limit); // restore old limit
        return group;
    }

}
