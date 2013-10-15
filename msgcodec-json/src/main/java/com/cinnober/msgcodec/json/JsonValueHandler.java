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
import com.cinnober.msgcodec.EnumSymbols;
import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.Factory;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Symbol;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * @author Mikael Brannstrom
 *
 */
abstract class JsonValueHandler<T> {
    static final String TYPE_FIELD = "$type";

    static final Int8Handler INT8 = new Int8Handler();
    static final Int16Handler INT16 = new Int16Handler();
    static final Int32Handler INT32 = new Int32Handler();
    static final Int64Handler INT64 = new Int64Handler();

    static final UInt8Handler UINT8 = new UInt8Handler();
    static final UInt16Handler UINT16 = new UInt16Handler();
    static final UInt32Handler UINT32 = new UInt32Handler();
    static final UInt64Handler UINT64 = new UInt64Handler();
    static final StringHandler STRING = new StringHandler();
    static final BinaryHandler BINARY = new BinaryHandler();
    static final BooleanHandler BOOLEAN = new BooleanHandler();
    static final DecimalHandler DECIMAL = new DecimalHandler();
    static final BigDecimalHandler BIGDECIMAL = new BigDecimalHandler();
    static final BigIntHandler BIGINT = new BigIntHandler();
    static final Float32Handler FLOAT32 = new Float32Handler();
    static final Float64Handler FLOAT64 = new Float64Handler();

    abstract void writeValue(T value, JsonGenerator g) throws IOException;
    abstract T readValue(JsonParser p) throws IOException;

    static class Int8Handler extends JsonValueHandler<Byte> {
        @Override
        void writeValue(Byte value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        Byte readValue(JsonParser p) throws IOException {
            return (byte) p.getValueAsInt();
        }
    }
    static class Int16Handler extends JsonValueHandler<Short> {
        @Override
        void writeValue(Short value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        Short readValue(JsonParser p) throws IOException {
            return (short) p.getValueAsInt();
        }
    }
    static class Int32Handler extends JsonValueHandler<Integer> {
        @Override
        void writeValue(Integer value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        Integer readValue(JsonParser p) throws IOException {
            return p.getValueAsInt();
        }
    }
    static class Int64Handler extends JsonValueHandler<Long> {
        @Override
        void writeValue(Long value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        Long readValue(JsonParser p) throws IOException {
            return p.getValueAsLong();
        }
    }
    static class UInt8Handler extends JsonValueHandler<Byte> {
        @Override
        void writeValue(Byte value, JsonGenerator g) throws IOException {
            g.writeNumber(value.byteValue() & 0xff);
        }
        @Override
        Byte readValue(JsonParser p) throws IOException {
            return (byte) p.getValueAsInt();
        }
    }
    static class UInt16Handler extends JsonValueHandler<Short> {
        @Override
        void writeValue(Short value, JsonGenerator g) throws IOException {
            g.writeNumber(value.shortValue() & 0xffff);
        }
        @Override
        Short readValue(JsonParser p) throws IOException {
            return (short) p.getValueAsInt();
        }
    }
    static class UInt32Handler extends JsonValueHandler<Integer> {
        @Override
        void writeValue(Integer value, JsonGenerator g) throws IOException {
            g.writeNumber(value.intValue() & 0xffffffffL);
        }
        @Override
        Integer readValue(JsonParser p) throws IOException {
            return (int) p.getValueAsLong();
        }
    }
    static class UInt64Handler extends JsonValueHandler<Long> {
        @Override
        void writeValue(Long value, JsonGenerator g) throws IOException {
            long v = value.longValue();
            if (v < 0) {
                // TODO: write via BigInteger
                g.writeNumber(v);
            } else {
                g.writeNumber(v);
            }
        }
        @Override
        Long readValue(JsonParser p) throws IOException {
            // TODO: read via BigInteger?
            return p.getValueAsLong();
        }
    }
    static class StringHandler extends JsonValueHandler<String> {
        @Override
        void writeValue(String value, JsonGenerator g) throws IOException {
            g.writeString(value);
        }
        @Override
        String readValue(JsonParser p) throws IOException {
            return p.getValueAsString();
        }
    }
    static class BinaryHandler extends JsonValueHandler<byte[]> {
        @Override
        void writeValue(byte[] value, JsonGenerator g) throws IOException {
            g.writeBinary(value);
        }
        @Override
        byte[] readValue(JsonParser p) throws IOException {
            return p.getBinaryValue();
        }
    }
    static class BooleanHandler extends JsonValueHandler<Boolean> {
        @Override
        void writeValue(Boolean value, JsonGenerator g) throws IOException {
            g.writeBoolean(value);
        }
        @Override
        Boolean readValue(JsonParser p) throws IOException {
            return p.getBooleanValue();
        }
    }
    static class DecimalHandler extends JsonValueHandler<BigDecimal> {
        @Override
        void writeValue(BigDecimal value, JsonGenerator g) throws IOException {
            g.writeNumber(value); // TODO: validate range
        }
        @Override
        BigDecimal readValue(JsonParser p) throws IOException {
            return p.getDecimalValue(); // TODO: validate range
        }
    }
    static class BigDecimalHandler extends JsonValueHandler<BigDecimal> {
        @Override
        void writeValue(BigDecimal value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        BigDecimal readValue(JsonParser p) throws IOException {
            return p.getDecimalValue();
        }
    }
    static class BigIntHandler extends JsonValueHandler<BigInteger> {
        @Override
        void writeValue(BigInteger value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        BigInteger readValue(JsonParser p) throws IOException {
            return p.getBigIntegerValue();
        }
    }
    static class Float32Handler extends JsonValueHandler<Float> {
        @Override
        void writeValue(Float value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        Float readValue(JsonParser p) throws IOException {
            return p.getFloatValue();
        }
    }
    static class Float64Handler extends JsonValueHandler<Double> {
        @Override
        void writeValue(Double value, JsonGenerator g) throws IOException {
            g.writeNumber(value);
        }
        @Override
        Double readValue(JsonParser p) throws IOException {
            return p.getDoubleValue();
        }
    }

    static abstract class TimeHandler<T> extends JsonValueHandler<T> {
        private final Epoch epoch;
        private final TimeUnit unit;

        public TimeHandler(TypeDef.Time type) {
            this.epoch = type.getEpoch();
            this.unit = type.getUnit();
        }

        /** Convert the value to a long value for the specified epoch and time unit. */
        protected abstract long convertToLong(T value);
        /** Convert the value from a long value for the specified epoch and time unit. */
        protected abstract T convertFromLong(long value);

        @Override
        void writeValue(T value, JsonGenerator g) throws IOException {
            long timeValue = convertToLong(value);
            // TODO: format long to string
            String timeStr = "TODO-TIME:" + Long.toString(timeValue);
            g.writeString(timeStr);
        }

        @Override
        T readValue(JsonParser p) throws IOException {
            String s = p.getText();
            long timeValue = Long.parseLong(s.substring("TODO-TIME:".length()));
            return convertFromLong(timeValue);
        }
    }

    static class IntTimeHandler extends TimeHandler<Integer> {
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

    static class LongTimeHandler extends TimeHandler<Long> {
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
    static class DateTimeHandler extends TimeHandler<Date> {
        private final long timeUnitInMillis;
        private final long epochOffset;
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
    static class EnumHandler<E extends Enum<E>> extends JsonValueHandler<E> {
        private final EnumSymbols<E> enumSymbols;
        public EnumHandler(TypeDef.Enum typeDef, Class<E> enumClass) {
            this.enumSymbols = new EnumSymbols<E>(typeDef, enumClass);
        }
        @Override
        void writeValue(E value, JsonGenerator g) throws IOException {
            Symbol symbol = enumSymbols.getSymbol(value);
            if (symbol == null) {
                throw new IOException("Not a valid enum: " + value);
            }
            g.writeString(symbol.getName());
        }
        @Override
        E readValue(JsonParser p) throws IOException {
            String str = p.getText();
            E value = enumSymbols.getEnum(str);
            if (value == null) {
                throw new IOException("Not a valid symbol: " + str);
            }
            return value;
        }
    }
    static class IntEnumHandler extends JsonValueHandler<Integer> {
        private final Map<String, Integer> idByName;
        private final Map<Integer, String> nameById;
        public IntEnumHandler(TypeDef.Enum typeDef) {
            idByName = new HashMap<>(typeDef.getSymbols().size() * 2);
            nameById = new HashMap<>(typeDef.getSymbols().size() * 2);
            for (Symbol symbol : typeDef.getSymbols()) {
                idByName.put(symbol.getName(), symbol.getId());
                nameById.put(symbol.getId(), symbol.getName());
            }
        }
        @Override
        void writeValue(Integer value, JsonGenerator g) throws IOException {
            String name = nameById.get(value);
            if (name == null) {
                throw new IOException("Not a valid enum: " + value);
            }
            g.writeString(name);
        }
        @Override
        Integer readValue(JsonParser p) throws IOException {
            String str = p.getText();
            Integer value = idByName.get(str);
            if (value == null) {
                throw new IOException("Not a valid symbol: " + str);
            }
            return value;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static class CollectionSequenceHandler extends JsonValueHandler<Collection> {
        private final JsonValueHandler componentHandler;
        public CollectionSequenceHandler(JsonValueHandler componentHandler) {
            this.componentHandler = componentHandler;
        }
        @Override
        void writeValue(Collection list, JsonGenerator g) throws IOException {
            g.writeStartArray();
            for (Object value : list) {
                componentHandler.writeValue(value, g);
            }
            g.writeEndArray();
        }
        @Override
        Collection readValue(JsonParser p) throws IOException {
            Collection list = new ArrayList();
            // start array already consumed
            while (p.nextToken() != JsonToken.END_ARRAY) {
                list.add(componentHandler.readValue(p));
            }
            return list;
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static class ArraySequenceHandler extends JsonValueHandler<Object> {
        private final JsonValueHandler componentHandler;
        private final Class<?> componentType;
        public ArraySequenceHandler(JsonValueHandler componentHandler, Class<?> componentType) {
            this.componentHandler = componentHandler;
            this.componentType = componentType;
        }
        @Override
        void writeValue(Object array, JsonGenerator g) throws IOException {
            g.writeStartArray();
            int length = Array.getLength(array);
            for (int i=0; i<length; i++) {
                Object value = Array.get(array, i);
                componentHandler.writeValue(value, g);
            }
            g.writeEndArray();
        }
        @Override
        Object readValue(JsonParser p) throws IOException {
            Collection list = new LinkedList();
            // start array already consumed
            while (p.nextToken() != JsonToken.END_ARRAY) {
                list.add(componentHandler.readValue(p));
            }
            Object array = Array.newInstance(componentType, list.size());
            int i=0;
            for (Object value : list) {
                Array.set(array, i, value);
            }

            return array;
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static class FieldHandler {
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
    static class StaticGroupHandler extends JsonValueHandler<Object> {
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
        void writeValue(Object value, JsonGenerator g) throws IOException {
            writeValue(value, g, false);
        }

        @Override
        Object readValue(JsonParser p) throws IOException {
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
                        throw new IOException("Unknown field: " + fieldName);
                    }
                    fieldHandler.readValue(group, p);
                }
            }
        }

        /**
         * @return
         */
        Map<String, FieldHandler> getFields() {
            return fields;
        }
    }
    static class DynamicGroupHandler extends JsonValueHandler<Object> {
        private final JsonCodec jsonCodec;
        DynamicGroupHandler(JsonCodec jsonCodec) {
            this.jsonCodec = jsonCodec;
        }

        @Override
        void writeValue(Object value, JsonGenerator g) throws IOException {
            StaticGroupHandler groupHandler = jsonCodec.lookupGroupByValue(value);
            if (groupHandler == null) {
                throw new IOException("Cannot encode group (unknown type)");
            }
            groupHandler.writeValue(value, g, true);
        }

        @Override
        Object readValue(JsonParser p) throws IOException {
            if (p.nextToken() != JsonToken.FIELD_NAME || !p.getText().equals(TYPE_FIELD)) {
                throw new IOException("Expected field " + TYPE_FIELD);
            }
            p.nextToken(); // field value
            String groupName = p.getText();
            StaticGroupHandler groupHandler = jsonCodec.lookupGroupByName(groupName);
            if (groupHandler == null) {
                throw new IOException("Unknown type: " + groupName);
            }
            return groupHandler.readValue(p);
        }

    }

}
