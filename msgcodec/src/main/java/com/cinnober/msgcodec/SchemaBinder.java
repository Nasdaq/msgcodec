/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
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
package com.cinnober.msgcodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * The schema binder can re-bind a schema to another schema.
 * 
 * <p>This is in particular useful for schema/protocol upgrades when the client is
 * compiled against an older version of the protocol, but need to communicate with
 * a new server.
 *
 * <p>Example:<pre>
 * Schema srcSchema = ...; // bound to classes we want to use
 * Schema dstSchema = ...; // how we want to communicate
 * SchemaBinder binder = new SchemaBinder(origSchema);
 * Schema newSchema = binder.bind(dstSchema); // the result
 * </pre>
 *
 * @author mikael.brannstrom
 */
public class SchemaBinder {

    private final Schema src;

    /**
     * Create a new schema binder.
     * @param src the source schema, which is bound, not null.
     */
    public SchemaBinder(Schema src) {
        if (!src.isBound()) {
            throw new IllegalArgumentException("Source schema must be bound");
        }
        this.src = src;
    }

    /**
     * Bind the specified destination schema using the bindings of the source schema.
     * @param dst the schema that should be bound, not null.
     * @param dirFn function that can set the expected schema compatibility requirement for each group
     * found in the destination schema, not null.
     * @return a bound copy of the destination schema.
     * @throws IncompatibleSchemaException if an incompatibility was found between the schemas.
     */
    public Schema bind(Schema dst, Function<GroupDef, Direction> dirFn) throws IncompatibleSchemaException {
        Map<String,GroupDef> newGroups = new HashMap<>();
        for (GroupDef dstGroup : dst.getGroups()) {
            Direction dir = dirFn.apply(dstGroup);
            GroupDef srcGroup = src.getGroup(dstGroup.getName());

            GroupBinding groupBinding;
            ArrayList<FieldDef> newFields = new ArrayList<>(dstGroup.getFields().size());
            if (srcGroup == null) {
                if (dstGroup.getSuperGroup() != null) {
                    // flatten into super group
                    GroupBinding superBinding = newGroups.get(dstGroup.getSuperGroup()).getBinding();
                    groupBinding = new GroupBinding(superBinding.getFactory(), null);
                } else {
                    groupBinding = new GroupBinding(Object::new, null);
                }
                for (FieldDef dstField : dstGroup.getFields()) {
                    newFields.add(IgnoreAccessor.bindField(dstField));
                }
            } else {
                if (!Objects.equals(srcGroup.getSuperGroup(), dstGroup.getSuperGroup())) {
                    throw new IncompatibleSchemaException("Different group inheritance" + details(dstGroup, dir));
                }
                groupBinding = srcGroup.getBinding();
                for (FieldDef dstField : dstGroup.getFields()) {
                    FieldDef srcField = srcGroup.getField(dstField.getName());
                    newFields.add(bindField(srcField, dstField, dstGroup, dst, dir));
                }
            }

            GroupDef newGroup = new GroupDef(
                    dstGroup.getName(),
                    dstGroup.getId(),
                    dstGroup.getSuperGroup(),
                    newFields,
                    dstGroup.getAnnotations(),
                    groupBinding);
            newGroups.put(newGroup.getName(), newGroup);
        }
        
        return new Schema(newGroups.values(), dst.getNamedTypes(), dst.getAnnotations(), src.getBinding());
    }

    private FieldDef bindField(FieldDef srcField, FieldDef dstField, GroupDef dstGroup, Schema dst, Direction dir)
            throws IncompatibleSchemaException {
        if (srcField == null) {
            if (dstField.isRequired() && dir != Direction.INBOUND) {
                throw new IncompatibleSchemaException("Required field not found" + details(dstGroup, dstField, dir));
            }
            return IgnoreAccessor.bindField(dstField);
        } else {
            if (srcField.isRequired() && !dstField.isRequired() && dir != Direction.OUTBOUND) {
                throw new IncompatibleSchemaException("Field presence changed req -> opt" + 
                        details(dstGroup, dstField, dir));
            }
            if (!srcField.isRequired() && dstField.isRequired() && dir != Direction.INBOUND) {
                throw new IncompatibleSchemaException("Field presence changed opt -> req" +
                        details(dstGroup, dstField, dir));
            }
            TypeDef srcType = src.resolveToType(srcField.getType(), true);
            TypeDef dstType = dst.resolveToType(dstField.getType(), true);
            if (srcType.equals(dstType)) {
                return dstField.bind(srcField.getBinding());
            }
            
            switch (dir) {
                case BOTH:
                    throw new IncompatibleSchemaException("Different types" + details(dstGroup, dstField, dir));
                case INBOUND:
                {
                    // TODO: attempt to narrow the binding, e.g. int64 in src and int32 in dst
                    // narrow can also mean having more enum symbols in src than in dst
                    Accessor narrowAccessor = narrowAccessor(srcField.getAccessor(), srcType, dstType);
                    if (narrowAccessor == null) {
                        throw new IncompatibleSchemaException("Source type not eq or wider" +
                                details(dstGroup, dstField, dir));
                    }
                    return dstField.bind(new FieldBinding(
                            narrowAccessor,
                            dstType.getDefaultJavaType(),
                            dstType.getDefaultJavaComponentType()));
                }
                case OUTBOUND:
                    // TODO: attempt to widen the binding, e.g. int32 in src and int64 in dst
                    // widening can also mean having more enum symbols in dst than in src
                    throw new IncompatibleSchemaException("Destination type not eq or wider" +
                            details(dstGroup, dstField, dir));
                default:
                    throw new RuntimeException("Unhandled case: " + dir);
            }
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private Accessor narrowAccessor(Accessor accessor, TypeDef srcType, TypeDef dstType) {
        switch (srcType.getType()) {
            case INT64:
            case UINT64:
                switch (dstType.getType()) {
                    case INT32:
                        return new ConverterAccessor<>(accessor, SchemaBinder::longToInt, SchemaBinder::intToLong);
                    case UINT32:
                        return new ConverterAccessor<>(accessor, SchemaBinder::longToInt, SchemaBinder::uIntToLong);
                    case INT16:
                        return new ConverterAccessor<>(accessor, SchemaBinder::longToShort, SchemaBinder::shortToLong);
                    case UINT16:
                        return new ConverterAccessor<>(accessor, SchemaBinder::longToShort, SchemaBinder::uShortToLong);
                    case INT8:
                        return new ConverterAccessor<>(accessor, SchemaBinder::longToByte, SchemaBinder::byteToLong);
                    case UINT8:
                        return new ConverterAccessor<>(accessor, SchemaBinder::longToByte, SchemaBinder::uByteToLong);
                    default:
                        return null;
                }
            case INT32:
            case UINT32:
                switch (dstType.getType()) {
                    case INT16:
                        return new ConverterAccessor<>(accessor, SchemaBinder::intToShort, SchemaBinder::shortToInt);
                    case UINT16:
                        return new ConverterAccessor<>(accessor, SchemaBinder::intToShort, SchemaBinder::uShortToInt);
                    case INT8:
                        return new ConverterAccessor<>(accessor, SchemaBinder::intToByte, SchemaBinder::byteToInt);
                    case UINT8:
                        return new ConverterAccessor<>(accessor, SchemaBinder::intToByte, SchemaBinder::uByteToInt);
                    default:
                        return null;
                }
            case INT16:
            case UINT16:
                switch (dstType.getType()) {
                    case INT8:
                        return new ConverterAccessor<>(accessor, SchemaBinder::shortToByte, SchemaBinder::byteToShort);
                    case UINT8:
                        return new ConverterAccessor<>(accessor, SchemaBinder::shortToByte, SchemaBinder::uByteToShort);
                    default:
                        return null;
                }
            // TODO: enum, float, etc
            default:
                return null;
        }
    }
    
    private static Integer longToInt(Long v) {
        return v.intValue();
    }
    private static Short longToShort(Long v) {
        return v.shortValue();
    }
    private static Byte longToByte(Long v) {
        return v.byteValue();
    }
    private static Short intToShort(Integer v) {
        return v.shortValue();
    }
    private static Byte intToByte(Integer v) {
        return v.byteValue();
    }
    private static Byte shortToByte(Short v) {
        return v.byteValue();
    }
    private static Short byteToShort(Byte v) {
        return v.shortValue();
    }
    private static Integer byteToInt(Byte v) {
        return v.intValue();
    }
    private static Long byteToLong(Byte v) {
        return v.longValue();
    }
    private static Integer shortToInt(Short v) {
        return v.intValue();
    }
    private static Long shortToLong(Short v) {
        return v.longValue();
    }
    private static Long intToLong(Integer v) {
        return v.longValue();
    }
    private static Short uByteToShort(Byte v) {
        return (short)(v & 0xff);
    }
    private static Integer uByteToInt(Byte v) {
        return v & 0xff;
    }
    private static Long uByteToLong(Byte v) {
        return v & 0xffL;
    }
    private static Integer uShortToInt(Short v) {
        return v & 0xffff;
    }
    private static Long uShortToLong(Short v) {
        return v & 0xffffL;
    }
    private static Long uIntToLong(Integer v) {
        return v & 0xffffffffL;
    }

    private static String details(GroupDef group, FieldDef field, Direction dir) {
        return String.format(", field '%s' in %s group '%s'.",
                field.getName(), dir, group.getName());
    }
    private static String details(GroupDef group, Direction dir) {
        return String.format(", %s group '%s'.", dir, group.getName());
    }

    /**
     * Direction in which a group is expected to be sent.
     * The direction puts different restrictions on what schema changes are allowed.
     */
    public static enum Direction {
        /**
         * Inbound groups/messages, to be received and decoded.
         */
        INBOUND("inbound"),
        /**
         * Outbound groups/messages, to be sent and encoded.
         */
        OUTBOUND("outbound"),
        /**
         * Both inbound and outbound groups/messages, to be received/sent and decoded/encoded.
         */
        BOTH("in/out-bound");
        private final String s;
        private Direction(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }

    private static class ConverterAccessor<T,S,D> implements Accessor<T,D> {
        private final Accessor<T,S> accessor;
        private final Function<S,D> srcToDstFn;
        private final Function<D,S> dstToSrcFn;
        ConverterAccessor(Accessor<T,S> accessor, Function<S,D> srcToDstFn, Function<D,S> dstToSrcFn) {
            this.accessor = accessor;
            this.srcToDstFn = srcToDstFn;
            this.dstToSrcFn = dstToSrcFn;
        }
        @Override
        public D getValue(T obj) {
            return srcToDstFn.apply(accessor.getValue(obj));
        }
        @Override
        public void setValue(T obj, D value) {
            accessor.setValue(obj, dstToSrcFn.apply(value));
        }
    }
}
