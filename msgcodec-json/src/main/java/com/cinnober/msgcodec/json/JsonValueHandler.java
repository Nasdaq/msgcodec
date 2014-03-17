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
package com.cinnober.msgcodec.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.EnumSymbols;
import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.Factory;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Symbol;
import com.cinnober.msgcodec.util.TimeFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.text.ParseException;
import java.util.List;

/**
 * Writes and reads values to and from JsonGenerator and JsonParser respectively.
 * 
 * @author Mikael Brannstrom
 * @param <T> the value type
 */
public abstract class JsonValueHandler<T> {
    static final String TYPE_FIELD = "$type";

    public static final JsonValueHandler<Byte> INT8 = new Int8Handler();
    public static final JsonValueHandler<Short> INT16 = new Int16Handler();
    public static final JsonValueHandler<Integer> INT32 = new Int32Handler();
    public static final JsonValueHandler<Long> INT64 = new Int64Handler();

    public static final JsonValueHandler<Byte> UINT8 = new UInt8Handler();
    public static final JsonValueHandler<Short> UINT16 = new UInt16Handler();
    public static final JsonValueHandler<Integer> UINT32 = new UInt32Handler();
    public static final JsonValueHandler<Long> UINT64 = new UInt64Handler();
    public static final JsonValueHandler<String> STRING = new StringHandler();
    public static final JsonValueHandler<byte[]> BINARY = new BinaryHandler();
    public static final JsonValueHandler<Boolean> BOOLEAN = new BooleanHandler();
    public static final JsonValueHandler<BigDecimal> DECIMAL = new DecimalHandler();
    public static final JsonValueHandler<BigDecimal> BIGDECIMAL = new BigDecimalHandler();
    public static final JsonValueHandler<BigInteger> BIGINT = new BigIntHandler();
    public static final JsonValueHandler<Float> FLOAT32 = new Float32Handler();
    public static final JsonValueHandler<Double> FLOAT64 = new Float64Handler();


    /**
     * Returns a the basic value handler for the specified type definition and Java class.
     *
     * <p><b>Note:</b> the types SEQUENCE, REFERENCE and DYNAMIC_REFERENCE are not supported.
     *
     * @param <T> the java type
     * @param type the type definition, not null.
     * @param javaClass the java class, not null.
     * @return the json value handler, not null.
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonValueHandler<T> getValueHandler(TypeDef type, Class<T> javaClass) {
        switch (type.getType()) {
        case INT8:
            checkType(javaClass, byte.class, Byte.class);
            return (JsonValueHandler<T>) JsonValueHandler.INT8;
        case INT16:
            checkType(javaClass, short.class, Short.class);
            return (JsonValueHandler<T>) JsonValueHandler.INT16;
        case INT32:
            checkType(javaClass, int.class, Integer.class);
            return (JsonValueHandler<T>) JsonValueHandler.INT32;
        case INT64:
            checkType(javaClass, long.class, Long.class);
            return (JsonValueHandler<T>) JsonValueHandler.INT64;
        case UINT8:
            checkType(javaClass, byte.class, Byte.class);
            return (JsonValueHandler<T>) JsonValueHandler.UINT8;
        case UINT16:
            checkType(javaClass, short.class, Short.class);
            return (JsonValueHandler<T>) JsonValueHandler.UINT16;
        case UINT32:
            checkType(javaClass, int.class, Integer.class);
            return (JsonValueHandler<T>) JsonValueHandler.UINT32;
        case UINT64:
            checkType(javaClass, long.class, Long.class);
            return (JsonValueHandler<T>) JsonValueHandler.UINT64;
        case STRING:
            checkType(javaClass, String.class);
            return (JsonValueHandler<T>) JsonValueHandler.STRING;
        case BOOLEAN:
            checkType(javaClass, boolean.class, Boolean.class);
            return (JsonValueHandler<T>) JsonValueHandler.BOOLEAN;
        case BINARY:
            checkType(javaClass, byte[].class);
            return (JsonValueHandler<T>) JsonValueHandler.BINARY;
        case DECIMAL:
            checkType(javaClass, BigDecimal.class);
            return (JsonValueHandler<T>) JsonValueHandler.DECIMAL;
        case BIGDECIMAL:
            checkType(javaClass, BigDecimal.class);
            return (JsonValueHandler<T>) JsonValueHandler.BIGDECIMAL;
        case BIGINT:
            checkType(javaClass, BigInteger.class);
            return (JsonValueHandler<T>) JsonValueHandler.BIGINT;
        case FLOAT32:
            checkType(javaClass, float.class, Float.class);
            return (JsonValueHandler<T>) JsonValueHandler.FLOAT32;
        case FLOAT64:
            checkType(javaClass, double.class, Double.class);
            return (JsonValueHandler<T>) JsonValueHandler.FLOAT64;
        case ENUM:
            if (javaClass.isEnum()) {
                return new JsonValueHandler.EnumHandler((TypeDef.Enum)type, javaClass);
            } else if(javaClass.equals(Integer.class) || javaClass.equals(int.class)) {
                return (JsonValueHandler<T>) new JsonValueHandler.IntEnumHandler((TypeDef.Enum)type);
            } else {
                throw new IllegalArgumentException("Illegal enum java class: " + javaClass);
            }
        case TIME:
            if (javaClass.equals(Date.class)) {
                return (JsonValueHandler<T>) new JsonValueHandler.DateTimeHandler((TypeDef.Time)type);
            } else if(javaClass.equals(Integer.class) || javaClass.equals(int.class)) {
                return (JsonValueHandler<T>) new JsonValueHandler.IntTimeHandler((TypeDef.Time)type);
            } else if(javaClass.equals(Long.class) || javaClass.equals(long.class)) {
                return (JsonValueHandler<T>) new JsonValueHandler.LongTimeHandler((TypeDef.Time)type);
            } else {
                throw new IllegalArgumentException("Illegal time java class: " + javaClass);
            }
        case SEQUENCE:
        case REFERENCE:
        case DYNAMIC_REFERENCE:
            throw new IllegalArgumentException("Illegal type: " + type);
        default:
            throw new RuntimeException("Unhandled type: " + type.getType());
        }
    }

    private static <T> void checkType(Class<T> type, Class<?> ... expTypes) {
        for (Class<?> expType : expTypes) {
            if (type == expType) {
                return;
            }
        }
        throw new IllegalArgumentException("Illegal type java class: " + type);
    }

    /**
     * Write the value to the specified json generator.
     * @param value the value, not null.
     * @param g the json generator to write to, not null.
     * @throws IOException if the json generator throws an exception.
     */
    public abstract void writeValue(T value, JsonGenerator g) throws IOException;
    /**
     * Read a value from the specified json parser.
     * @param p the json parser to read from, not null.
     * @return the value, not null.
     * @throws IOException if the json parser throws an exception.
     */
    public abstract T readValue(JsonParser p) throws IOException;

    static class Int8Handler extends JsonValueHandler<Byte> {
        private Int8Handler() {}
        @Override
        public void writeValue(Byte value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        public Byte readValue(JsonParser p) throws IOException {
            return (byte) p.getValueAsInt();
        }
    }
    static class Int16Handler extends JsonValueHandler<Short> {
        private Int16Handler() {}
        @Override
        public void writeValue(Short value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        public Short readValue(JsonParser p) throws IOException {
            return (short) p.getValueAsInt();
        }
    }
    static class Int32Handler extends JsonValueHandler<Integer> {
        private Int32Handler() {}
        @Override
        public void writeValue(Integer value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        public Integer readValue(JsonParser p) throws IOException {
            return p.getValueAsInt();
        }
    }
    static class Int64Handler extends JsonValueHandler<Long> {
        private Int64Handler() {}
        @Override
        public void writeValue(Long value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        public Long readValue(JsonParser p) throws IOException {
            return p.getValueAsLong();
        }
    }
    static class UInt8Handler extends JsonValueHandler<Byte> {
        private UInt8Handler() {}
        @Override
        public void writeValue(Byte value, JsonGenerator g) throws IOException {
            g.writeNumber(value.byteValue() & 0xff);
        }
        @Override
        public Byte readValue(JsonParser p) throws IOException {
            return (byte) p.getValueAsInt();
        }
    }
    static class UInt16Handler extends JsonValueHandler<Short> {
        private UInt16Handler() {}
        @Override
        public void writeValue(Short value, JsonGenerator g) throws IOException {
            g.writeNumber(value.shortValue() & 0xffff);
        }
        @Override
        public Short readValue(JsonParser p) throws IOException {
            return (short) p.getValueAsInt();
        }
    }
    static class UInt32Handler extends JsonValueHandler<Integer> {
        private UInt32Handler() {}
        @Override
        public void writeValue(Integer value, JsonGenerator g) throws IOException {
            g.writeNumber(value.intValue() & 0xffffffffL);
        }
        @Override
        public Integer readValue(JsonParser p) throws IOException {
            return (int) p.getValueAsLong();
        }
    }
    static class UInt64Handler extends JsonValueHandler<Long> {
        private UInt64Handler() {}
        @Override
        public void writeValue(Long value, JsonGenerator g) throws IOException {
            long v = value.longValue();
            if (v < 0) {
                // TODO: write via BigInteger
                g.writeNumber(v);
            } else {
                g.writeNumber(v);
            }
        }
        @Override
        public Long readValue(JsonParser p) throws IOException {
            // TODO: read via BigInteger?
            return p.getValueAsLong();
        }
    }
    static class StringHandler extends JsonValueHandler<String> {
        private StringHandler() {}
        @Override
        public void writeValue(String value, JsonGenerator g) throws IOException {
            g.writeString(value);
        }
        @Override
        public String readValue(JsonParser p) throws IOException {
            return p.getValueAsString();
        }
    }
    static class BinaryHandler extends JsonValueHandler<byte[]> {
        private BinaryHandler() {}
        @Override
        public void writeValue(byte[] value, JsonGenerator g) throws IOException {
            g.writeBinary(value);
        }
        @Override
        public byte[] readValue(JsonParser p) throws IOException {
            return p.getBinaryValue();
        }
    }
    static class BooleanHandler extends JsonValueHandler<Boolean> {
        private BooleanHandler() {}
        @Override
        public void writeValue(Boolean value, JsonGenerator g) throws IOException {
            g.writeBoolean(value);
        }
        @Override
        public Boolean readValue(JsonParser p) throws IOException {
            return p.getBooleanValue();
        }
    }
    static class DecimalHandler extends JsonValueHandler<BigDecimal> {
        private DecimalHandler() {}
        @Override
        public void writeValue(BigDecimal value, JsonGenerator g) throws IOException {
            g.writeNumber(value); // TODO: validate range
        }
        @Override
        public BigDecimal readValue(JsonParser p) throws IOException {
            return p.getDecimalValue(); // TODO: validate range
        }
    }
    static class BigDecimalHandler extends JsonValueHandler<BigDecimal> {
        private BigDecimalHandler() {}
        @Override
        public void writeValue(BigDecimal value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        public BigDecimal readValue(JsonParser p) throws IOException {
            return p.getDecimalValue();
        }
    }
    static class BigIntHandler extends JsonValueHandler<BigInteger> {
        private BigIntHandler() {}
        @Override
        public void writeValue(BigInteger value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        public BigInteger readValue(JsonParser p) throws IOException {
            return p.getBigIntegerValue();
        }
    }
    static class Float32Handler extends JsonValueHandler<Float> {
        private Float32Handler() {}
        @Override
        public void writeValue(Float value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        public Float readValue(JsonParser p) throws IOException {
            return p.getFloatValue();
        }
    }
    static class Float64Handler extends JsonValueHandler<Double> {
        private Float64Handler() {}
        @Override
        public void writeValue(Double value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        public Double readValue(JsonParser p) throws IOException {
            return p.getDoubleValue();
        }
    }

    public static abstract class TimeHandler<T> extends JsonValueHandler<T> {
        private final Epoch epoch;
        private final TimeUnit unit;
        private final TimeFormat timeFormat;

        public TimeHandler(TypeDef.Time type) {
            this.epoch = type.getEpoch();
            this.unit = type.getUnit();
            this.timeFormat = TimeFormat.getTimeFormat(unit, epoch);
        }

        /** Convert the value to a long value for the specified epoch and time unit. */
        protected abstract long convertToLong(T value);
        /** Convert the value from a long value for the specified epoch and time unit. */
        protected abstract T convertFromLong(long value);

        @Override
        public void writeValue(T value, JsonGenerator g) throws IOException {
            long timeValue = convertToLong(value);
            String timeStr = timeFormat.format(timeValue);
            g.writeString(timeStr);
        }

        @Override
        public T readValue(JsonParser p) throws IOException {
            try {
                String s = p.getText();
                long timeValue = timeFormat.parse(s);
                return convertFromLong(timeValue);
            } catch (ParseException e) {
                throw new DecodeException("Could not parse time", e);
            }
        }
    }

    public static class IntTimeHandler extends TimeHandler<Integer> {
        public IntTimeHandler(TypeDef.Time type) {
            super(type);
        }

        @Override
        protected long convertToLong(Integer value) {
            return value;
        }

        @Override
        protected Integer convertFromLong(long value) {
            return (int)value;
        }
    }

    public static class LongTimeHandler extends TimeHandler<Long> {
        public LongTimeHandler(TypeDef.Time type) {
            super(type);
        }

        @Override
        protected long convertToLong(Long value) {
            return value;
        }

        @Override
        protected Long convertFromLong(long value) {
            return value;
        }
    }

    private static long getTimeInMillis(TimeUnit unit) {
        switch (unit) {
        case MILLISECONDS:
            return 1;
        case SECONDS:
            return 1000;
        case MINUTES:
            return 60*1000;
        case HOURS:
            return 60*60*1000;
        case DAYS:
            return 24*60*60*1000;
        default:
            throw new IllegalArgumentException("Date does not support " + unit);
        }
    }
    private static long getEpochOffset(Epoch epoch) {
        switch (epoch) {
        case UNIX:
            return 0;
        case Y2K:
            return 946706400000L;
        case MIDNIGHT:
            return 0;
        default:
            throw new IllegalArgumentException("Date does not support " + epoch);
        }
    }
    public static class DateTimeHandler extends TimeHandler<Date> {
        private final long timeUnitInMillis;
        private final long epochOffset;
        /**
         * Create a new date type handler.
         * @param type the time type, not null.
         */
        public DateTimeHandler(TypeDef.Time type) {
            super(type);
            timeUnitInMillis = getTimeInMillis(type.getUnit());
            epochOffset = getEpochOffset(type.getEpoch());
        }

        @Override
        protected long convertToLong(Date value) {
            return (value.getTime()-epochOffset)/timeUnitInMillis;

        }

        @Override
        protected Date convertFromLong(long value) {
            return new Date(value*timeUnitInMillis+epochOffset);
        }
    }

    public static class EnumHandler<E extends Enum<E>> extends JsonValueHandler<E> {
        private final EnumSymbols<E> enumSymbols;
        /**
         * Create a new Java enum handler.
         * @param type the enum type, not null.
         * @param enumClass the Java enum class, not null.
         */
        public EnumHandler(TypeDef.Enum type, Class<E> enumClass) {
            this.enumSymbols = new EnumSymbols<>(type, enumClass);
        }
        @Override
        public void writeValue(E value, JsonGenerator g) throws IOException {
            Symbol symbol = enumSymbols.getSymbol(value);
            if (symbol == null) {
                throw new IllegalArgumentException("Not a valid enum: " + value);
            }
            g.writeString(symbol.getName());
        }
        @Override
        public E readValue(JsonParser p) throws IOException {
            String str = p.getText();
            E value = enumSymbols.getEnum(str);
            if (value == null) {
                throw new DecodeException("Not a valid symbol: " + str);
            }
            return value;
        }
    }
    public static class IntEnumHandler extends JsonValueHandler<Integer> {
        private final Map<String, Integer> idByName;
        private final Map<Integer, String> nameById;
        /**
         * Create a new int enum handler.
         * @param typeDef the enum type, not null.
         */
        public IntEnumHandler(TypeDef.Enum typeDef) {
            idByName = new HashMap<>(typeDef.getSymbols().size() * 2);
            nameById = new HashMap<>(typeDef.getSymbols().size() * 2);
            for (Symbol symbol : typeDef.getSymbols()) {
                idByName.put(symbol.getName(), symbol.getId());
                nameById.put(symbol.getId(), symbol.getName());
            }
        }
        @Override
        public void writeValue(Integer value, JsonGenerator g) throws IOException {
            String name = nameById.get(value);
            if (name == null) {
                throw new IllegalArgumentException("Not a valid enum: " + value);
            }
            g.writeString(name);
        }
        @Override
        public Integer readValue(JsonParser p) throws IOException {
            String str = p.getText();
            Integer value = idByName.get(str);
            if (value == null) {
                throw new DecodeException("Not a valid symbol: " + str);
            }
            return value;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class ListSequenceHandler extends JsonValueHandler<List> {
        private final JsonValueHandler componentHandler;
        /**
         * Create a list sequence handler.
         * @param componentHandler the handler for the list component values, not null.
         */
        public ListSequenceHandler(JsonValueHandler componentHandler) {
            this.componentHandler = componentHandler;
        }
        @Override
        public void writeValue(List list, JsonGenerator g) throws IOException {
            g.writeStartArray();
            for (Object value : list) {
                componentHandler.writeValue(value, g);
            }
            g.writeEndArray();
        }
        @Override
        public List readValue(JsonParser p) throws IOException {
            List list = new ArrayList();
            // start array already consumed
            while (p.nextToken() != JsonToken.END_ARRAY) {
                list.add(componentHandler.readValue(p));
            }
            return list;
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class ArraySequenceHandler extends JsonValueHandler<Object> {
        private final JsonValueHandler componentHandler;
        private final Class<?> componentType;
        /**
         * Create an array sequence handler.
         * @param componentHandler the handler for the list component values, not null.
         * @param componentType the component java type, not null.
         */
        public ArraySequenceHandler(JsonValueHandler componentHandler, Class<?> componentType) {
            this.componentHandler = componentHandler;
            this.componentType = componentType;
        }
        @Override
        public void writeValue(Object array, JsonGenerator g) throws IOException {
            g.writeStartArray();
            int length = Array.getLength(array);
            for (int i=0; i<length; i++) {
                Object value = Array.get(array, i);
                componentHandler.writeValue(value, g);
            }
            g.writeEndArray();
        }
        @Override
        public Object readValue(JsonParser p) throws IOException {
            Collection list = new LinkedList();
            // start array already consumed
            while (p.nextToken() != JsonToken.END_ARRAY) {
                list.add(componentHandler.readValue(p));
            }
            Object array = Array.newInstance(componentType, list.size());
            int i=0;
            for (Object value : list) {
                Array.set(array, i++, value);
            }

            return array;
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class FieldHandler {
        private final String name;
        private final Accessor accessor;
        private final JsonValueHandler valueHandler;
        FieldHandler(FieldDef field, JsonValueHandler valueHandler) {
            this.name = field.getName();
            this.accessor = field.getBinding().getAccessor();
            this.valueHandler = valueHandler;
        }

        void writeValue(Object group, JsonGenerator g) throws IOException {
            Object value = accessor.getValue(group);
            if (value != null) {
                g.writeFieldName(name);
                valueHandler.writeValue(value, g);
            }
        }

        void readValue(Object group, JsonParser p) throws IOException {
            Object value = valueHandler.readValue(p);
            accessor.setValue(group, value);
        }
    }

    @SuppressWarnings({"rawtypes"})
    public static class StaticGroupHandler extends JsonValueHandler<Object> {
        private final String name;
        private final Factory factory;
        private Map<String, FieldHandler> fields;
        StaticGroupHandler(GroupDef group) {
            this.name = group.getName();
            this.factory = group.getBinding().getFactory();
        }

        void init(Map<String, FieldHandler> fields) {
            this.fields = fields;
        }

        void writeValue(Object value, JsonGenerator g, boolean dynamic) throws IOException {
            g.writeStartObject();
            if (dynamic) {
                g.writeFieldName(TYPE_FIELD);
                g.writeString(name);
            }
            for (FieldHandler field : fields.values()) {
                field.writeValue(value, g);
            }
            g.writeEndObject();
        }

        @Override
        public void writeValue(Object value, JsonGenerator g) throws IOException {
            writeValue(value, g, false);
        }

        @Override
        public Object readValue(JsonParser p) throws IOException {
            Object group = factory.newInstance();
            readValue(group, p);
            return group;
        }

        void readValue(Object group, JsonParser p) throws IOException {
            // startObject has already been read
            while (p.nextToken() == JsonToken.FIELD_NAME) {
                String fieldName = p.getCurrentName();
                if (p.nextToken() != JsonToken.VALUE_NULL) {
                    FieldHandler fieldHandler = fields.get(fieldName);
                    if (fieldHandler == null) {
                        throw new DecodeException("Unknown field: " + fieldName);
                    }
                    fieldHandler.readValue(group, p);
                }
            }
        }

        Map<String, FieldHandler> getFields() {
            return fields;
        }
    }
    public static class DynamicGroupHandler extends JsonValueHandler<Object> {
        private final JsonCodec jsonCodec;
        DynamicGroupHandler(JsonCodec jsonCodec) {
            this.jsonCodec = jsonCodec;
        }

        @Override
        public void writeValue(Object value, JsonGenerator g) throws IOException {
            StaticGroupHandler groupHandler = jsonCodec.lookupGroupByValue(value);
            if (groupHandler == null) {
                throw new IllegalArgumentException("Cannot encode group (unknown type)");
            }
            groupHandler.writeValue(value, g, true);
        }

        @Override
        public Object readValue(JsonParser p) throws IOException {
            if (p.nextToken() != JsonToken.FIELD_NAME || !p.getText().equals(TYPE_FIELD)) {
                throw new DecodeException("Expected field " + TYPE_FIELD);
            }
            p.nextToken(); // field value
            String groupName = p.getText();
            StaticGroupHandler groupHandler = jsonCodec.lookupGroupByName(groupName);
            if (groupHandler == null) {
                throw new DecodeException("Unknown type: " + groupName);
            }
            return groupHandler.readValue(p);
        }

    }

}
