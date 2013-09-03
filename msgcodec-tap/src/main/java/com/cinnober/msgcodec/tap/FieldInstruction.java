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

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.EnumSymbols;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.TypeDef;

/**
 * A field instruction can encode or decode a field in a group.
 *
 * @author mikael.brannstrom
 *
 */
abstract class FieldInstruction<V> {
    protected final FieldDef field;
    @SuppressWarnings("rawtypes")
    protected final Accessor accessor;

    /**
     * Create a field instruction.
     * @param field the field, or null if the instruction will be used within another field, e.g. a sequence.
     */
    public FieldInstruction(FieldDef field) {
        this.field = field;
        this.accessor = field != null ? field.getAccessor() : null;
    }

    /** Encode the field from the group to the output stream.
     *
     * <p>This method is only applicable when the field definition have been specified
     * in the constructor.
     *
     * @param group the group from where the field is read, not null.
     * @param out where the encoded field is written
     * @throws IOException
     * @see #encodeValue(Object, TapOutputStream)
     */
    @SuppressWarnings("unchecked")
    public void encodeField(Object group, TapOutputStream out) throws IOException {
        Object value = accessor.getValue(group);
        encodeValue((V)value, out);
    }

    /** Decodes the field from the input stream into the specified group.
     *
     * <p>This method is only applicable when the field definition have been specified
     * in the constructor.
     *
     * @param group the group into which the field is copied.
     * @param in where to read the encoded field.
     * @throws IOException
     * @see #decodeValue(TapInputStream)
     */
    @SuppressWarnings("unchecked")
    public void decodeField(Object group, TapInputStream in) throws IOException {
        Object value = decodeValue(in);
        accessor.setValue(group, value);
    }

    /** Check if the value is null and throw an IOException in that case.
     *
     * @param value the value to be checked
     * @throws IOException if the value is null
     */
    protected void require(Object value) throws IOException {
        if (value == null) {
            throw new IOException("Field value is required, cannot encode: " + field.toString());
        }
    }

    /** Encode the value to the output stream.
     *
     * @param value the value to be encoded.
     * @param out
     * @throws IOException
     */
    public abstract void encodeValue(V value, TapOutputStream out) throws IOException;
    /** Decode a value from the input stream.
     *
     * @param in
     * @return the decoded value.
     * @throws IOException
     */
    public abstract V decodeValue(TapInputStream in) throws IOException;

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(getClass().getSimpleName());
        str.append(" [field = ").append(field.toString()).append(" ]");
        return str.toString();
    }


    // --- INTEGER TYPES ---
    static class Int8 extends FieldInstruction<Byte> {
        public Int8(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Byte value, TapOutputStream out) throws IOException {
            require(value);
            out.write(0xff & value);
        }
        @Override
        public Byte decodeValue(TapInputStream in) throws IOException {
            return in.readByte();
        }
    }
    static class VarInt16 extends FieldInstruction<Short> {
        public VarInt16(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, TapOutputStream out) throws IOException {
            require(value);
            out.writeVarShort(value);
        }
        @Override
        public Short decodeValue(TapInputStream in) throws IOException {
            return in.readVarShort();
        }
    }
    static class VarInt32 extends FieldInstruction<Integer> {
        public VarInt32(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, TapOutputStream out) throws IOException {
            require(value);
            out.writeVarInt(value);
        }
        @Override
        public Integer decodeValue(TapInputStream in) throws IOException {
            return in.readVarInt();
        }
    }
    static class VarInt64 extends FieldInstruction<Long> {
        public VarInt64(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, TapOutputStream out) throws IOException {
            require(value);
            out.writeVarLong(value);
        }
        @Override
        public Long decodeValue(TapInputStream in) throws IOException {
            return in.readVarLong();
        }
    }
    static class Null<T> extends FieldInstruction<T> {
        private final FieldInstruction<T> instruction;
        public Null(FieldInstruction<T> instruction) {
            super(instruction.field);
            this.instruction = instruction;
        }
        @Override
        public void encodeValue(T value, TapOutputStream out) throws IOException {
            if (value == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                instruction.encodeValue(value, out);
            }
        }
        @Override
        public T decodeValue(TapInputStream in) throws IOException {
            if (in.readBoolean()) {
                return instruction.decodeValue(in);
            } else {
                return null;
            }
        }
    }

    // --- FLOATING POINT NUMBERS ---
    static class Float32 extends FieldInstruction<Float> {
        public Float32(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Float value, TapOutputStream out) throws IOException {
            require(value);
            out.writeFloat(value);
        }
        @Override
        public Float decodeValue(TapInputStream in) throws IOException {
            return in.readFloat();
        }
    }
    static class Float64 extends FieldInstruction<Double> {
        public Float64(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Double value, TapOutputStream out) throws IOException {
            require(value);
            out.writeDouble(value);
        }
        @Override
        public Double decodeValue(TapInputStream in) throws IOException {
            return in.readDouble();
        }
    }

    // --- DECIMAL ---
    static class Decimal extends FieldInstruction<BigDecimal> {
        public Decimal(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(BigDecimal value, TapOutputStream out) throws IOException {
            require(value);
            try {
                out.writeVarLong(value.scaleByPowerOfTen(6).longValueExact());
            } catch (ArithmeticException e) {
                throw new IOException("Too many decmals (max 6) or too large mantissa (max 64 bits)", e);
            }
        }
        @Override
        public BigDecimal decodeValue(TapInputStream in) throws IOException {
            return BigDecimal.valueOf(in.readVarLong(), 6);
        }
    }

    // --- Boolean ---
    static class Boolean extends FieldInstruction<java.lang.Boolean> {
        public Boolean(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.lang.Boolean value, TapOutputStream out) throws IOException {
            require(value);
            out.writeBoolean(value);
        }
        @Override
        public java.lang.Boolean decodeValue(TapInputStream in) throws IOException {
            return in.readBoolean();
        }
    }

    // --- TIME --- // TODO

    // --- STRING ---
    static class StringLatin1Null extends FieldInstruction<String> {
        public StringLatin1Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(String value, TapOutputStream out) throws IOException {
            out.writeStringLatin1(value);
        }
        @Override
        public String decodeValue(TapInputStream in) throws IOException {
            return in.readStringLatin1();
        }
    }
    static class StringUTF8Null extends FieldInstruction<String> {
        public StringUTF8Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(String value, TapOutputStream out) throws IOException {
            out.writeStringUTF8(value);
        }
        @Override
        public String decodeValue(TapInputStream in) throws IOException {
            return in.readStringUTF8();
        }
    }

    // --- BINARY ---
    static class BinaryNull extends FieldInstruction<byte[]> {
        public BinaryNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(byte[] value, TapOutputStream out) throws IOException {
            out.writeBinary(value);
        }
        @Override
        public byte[] decodeValue(TapInputStream in) throws IOException {
            return in.readBinary();
        }
    }

    // --- ENUM ---
    @SuppressWarnings("rawtypes")
    static class Enumeration extends FieldInstruction<Enum> {
        private final EnumSymbols enumSymbols;
        @SuppressWarnings("unchecked")
        public Enumeration(FieldDef field, TypeDef.Enum enumDef) {
            super(field);
            Class enumType = field.getJavaClass();
            this.enumSymbols = new EnumSymbols<>(enumDef, enumType);
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(Enum value, TapOutputStream out) throws IOException {
            require(value);
            TypeDef.Symbol symbol = enumSymbols.getSymbol(value);
            if (symbol == null) {
                throw new IOException("Value is not a valid enumeration symbol: " + value);
            }
            out.writeVarInt(symbol.getId());
        }
        @Override
        public Enum decodeValue(TapInputStream in) throws IOException {
            int enumValue = in.readVarInt();
            Enum en = enumSymbols.getEnum(enumValue);
            if (en == null) {
                throw new IOException("Illegal or unknown enum value: " + enumValue);
            }
            return en;
        }
    }
    static class IntEnumeration extends FieldInstruction<Integer> {
        public IntEnumeration(FieldDef field) {
            super(field);
            // PENDING: validate input/output that it belongs to the enumeration?
        }
        @Override
        public void encodeValue(Integer value, TapOutputStream out) throws IOException {
            require(value);
            out.writeVarInt(value);
        }
        @Override
        public Integer decodeValue(TapInputStream in) throws IOException {
            return in.readVarInt();
        }
    }

    // --- SEQUENCE ---
    @SuppressWarnings("rawtypes")
    static class CollectionSequence extends FieldInstruction<Collection> {
        private final FieldInstruction elementInstruction;
        public CollectionSequence(FieldDef field, FieldInstruction elementInstruction) {
            super(field);
            this.elementInstruction = elementInstruction;
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(Collection value, TapOutputStream out) throws IOException {
            if (value == null) {
                // Note: no distinction between empty list and null
                out.writeVarInt(0);
            } else {
                out.writeVarInt(value.size());
                for (Object element : value) {
                    elementInstruction.encodeValue(element, out);
                }
            }
        }
        @SuppressWarnings("unchecked")
        @Override
        public Collection decodeValue(TapInputStream in) throws IOException {
            int size = in.readVarInt();
            if (size == 0) {
                if (field.isRequired()) {
                    return Collections.emptyList();
                } else {
                    return null;
                }
            }
            if (size < 0) {
                throw new IOException("Sequence size overflow: " + size);
            }
            // TODO: sanity check for size to avoid OutOfMemoryError
            ArrayList value = new ArrayList(size);
            for (int i=0; i<size; i++) {
                value.add(elementInstruction.decodeValue(in));
            }
            return value;
        }
    }
    @SuppressWarnings("rawtypes")
    static class ArraySequence extends FieldInstruction<Object> { // actually <array>
        private final FieldInstruction elementInstruction;
        private final Class<?> componentType;
        public ArraySequence(FieldDef field, FieldInstruction elementInstruction) {
            super(field);
            this.elementInstruction = elementInstruction;
            this.componentType = field.getJavaClass().getComponentType();
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(Object value, TapOutputStream out) throws IOException {
            if (value == null) {
                // Note: no distinction between empty list and null
                out.writeVarInt(0);
            } else {
                int size = Array.getLength(value);
                out.writeVarInt(size);
                for (int i=0; i<size; i++) {
                    elementInstruction.encodeValue(Array.get(value, i), out);
                }
            }
        }
        @Override
        public Object decodeValue(TapInputStream in) throws IOException {
            int size = in.readVarInt();
            if (size == 0) {
                if (field.isRequired()) {
                    return Array.newInstance(componentType, 0);
                } else {
                    return null;
                }
            }
            if (size < 0) {
                throw new IOException("Sequence size overflow: " + size);
            }
            // TODO: sanity check for size to avoid OutOfMemoryError
            Object value = Array.newInstance(componentType, size);
            for (int i=0; i<size; i++) {
                Array.set(value, i, elementInstruction.decodeValue(in));
            }
            return value;
        }
    }
    // --- GROUP ---
    static class DynamicGroupNull extends FieldInstruction<Object> {
        private final TapCodec codec;
        public DynamicGroupNull(FieldDef field, TapCodec codec) {
            super(field);
            this.codec = codec;
        }
        @Override
        public void encodeValue(Object value, TapOutputStream out) throws IOException {
            codec.writeDynamicGroup(value, out);
        }
        @Override
        public Object decodeValue(TapInputStream in) throws IOException {
            return codec.readDynamicGroup(in);
        }
    }
    static class StaticGroupNull extends FieldInstruction<Object> {
        private final StaticGroupInstruction groupInstruction;
        private final TapCodec codec;
        public StaticGroupNull(FieldDef field, StaticGroupInstruction groupInstruction, TapCodec codec) {
            super(field);
            this.groupInstruction = groupInstruction;
            this.codec = codec;
        }
        @Override
        public void encodeValue(Object value, TapOutputStream out) throws IOException {
            codec.writeStaticGroup(value, out, groupInstruction);
        }
        @Override
        public Object decodeValue(TapInputStream in) throws IOException {
            return codec.readStaticGroup(in, groupInstruction);
        }
    }

}
