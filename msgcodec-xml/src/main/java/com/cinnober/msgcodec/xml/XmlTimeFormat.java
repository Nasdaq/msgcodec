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
public abstract class XmlTimeFormat<T> implements XmlFormat<T> {

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

    public static class UInt64TimeFormat extends XmlTimeFormat<Long> {

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

    public static class UInt32TimeFormat extends XmlTimeFormat<Integer> {

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
