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

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.EnumSymbols;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Time;
import java.util.List;

import static com.cinnober.msgcodec.blink.DateUtil.*;

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
     * @see #encodeValue(Object, BlinkOutputStream)
     */
    @SuppressWarnings("unchecked")
    public void encodeField(Object group, BlinkOutputStream out) throws IOException {
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
     * @see #decodeValue(BlinkInputStream)
     */
    @SuppressWarnings("unchecked")
    public void decodeField(Object group, BlinkInputStream in) throws IOException {
        Object value = decodeValue(in);
        accessor.setValue(group, value);
    }

    /** Check if the value is null and throw an IOException in that case.
     *
     * @param value the value to be checked
     * @throws IllegalArgumentException if the value is null
     */
    protected void require(Object value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Field value is required, cannot encode: " + field.toString());
        }
    }

    /** Encode the value to the output stream.
     *
     * @param value the value to be encoded.
     * @param out
     * @throws IOException
     */
    public abstract void encodeValue(V value, BlinkOutputStream out) throws IOException;
    /** Decode a value from the input stream.
     *
     * @param in
     * @return the decoded value.
     * @throws IOException
     */
    public abstract V decodeValue(BlinkInputStream in) throws IOException;

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
        public void encodeValue(Byte value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeInt8(value);
        }
        @Override
        public Byte decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt8();
        }
    }
    static class Int16 extends FieldInstruction<Short> {
        public Int16(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeInt16(value);
        }
        @Override
        public Short decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt16();
        }
    }
    static class Int32 extends FieldInstruction<Integer> {
        public Int32(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeInt32(value);
        }
        @Override
        public Integer decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt32();
        }
    }
    static class Int64 extends FieldInstruction<Long> {
        public Int64(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeInt64(value);
        }
        @Override
        public Long decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt64();
        }
    }
    static class Int8Null extends FieldInstruction<Byte> {
        public Int8Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Byte value, BlinkOutputStream out) throws IOException {
            out.writeInt8Null(value);
        }
        @Override
        public Byte decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt8Null();
        }
    }
    static class Int16Null extends FieldInstruction<Short> {
        public Int16Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, BlinkOutputStream out) throws IOException {
            out.writeInt16Null(value);
        }
        @Override
        public Short decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt16Null();
        }
    }
    static class Int32Null extends FieldInstruction<Integer> {
        public Int32Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, BlinkOutputStream out) throws IOException {
            out.writeInt32Null(value);
        }
        @Override
        public Integer decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt32Null();
        }
    }
    static class Int64Null extends FieldInstruction<Long> {
        public Int64Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, BlinkOutputStream out) throws IOException {
            out.writeInt64Null(value);
        }
        @Override
        public Long decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt64Null();
        }
    }
    static class UInt8 extends FieldInstruction<Byte> {
        public UInt8(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Byte value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeUInt8(value);
        }
        @Override
        public Byte decodeValue(BlinkInputStream in) throws IOException {
            return in.readUInt8();
        }
    }
    static class UInt16 extends FieldInstruction<Short> {
        public UInt16(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeUInt16(value);
        }
        @Override
        public Short decodeValue(BlinkInputStream in) throws IOException {
            return in.readUInt16();
        }
    }
    static class UInt32 extends FieldInstruction<Integer> {
        public UInt32(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeUInt32(value);
        }
        @Override
        public Integer decodeValue(BlinkInputStream in) throws IOException {
            return in.readUInt32();
        }
    }
    static class UInt64 extends FieldInstruction<Long> {
        public UInt64(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeUInt64(value);
        }
        @Override
        public Long decodeValue(BlinkInputStream in) throws IOException {
            return in.readUInt64();
        }
    }
    static class UInt8Null extends FieldInstruction<Byte> {
        public UInt8Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Byte value, BlinkOutputStream out) throws IOException {
            out.writeUInt8Null(value);
        }
        @Override
        public Byte decodeValue(BlinkInputStream in) throws IOException {
            return in.readUInt8Null();
        }
    }
    static class UInt16Null extends FieldInstruction<Short> {
        public UInt16Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, BlinkOutputStream out) throws IOException {
            out.writeUInt16Null(value);
        }
        @Override
        public Short decodeValue(BlinkInputStream in) throws IOException {
            return in.readUInt16Null();
        }
    }
    static class UInt32Null extends FieldInstruction<Integer> {
        public UInt32Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, BlinkOutputStream out) throws IOException {
            out.writeUInt32Null(value);
        }
        @Override
        public Integer decodeValue(BlinkInputStream in) throws IOException {
            return in.readUInt32Null();
        }
    }
    static class UInt64Null extends FieldInstruction<Long> {
        public UInt64Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, BlinkOutputStream out) throws IOException {
            out.writeUInt64Null(value);
        }
        @Override
        public Long decodeValue(BlinkInputStream in) throws IOException {
            return in.readUInt64Null();
        }
    }

    // --- FLOATING POINT NUMBERS ---
    static class Float32 extends FieldInstruction<Float> {
        public Float32(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Float value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeFloat32(value);
        }
        @Override
        public Float decodeValue(BlinkInputStream in) throws IOException {
            return in.readFloat32();
        }
    }
    static class Float32Null extends FieldInstruction<Float> {
        public Float32Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Float value, BlinkOutputStream out) throws IOException {
            out.writeFloat32Null(value);
        }
        @Override
        public Float decodeValue(BlinkInputStream in) throws IOException {
            return in.readFloat32Null();
        }
    }
    static class Float64 extends FieldInstruction<Double> {
        public Float64(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Double value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeFloat64(value);
        }
        @Override
        public Double decodeValue(BlinkInputStream in) throws IOException {
            return in.readFloat64();
        }
    }
    static class Float64Null extends FieldInstruction<Double> {
        public Float64Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Double value, BlinkOutputStream out) throws IOException {
            out.writeFloat64Null(value);
        }
        @Override
        public Double decodeValue(BlinkInputStream in) throws IOException {
            return in.readFloat64Null();
        }
    }

    // --- DECIMAL ---
    static class Decimal extends FieldInstruction<java.math.BigDecimal> {
        public Decimal(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.math.BigDecimal value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeDecimal(value);
        }
        @Override
        public java.math.BigDecimal decodeValue(BlinkInputStream in) throws IOException {
            return in.readDecimal();
        }
    }
    static class DecimalNull extends FieldInstruction<java.math.BigDecimal> {
        public DecimalNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.math.BigDecimal value, BlinkOutputStream out) throws IOException {
            out.writeDecimalNull(value);
        }
        @Override
        public java.math.BigDecimal decodeValue(BlinkInputStream in) throws IOException {
            return in.readDecimalNull();
        }
    }

    // --- BIGDECIMAL ---
    static class BigDecimal extends FieldInstruction<java.math.BigDecimal> {
        public BigDecimal(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.math.BigDecimal value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeBigDecimal(value);
        }
        @Override
        public java.math.BigDecimal decodeValue(BlinkInputStream in) throws IOException {
            return in.readBigDecimal();
        }
    }
    static class BigDecimalNull extends FieldInstruction<java.math.BigDecimal> {
        public BigDecimalNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.math.BigDecimal value, BlinkOutputStream out) throws IOException {
            out.writeBigDecimalNull(value);
        }
        @Override
        public java.math.BigDecimal decodeValue(BlinkInputStream in) throws IOException {
            return in.readBigDecimalNull();
        }
    }
    // --- BIGDINT ---
    static class BigInt extends FieldInstruction<BigInteger> {
        public BigInt(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(BigInteger value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeBigInt(value);
        }
        @Override
        public BigInteger decodeValue(BlinkInputStream in) throws IOException {
            return in.readBigInt();
        }
    }
    static class BigIntNull extends FieldInstruction<BigInteger> {
        public BigIntNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(BigInteger value, BlinkOutputStream out) throws IOException {
            out.writeBigIntNull(value);
        }
        @Override
        public BigInteger decodeValue(BlinkInputStream in) throws IOException {
            return in.readBigIntNull();
        }
    }

    // --- Boolean ---
    static class Boolean extends FieldInstruction<java.lang.Boolean> {
        public Boolean(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.lang.Boolean value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeBoolean(value);
        }
        @Override
        public java.lang.Boolean decodeValue(BlinkInputStream in) throws IOException {
            return in.readBoolean();
        }
    }
    static class BooleanNull extends FieldInstruction<java.lang.Boolean> {
        public BooleanNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.lang.Boolean value, BlinkOutputStream out) throws IOException {
            out.writeBooleanNull(value);
        }
        @Override
        public java.lang.Boolean decodeValue(BlinkInputStream in) throws IOException {
            return in.readBooleanNull();
        }
    }

    // --- TIME ---
    static class DateTime extends FieldInstruction<Date> {
        private final long timeUnitInMillis;
        private final long epochOffset;
        public DateTime(FieldDef field) {
            super(field);
            Time typeDef = (Time) field.getType();
            timeUnitInMillis = getTimeInMillis(typeDef.getUnit());
            epochOffset = getEpochOffset(typeDef.getEpoch());
        }
        @Override
        public void encodeValue(Date value, BlinkOutputStream out) throws IOException {
            require(value);
            long time = value.getTime();
            out.writeInt64((time-epochOffset)/timeUnitInMillis);
        }
        @Override
        public Date decodeValue(BlinkInputStream in) throws IOException {
            return new Date(in.readInt64()*timeUnitInMillis+epochOffset);
        }
    }
    static class DateTimeNull extends FieldInstruction<Date> {
        private final long timeUnitInMillis;
        private final long epochOffset;
        public DateTimeNull(FieldDef field) {
            super(field);
            Time typeDef = (Time) field.getType();
            timeUnitInMillis = getTimeInMillis(typeDef.getUnit());
            epochOffset = getEpochOffset(typeDef.getEpoch());
        }
        @Override
        public void encodeValue(Date value, BlinkOutputStream out) throws IOException {
            if (value == null) {
                out.writeUInt64Null(null);
            } else {
                long time = value.getTime();
                out.writeInt64((time-epochOffset)/timeUnitInMillis);
            }
        }
        @Override
        public Date decodeValue(BlinkInputStream in) throws IOException {
            Long time = in.readInt64Null();
            if (time == null) {
                return null;
            } else {
                return new Date(time*timeUnitInMillis+epochOffset);
            }
        }
    }
    // --- STRING ---
    static class StringUTF8 extends FieldInstruction<String> {
        public StringUTF8(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(String value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeStringUTF8(value);
        }
        @Override
        public String decodeValue(BlinkInputStream in) throws IOException {
            return in.readStringUTF8();
        }
    }
    static class StringUTF8Null extends FieldInstruction<String> {
        public StringUTF8Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(String value, BlinkOutputStream out) throws IOException {
            out.writeStringUTF8Null(value);
        }
        @Override
        public String decodeValue(BlinkInputStream in) throws IOException {
            return in.readStringUTF8Null();
        }
    }

    // --- BINARY ---
    static class Binary extends FieldInstruction<byte[]> {
        public Binary(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(byte[] value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeBinary(value);
        }
        @Override
        public byte[] decodeValue(BlinkInputStream in) throws IOException {
            return in.readBinary();
        }
    }
    static class BinaryNull extends FieldInstruction<byte[]> {
        public BinaryNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(byte[] value, BlinkOutputStream out) throws IOException {
            out.writeBinaryNull(value);
        }
        @Override
        public byte[] decodeValue(BlinkInputStream in) throws IOException {
            return in.readBinaryNull();
        }
    }

    // --- ENUM ---
    @SuppressWarnings("rawtypes")
    static class Enumeration extends FieldInstruction<Enum> {
        private final EnumSymbols enumSymbols;
        @SuppressWarnings("unchecked")
        public Enumeration(FieldDef field, TypeDef.Enum enumDef, Class enumType) {
            super(field);
            this.enumSymbols = new EnumSymbols<>(enumDef, enumType);
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(Enum value, BlinkOutputStream out) throws IOException {
            require(value);
            TypeDef.Symbol symbol = enumSymbols.getSymbol(value);
            if (symbol == null) {
                throw new IllegalArgumentException("Value is not a valid enumeration symbol: " + value);
            }
            out.writeInt32(symbol.getId());
        }
        @Override
        public Enum decodeValue(BlinkInputStream in) throws IOException {
            int enumValue = in.readInt32();
            Enum en = enumSymbols.getEnum(enumValue);
            if (en == null) {
                throw new DecodeException("Illegal or unknown enum value: " + enumValue);
            }
            return en;
        }
    }
    @SuppressWarnings("rawtypes")
    static class EnumerationNull extends FieldInstruction<Enum> {
        private final EnumSymbols enumSymbols;
        @SuppressWarnings("unchecked")
        public EnumerationNull(FieldDef field, TypeDef.Enum enumDef, Class enumType) {
            super(field);
            this.enumSymbols = new EnumSymbols<>(enumDef, enumType);
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(Enum value, BlinkOutputStream out) throws IOException {
            if (value == null) {
                out.writeInt32Null(null);
            } else {
                TypeDef.Symbol symbol = enumSymbols.getSymbol(value);
                if (symbol == null) {
                    throw new IllegalArgumentException("Value is not a valid enumeration symbol: " + value);
                }
                out.writeInt32Null(symbol.getId());
            }
        }
        @Override
        public Enum decodeValue(BlinkInputStream in) throws IOException {
            Integer enumValueObj = in.readInt32Null();
            if (enumValueObj == null) {
                return null;
            }
            int enumValue = enumValueObj.intValue();
            Enum en = enumSymbols.getEnum(enumValue);
            if (en == null) {
                throw new DecodeException("Illegal or unknown enum value: " + enumValue);
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
        public void encodeValue(Integer value, BlinkOutputStream out) throws IOException {
            require(value);
            out.writeInt32(value);
        }
        @Override
        public Integer decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt32();
        }
    }
    static class IntEnumerationNull extends FieldInstruction<Integer> {
        public IntEnumerationNull(FieldDef field) {
            super(field);
            // PENDING: validate input/output that it belongs to the enumeration?
        }
        @Override
        public void encodeValue(Integer value, BlinkOutputStream out) throws IOException {
            out.writeInt32Null(value);
        }
        @Override
        public Integer decodeValue(BlinkInputStream in) throws IOException {
            return in.readInt32Null();
        }
    }


    // --- SEQUENCE ---
    @SuppressWarnings("rawtypes")
    static class ListSequence extends FieldInstruction<List> {
        private final FieldInstruction elementInstruction;
        public ListSequence(FieldDef field, FieldInstruction elementInstruction) {
            super(field);
            this.elementInstruction = elementInstruction;
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(List value, BlinkOutputStream out) throws IOException {
            require(value);
            int length = value.size();
            out.writeUInt32(length);
            for (Object element : value) {
                elementInstruction.encodeValue(element, out);
            }

        }
        @SuppressWarnings("unchecked")
        @Override
        public List decodeValue(BlinkInputStream in) throws IOException {
            int size = in.readUInt32();
            if (size < 0) {
                throw new DecodeException("Sequence size overflow: " + size);
            }
            ArrayList value = new ArrayList(size);
            for (int i=0; i<size; i++) {
                value.add(elementInstruction.decodeValue(in));
            }
            return value;
        }
    }
    @SuppressWarnings("rawtypes")
    static class ListSequenceNull extends FieldInstruction<List> {
        private final FieldInstruction elementInstruction;
        public ListSequenceNull(FieldDef field, FieldInstruction elementInstruction) {
            super(field);
            this.elementInstruction = elementInstruction;
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(List value, BlinkOutputStream out) throws IOException {
            if (value == null) {
                out.writeUInt32Null(null);
            } else {
                int length = value.size();
                out.writeUInt32Null(length);
                for (Object element : value) {
                    elementInstruction.encodeValue(element, out);
                }
            }
        }
        @SuppressWarnings("unchecked")
        @Override
        public List decodeValue(BlinkInputStream in) throws IOException {
            Integer sizeObj = in.readUInt32Null();
            if (sizeObj == null) {
                return null;
            } else {
                int size = sizeObj.intValue();
                if (size < 0) {
                    throw new DecodeException("Sequence size overflow: " + size);
                }
                ArrayList value = new ArrayList(size);
                for (int i=0; i<size; i++) {
                    value.add(elementInstruction.decodeValue(in));
                }
                return value;
            }
        }
    }
    @SuppressWarnings("rawtypes")
    static class ArraySequence extends FieldInstruction<Object> { // actually <array>
        private final FieldInstruction elementInstruction;
        private final Class<?> componentType;
        public ArraySequence(FieldDef field, FieldInstruction elementInstruction) {
            super(field);
            this.elementInstruction = elementInstruction;
            this.componentType = field.getComponentJavaClass();
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(Object value, BlinkOutputStream out) throws IOException {
            require(value);
            int size = Array.getLength(value);
            out.writeUInt32(size);
            for (int i=0; i<size; i++) {
                elementInstruction.encodeValue(Array.get(value, i), out);
            }
        }
        @Override
        public Object decodeValue(BlinkInputStream in) throws IOException {
            int size = in.readUInt32();
            if (size < 0) {
                throw new DecodeException("Sequence size overflow: " + size);
            }
            Object value = Array.newInstance(componentType, size);
            for (int i=0; i<size; i++) {
                Array.set(value, i, elementInstruction.decodeValue(in));
            }
            return value;
        }
    }
    @SuppressWarnings("rawtypes")
    static class ArraySequenceNull extends FieldInstruction<Object> { // actually <array>
        private final FieldInstruction elementInstruction;
        private final Class<?> componentType;
        public ArraySequenceNull(FieldDef field, FieldInstruction elementInstruction) {
            super(field);
            this.elementInstruction = elementInstruction;
            this.componentType = field.getComponentJavaClass();
        }
        @SuppressWarnings("unchecked")
        @Override
        public void encodeValue(Object value, BlinkOutputStream out) throws IOException {
            if (value == null) {
                out.writeUInt32Null(null);
            } else {
                int size = Array.getLength(value);
                out.writeUInt32Null(size);
                for (int i=0; i<size; i++) {
                    elementInstruction.encodeValue(Array.get(value, i), out);
                }
            }
        }
        @Override
        public Object decodeValue(BlinkInputStream in) throws IOException {
            Integer sizeObj = in.readUInt32Null();
            if (sizeObj == null) {
                return null;
            } else {
                int size = sizeObj.intValue();
                if (size < 0) {
                    throw new DecodeException("Sequence size overflow: " + size);
                }
                Object value = Array.newInstance(componentType, size);
                for (int i=0; i<size; i++) {
                    Array.set(value, i, elementInstruction.decodeValue(in));
                }
                return value;
            }
        }
    }
    // --- GROUP ---
    static class DynamicGroup extends FieldInstruction<Object> {
        private final GeneratedCodec codec;
        public DynamicGroup(FieldDef field, GeneratedCodec codec) {
            super(field);
            this.codec = codec;
        }
        @Override
        public void encodeValue(Object value, BlinkOutputStream out) throws IOException {
            require(value);
            codec.writeDynamicGroup(out, value);
        }
        @Override
        public Object decodeValue(BlinkInputStream in) throws IOException {
            return codec.readDynamicGroup(in);
        }
    }
    static class DynamicGroupNull extends FieldInstruction<Object> {
        private final GeneratedCodec codec;
        public DynamicGroupNull(FieldDef field, GeneratedCodec codec) {
            super(field);
            this.codec = codec;
        }
        @Override
        public void encodeValue(Object value, BlinkOutputStream out) throws IOException {
            codec.writeDynamicGroupNull(out, value);
        }
        @Override
        public Object decodeValue(BlinkInputStream in) throws IOException {
            return codec.readDynamicGroupNull(in);
        }
    }


    static class StaticGroup extends FieldInstruction<Object> {
        private final StaticGroupInstruction groupInstruction;
        public StaticGroup(FieldDef field, StaticGroupInstruction groupInstruction) {
            super(field);
            this.groupInstruction = groupInstruction;
        }
        @Override
        public void encodeValue(Object value, BlinkOutputStream out) throws IOException {
            require(value);
            groupInstruction.encodeGroup(value, out);
        }
        @Override
        public Object decodeValue(BlinkInputStream in) throws IOException {
            return groupInstruction.decodeGroup(in);
        }
    }
    static class StaticGroupNull extends FieldInstruction<Object> {
        private final StaticGroupInstruction groupInstruction;
        public StaticGroupNull(FieldDef field, StaticGroupInstruction groupInstruction) {
            super(field);
            this.groupInstruction = groupInstruction;
        }
        @Override
        public void encodeValue(Object value, BlinkOutputStream out) throws IOException {
            if (value == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                groupInstruction.encodeGroup(value, out);
            }
        }
        @Override
        public Object decodeValue(BlinkInputStream in) throws IOException {
            boolean present = in.readBoolean();
            if (present) {
                return groupInstruction.decodeGroup(in);
            } else {
                return null;
            }
        }
    }

}
