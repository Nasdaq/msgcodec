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
package com.cinnober.msgcodec;

import com.cinnober.msgcodec.TypeDef.Symbol;
import com.cinnober.msgcodec.TypeDef.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The schema binder can re-bind a schema to another schema.
 * 
 * <p>
 * This is in particular useful for schema/protocol upgrades when the client is
 * compiled against an older version of the protocol, but need to communicate
 * with a new server.
 *
 * <p>
 * Example:
 * 
 * <pre>
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
     * 
     * @param src
     *            the source schema, which is bound, not null.
     */
    public SchemaBinder(Schema src) {
        if (!src.isBound()) {
            throw new IllegalArgumentException("Source schema must be bound");
        }
        this.src = src;
    }

    private void checkRequiredFields(GroupDef src, GroupDef dst, Direction dir) throws IncompatibleSchemaException {
        if (!dir.equals(Direction.OUTBOUND)) {
            for (FieldDef f : src.getFields()) {
                FieldDef dstField = dst.getField(f.getName());
                if (f.isRequired() && dstField == null) {
                    throw new IncompatibleSchemaException(
                            "Field presence changed req -> unavailable" + details(src, f, dir));
                }
                if (f.isRequired() && (!dstField.isRequired())) {
                    throw new IncompatibleSchemaException(
                            "Field presence changed req -> opt" + details(dst, f, dir));
                }
            }
        }
        if (!dir.equals(Direction.INBOUND)) {
            for (FieldDef f : dst.getFields()) {
                FieldDef srcField = src.getField(f.getName());
                if (f.isRequired() && srcField == null) {
                    throw new IncompatibleSchemaException(
                            "Field presence changed unavailable -> req" + details(dst, f, dir));
                }
                if (f.isRequired() && (!srcField.isRequired())) {
                    throw new IncompatibleSchemaException(
                            "Field presence changed opt -> req" + details(dst, f, dir));
                }
            }
        }
    }

    /**
     * Bind the specified destination schema using the bindings of the source
     * schema.
     *
     * @param dst
     *            the schema that should be bound, not null.
     * @param dirFn
     *            function that can set the expected schema compatibility
     *            requirement for each group found in the destination schema,
     *            not null.
     * @return a bound copy of the destination schema.
     * @throws IncompatibleSchemaException
     *             if an incompatibility was found between the schemas.
     */
    public Schema bind(Schema dst, Function<GroupDef, Direction> dirFn) throws IncompatibleSchemaException {
        Map<String, GroupDef> newGroups = new HashMap<>();
        for (GroupDef dstGroup : dst.getGroups()) {
            Direction dir = dirFn.apply(dstGroup);
            GroupDef srcGroup = src.getGroup(dstGroup.getName());

            GroupBinding groupBinding;
            ArrayList<FieldDef> newFields = new ArrayList<>(dstGroup.getFields().size());
            if (srcGroup == null) {
                if (src.getGroup(dstGroup.getId()) != null) {
                    throw new IncompatibleSchemaException("Group name mismatch on groups with id "+dstGroup.getId()+"!");
                }
                if (dstGroup.getSuperGroup() != null) {
                    // flatten into super group
                    GroupBinding superBinding = newGroups.get(dstGroup.getSuperGroup()).getBinding();
                    groupBinding = new GroupBinding(superBinding.getFactory(),
                            superBinding.getGroupType() instanceof MissingGroupType ? new MissingGroupType(dstGroup.getId()) : superBinding.getGroupType());
                } else {
                    groupBinding = new GroupBinding(Object::new,
                            new MissingGroupType(dstGroup.getId()));
                }
                for (FieldDef dstField : dstGroup.getFields()) {
                    newFields.add(new CreateAccessor<>(dstField).bindField(dstField));
                }
            } else {
                if (srcGroup.getId() != dstGroup.getId()) {
                    throw new IncompatibleSchemaException("Source and destination groups have different ids" + details(dstGroup, dir));
                }
                if (!Objects.equals(srcGroup.getSuperGroup(), dstGroup.getSuperGroup())) {
                    throw new IncompatibleSchemaException("Different group inheritance" + details(dstGroup, dir));
                }
                groupBinding = srcGroup.getBinding();
                checkRequiredFields(srcGroup, dstGroup, dir);

                for (FieldDef dstField : dstGroup.getFields()) {
                    FieldDef srcField = srcGroup.getField(dstField.getName());
                    
                    if(srcField == null) {
                        try {
                            newFields.add(new CreateAccessor<>(dstField).bindField(dstField));
                        } catch (SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    else {
                        newFields.add(bindField(srcField, dstField, srcGroup, dst, dir));
                    }
                }
            }

            GroupDef newGroup = new GroupDef(dstGroup.getName(), dstGroup.getId(), dstGroup.getSuperGroup(), newFields,
                    dstGroup.getAnnotations(), groupBinding);
            newGroups.put(newGroup.getName(), newGroup);
        }

        Schema s = new Schema(newGroups.values(), dst.getNamedTypes(), dst.getAnnotations(), src.getBinding(),
                buildRemovedClassRemap(newGroups, src.getGroups()));

        return s;
    }

    private Map<Object, Integer> buildRemovedClassRemap(Map<String,GroupDef> newGroups, Collection<GroupDef> sourceGroups) {
        HashMap<Object, Integer> remap = new HashMap<>();


        Map<String,GroupDef> sourceOnlyGroups = sourceGroups.stream().filter(def -> !newGroups.containsKey(def.getName())).collect(Collectors.toMap(GroupDef::getName,
                d -> d));

        for (GroupDef sourceGroup: sourceOnlyGroups.values()) {
            String superName = sourceGroup.getSuperGroup();
            if (superName != null) {
                while (superName != null && (!newGroups.containsKey(superName))) {
                    GroupDef parent = sourceOnlyGroups.get(superName);
                    superName = parent != null ? parent.getSuperGroup() : null;
                }
                if (superName != null) {
                    remap.put(sourceGroup.getBinding().getGroupType(), newGroups.get(superName).getId());
                }
            }
        }

        return remap;
    }

    private class MissingGroupType {
        private final int id;

        public MissingGroupType(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, MissingGroupType.class.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof MissingGroupType) && ((MissingGroupType) obj).id == id;
        }

        @Override
        public String toString() {
            return "-<MISSING:"+id+">-";
        }
    }

    private Direction getAcceptableEnumConversion(TypeDef.Enum source, TypeDef.Enum destination) {
        List<Symbol> sourceSymbols = source.getSymbols();
        List<Symbol> destinationSymbols = destination.getSymbols();
        if (sourceSymbols.size() > destinationSymbols.size()) {
            Set<String> lookupSet = sourceSymbols.stream().map(Symbol::getName).collect(Collectors.toSet());
            for (Symbol symbol : destinationSymbols) {
                if (!lookupSet.contains(symbol.getName())) {
                    return null;
                }
            }
            return Direction.INBOUND;
        } else {
            Set<String> lookupSet = destinationSymbols.stream().map(Symbol::getName).collect(Collectors.toSet());
            for (Symbol symbol : sourceSymbols) {
                if (!lookupSet.contains(symbol.getName())) {
                    return null;
                }
            }
            return sourceSymbols.size() == destinationSymbols.size() ? Direction.BOTH : Direction.OUTBOUND;
        }

    }

    private FieldDef bindField(FieldDef srcField, FieldDef dstField, GroupDef dstGroup, Schema dst, Direction dir)
            throws IncompatibleSchemaException {
        TypeDef srcType = src.resolveToType(srcField.getType(), true);
        TypeDef dstType = dst.resolveToType(dstField.getType(), true);
        if (srcType.equals(dstType)) {
            return dstField.bind(srcField.getBinding());
        }

        Class<?> javaType = dstType.getDefaultJavaType();
        Class<?> javaComponentType = dstType.getDefaultJavaComponentType();

        switch (dir) {
        case BOTH:
            throw new IncompatibleSchemaException("Different types" + details(dstGroup, dstField, dir));
        case INBOUND:
            Accessor<?, ?> narrowAccessor = inboundAccessor(srcField.getAccessor(), srcType, dst, dstType, dir);
            if (narrowAccessor == null) {
                throw new IncompatibleSchemaException(
                        "Source type not eq or wider" + details(dstGroup, dstField, dir));
            }
            SymbolMapping<?> inboundMapping = srcField.getBinding().getSymbolMapping();

            if (inboundMapping != null) {
                switch (dstType.getType()) {
                case ENUM:
                    inboundMapping = new ConverterSymbolMapping<>(
                            inboundMapping, ((TypeDef.Enum) dstType).getSymbols());
                    javaType = srcField.getJavaClass();
                    break;
                case SEQUENCE:
                    TypeDef componentType = dst.resolveToType(
                            ((TypeDef.Sequence) dstType).getComponentType(), true);
                    if (componentType != null && componentType.getType() == Type.ENUM) {
                        inboundMapping = new ConverterSymbolMapping<>(
                                inboundMapping, ((TypeDef.Enum) componentType).getSymbols());
                        javaType = srcField.getJavaClass();
                        javaComponentType = srcField.getComponentJavaClass();
                        break;
                    }
                default:
                    throw new IncompatibleSchemaException("Symbol mapping but no enum");
                }
            }

            return dstField.bind(new FieldBinding(narrowAccessor, javaType, javaComponentType, inboundMapping));
        case OUTBOUND:
            Accessor<?, ?> widenAccessor = outboundAccessor(srcField.getAccessor(), srcType, dst, dstType, dir);
            if (widenAccessor == null) {
                throw new IncompatibleSchemaException(
                        "Source type not eq or narrower " + details(dstGroup, dstField, dir));
            }
            SymbolMapping<?> outboundMapping = srcField.getBinding().getSymbolMapping();

            if (outboundMapping != null) {
                switch (dstType.getType()) {
                case ENUM:
                    outboundMapping = new ConverterSymbolMapping<>(
                            outboundMapping, ((TypeDef.Enum) dstType).getSymbols());
                    javaType = srcField.getJavaClass();
                    break;
                case SEQUENCE:
                    TypeDef componentType = dst.resolveToType(
                            ((TypeDef.Sequence) dstType).getComponentType(), true);
                    if (componentType != null && componentType.getType() == Type.ENUM) {
                        outboundMapping = new ConverterSymbolMapping<>(
                                outboundMapping, ((TypeDef.Enum) componentType).getSymbols());
                        javaType = srcField.getJavaClass();
                        javaComponentType = srcField.getComponentJavaClass();
                        break;
                    }
                default:
                    throw new IncompatibleSchemaException("Symbol mapping but no enum");
                }
            }

            return dstField.bind(new FieldBinding(widenAccessor, javaType, javaComponentType, outboundMapping));
        default:
            throw new RuntimeException("Unhandled case: " + dir);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Accessor inboundAccessor(Accessor accessor, TypeDef srcType, Schema dst, TypeDef dstType, Direction dir) {
        switch (srcType.getType()) {
        case INT64:
        case UINT64:
            switch (dstType.getType()) {
            case INT32:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::intToLong);
            case UINT32:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::uIntToLong);
            case INT16:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::shortToLong);
            case UINT16:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::uShortToLong);
            case INT8:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::byteToLong);
            case UINT8:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::uByteToLong);
            default:
                return null;
            }
        case INT32:
        case UINT32:
            switch (dstType.getType()) {
            case INT16:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::shortToInt);
            case UINT16:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::uShortToInt);
            case INT8:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::byteToInt);
            case UINT8:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::uByteToInt);
            default:
                return null;
            }
        case INT16:
        case UINT16:
            switch (dstType.getType()) {
            case INT8:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::byteToShort);
            case UINT8:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::uByteToShort);
            default:
                return null;
            }
        case FLOAT64:
            switch (dstType.getType()) {
            case FLOAT32:
                return new ConverterAccessor<>(accessor, SchemaBinder::invalidConversion, SchemaBinder::floatToDouble);
            default:
                return null;
            }

        case ENUM:
        	if (dstType.getType() == Type.ENUM) {
                Direction acceptableDirection = getAcceptableEnumConversion((TypeDef.Enum)srcType, (TypeDef.Enum)dstType);
                return (acceptableDirection == Direction.INBOUND || acceptableDirection == Direction.BOTH) ? accessor : null;
        	}
        	return null;
        case SEQUENCE:
            if (dstType.getType() == Type.SEQUENCE) {
                TypeDef srcComponentType = src.resolveToType(((TypeDef.Sequence) srcType).getComponentType(), true);
                TypeDef dstComponentType = dst.resolveToType(((TypeDef.Sequence) dstType).getComponentType(), true);
                
                if (srcComponentType != null && dstComponentType != null 
                        && srcComponentType.getType() == Type.ENUM && dstComponentType.getType() == Type.ENUM) {
                    return accessor;                    
                }
            }
            return null;
        default:
            return null;
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Accessor outboundAccessor(Accessor accessor, TypeDef srcType, Schema dst, TypeDef dstType, Direction dir) {
        switch (srcType.getType()) {
        case INT8:
        case UINT8:
            switch (dstType.getType()) {
            case INT16:
                return new ConverterAccessor<>(accessor, SchemaBinder::byteToShort, SchemaBinder::invalidConversion);
            case UINT16:
                return new ConverterAccessor<>(accessor, SchemaBinder::uByteToShort, SchemaBinder::invalidConversion);
            case INT32:
                return new ConverterAccessor<>(accessor, SchemaBinder::byteToInt, SchemaBinder::invalidConversion);
            case UINT32:
                return new ConverterAccessor<>(accessor, SchemaBinder::uByteToInt, SchemaBinder::invalidConversion);
            case INT64:
                return new ConverterAccessor<>(accessor, SchemaBinder::byteToLong, SchemaBinder::invalidConversion);
            case UINT64:
                return new ConverterAccessor<>(accessor, SchemaBinder::uByteToLong, SchemaBinder::invalidConversion);
            default:
                return null;
            }

        case INT16:
        case UINT16:
            switch (dstType.getType()) {
            case INT32:
                return new ConverterAccessor<>(accessor, SchemaBinder::shortToInt, SchemaBinder::invalidConversion);
            case UINT32:
                return new ConverterAccessor<>(accessor, SchemaBinder::uShortToInt, SchemaBinder::invalidConversion);
            case INT64:
                return new ConverterAccessor<>(accessor, SchemaBinder::shortToLong, SchemaBinder::invalidConversion);
            case UINT64:
                return new ConverterAccessor<>(accessor, SchemaBinder::uShortToLong, SchemaBinder::invalidConversion);
            default:
                return null;
            }

        case INT32:
        case UINT32:
            switch (dstType.getType()) {
            case INT64:
                return new ConverterAccessor<>(accessor, SchemaBinder::intToLong, SchemaBinder::invalidConversion);
            case UINT64:
                return new ConverterAccessor<>(accessor, SchemaBinder::uIntToLong, SchemaBinder::invalidConversion);
            default:
                return null;
            }

        case FLOAT32:
            switch (dstType.getType()) {
            case FLOAT64:
                return new ConverterAccessor<>(accessor, SchemaBinder::floatToDouble, SchemaBinder::invalidConversion);
            default:
                return null;
            }

        case ENUM:
            if (dstType.getType() == Type.ENUM) {
                Direction acceptableDirection = getAcceptableEnumConversion((TypeDef.Enum)srcType, (TypeDef.Enum)dstType);
                return (acceptableDirection == Direction.OUTBOUND || acceptableDirection == Direction.BOTH) ? accessor : null;
            }
            return null;
        case SEQUENCE:
            if (dstType.getType() == Type.SEQUENCE) {
                TypeDef srcComponentType = src.resolveToType(((TypeDef.Sequence) srcType).getComponentType(), true);
                TypeDef dstComponentType = dst.resolveToType(((TypeDef.Sequence) dstType).getComponentType(), true);
                
                if (srcComponentType != null && dstComponentType != null 
                        && srcComponentType.getType() == Type.ENUM && dstComponentType.getType() == Type.ENUM) {
                    return accessor;                    
                }
            }
            return null;
        default:
            return null;

        }
    }

    private static <V,W> W invalidConversion(V v) { throw new UnsupportedOperationException("Narrowing data conversion not supported!"); }

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
        return (short) (v & 0xff);
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

    private static Float doubleToFloat(Double v) {
        return v.floatValue();
    }

    private static Double floatToDouble(Float v) {
        return v.doubleValue();
    }

    private static String details(GroupDef group, FieldDef field, Direction dir) {
        return String.format(", field '%s' in %s group '%s'.", field.getName(), dir, group.getName());
    }

    private static String details(GroupDef group, Direction dir) {
        return String.format(", %s group '%s'.", dir, group.getName());
    }

    /**
     * Direction in which a group is expected to be sent. The direction puts
     * different restrictions on what schema changes are allowed.
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
          * Both inbound and outbound groups/messages, to
          * be received/sent and decoded/encoded.
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

    private static class ConverterAccessor<T, S, D> implements Accessor<T, D> {
        private final Accessor<T, S> accessor;
        private final Function<S, D> srcToDstFn;
        private final Function<D, S> dstToSrcFn;

        ConverterAccessor(Accessor<T, S> accessor, Function<S, D> srcToDstFn, Function<D, S> dstToSrcFn) {
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
    
    private static class ConverterSymbolMapping<E> implements SymbolMapping<E> {
        private final Map<Integer, E> idToValue = new HashMap<>();
        private final Map<String, E> nameToValue = new HashMap<>();
        private final Map<E, Symbol> valueToSymbol = new HashMap<>();

        /*
         * Create a SymbolMapping mapping dstSymbols to the object representation in the srcMapping
         */
        ConverterSymbolMapping(SymbolMapping<E> srcMapping, Collection<Symbol> dstSymbols) {
            for (Symbol symbol : dstSymbols) {
                E value = null;
                
                try {
                    value = srcMapping.lookup(symbol.getName());
                } catch (IllegalArgumentException e) {
                    // Ignore, should just be a case of widening
                }
                
                if (value != null) {
                    valueToSymbol.put(value, symbol);
                    idToValue.put(symbol.getId(), value);
                    nameToValue.put(symbol.getName(), value);
                }
            }
        }
        
        @Override
        public E lookup(Integer id) throws IllegalArgumentException {
            if (id == null) {
                return null;
            }
            
            E value = idToValue.get(id);
            
            if (value == null) {
                throw new IllegalArgumentException("Attempted to lookup nonexistant enum value with id: " + id);
            }
            
            return value;
        }

        @Override
        public E lookup(String name) throws IllegalArgumentException {
            if (name == null) {
                return null;
            }
            
            E value = nameToValue.get(name);
            
            if (value == null) {
                throw new IllegalArgumentException("Attempted to lookup nonexistant enum value with name: " + name);
            }
            
            return value;
        }

        @Override
        public Integer getId(E value) throws IllegalArgumentException {
            if (value == null) {
                return null;
            }
            
            Symbol symbol = valueToSymbol.get(value);
            
            if (symbol == null) {
                throw new IllegalArgumentException("Attempted to get id of unmapped enum value: " + value);
            }
            
            return symbol.getId();
        }

        @Override
        public String getName(E value) throws IllegalArgumentException {
            if (value == null) {
                return null;
            }

            Symbol symbol = valueToSymbol.get(value);
            
            if (symbol == null) {
                throw new IllegalArgumentException("Attempted to get id of unmapped enum value: " + value);
            }
            
            return symbol.getName();
        }
    }
}
