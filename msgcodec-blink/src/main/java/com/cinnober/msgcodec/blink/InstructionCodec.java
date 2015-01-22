/*
 * Copyright (c) 2014 Cinnober Financial Technology AB, Stockholm,
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
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.GroupTypeAccessor;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.util.LimitInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A codec for a specific dictionary, which use FieldInstruction for encoding/decoding.
 *
 * @author mikael.brannstrom
 */
class InstructionCodec extends GeneratedCodec {
    private final GroupTypeAccessor groupTypeAccessor;
    private final Map<Object, StaticGroupInstruction> groupInstructionsByGroupType;
    /** The compiled static groups by group id. */
    private final Map<Integer, StaticGroupInstruction> groupInstructionsById;

    public InstructionCodec(BlinkCodec codec, ProtocolDictionary dictionary) {
        super(codec);
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
                FieldInstruction fieldInstruction = createFieldInstruction(dictionary, fieldDef, fieldDef.getType(),
                        fieldDef.getJavaClass(), fieldDef.getComponentJavaClass());
                groupInstruction.initFieldInstruction(index++, fieldInstruction);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private FieldInstruction createFieldInstruction(ProtocolDictionary dictionary, FieldDef field, TypeDef type,
            Class<?> javaClass, Class<?> componentJavaClass) {
        type = dictionary.resolveToType(type, true);
        GroupDef typeGroup = dictionary.resolveToGroup(type);
        boolean required = field == null || field.isRequired();
        if (type instanceof TypeDef.Sequence) {
            // --- SEQUENCE ---
            FieldInstruction elementInstruction =
                    createFieldInstruction(dictionary, null, ((TypeDef.Sequence) type).getComponentType(),
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
    protected void writeStaticGroupWithId(OutputStream out, Object value) throws IOException, IllegalArgumentException {
        if (out instanceof BlinkOutputStream) {
            writeStaticGroupWithId((BlinkOutputStream)out, value);
        } else {
            writeStaticGroupWithId(new BlinkOutputStream(out), value);
        }
    }
    private void writeStaticGroupWithId(BlinkOutputStream out, Object value) throws IOException, IllegalArgumentException {
        Object groupType = groupTypeAccessor.getGroupType(value);
        StaticGroupInstruction groupInstruction = groupInstructionsByGroupType.get(groupType);
        if (groupInstruction == null) {
            throw new IllegalArgumentException("Cannot encode group. Group type not found in protocol dictionary: " +
                    groupType);
        }
        groupInstruction.encodeGroupId(out);
        groupInstruction.encodeGroup(value, out);
    }

    @Override
    protected Object readStaticGroup(int groupId, LimitInputStream in) throws IOException, DecodeException {
        if (in instanceof BlinkInputStream) {
            return readStaticGroup(groupId, (BlinkInputStream)in);
        } else {
            return readStaticGroup(groupId, new BlinkInputStream(in));
        }
    }
    private Object readStaticGroup(int groupId, BlinkInputStream in) throws IOException, DecodeException {
        StaticGroupInstruction groupInstruction = groupInstructionsById.get(groupId);
        if (groupInstruction == null) {
            throw new DecodeException("Unknown group id: " + groupId);
        }

        int limit = in.limit();
        Object group = groupInstruction.decodeGroup(in);
        in.skip(in.limit());
        in.limit(limit); // restore old limit
        return group;
    }

}
