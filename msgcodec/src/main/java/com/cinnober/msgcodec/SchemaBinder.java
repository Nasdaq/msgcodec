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
                    // TODO: attempt to narrow the binding, e.g. int64 in src and int32 in dst
                    // narrow can also mean having more enum symbols in src than in dst
                    throw new IncompatibleSchemaException("Source type not eq or wider" +
                            details(dstGroup, dstField, dir));
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
}
