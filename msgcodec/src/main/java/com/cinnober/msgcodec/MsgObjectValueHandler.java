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

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.util.TimeFormat;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Mikael Brannstrom
 *
 */
abstract class MsgObjectValueHandler<T> {
    static final SimpleHandler SIMPLE = new SimpleHandler();
    static final UInt8Handler UINT8 = new UInt8Handler();
    static final UInt16Handler UINT16 = new UInt16Handler();
    static final UInt32Handler UINT32 = new UInt32Handler();
    static final UInt64Handler UINT64 = new UInt64Handler();
    static final BinaryHandler BINARY = new BinaryHandler();
    static final RefGroupHandler GROUP = new RefGroupHandler();

    /**
     * Append the specified value to the string builder.
     * @param value the value, not null.
     * @param appendTo the string builder to append to, not null.
     */
    abstract void appendToString(T value, StringBuilder appendTo);
    /**
     * Check if two values are equal.
     * @param value1 value1, or null
     * @param value2 value2, or null
     * @return true if equals, otherwise false.
     */
    boolean equals(T value1, T value2) {
        return Objects.equals(value1, value2);
    }
    /**
     * Calculate the hash code for the specified value.
     * @param value the value, or null.
     * @return the hash code
     */
    int hashCode(T value) {
        return Objects.hashCode(value);
    }
    
    static class SimpleHandler extends MsgObjectValueHandler<Object> {
        @Override
        void appendToString(Object value, StringBuilder appendTo) {
            appendTo.append(value);
        }
    }
    static abstract class UnsignedIntHandler<T extends Number> extends MsgObjectValueHandler<T> {
        @Override
        @SuppressWarnings("unchecked")
        void appendToString(Number value, StringBuilder appendTo) {
            appendTo.append(format((T) value));
        }
        abstract String format(T value);
    }
    static class UInt8Handler extends UnsignedIntHandler<Byte> {
        @Override
        String format(Byte value) {
            return Integer.toString(0xff & value);
        }
    }
    static class UInt16Handler extends UnsignedIntHandler<Short> {
        @Override
        String format(Short value) {
            return Integer.toString(0xffff & value);
        }
    }
    static class UInt32Handler extends UnsignedIntHandler<Integer> {
        @Override
        String format(Integer value) {
            return Long.toString(0xffffffffL & value);
        }
    }
    static class UInt64Handler extends UnsignedIntHandler<Long> {
        @Override
        String format(Long value) {
            long val = value.longValue();
            if (val < 0) {
                return BigInteger.valueOf(val).flipBit(63).toString();
            } else {
                return Long.toString(value);
            }
        }
    }
    static class BinaryHandler extends MsgObjectValueHandler<byte[]> {
        @Override
        void appendToString(byte[] value, StringBuilder appendTo) {
            appendTo.append(ByteArrays.toHex(value));
        }

        @Override
        boolean equals(byte[] value1, byte[] value2) {
            return Arrays.equals(value2, value2);
        }

        @Override
        int hashCode(byte[] value) {
            return Arrays.hashCode(value);
        }
    }
    static abstract class TimeHandler<T> extends MsgObjectValueHandler<T> {
        private final TimeFormat timeFormat;

        public TimeHandler(Time time) {
            timeFormat = TimeFormat.getTimeFormat(unit(time), epoch(time));
        }

        protected final TimeUnit unit(Time time) {
            return time != null ? time.unit() : TimeUnit.MILLISECONDS;
        }
        protected final Epoch epoch(Time time) {
            return time != null ? time.epoch() : Epoch.UNIX;
        }

        /** Convert the value to a long value for the specified epoch and time unit. */
        protected abstract long convertToLong(T value);

        @Override
        void appendToString(T value, StringBuilder appendTo) {
            long timeValue = convertToLong(value);
            timeFormat.format(timeValue, appendTo);
        }
    }

    static class IntTimeHandler extends TimeHandler<Integer> {
        public IntTimeHandler(Time time) {
            super(time);
        }
        @Override
        protected long convertToLong(Integer value) {
            return value;
        }
    }

    static class LongTimeHandler extends TimeHandler<Long> {
        public LongTimeHandler(Time time) {
            super(time);
        }
        @Override
        protected long convertToLong(Long value) {
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
        public DateTimeHandler(Time time) {
            super(time);
            timeUnitInMillis = getTimeInMillis(unit(time));
            epochOffset = getEpochOffset(epoch(time));
        }
        @Override
        protected long convertToLong(Date value) {
            return (value.getTime()-epochOffset)/timeUnitInMillis;
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static class ListSequenceHandler extends MsgObjectValueHandler<List> {
        private final MsgObjectValueHandler componentHandler;
        public ListSequenceHandler(MsgObjectValueHandler componentHandler) {
            this.componentHandler = componentHandler;
        }
        @Override
        void appendToString(List value, StringBuilder appendTo) {
            appendTo.append("[");
            boolean comma = false;
            for (Object item : value) {
                if (comma) {
                    appendTo.append(", ");
                } else {
                    comma = true;
                }
                if (item != null) {
                    componentHandler.appendToString(item, appendTo);
                } else {
                    appendTo.append("null");
                }
            }
            appendTo.append("]");
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static class ArraySequenceHandler extends MsgObjectValueHandler<Object> {
        private final MsgObjectValueHandler componentHandler;
        public ArraySequenceHandler(MsgObjectValueHandler componentHandler) {
            this.componentHandler = componentHandler;
        }
        @Override
        void appendToString(Object value, StringBuilder appendTo) {
            appendTo.append("[");
            int length = Array.getLength(value);
            boolean comma = false;
            for (int i=0; i<length; i++) {
                if (comma) {
                    appendTo.append(", ");
                } else {
                    comma = true;
                }
                Object item = Array.get(value, i);
                if (item != null) {
                    componentHandler.appendToString(item, appendTo);
                } else {
                    appendTo.append("null");
                }
            }
            appendTo.append("]");
        }

        @Override
        boolean equals(Object value1, Object value2) {
            if (value1 == null || value2 == null) {
                return value1 == value2;
            }
            if (value1 == value2) {
                return true;
            }
            int length = Array.getLength(value1);
            if (Array.getLength(value2) != length) {
                return false;
            }
            for (int i=0; i<length; i++) {
                Object item1 = Array.get(value1, i);
                Object item2 = Array.get(value2, i);
                if (!componentHandler.equals(item1, item2)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        int hashCode(Object value) {
            int hash = 17;
            if (value != null) {
                int length = Array.getLength(value);
                for (int i=0; i<length; i++) {
                    Object item = Array.get(value, i);
                    hash = hash * 3 + componentHandler.hashCode(item);
                }
            }
            return hash;
        }

    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static class FieldHandler implements Comparable<FieldHandler> {
        private final String name;
        private final int id;
        private final Field field;
        private final MsgObjectValueHandler valueHandler;
        FieldHandler(Field field, MsgObjectValueHandler valueHandler) {
            Name nameAnot = field.getAnnotation(Name.class);
            this.name = nameAnot != null ? nameAnot.value() : field.getName();
            Id idAnot = field.getAnnotation(Id.class);
            this.id = idAnot != null ? -idAnot.value() : -1;
            this.field = field;
            field.setAccessible(true); // make it faster
            this.valueHandler = valueHandler;
        }

        Object getValue(Object group) {
            try {
                return field.get(group);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                return null;
            }
        }
        void appendToString(Object group, StringBuilder appendTo) {
            Object value = getValue(group);
            appendTo.append(name).append("=");
            if (value != null) {
                valueHandler.appendToString(value, appendTo);
            } else {
                appendTo.append("null");
            }
        }
        int hashCode(Object group) {
            return valueHandler.hashCode(getValue(group));
        }
        boolean equals(Object group1, Object group2) {
            return valueHandler.equals(getValue(group1), getValue(group2));
        }

        @Override
        public int compareTo(FieldHandler o) {
            final FieldHandler o1 = o, o2 = this;
            int id1 = o1.id;
            int id2 = o2.id;
            if (id1 == -1) {
                if (id2 == -1) {
                    return o1.name.compareTo(o2.name);
                } else {
                    return 1;
                }
            } else if (id2 == -1) {
                return -1;
            } else {
                return Long.compare(0xffffffffL & id1, 0xffffffffL & id2);
            }
        }
    }

    static class GroupHandler extends MsgObjectValueHandler<Object> {
        private final String name;
        private FieldHandler[] fields;
        GroupHandler(Class<?> groupType) {
            Name nameAnot = groupType.getAnnotation(Name.class);
            this.name = nameAnot != null ? nameAnot.value() : groupType.getSimpleName();
        }

        void init(FieldHandler[] fields) {
            this.fields = fields;
            Arrays.sort(this.fields);
        }

        @Override
        void appendToString(Object value, StringBuilder appendTo) {
            if (value == null) {
                appendTo.append("null");
            } else {
                appendTo.append(name).append(" [");
                boolean comma = false;
                for (FieldHandler field : fields) {
                    if (comma) {
                        appendTo.append(", ");
                    } else {
                        comma = true;
                    }
                    field.appendToString(value, appendTo);
                }
                appendTo.append("]");
            }
        }

        @Override
        int hashCode(Object value) {
            if (value == null) {
                return 17;
            }
            int hash = 13;
            for (FieldHandler field : fields) {
                hash = hash * 7 + field.hashCode(value);
            }
            return hash;
        }

        @Override
        boolean equals(Object value1, Object value2) {
            if (value1 == null || value2 == null) {
                return value1 == value2;
            }
            if (value1 == value2) {
                return true;
            }
            if (value1.getClass() != value2.getClass()) {
                return false;
            }
            for (FieldHandler field : fields) {
                if (!field.equals(value1, value2)) {
                    return false;
                }
            }
            return true;
        }
        Collection<FieldHandler> fields() {
            return Arrays.asList(fields);
        }
    }

    static class RefGroupHandler extends MsgObjectValueHandler<Object> {
        @Override
        void appendToString(Object value, StringBuilder appendTo) {
            appendTo.append(value.toString());
        }
    }

}
