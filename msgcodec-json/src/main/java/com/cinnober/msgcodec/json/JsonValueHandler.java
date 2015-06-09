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
import com.cinnober.msgcodec.ObjectInstantiationException;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Symbol;
import com.cinnober.msgcodec.util.TimeFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.text.ParseException;
import java.util.BitSet;
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
    public static final JsonValueHandler<Long> INT64 = new Int64Handler(false);
    public static final JsonValueHandler<Long> INT64_SAFE = new Int64Handler(true);

    public static final JsonValueHandler<Byte> UINT8 = new UInt8Handler();
    public static final JsonValueHandler<Short> UINT16 = new UInt16Handler();
    public static final JsonValueHandler<Integer> UINT32 = new UInt32Handler();
    public static final JsonValueHandler<Long> UINT64 = new UInt64Handler(false);
    public static final JsonValueHandler<Long> UINT64_SAFE = new UInt64Handler(true);
    public static final JsonValueHandler<String> STRING = new StringHandler();
    public static final JsonValueHandler<byte[]> BINARY = new BinaryHandler();
    public static final JsonValueHandler<Boolean> BOOLEAN = new BooleanHandler();
    public static final JsonValueHandler<BigDecimal> DECIMAL = new DecimalHandler(false);
    public static final JsonValueHandler<BigDecimal> DECIMAL_SAFE = new DecimalHandler(true);
    public static final JsonValueHandler<BigDecimal> BIGDECIMAL = new BigDecimalHandler(false);
    public static final JsonValueHandler<BigDecimal> BIGDECIMAL_SAFE = new BigDecimalHandler(true);
    public static final JsonValueHandler<BigInteger> BIGINT = new BigIntHandler(false);
    public static final JsonValueHandler<BigInteger> BIGINT_SAFE = new BigIntHandler(true);
    public static final JsonValueHandler<Float> FLOAT32 = new Float32Handler();
    public static final JsonValueHandler<Double> FLOAT64 = new Float64Handler();

    static final long MAX_SAFE_INTEGER = 9007199254740991L;
    static final long MIN_SAFE_INTEGER = -9007199254740991L;

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
    public static <T> JsonValueHandler<T> getValueHandlerXXX(TypeDef type, Class<T> javaClass) {
        return getValueHandler(type, javaClass, true);
    }
    /**
     * Returns a the basic value handler for the specified type definition and Java class.
     *
     * <p><b>Note:</b> the types SEQUENCE, REFERENCE and DYNAMIC_REFERENCE are not supported.
     *
     * @param <T> the java type
     * @param type the type definition, not null.
     * @param javaClass the java class, not null.
     * @param jsSafe true if unsafe JavaScript numeric values should be encoded as strings, otherwise false.
     * @return the json value handler, not null.
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonValueHandler<T> getValueHandler(TypeDef type, Class<T> javaClass, boolean jsSafe) {
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
            return (JsonValueHandler<T>) (jsSafe ? JsonValueHandler.INT64_SAFE : JsonValueHandler.INT64);
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
            return (JsonValueHandler<T>) (jsSafe ? JsonValueHandler.UINT64_SAFE : JsonValueHandler.UINT64);
        case STRING:
            checkType(javaClass, String.class);
            return ((TypeDef.StringUnicode)type).hasMaxSize() ?
                    (JsonValueHandler<T>) new StringHandler(((TypeDef.StringUnicode)type).getMaxSize()) :
                    (JsonValueHandler<T>) JsonValueHandler.STRING;
        case BOOLEAN:
            checkType(javaClass, boolean.class, Boolean.class);
            return (JsonValueHandler<T>) JsonValueHandler.BOOLEAN;
        case BINARY:
            checkType(javaClass, byte[].class);
            return ((TypeDef.Binary)type).hasMaxSize() ?
                    (JsonValueHandler<T>) new BinaryHandler(((TypeDef.Binary)type).getMaxSize()) :
                    (JsonValueHandler<T>) JsonValueHandler.BINARY;
        case DECIMAL:
            checkType(javaClass, BigDecimal.class);
            return (JsonValueHandler<T>) (jsSafe ? JsonValueHandler.DECIMAL_SAFE : JsonValueHandler.DECIMAL);
        case BIGDECIMAL:
            checkType(javaClass, BigDecimal.class);
            return (JsonValueHandler<T>) (jsSafe ? JsonValueHandler.BIGDECIMAL_SAFE : JsonValueHandler.BIGDECIMAL);
        case BIGINT:
            checkType(javaClass, BigInteger.class);
            return (JsonValueHandler<T>) (jsSafe ? JsonValueHandler.BIGINT_SAFE : JsonValueHandler.BIGINT);
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

    static boolean isJavaScriptSafeSigned(long value) {
        return MIN_SAFE_INTEGER <= value && value <= MAX_SAFE_INTEGER;
    }
    static boolean isJavaScriptSafeUnsigned(long value) {
        return 0L <= value && value <= MAX_SAFE_INTEGER;
    }
    static boolean isJavaScriptSafeSigned(BigInteger value) {
        int bitlen = value.bitLength();
        return bitlen <= 52 || (bitlen == 53 && isJavaScriptSafeSigned(value.longValue()));
    }
    static boolean isJavaScriptSafeSigned(BigDecimal value) {
        // the number of decimal digits a double can uniquely identify is 15
        return value.precision() <= 15;
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
            return (byte) p.getIntValue();
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
            return (short) p.getIntValue();
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
            return p.getIntValue();
        }
    }
    static class Int64Handler extends JsonValueHandler<Long> {
        private final boolean jsSafe;
        private Int64Handler(boolean jsSafe) {
            this.jsSafe = jsSafe;
        }
        @Override
        public void writeValue(Long value, JsonGenerator g) throws IOException {
            long v = value.longValue();
            if (jsSafe && !isJavaScriptSafeUnsigned(v)) {
                g.writeString(Long.toString(v));
            } else {
                g.writeNumber(v);
            }
        }
        @Override
        public Long readValue(JsonParser p) throws IOException {
            switch (p.getCurrentToken()) {
                case VALUE_NUMBER_INT:
                    return p.getLongValue();
                case VALUE_STRING:
                    return Long.parseLong(p.getText());
                default:
                    throw new DecodeException("Found " + p.getCurrentToken() + " while parsing an int64");
            }
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
            return (byte) p.getIntValue();
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
            return (short) p.getIntValue();
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
            return (int) p.getLongValue();
        }
    }
    static class UInt64Handler extends JsonValueHandler<Long> {
        private static final BigInteger TWO_POW_64 = BigInteger.ONE.shiftLeft(64);
        private final boolean jsSafe;
        private UInt64Handler(boolean jsSafe) {
            this.jsSafe = jsSafe;
        }
        @Override
        public void writeValue(Long value, JsonGenerator g) throws IOException {
            long v = value.longValue();
            if (jsSafe && !isJavaScriptSafeUnsigned(value)) {
                if (value < 0) {
                    g.writeString(TWO_POW_64.add(BigInteger.valueOf(v)).toString());
                } else {
                    g.writeString(Long.toString(v));
                }
            } else {
                if (v < 0) {
                    g.writeNumber(TWO_POW_64.add(BigInteger.valueOf(v)).toString());
                } else {
                    g.writeNumber(v);
                }
            }
        }
        @Override
        public Long readValue(JsonParser p) throws IOException {
            // TODO: we're not validating that the parsed value is positive and less than 2^64
            switch (p.getCurrentToken()) {
                case VALUE_NUMBER_INT:
                    return p.getBigIntegerValue().longValue();
                case VALUE_STRING:
                    return new BigInteger(p.getText()).longValue();
                default:
                    throw new DecodeException("Found " + p.getCurrentToken() + " while parsing an uint64");
            }
        }
    }
    static class StringHandler extends JsonValueHandler<String> {
        final int maxSize;
        private StringHandler() {
            this(-1);
        }
        private StringHandler(int maxSize) {
            this.maxSize = maxSize;
        }
        @Override
        public void writeValue(String value, JsonGenerator g) throws IOException {
            if (maxSize != -1 && value.length() > maxSize) {
                // PENDING: should actually check number of bytes (not chars), but that is expensive
                throw new IllegalArgumentException("String length ("+value.length()+") exceeds max size "+maxSize);
            }
            g.writeString(value);
        }
        @Override
        public String readValue(JsonParser p) throws IOException {
            String value = p.getText();
            if (maxSize != -1 && value.length() > maxSize) {
                // PENDING: should actually check number of bytes (not chars), but that is expensive
                throw new DecodeException("String length ("+value.length()+") exceeds max size "+maxSize);
            }
            return value;
        }
    }
    static class BinaryHandler extends JsonValueHandler<byte[]> {
        final int maxSize;
        private BinaryHandler() {
            this(-1);
        }
        private BinaryHandler(int maxSize) {
            this.maxSize = maxSize;
        }
        @Override
        public void writeValue(byte[] value, JsonGenerator g) throws IOException {
            if (maxSize != -1 && value.length > maxSize) {
                throw new IllegalArgumentException("Binary length ("+value.length+") exceeds max size "+maxSize);
            }
            g.writeBinary(value);
        }
        @Override
        public byte[] readValue(JsonParser p) throws IOException {
            byte[] value = p.getBinaryValue();
            if (maxSize != -1 && value.length > maxSize) {
                throw new DecodeException("Binary length ("+value.length+") exceeds max size "+maxSize);
            }
            return value;
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
        private final boolean jsSafe;
        private DecimalHandler(boolean jsSafe) {
            this.jsSafe = jsSafe;
        }
        @Override
        public void writeValue(BigDecimal value, JsonGenerator g) throws IOException {
            TypeDef.checkDecimal(value);
            if (jsSafe && !isJavaScriptSafeSigned(value)) {
                g.writeString(value.toString());
            } else {
                g.writeNumber(value);
            }
        }
        @Override
        public BigDecimal readValue(JsonParser p) throws IOException {
            switch (p.getCurrentToken()) {
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    return checkRange(p.getDecimalValue());
                case VALUE_STRING:
                    return checkRange(new BigDecimal(p.getText()));
                default:
                    throw new DecodeException("Found " + p.getCurrentToken() + " while parsing a decimal");
            }
        }
        private static BigDecimal checkRange(BigDecimal value) throws DecodeException {
            try {
                return TypeDef.checkDecimal(value);
            } catch (IllegalArgumentException e) {
                throw new DecodeException(e.getMessage());
            }
        }
    }
    static class BigDecimalHandler extends JsonValueHandler<BigDecimal> {
        private final boolean jsSafe;
        private BigDecimalHandler(boolean jsSafe) {
            this.jsSafe = jsSafe;
        }
        @Override
        public void writeValue(BigDecimal value, JsonGenerator g) throws IOException {
            if (jsSafe && !isJavaScriptSafeSigned(value)) {
                g.writeString(value.toString());
            } else {
                g.writeNumber(value);
            }
        }
        @Override
        public BigDecimal readValue(JsonParser p) throws IOException {
            switch (p.getCurrentToken()) {
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    return p.getDecimalValue();
                case VALUE_STRING:
                    return new BigDecimal(p.getText());
                default:
                    throw new DecodeException("Found " + p.getCurrentToken() + " while parsing a big decimal");
            }
        }
    }
    static class BigIntHandler extends JsonValueHandler<BigInteger> {
        private final boolean jsSafe;
        private BigIntHandler(boolean jsSafe) {
            this.jsSafe = jsSafe;
        }
        @Override
        public void writeValue(BigInteger value, JsonGenerator g) throws IOException {
            if (jsSafe && !isJavaScriptSafeSigned(value)) {
                g.writeString(value.toString());
            } else {
                g.writeNumber(value);
            }
        }
        @Override
        public BigInteger readValue(JsonParser p) throws IOException {
            switch (p.getCurrentToken()) {
                case VALUE_NUMBER_INT:
                    return p.getBigIntegerValue();
                case VALUE_STRING:
                    return new BigInteger(p.getText());
                default:
                    throw new DecodeException("Found " + p.getCurrentToken() + " while parsing a big integer");
            }
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
            switch (p.getCurrentToken()) {
                case VALUE_NUMBER_FLOAT:
                case VALUE_NUMBER_INT:
                    return p.getFloatValue();
                case VALUE_STRING:
                    switch (p.getText()) {
                        case "NaN":
                            return Float.NaN;
                        case "Infinity":
                            return Float.POSITIVE_INFINITY;
                        case "-Infinity":
                            return Float.NEGATIVE_INFINITY;
                        default:
                            throw new DecodeException("Illegal float32 string value: " + p.getText());
                    }
                default:
                    throw new DecodeException("Found " + p.getCurrentToken() + " while parsing a big integer");
            }
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
            switch (p.getCurrentToken()) {
                case VALUE_NUMBER_FLOAT:
                case VALUE_NUMBER_INT:
                    return p.getDoubleValue();
                case VALUE_STRING:
                    switch (p.getText()) {
                        case "NaN":
                            return Double.NaN;
                        case "Infinity":
                            return Double.POSITIVE_INFINITY;
                        case "-Infinity":
                            return Double.NEGATIVE_INFINITY;
                        default:
                            throw new DecodeException("Illegal float64 string value: " + p.getText());
                    }
                default:
                    throw new DecodeException("Found " + p.getCurrentToken() + " while parsing a big integer");
            }
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

        /**
         * Convert the value to a long value for the specified epoch and time unit.
         *
         * @param value the time value, not null.
         * @return the long value for the specified epoch and time unit.
         */
        protected abstract long convertToLong(T value);
        /** 
         * Convert the value from a long value for the specified epoch and time unit.
         *
         * @param value the long value for the specified epoch and time unit.
         * @return the time value, not null.
         */
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

        public JsonValueHandler getComponentHandler() {
            return componentHandler;
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

        public JsonValueHandler getComponentHandler() {
            return componentHandler;
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class FieldHandler {
        private final String name;
        private final Accessor accessor;
        private final boolean required;
        private final int requiredSlot;
        private final JsonValueHandler valueHandler;

        public FieldHandler(String name, Accessor accessor, boolean required, int requiredSlot,
                JsonValueHandler valueHandler) {
            this.name = name;
            this.accessor = accessor;
            this.required = required;
            this.requiredSlot = requiredSlot;
            this.valueHandler = valueHandler;
        }

        void writeValue(Object group, JsonGenerator g) throws IOException {
            Object value = accessor.getValue(group);
            if (value != null) {
                g.writeFieldName(name);
                valueHandler.writeValue(value, g);
            } else if (required) {
                throw new IllegalArgumentException("Missing required field value");
            }
        }

        void readValue(Object group, JsonParser p) throws IOException {
            Object value = valueHandler.readValue(p);
            accessor.setValue(group, value);
        }
        void readNull() throws IOException {
            if (required) {
                throw new DecodeException("Found null for non-optional field");
            }
        }

        boolean isRequired() {
            return required;
        }

        int getRequiredSlot() {
            return requiredSlot;
        }

        public JsonValueHandler getValueHandler() {
            return valueHandler;
        }
    }

    @SuppressWarnings({"rawtypes"})
    public static class StaticGroupHandler extends JsonValueHandler<Object> {
        private final String name;
        private final Factory factory;
        private Map<String, FieldHandler> fields;
        private int numRequiredFields;
        StaticGroupHandler(GroupDef group) {
            this.name = group.getName();
            this.factory = group.getBinding().getFactory();
        }

        void init(Map<String, FieldHandler> fields) {
            this.fields = fields;
            this.numRequiredFields =
                    (int) fields.values().stream().mapToInt(FieldHandler::getRequiredSlot).filter(i -> i>=0).count();
            
        }

        public int getNumRequiredFields() {
            return numRequiredFields;
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
            Object group;
            try {
                group = factory.newInstance();
            } catch (ObjectInstantiationException e) {
                throw new DecodeException(e);
            }
            readValue(group, p);
            return group;
        }

        void readValue(Object group, JsonParser p) throws IOException {
            // startObject has already been read
            BitSet requiredFields = new BitSet(numRequiredFields);
            requiredFields.set(0, numRequiredFields);
            while (p.nextToken() == JsonToken.FIELD_NAME) {
                String fieldName = p.getText();
                FieldHandler fieldHandler = fields.get(fieldName);
                if (fieldHandler == null) {
                    throw new DecodeException("Unknown field: " + fieldName);
                }
                if (p.nextToken() == JsonToken.VALUE_NULL) {
                    fieldHandler.readNull();
                } else {
                    fieldHandler.readValue(group, p);
                    if (fieldHandler.isRequired()) {
                        requiredFields.clear(fieldHandler.getRequiredSlot());
                    }
                }
            }

            if (!requiredFields.isEmpty()) {
                throw new DecodeException("Some required fields are missing");
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
            if (p.nextToken() != JsonToken.FIELD_NAME) {
                throw new DecodeException("Expected field");
            }
            String groupName;
            if (p.getText().equals(TYPE_FIELD)) {
                p.nextToken(); // field value
                groupName = p.getText();
            } else {
                TypeScannerJsonParser p2;
                if (p instanceof TypeScannerJsonParser) {
                    p2 = (TypeScannerJsonParser) p;
                } else {
                    p2 = new TypeScannerJsonParser(p);
                    p = p2;
                }
                groupName = p2.findType();
            }
            StaticGroupHandler groupHandler = jsonCodec.lookupGroupByName(groupName);
            if (groupHandler == null) {
                throw new DecodeException("Unknown type: " + groupName);
            }
            return groupHandler.readValue(p);
        }

    }

}
