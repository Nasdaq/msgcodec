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
package com.cinnober.msgcodec.xml;

import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.util.TimeFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * @author mikael.brannstrom
 *
 */
abstract class XmlTimeFormat<T> implements XmlFormat<T> {

    private final TimeFormat timeFormat;

    protected XmlTimeFormat(TypeDef.Time type) {
        this.timeFormat = TimeFormat.getTimeFormat(type.getUnit(), type.getEpoch());
    }

    protected abstract long convertToLong(T value);
    protected abstract T convertFromLong(long timeValue);

    @Override
    public String format(T value) throws FormatException {
        return timeFormat.format(convertToLong(value));
    }

    @Override
    public T parse(String str) throws FormatException {
        try {
            return convertFromLong(timeFormat.parse(str));
        } catch (ParseException e) {
            throw new FormatException("Could not parse time", e);
        }
    }

    static class UInt64TimeFormat extends XmlTimeFormat<Long> {

        public UInt64TimeFormat(TypeDef.Time type) {
            super(type);
        }

        @Override
        protected Long convertFromLong(long timeValue) {
            return timeValue;
        }
        @Override
        protected long convertToLong(Long value) {
            return value;
        }
    }

    static class UInt32TimeFormat extends XmlTimeFormat<Integer> {

        public UInt32TimeFormat(TypeDef.Time type) {
            super(type);
        }

        @Override
        protected Integer convertFromLong(long timeValue) {
            return (int)timeValue;
        }

        @Override
        protected long convertToLong(Integer value) {
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
    static class DateTimeFormat extends XmlTimeFormat<Date> {
        private final long timeUnitInMillis;
        private final long epochOffset;
        public DateTimeFormat(TypeDef.Time type) {
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
}
