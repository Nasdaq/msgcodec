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

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.ByteSink;
import com.cinnober.msgcodec.ByteSource;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.EnumSymbols;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Time;

import static com.cinnober.msgcodec.blink.DateUtil.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * @see #encodeValue(Object, ByteSink)
     */
    @SuppressWarnings("unchecked")
    public void encodeField(Object group, ByteSink out) throws IOException {
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
     * @see #decodeValue(ByteSource)
     */
    @SuppressWarnings("unchecked")
    public void decodeField(Object group, ByteSource in) throws IOException {
        Object value;
        try {
            value = decodeValue(in);
        } catch (Exception e) {
            if (field != null) {
                throw new FieldDecodeException(field.getName(), e);
            } else {
                throw e;
            }
        }
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
    public abstract void encodeValue(V value, ByteSink out) throws IOException;
    /** Decode a value from the input stream.
     *
     * @param in
     * @return the decoded value.
     * @throws IOException
     */
    public abstract V decodeValue(ByteSource in) throws IOException;

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
        public void encodeValue(Byte value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeInt8(out, value);
        }
        @Override
        public Byte decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt8(in);
        }
    }
    static class Int16 extends FieldInstruction<Short> {
        public Int16(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeInt16(out, value);
        }
        @Override
        public Short decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt16(in);
        }
    }
    static class Int32 extends FieldInstruction<Integer> {
        public Int32(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeInt32(out, value);
        }
        @Override
        public Integer decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt32(in);
        }
    }
    static class Int64 extends FieldInstruction<Long> {
        public Int64(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeInt64(out, value);
        }
        @Override
        public Long decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt64(in);
        }
    }
    static class Int8Null extends FieldInstruction<Byte> {
        public Int8Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Byte value, ByteSink out) throws IOException {
            BlinkOutput.writeInt8Null(out, value);
        }
        @Override
        public Byte decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt8Null(in);
        }
    }
    static class Int16Null extends FieldInstruction<Short> {
        public Int16Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, ByteSink out) throws IOException {
            BlinkOutput.writeInt16Null(out, value);
        }
        @Override
        public Short decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt16Null(in);
        }
    }
    static class Int32Null extends FieldInstruction<Integer> {
        public Int32Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, ByteSink out) throws IOException {
            BlinkOutput.writeInt32Null(out, value);
        }
        @Override
        public Integer decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt32Null(in);
        }
    }
    static class Int64Null extends FieldInstruction<Long> {
        public Int64Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, ByteSink out) throws IOException {
            BlinkOutput.writeInt64Null(out, value);
        }
        @Override
        public Long decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt64Null(in);
        }
    }
    static class UInt8 extends FieldInstruction<Byte> {
        public UInt8(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Byte value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeUInt8(out, value);
        }
        @Override
        public Byte decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readUInt8(in);
        }
    }
    static class UInt16 extends FieldInstruction<Short> {
        public UInt16(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeUInt16(out, value);
        }
        @Override
        public Short decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readUInt16(in);
        }
    }
    static class UInt32 extends FieldInstruction<Integer> {
        public UInt32(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeUInt32(out, value);
        }
        @Override
        public Integer decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readUInt32(in);
        }
    }
    static class UInt64 extends FieldInstruction<Long> {
        public UInt64(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeUInt64(out, value);
        }
        @Override
        public Long decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readUInt64(in);
        }
    }
    static class UInt8Null extends FieldInstruction<Byte> {
        public UInt8Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Byte value, ByteSink out) throws IOException {
            BlinkOutput.writeUInt8Null(out, value);
        }
        @Override
        public Byte decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readUInt8Null(in);
        }
    }
    static class UInt16Null extends FieldInstruction<Short> {
        public UInt16Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Short value, ByteSink out) throws IOException {
            BlinkOutput.writeUInt16Null(out, value);
        }
        @Override
        public Short decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readUInt16Null(in);
        }
    }
    static class UInt32Null extends FieldInstruction<Integer> {
        public UInt32Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Integer value, ByteSink out) throws IOException {
            BlinkOutput.writeUInt32Null(out, value);
        }
        @Override
        public Integer decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readUInt32Null(in);
        }
    }
    static class UInt64Null extends FieldInstruction<Long> {
        public UInt64Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Long value, ByteSink out) throws IOException {
            BlinkOutput.writeUInt64Null(out, value);
        }
        @Override
        public Long decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readUInt64Null(in);
        }
    }

    // --- FLOATING POINT NUMBERS ---
    static class Float32 extends FieldInstruction<Float> {
        public Float32(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Float value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeFloat32(out, value);
        }
        @Override
        public Float decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readFloat32(in);
        }
    }
    static class Float32Null extends FieldInstruction<Float> {
        public Float32Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Float value, ByteSink out) throws IOException {
            BlinkOutput.writeFloat32Null(out, value);
        }
        @Override
        public Float decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readFloat32Null(in);
        }
    }
    static class Float64 extends FieldInstruction<Double> {
        public Float64(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Double value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeFloat64(out, value);
        }
        @Override
        public Double decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readFloat64(in);
        }
    }
    static class Float64Null extends FieldInstruction<Double> {
        public Float64Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(Double value, ByteSink out) throws IOException {
            BlinkOutput.writeFloat64Null(out, value);
        }
        @Override
        public Double decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readFloat64Null(in);
        }
    }

    // --- DECIMAL ---
    static class Decimal extends FieldInstruction<java.math.BigDecimal> {
        public Decimal(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.math.BigDecimal value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeDecimal(out, value);
        }
        @Override
        public java.math.BigDecimal decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readDecimal(in);
        }
    }
    static class DecimalNull extends FieldInstruction<java.math.BigDecimal> {
        public DecimalNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.math.BigDecimal value, ByteSink out) throws IOException {
            BlinkOutput.writeDecimalNull(out, value);
        }
        @Override
        public java.math.BigDecimal decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readDecimalNull(in);
        }
    }

    // --- BIGDECIMAL ---
    static class BigDecimal extends FieldInstruction<java.math.BigDecimal> {
        public BigDecimal(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.math.BigDecimal value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeBigDecimal(out, value);
        }
        @Override
        public java.math.BigDecimal decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readBigDecimal(in);
        }
    }
    static class BigDecimalNull extends FieldInstruction<java.math.BigDecimal> {
        public BigDecimalNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.math.BigDecimal value, ByteSink out) throws IOException {
            BlinkOutput.writeBigDecimalNull(out, value);
        }
        @Override
        public java.math.BigDecimal decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readBigDecimalNull(in);
        }
    }
    // --- BIGDINT ---
    static class BigInt extends FieldInstruction<BigInteger> {
        public BigInt(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(BigInteger value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeBigInt(out, value);
        }
        @Override
        public BigInteger decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readBigInt(in);
        }
    }
    static class BigIntNull extends FieldInstruction<BigInteger> {
        public BigIntNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(BigInteger value, ByteSink out) throws IOException {
            BlinkOutput.writeBigIntNull(out, value);
        }
        @Override
        public BigInteger decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readBigIntNull(in);
        }
    }

    // --- Boolean ---
    static class Boolean extends FieldInstruction<java.lang.Boolean> {
        public Boolean(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.lang.Boolean value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeBoolean(out, value);
        }
        @Override
        public java.lang.Boolean decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readBoolean(in);
        }
    }
    static class BooleanNull extends FieldInstruction<java.lang.Boolean> {
        public BooleanNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(java.lang.Boolean value, ByteSink out) throws IOException {
            BlinkOutput.writeBooleanNull(out, value);
        }
        @Override
        public java.lang.Boolean decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readBooleanNull(in);
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
        public void encodeValue(Date value, ByteSink out) throws IOException {
            require(value);
            long time = value.getTime();
            BlinkOutput.writeInt64(out, (time-epochOffset)/timeUnitInMillis);
        }
        @Override
        public Date decodeValue(ByteSource in) throws IOException {
            return new Date(BlinkInput.readInt64(in)*timeUnitInMillis+epochOffset);
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
        public void encodeValue(Date value, ByteSink out) throws IOException {
            if (value == null) {
                BlinkOutput.writeUInt64Null(out, null);
            } else {
                long time = value.getTime();
                BlinkOutput.writeInt64(out, (time-epochOffset)/timeUnitInMillis);
            }
        }
        @Override
        public Date decodeValue(ByteSource in) throws IOException {
            Long time = BlinkInput.readInt64Null(in);
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
        public void encodeValue(String value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeStringUTF8(out, value);
        }
        @Override
        public String decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readStringUTF8(in);
        }
    }
    static class StringUTF8Null extends FieldInstruction<String> {
        public StringUTF8Null(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(String value, ByteSink out) throws IOException {
            BlinkOutput.writeStringUTF8Null(out, value);
        }
        @Override
        public String decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readStringUTF8Null(in);
        }
    }

    // --- BINARY ---
    static class Binary extends FieldInstruction<byte[]> {
        public Binary(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(byte[] value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeBinary(out, value);
        }
        @Override
        public byte[] decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readBinary(in);
        }
    }
    static class BinaryNull extends FieldInstruction<byte[]> {
        public BinaryNull(FieldDef field) {
            super(field);
        }
        @Override
        public void encodeValue(byte[] value, ByteSink out) throws IOException {
            BlinkOutput.writeBinaryNull(out, value);
        }
        @Override
        public byte[] decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readBinaryNull(in);
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
        public void encodeValue(Enum value, ByteSink out) throws IOException {
            require(value);
            TypeDef.Symbol symbol = enumSymbols.getSymbol(value);
            if (symbol == null) {
                throw new IllegalArgumentException("Value is not a valid enumeration symbol: " + value);
            }
            BlinkOutput.writeInt32(out, symbol.getId());
        }
        @Override
        public Enum decodeValue(ByteSource in) throws IOException {
            int enumValue = BlinkInput.readInt32(in);
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
        public void encodeValue(Enum value, ByteSink out) throws IOException {
            if (value == null) {
                BlinkOutput.writeInt32Null(out, null);
            } else {
                TypeDef.Symbol symbol = enumSymbols.getSymbol(value);
                if (symbol == null) {
                    throw new IllegalArgumentException("Value is not a valid enumeration symbol: " + value);
                }
                BlinkOutput.writeInt32Null(out, symbol.getId());
            }
        }
        @Override
        public Enum decodeValue(ByteSource in) throws IOException {
            Integer enumValueObj = BlinkInput.readInt32Null(in);
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
        public void encodeValue(Integer value, ByteSink out) throws IOException {
            require(value);
            BlinkOutput.writeInt32(out, value);
        }
        @Override
        public Integer decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt32(in);
        }
    }
    static class IntEnumerationNull extends FieldInstruction<Integer> {
        public IntEnumerationNull(FieldDef field) {
            super(field);
            // PENDING: validate input/output that it belongs to the enumeration?
        }
        @Override
        public void encodeValue(Integer value, ByteSink out) throws IOException {
            BlinkOutput.writeInt32Null(out, value);
        }
        @Override
        public Integer decodeValue(ByteSource in) throws IOException {
            return BlinkInput.readInt32Null(in);
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
        public void encodeValue(List value, ByteSink out) throws IOException {
            require(value);
            int length = value.size();
            BlinkOutput.writeUInt32(out, length);
            for (Object element : value) {
                elementInstruction.encodeValue(element, out);
            }

        }
        @SuppressWarnings("unchecked")
        @Override
        public List decodeValue(ByteSource in) throws IOException {
            int size = BlinkInput.readUInt32(in);
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
        public void encodeValue(List value, ByteSink out) throws IOException {
            if (value == null) {
                BlinkOutput.writeUInt32Null(out, null);
            } else {
                int length = value.size();
                BlinkOutput.writeUInt32Null(out, length);
                for (Object element : value) {
                    elementInstruction.encodeValue(element, out);
                }
            }
        }
        @SuppressWarnings("unchecked")
        @Override
        public List decodeValue(ByteSource in) throws IOException {
            Integer sizeObj = BlinkInput.readUInt32Null(in);
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
        public void encodeValue(Object value, ByteSink out) throws IOException {
            require(value);
            int size = Array.getLength(value);
            BlinkOutput.writeUInt32(out, size);
            for (int i=0; i<size; i++) {
                elementInstruction.encodeValue(Array.get(value, i), out);
            }
        }
        @Override
        public Object decodeValue(ByteSource in) throws IOException {
            int size = BlinkInput.readUInt32(in);
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
        public void encodeValue(Object value, ByteSink out) throws IOException {
            if (value == null) {
                BlinkOutput.writeUInt32Null(out, null);
            } else {
                int size = Array.getLength(value);
                BlinkOutput.writeUInt32Null(out, size);
                for (int i=0; i<size; i++) {
                    elementInstruction.encodeValue(Array.get(value, i), out);
                }
            }
        }
        @Override
        public Object decodeValue(ByteSource in) throws IOException {
            Integer sizeObj = BlinkInput.readUInt32Null(in);
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
        public void encodeValue(Object value, ByteSink out) throws IOException {
            require(value);
            codec.writeDynamicGroup(out, value);
        }
        @Override
        public Object decodeValue(ByteSource in) throws IOException {
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
        public void encodeValue(Object value, ByteSink out) throws IOException {
            codec.writeDynamicGroupNull(out, value);
        }
        @Override
        public Object decodeValue(ByteSource in) throws IOException {
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
        public void encodeValue(Object value, ByteSink out) throws IOException {
            require(value);
            groupInstruction.encodeGroup(value, out);
        }
        @Override
        public Object decodeValue(ByteSource in) throws IOException {
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
        public void encodeValue(Object value, ByteSink out) throws IOException {
            if (value == null) {
                BlinkOutput.writeBoolean(out, false);
            } else {
                BlinkOutput.writeBoolean(out, true);
                groupInstruction.encodeGroup(value, out);
            }
        }
        @Override
        public Object decodeValue(ByteSource in) throws IOException {
            boolean present = BlinkInput.readBoolean(in);
            if (present) {
                return groupInstruction.decodeGroup(in);
            } else {
                return null;
            }
        }
    }

}
