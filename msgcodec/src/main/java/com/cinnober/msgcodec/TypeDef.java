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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.cinnober.msgcodec.messages.MetaTypeDef;

/**
 * Type definition.
 * TypeDef is immutable.
 *
 * <p>The following data types are supported. Some types are parametrizable, such as sequence, enum and reference.
 *
 * <table>
 * <caption>Data types</caption>
 * <tr style="text-align: left">
 * <th>{@link Type Type}</th><th>Description</th><th>Java type</th>
 * </tr>
 * <tr>
 * <td>{@link Type#UINT8 UINT8}</td>
 * <td>Unsigned 8-bit integer.</td>
 * <td>byte or Byte.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#UINT16 UINT16}</td>
 * <td>Unsigned 16-bit integer.</td>
 * <td>short or Short.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#UINT32 UINT32}</td>
 * <td>Unsigned 32-bit integer.</td>
 * <td>int or Integer.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#UINT64 UINT64}</td>
 * <td>Unsigned 64-bit integer.</td>
 * <td>long or Long.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#INT8 INT8}</td>
 * <td>Signed 8-bit integer.</td>
 * <td>byte or Byte.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#INT16 INT16}</td>
 * <td>Signed 16-bit integer.</td>
 * <td>short or Short.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#INT32 INT32}</td>
 * <td>Signed 32-bit integer.</td>
 * <td>int or Integer.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#INT64 INT64}</td>
 * <td>Signed 64-bit integer.</td>
 * <td>long or Long.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#FLOAT32 FLOAT32}</td>
 * <td>32-bit floating point number.</td>
 * <td>float or Float.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#FLOAT64 FLOAT64}</td>
 * <td>64-bit floating point number.</td>
 * <td>double or Double.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#DECIMAL DECIMAL}</td>
 * <td>Decimal number with a signed 64-bit mantissa and signed 8-bit exponent.</td>
 * <td>BigDecimal.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#BOOLEAN BOOLEAN}</td>
 * <td>Boolean.</td>
 * <td>boolean or Boolean.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#ENUM ENUM}</td>
 * <td>Enumeration.</td>
 * <td>Enum, int or Integer.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#TIME TIME}</td>
 * <td>Time, date or date+time.</td>
 * <td>Date, long, Long, int or Integer.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#BIGINT BIGINT}</td>
 * <td>Big integer with unlimited precision.</td>
 * <td>BigInteger.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#BIGDECIMAL BIGDECIMAL}</td>
 * <td>Decimal number with a signed unlimited mantissa and signed 32-bit exponent.</td>
 * <td>BigDecimal.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#STRING STRING}</td>
 * <td>String (Unicode is the default charset).</td>
 * <td>String.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#BINARY BINARY}</td>
 * <td>Binary data.</td>
 * <td>byte[].</td>
 * </tr>
 * <tr>
 * <td>{@link Type#REFERENCE REFERENCE}</td>
 * <td>Static reference to a named type or group.</td>
 * <td>Depends on the reference.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#DYNAMIC_REFERENCE DYNAMIC_REFERENCE}</td>
 * <td>Dynamic reference to a named type or group, including any subclasses.</td>
 * <td>Depends on the reference.</td>
 * </tr>
 * <tr>
 * <td>{@link Type#SEQUENCE SEQUENCE}</td>
 * <td>Sequence. A sequence of sequence is not allowed.</td>
 * <td>java.util.List or array of the component type.</td>
 * </tr>
 * </table>
 *
 * @author mikael.brannstrom
 *
 */
public abstract class TypeDef {
    /** Type enumeration of TypeDef. */
    public static enum Type {
        /** Unsigned 8-bit integer. Java type: byte or Byte.
         * @see TypeDef#UINT8
         */
        UINT8,
        /** Unsigned 16-bit integer. Java type: short or Short.
         * @see TypeDef#UINT16
         */
        UINT16,
        /** Unsigned 32-bit integer. Java type: int or Integer.
         * @see TypeDef#UINT32
         */
        UINT32,
        /** Unsigned 64-bit integer. Java type: long or Long.
         * @see TypeDef#UINT64
         */
        UINT64,
        /** Signed 8-bit integer. Java type: byte or Byte.
         * @see TypeDef#INT8
         */
        INT8,
        /** Signed 16-bit integer. Java type: short or Short.
         * @see TypeDef#INT16
         */
        INT16,
        /** Signed 32-bit integer. Java type: int or Integer.
         * @see TypeDef#INT32
         */
        INT32,
        /** Signed 64-bit integer. Java type: long or Long.
         * @see TypeDef#INT64
         */
        INT64,
        /** 32-bit floating point number. Java type: float or Float.
         * @see TypeDef#FLOAT32
         */
        FLOAT32,
        /** 64-bit floating point number. Java type: double or Double.
         * @see TypeDef#FLOAT64
         */
        FLOAT64,
        /** Decimal number with a signed 64-bit mantissa and signed 8-bit exponent. Java type: BigDecimal.
         * @see TypeDef#DECIMAL
         */
        DECIMAL,
        /** Boolean. Java type: boolean or Boolean.
         * @see TypeDef#BOOLEAN
         */
        BOOLEAN,
        /** Enumeration. Java type: Enum, int or Integer.
         * @see TypeDef.Enum
         */
        ENUM,
        /** Time, date or date+time. Java type: Date, long, Long, int or Integer.
         * @see TypeDef.Time
         */
        TIME,
        /** Big integer with unlimited precision. Java type: BigInteger.
         * @see TypeDef#BIGINT
         */
        BIGINT,
        /** Decimal number with a signed unlimited mantissa and signed 32-bit exponent. Java type: BigDecimal.
         * @see TypeDef#BIGDECIMAL
         */
        BIGDECIMAL,
        /** String (Unicode is the default charset). Java type: String.
         * @see TypeDef.StringUnicode
         * @see TypeDef#STRING
         */
        STRING,
        /** Binary data. Java type: byte[].
         * @see TypeDef.Binary
         * @see TypeDef#BINARY
         */
        BINARY,
        /** Static reference to a named type or group.
         * @see TypeDef.Reference
         */
        REFERENCE,
        /** Dynamic reference to a named type or group, including any subclasses.
         * @see TypeDef.DynamicReference
         */
        DYNAMIC_REFERENCE,
        /** Sequence. A sequence of sequence is not allowed. Java type: java.util.List or array.
         * @see TypeDef.Sequence
         */
        SEQUENCE,
    }

    public static final TypeDef UINT8 = new Simple(Type.UINT8, Byte.class, "u8", MetaTypeDef.UINT8);
    public static final TypeDef UINT16 = new Simple(Type.UINT16, Short.class, "u16", MetaTypeDef.UINT16);
    public static final TypeDef UINT32 = new Simple(Type.UINT32, Integer.class, "u32", MetaTypeDef.UINT32);
    public static final TypeDef UINT64 = new Simple(Type.UINT64, Long.class, "u64", MetaTypeDef.UINT64);

    public static final TypeDef INT8 = new Simple(Type.INT8, Byte.class, "i8", MetaTypeDef.INT8);
    public static final TypeDef INT16 = new Simple(Type.INT16, Short.class, "i16", MetaTypeDef.INT16);
    public static final TypeDef INT32 = new Simple(Type.INT32, Integer.class, "i32", MetaTypeDef.INT32);
    public static final TypeDef INT64 = new Simple(Type.INT64, Long.class, "i64", MetaTypeDef.INT64);

    public static final TypeDef FLOAT32 = new Simple(Type.FLOAT32, Float.class, "f32", MetaTypeDef.FLOAT32);
    public static final TypeDef FLOAT64 = new Simple(Type.FLOAT64, Double.class, "f64", MetaTypeDef.FLOAT64);

    public static final TypeDef BIGINT = new Simple(Type.BIGINT, BigInteger.class, "bigInt", MetaTypeDef.BIGINT);
    public static final TypeDef BIGDECIMAL = new Simple(Type.BIGDECIMAL, BigDecimal.class, "bigDecimal",
            MetaTypeDef.BIGDECIMAL);

    public static final TypeDef DECIMAL = new Simple(Type.DECIMAL, BigDecimal.class, "decimal", MetaTypeDef.DECIMAL);
    public static final TypeDef BOOLEAN = new Simple(Type.BOOLEAN, Boolean.class, "boolean", MetaTypeDef.BOOLEAN);
    public static final TypeDef OBJECT = new DynamicReference(null);

    public static final TypeDef.StringUnicode STRING = new StringUnicode();
    public static final TypeDef.Binary BINARY = new Binary();
    public static final TypeDef DATETIME_MILLIS_UTC =
            new Time(TimeUnit.MILLISECONDS, Epoch.UNIX, TimeZone.getTimeZone("UTC"));
    public static final TypeDef DATETIME_MILLIS = new Time(TimeUnit.MILLISECONDS, Epoch.UNIX, null);
    public static final TypeDef TIME_MILLIS = new Time(TimeUnit.MILLISECONDS, Epoch.MIDNIGHT, null);
    public static final TypeDef DATE = new Time(TimeUnit.DAYS, Epoch.UNIX, null);

    private final Type type;

    private TypeDef(Type type) {
        this.type = type;
    }

    /** Returns the type.
     *
     * @return the type, not null.
     */
    public Type getType() {
        return type;
    }

    public abstract MetaTypeDef toMessage();

    public abstract Class<?> getDefaultJavaType();
    public Class<?> getDefaultJavaComponentType() {
        return null;
    }

    public static boolean isDecimal(BigDecimal value) {
        int exp = -value.scale();
        if (exp < -128 || exp > 127) {
            return false;
        }
        BigInteger bigMantissa = value.unscaledValue();
        if(bigMantissa.bitLength() > 63) {
            return false;
        }
        return true;
    }
    
    public static BigDecimal checkDecimal(BigDecimal value) throws IllegalArgumentException {
        int exp = -value.scale();
        if (exp < -128 || exp > 127) {
            throw new IllegalArgumentException("Decimal exponent out of range [-128, 127]: " + exp);
        }
        BigInteger bigMantissa = value.unscaledValue();
        if(bigMantissa.bitLength() > 63) {
            throw new IllegalArgumentException("Decimal unscaled value too large: " + bigMantissa);
        }
        return value;
    }

    /** Simple no-arg type.
     * @see Type
     */
    public static class Simple extends TypeDef {
        private final Class<?> defaultJavaType;
        private final java.lang.String string;
        private final MetaTypeDef message;
        private Simple(Type type, Class<?> defaultJavaType, java.lang.String string, MetaTypeDef message) {
            super(type);
            this.defaultJavaType = defaultJavaType;
            this.string = string;
            this.message = message;
        }
        @Override
        public java.lang.String toString() {
            return string;
        }

        @Override
        public MetaTypeDef toMessage() {
            return message;
        }
        @Override
        public Class<?> getDefaultJavaType() {
            return defaultJavaType;
        }
        @Override
        public boolean equals(Object obj) {
            return obj == this; // private constructor, no need to check more
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

    }

    // --- parameterized ---

    /**
     * String type.
     *
     * The string can be configured with a (unsigned) max size in bytes.
     */
    public static class StringUnicode extends TypeDef {
        private final int maxSize;

        /**
         * @param maxSize the max size as an unsigned integer in bytes, or -1 for unlimited.
         */
        public StringUnicode(int maxSize) {
            super(Type.STRING);
            this.maxSize = maxSize;
        }
        /**
         * Create a string with no (unlimited) max size.
         */
        public StringUnicode() {
            this(-1);
        }
        /**
         * Returns the max size.
         * The value -1 (0xffffffff) is interpreted as unlimited.
         * @return the max size in bytes, as an unsigned integer.
         */
        public int getMaxSize() {
            return maxSize;
        }
        /**
         * Returns true if there is a max size.
         * @return true if there is a max size, false for unlimited.
         */
        public boolean hasMaxSize() {
            return maxSize != -1;
        }
        @Override
        public MetaTypeDef toMessage() {
            return new MetaTypeDef.MetaString(maxSize != -1 ? maxSize : null);
        }
        @Override
        public Class<?> getDefaultJavaType() {
            return String.class;
        }
        @Override
        public String toString() {
            return hasMaxSize() ?
                    "string (" + (0xffffffffL & maxSize) + ")" :
                    "string";
        }
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + this.maxSize;
            return hash;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final StringUnicode other = (StringUnicode) obj;
            return this.maxSize == other.maxSize;
        }
    }

    /**
     * Binary type.
     *
     * The binary can be configured with a (unsigned) max size in bytes.
     */
    public static class Binary extends TypeDef {
        private final int maxSize;

        /**
         * @param maxSize the max size as an unsigned integer in bytes, or -1 for unlimited.
         */
        public Binary(int maxSize) {
            super(Type.BINARY);
            this.maxSize = maxSize;
        }
        /**
         * Create a string with no (unlimited) max size.
         */
        public Binary() {
            this(-1);
        }
        /**
         * Returns the max size.
         * The value -1 (0xffffffff) is interpreted as unlimited.
         * @return the max size in bytes, as an unsigned integer.
         */
        public int getMaxSize() {
            return maxSize;
        }
        /**
         * Returns true if there is a max size.
         * @return true if there is a max size, false for unlimited.
         */
        public boolean hasMaxSize() {
            return maxSize != -1;
        }
        @Override
        public MetaTypeDef toMessage() {
            return new MetaTypeDef.MetaBinary(maxSize != -1 ? maxSize : null);
        }

        @Override
        public Class<?> getDefaultJavaType() {
            return byte[].class;
        }
        @Override
        public String toString() {
            return hasMaxSize() ?
                    "binary (" + (0xffffffffL & maxSize) + ")" :
                    "binary";
        }
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + this.maxSize;
            return hash;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Binary other = (Binary) obj;
            return this.maxSize == other.maxSize;
        }
    }

    /**
     * Time type.
     *
     * The time type is configured with <em>unit</em>, <em>epoch</em> and an optional <em>time zone</em>.
     * An UTC timestamp in milliseconds can be expressed as {@link TimeUnit#MILLISECONDS}, {@link Epoch#UNIX} and
     * time zone "UTC".
     * Similary a local date can be expressed as {@link TimeUnit#DAYS} and {@link Epoch#UNIX}.
     * Time of day can be expressed as {@link TimeUnit#SECONDS} and {@link Epoch#MIDNIGHT}.
     *
     * @see Type#TIME
     */
    public static class Time extends TypeDef {
        private final TimeUnit unit;
        private final Epoch epoch;
        private final TimeZone timeZone;
        /**
         * @param unit the time unit, not null.
         * @param epoch the epoch, not null.
         * @param timeZone the optional time zone, or null for unspecified.
         */
        public Time(TimeUnit unit, Epoch epoch, TimeZone timeZone) {
            super(Type.TIME);
            if (unit == null) {
                throw new NullPointerException("unit");
            }
            if (epoch == null) {
                throw new NullPointerException("epoch");
            }
            this.unit = unit;
            this.epoch = epoch;
            this.timeZone = timeZone;
        }

        /** Returns the time unit.
         * @return the unit, not null.
         */
        public TimeUnit getUnit() {
            return unit;
        }
        /** Returns the epoch.
         * @return the epoch, not null.
         */
        public Epoch getEpoch() {
            return epoch;
        }
        /** Returns the time zone.
         * @return the timeZone, or null for unspecified/local.
         */
        public TimeZone getTimeZone() {
            return timeZone;
        }
        @Override
        public java.lang.String toString() {
            StringBuilder str = new StringBuilder();
            str.append("time(");
            switch(unit) {
            case NANOSECONDS:
                str.append("nanos");
                break;
            case MICROSECONDS:
                str.append("micros");
                break;
            case MILLISECONDS:
                str.append("millis");
                break;
            case SECONDS:
                str.append("seconds");
                break;
            case MINUTES:
                str.append("minutes");
                break;
            case HOURS:
                str.append("hours");
                break;
            case DAYS:
                str.append("days");
                break;
            default:
                str.append(unit.toString());
                break;
            }
            switch(epoch) {
            case MIDNIGHT:
                str.append(",midnight");
                break;
            case UNIX:
                str.append(",unix");
                break;
            case Y2K:
                str.append(",y2k");
                break;
            default:
                str.append(",").append(epoch.toString());
                break;
            }
            if (timeZone != null) {
                str.append(",").append(timeZone.getID());
            }
            str.append(")");
            return str.toString();
        }

        @Override
        public MetaTypeDef toMessage() {
            return new MetaTypeDef.MetaTime(unit, epoch, timeZone != null ? timeZone.getID() : null);
        }

        @Override
        public Class<?> getDefaultJavaType() {
            return Long.class;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(Time.class)) {
                return false;
            }
            Time other = (Time) obj;
            return Objects.equals(unit, other.unit) &&
                Objects.equals(epoch, other.epoch) &&
                Objects.equals(timeZone, other.timeZone);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unit, epoch, timeZone);
        }

    }

    // --- parameterized ---

    /** Sequence type.
     *
     * The component type must be specified.
     * A sequence of sequence is not allowed, instead a group must be put in between.
     *
     * @see Type#SEQUENCE
     */
    public static class Sequence extends TypeDef {
        private final TypeDef componentType;

        public Sequence(TypeDef componentType) {
            super(Type.SEQUENCE);
            this.componentType = Objects.requireNonNull(componentType, "componentType");
        }

        /** Returns the type of the elements in this sequence.
         *
         * @return the component type, not null.
         */
        public TypeDef getComponentType() {
            return componentType;
        }
        @Override
        public java.lang.String toString() {
            return componentType.toString() + "[]";
        }

        @Override
        public MetaTypeDef toMessage() {
            return new MetaTypeDef.MetaSequence(componentType.toMessage());
        }

        @Override
        public Class<?> getDefaultJavaType() {
            return List.class;
        }

        @Override
        public Class<?> getDefaultJavaComponentType() {
            return componentType.getDefaultJavaType();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(Sequence.class)) {
                return false;
            }
            Sequence other = (Sequence) obj;
            return Objects.equals(componentType, other.componentType);
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }

    }

    /** Enumeration type.
     * @see Type#ENUM
     */
    public static class Enum extends TypeDef {
        private final List<Symbol> symbols;
        public Enum(Collection<Symbol> symbols) {
            super(Type.ENUM);
            this.symbols = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(symbols, "symbols")));
        }
        /** Returns the symbols of this enumeration.
         *
         * @return the symbols, not null.
         */
        public List<Symbol> getSymbols() {
            return symbols;
        }
        @Override
        public java.lang.String toString() {
            StringBuilder str = new StringBuilder();
            for (Symbol symbol : symbols) {
                if (str.length() != 0) {
                    str.append(" | ");
                }
                str.append(symbol.toString());
            }
            return str.toString();
        }
        @Override
        public MetaTypeDef toMessage() {
            ArrayList<MetaTypeDef.MetaSymbol> msgSymbols = new ArrayList<>(symbols.size());
            for (Symbol symbol : symbols) {
                msgSymbols.add(symbol.toMessage());
            }
            return new MetaTypeDef.MetaEnum(msgSymbols);
        }

        @Override
        public Class<?> getDefaultJavaType() {
            return Integer.class;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(Enum.class)) {
                return false;
            }
            Enum other = (Enum) obj;
            return Objects.equals(symbols, other.symbols);
        }

        @Override
        public int hashCode() {
            return symbols.hashCode();
        }

    }

    /** Reference type. */
    public static abstract class Ref extends TypeDef {
        private final java.lang.String refType;

        private Ref(Type pType, java.lang.String refType) {
            super(pType);
            this.refType = refType;
        }

        /** 
         * Returns the referenced type name.
         *
         * @return the named type or group, or null for any group (dynamic references only).
         */
        public java.lang.String getRefType() {
            return refType;
        }
        @Override
        public java.lang.String toString() {
            return refType == null ? "object" : refType;
        }
        @Override
        public Class<?> getDefaultJavaType() {
            return Object.class;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(getClass())) {
                return false;
            }
            Ref other = (Ref) obj;
            return Objects.equals(refType, other.refType);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(refType);
        }
    }

    /** Static reference type.
     * A static reference refers to the single type.
     * @see Type#REFERENCE
     */
    public static class Reference extends Ref {
        /** Create a static reference to named type or group.
         *
         * @param refType the named type or group, not null.
         */
        public Reference(java.lang.String refType) {
            super(Type.REFERENCE, Objects.requireNonNull(refType));
        }
        @Override
        public MetaTypeDef toMessage() {
            return new MetaTypeDef.MetaRef(getRefType());
        }
    }

    /** Dynamic reference type.
     * A dynamic reference refers to a group type and all sub classes of that type.
     * @see Type#DYNAMIC_REFERENCE
     */
    public static class DynamicReference extends Ref {
        /** Create a dynamic reference to a named type or group.
         *
         * @param refType the named type or group, or null to match any group.
         */
        public DynamicReference(java.lang.String refType) {
            super(Type.DYNAMIC_REFERENCE, refType);
        }
        @Override
        public java.lang.String toString() {
            return super.toString() + "*";
        }
        @Override
        public MetaTypeDef toMessage() {
            return new MetaTypeDef.MetaDynRef(getRefType());
        }
    }

    // --- misc ---

    /** Enumeration symbol. */
    public static class Symbol {
        private final java.lang.String name;
        private final int id;

        /** Create an enumeration symbol.
         *
         * @param name the symbol name, unique within the enumeration, not null.
         * @param id the symbol id, unique within the enumeration.
         */
        public Symbol(java.lang.String name, int id) {
            this.name = Objects.requireNonNull(name);
            this.id = id;
        }
        /** Returns the name of the symbol.
         *
         * @return the name, not null.
         */
        public java.lang.String getName() {
            return name;
        }
        /** Returns the id of the symbol.
         *
         * @return the id.
         */
        public int getId() {
            return id;
        }
        @Override
        public java.lang.String toString() {
            return name + "/" + id;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(Symbol.class)) {
                return false;
            }
            Symbol other = (Symbol) obj;
            return id == other.id && name.equals(other.name);
        }
        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
        public MetaTypeDef.MetaSymbol toMessage() {
            return new MetaTypeDef.MetaSymbol(name, id);
        }
    }
}
