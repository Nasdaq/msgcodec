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
package com.cinnober.msgcodec.util;

import com.cinnober.msgcodec.Epoch;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Time format is a fast and thread-safe formatter and parser for time and date.
 * <p>
 * The format depends on {@link Epoch} and {@link TimeUnit}. 
 * {@link Epoch#MIDNIGHT} is represented with a time only.
 * Otherwise the following applies (see {@link java.text.SimpleDateFormat} for the syntax):
 * <table>
 * <caption>Time format for different time units.</caption>
 * <tr><th>{@link TimeUnit}</th>              <th>Format</th></tr>
 * <tr><td>{@link TimeUnit#DAYS}</td>         <td><tt>yyyy-MM-dd</tt></td></tr>
 * <tr><td>{@link TimeUnit#HOURS}</td>        <td><tt>yyyy-MM-dd HH</tt></td></tr>
 * <tr><td>{@link TimeUnit#MINUTES}</td>      <td><tt>yyyy-MM-dd HH:mm</tt></td></tr>
 * <tr><td>{@link TimeUnit#SECONDS}</td><td>  <tt>yyyy-MM-dd HH:mm:ss</tt></td></tr>
 * <tr><td>{@link TimeUnit#MILLISECONDS}</td> <td><tt>yyyy-MM-dd HH:mm:ss.SSS</tt></td></tr>
 * <tr><td>{@link TimeUnit#MICROSECONDS}</td> <td><tt>yyyy-MM-dd HH:mm:ss.SSSSSS</tt></td></tr>
 * <tr><td>{@link TimeUnit#NANOSECONDS}</td>  <td><tt>yyyy-MM-dd HH:mm:ss.SSSSSSSSS</tt></td></tr>
 * </table>
 *
 * @author mikael.brannstrom
 *
 */
public abstract class TimeFormat {

    private static final Map<Integer, TimeFormat> instances = initInstances();

    /**
     * Format a time value.
     *
     * @param value the date and/or time value to be formatted.
     * @param str the string builder to append to, not null.
     * @return the string builder, not null.
     */
    public abstract StringBuilder format(long value, StringBuilder str);
    /**
     * Format a time value.
     *
     * @param value the date and/or time value to be formatted.
     * @return the formatted value as a string, not null.
     */
    public String format(long value) {
        return format(value, new StringBuilder()).toString();
    }

    /**
     * Parse a time value.
     * @param str the string to be parsed, not null.
     * @return the date and/or time value.
     * @throws ParseException if the string could not be parsed.
     */
    public long parse(CharSequence str) throws ParseException {
        return parse(str, new ParsePosition(0));
    }
    /**
     * Parse a time value.
     * @param str the string to be parsed, not null.
     * @param pos the position to parse from, not null.
     * @return the date and/or time value.
     * @throws ParseException if the string could not be parsed.
     */
    public abstract long parse(CharSequence str, ParsePosition pos) throws ParseException;

    /**
     * Returns a time format instance for the specified combination of time unit and epoch.
     * Note: the combination {@link TimeUnit#DAYS} since {@link Epoch#MIDNIGHT} is not valid.
     *
     * @param unit the time unit, not null.
     * @param epoch the epoch, not null.
     * @return the time format, not null.
     */
    public static TimeFormat getTimeFormat(TimeUnit unit, Epoch epoch) {
        TimeFormat format = instances.get(key(unit, epoch));
        if (format == null) {
            throw new IllegalArgumentException("Illegal combination: " + unit + " since " + epoch);
        }
        return format;
    }

    private static Map<Integer, TimeFormat> initInstances() {
        Map<Integer, TimeFormat> map = new HashMap<>(TimeUnit.values().length * Epoch.values().length * 2);
        for (Epoch epoch : Epoch.values()) {
            for (TimeUnit unit : TimeUnit.values()) {
                if (epoch == Epoch.MIDNIGHT && unit == TimeUnit.DAYS) {
                    continue;
                }
                map.put(key(unit, epoch), createTimeFormat(unit, epoch));
            }
        }
        return map;
    }


    // --- PRIVATE STUFF (package private if needed for JUnit Tests) ---------------------------------------------------

    private static int key(TimeUnit unit, Epoch epoch) {
        return (unit.ordinal() << 16) + epoch.ordinal();
    }

    private static TimeFormat createTimeFormat(TimeUnit unit, Epoch epoch) {
        TimeFormat timeOnlyFormat = null;
        long timePerDay;
        switch (unit) {
            case DAYS:
                timePerDay = 1;
                break;
            case HOURS:
                timePerDay = 24;
                timeOnlyFormat = new HoursTimeFormat();
                break;
            case MINUTES:
                timePerDay = 24 * 60;
                timeOnlyFormat = new MinutesTimeFormat();
                break;
            case SECONDS:
                timePerDay = 24 * 60 * 60;
                timeOnlyFormat = new SecondsTimeFormat();
                break;
            case MILLISECONDS:
                timePerDay = 24 * 60 * 60 * 1000L;
                timeOnlyFormat = new MillisTimeFormat();
                break;
            case MICROSECONDS:
                timePerDay = 24 * 60 * 60 * 1000_000L;
                timeOnlyFormat = new MicrosTimeFormat();
                break;
            case NANOSECONDS:
                timePerDay = 24 * 60 * 60 * 1000_000_000L;
                timeOnlyFormat = new NanosTimeFormat();
                break;
            default:
                throw new Error("Unhandled case: " + unit);
        }
        switch (epoch) {
            case MIDNIGHT:
                return timeOnlyFormat;
            case UNIX:
                if (unit == TimeUnit.DAYS) {
                    return new DateOnlyFormat(0);
                } else {
                    return new DateTimeFormat(timeOnlyFormat, 0, timePerDay);
                }
            case Y2K:
                if (unit == TimeUnit.DAYS) {
                    return new DateOnlyFormat(10957);
                } else {
                    return new DateTimeFormat(timeOnlyFormat, 10957, timePerDay);
                }
            default:
                throw new Error("Unhandled case: " + epoch);
        }
    }

    static int parseUnixDate(CharSequence str, ParsePosition pos) throws ParseException {
        int yy = parseInt(str, 4, pos);
        parseCheck(str, '-', pos);
        int mm = parseInt(str, 2, pos);
        parseCheck(str, '-', pos);
        int dd = parseInt(str, 2, pos);

        long m = (mm + 9) % 12;
        long y = yy - m / 10;
        int days = (int) (365*y + y/4 - y/100 + y/400 + (m*306 + 5)/10 + (dd - 1));
        return days - 719468;
    }

    private static long parseTimeNanos(CharSequence ch, ParsePosition pos) throws ParseException {
        long seconds = parseTimeSeconds(ch, pos);
        parseCheck(ch, '.', pos);
        int nanos = parseInt(ch, 9, pos);
        return seconds * 1000_000_000L + nanos;
    }

    private static long parseTimeMicros(CharSequence ch, ParsePosition pos) throws ParseException {
        long seconds = parseTimeSeconds(ch, pos);
        parseCheck(ch, '.', pos);
        int micros = parseInt(ch, 6, pos);
        return seconds * 1000_000L + micros;
    }

    private static long parseTimeMillis(CharSequence ch, ParsePosition pos) throws ParseException {
        long seconds = parseTimeSeconds(ch, pos);
        parseCheck(ch, '.', pos);
        int millis = parseInt(ch, 3, pos);
        return seconds * 1000L + millis;
    }

    private static long parseTimeSeconds(CharSequence ch, ParsePosition pos) throws ParseException {
        long minutes = parseTimeMinutes(ch, pos);
        parseCheck(ch, ':', pos);
        int seconds = parseInt(ch, 2, pos);
        return minutes * 60L + seconds;
    }

    private static long parseTimeMinutes(CharSequence ch, ParsePosition pos) throws ParseException {
        int hours = parseInt(ch, 2, pos);
        parseCheck(ch, ':', pos);
        int minutes = parseInt(ch, 2, pos);
        return hours * 60L + minutes;
    }

    private static long parseTimeHours(CharSequence ch, ParsePosition pos) throws ParseException {
        return parseInt(ch, 2, pos);
    }

    private static int parseInt(CharSequence ch, int decimals, ParsePosition pos) throws ParseException {
        int startIndex = pos.getIndex();
        int endIndex = startIndex + decimals;
        if (endIndex > ch.length()) {
            pos.setErrorIndex(ch.length());
            throw new ParseException("Expected additional " + (endIndex - ch.length()) + " digits", pos.getErrorIndex());
        }
        int value = 0;
        for (int i=startIndex; i<endIndex; i++) {
            char c = ch.charAt(i);
            if ('0' <= c && c <= '9') {
                value = value * 10 + c - '0';
            } else {
                pos.setErrorIndex(i);
                throw new ParseException("Expected digit", pos.getErrorIndex());
            }
        }
        pos.setIndex(endIndex);
        return value;
    }

    private static void parseCheck(CharSequence ch, char expectedChar, ParsePosition pos) throws ParseException {
        if (ch.charAt(pos.getIndex()) != expectedChar) {
            pos.setErrorIndex(pos.getIndex());
            throw new ParseException("Expected '"+expectedChar+"'", pos.getErrorIndex());
        }
        pos.setIndex(pos.getIndex() + 1);
    }

    static void formatUnixDate(int timeDays, StringBuilder appendTo) {
        long days = timeDays + 719468;
        long y = (10000*days + 14780) / 3652425;
        long ddd = days - (365*y + y/4 - y/100 + y/400);
        if (ddd < 0) {
            --y;
            ddd = days - (365*y + y/4 - y/100 + y/400);
        }
        long mi = (100*ddd + 52) / 3060;
        long mm = (mi + 2) % 12 + 1;
        y = y + (mi + 2) / 12;
        long dd = ddd - (mi*306 + 5) / 10 + 1;

        formatInt((int) y, 4, appendTo);
        appendTo.append('-');
        formatInt((int) mm, 2, appendTo);
        appendTo.append('-');
        formatInt((int) dd, 2, appendTo);
    }

    private static void formatTimeNanos(long timeNanos, StringBuilder appendTo) {
        timeNanos = wrap(timeNanos, 24*3600_000_000_000L);
        int nanos = (int) wrap(timeNanos, 1000_000_000L);
        int seconds = (int) (timeNanos / 1000_000_000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        formatHours(hours, appendTo);
        appendTo.append(':');
        formatMinutes(minutes, appendTo);
        appendTo.append(':');
        formatSeconds(seconds, appendTo);
        appendTo.append('.');
        formatNanos(nanos, appendTo);
    }

    private static void formatTimeMicros(long timeMicros, StringBuilder appendTo) {
        timeMicros = wrap(timeMicros, 24*3600_000_000L);
        int micros = (int) wrap(timeMicros, 1000_000L);
        int seconds = (int) (timeMicros / 1000_000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        formatHours(hours, appendTo);
        appendTo.append(':');
        formatMinutes(minutes, appendTo);
        appendTo.append(':');
        formatSeconds(seconds, appendTo);
        appendTo.append('.');
        formatMicros(micros, appendTo);
    }

    private static void formatTimeMillis(long timeMillis, StringBuilder appendTo) {
        timeMillis = wrap(timeMillis, 24*3600_000L);
        int millis = (int) wrap(timeMillis, 1000L);
        int seconds = (int) (timeMillis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        formatHours(hours, appendTo);
        appendTo.append(':');
        formatMinutes(minutes, appendTo);
        appendTo.append(':');
        formatSeconds(seconds, appendTo);
        appendTo.append('.');
        formatMillis(millis, appendTo);
    }

    private static void formatTimeSeconds(long timeSeconds, StringBuilder appendTo) {
        int seconds = (int) timeSeconds;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        formatHours(hours, appendTo);
        appendTo.append(':');
        formatMinutes(minutes, appendTo);
        appendTo.append(':');
        formatSeconds(seconds, appendTo);
    }

    private static void formatTimeMinutes(long timeMinutes, StringBuilder appendTo) {
        int minutes = (int) timeMinutes;
        int hours = minutes / 60;
        formatHours(hours, appendTo);
        appendTo.append(':');
        formatMinutes(minutes, appendTo);
    }

    private static void formatTimeHours(long timeHours, StringBuilder appendTo) {
        int hours = (int) timeHours;
        formatHours(hours, appendTo);
    }

    private static void formatHours(int hours, StringBuilder appendTo) {
        formatInt(wrap(hours, 24), 2, appendTo);
    }
    private static void formatMinutes(int minutes, StringBuilder appendTo) {
        formatInt(wrap(minutes, 60), 2, appendTo);
    }
    private static void formatSeconds(int seconds, StringBuilder appendTo) {
        formatInt(wrap(seconds, 60), 2, appendTo);
    }
    private static void formatMillis(int millis, StringBuilder appendTo) {
        formatInt(millis, 3, appendTo);
    }
    private static void formatMicros(int micros, StringBuilder appendTo) {
        formatInt(micros, 6, appendTo);
    }
    private static void formatNanos(int nanos, StringBuilder appendTo) {
        formatInt(nanos, 9, appendTo);
    }

    private static int wrap(int value, int wrap) {
        if (0 <= value && value < wrap) {
            return value; // optimize for common case
        }
        value = value % wrap;
        if (value < 0) {
            value += wrap;
        }
        return value;
    }

    private static long wrap(long value, long wrap) {
        if (0 <= value && value < wrap) {
            return value; // optimize for common case
        }
        value = value % wrap;
        if (value < 0) {
            value += wrap;
        }
        return value;
    }

    private static char formatDecimal(int value) {
        return (char) ('0' + wrap(value, 10));
    }

    @SuppressWarnings("fallthrough")
    private static void formatInt(int value, int digits, StringBuilder appendTo) {
        switch (digits) {
            default:
            case 10:
                appendTo.append(formatDecimal(value/1000_000_000));
            case 9:
                appendTo.append(formatDecimal(value/100_000_000));
            case 8:
                appendTo.append(formatDecimal(value/10_000_000));
            case 7:
                appendTo.append(formatDecimal(value/1000_000));
            case 6:
                appendTo.append(formatDecimal(value/100_000));
            case 5:
                appendTo.append(formatDecimal(value/10_000));
            case 4:
                appendTo.append(formatDecimal(value/1000));
            case 3:
                appendTo.append(formatDecimal(value/100));
            case 2:
                appendTo.append(formatDecimal(value/10));
            case 1:
                appendTo.append(formatDecimal(value));
            case 0:
                break;
        }
    }

    private static class NanosTimeFormat extends TimeFormat {
        @Override
        public StringBuilder format(long value, StringBuilder str) {
            formatTimeNanos(value, str);
            return str;
        }
        @Override
        public long parse(CharSequence str, ParsePosition pos) throws ParseException {
            return parseTimeNanos(str, pos);
        }
    }

    private static class MicrosTimeFormat extends TimeFormat {
        @Override
        public StringBuilder format(long value, StringBuilder str) {
            formatTimeMicros(value, str);
            return str;
        }
        @Override
        public long parse(CharSequence str, ParsePosition pos) throws ParseException {
            return parseTimeMicros(str, pos);
        }
    }

    private static class MillisTimeFormat extends TimeFormat {
        @Override
        public StringBuilder format(long value, StringBuilder str) {
            formatTimeMillis(value, str);
            return str;
        }
        @Override
        public long parse(CharSequence str, ParsePosition pos) throws ParseException {
            return parseTimeMillis(str, pos);
        }
    }

    private static class SecondsTimeFormat extends TimeFormat {
        @Override
        public StringBuilder format(long value, StringBuilder str) {
            formatTimeSeconds(value, str);
            return str;
        }
        @Override
        public long parse(CharSequence str, ParsePosition pos) throws ParseException {
            return parseTimeSeconds(str, pos);
        }
    }

    private static class MinutesTimeFormat extends TimeFormat {
        @Override
        public StringBuilder format(long value, StringBuilder str) {
            formatTimeMinutes(value, str);
            return str;
        }
        @Override
        public long parse(CharSequence str, ParsePosition pos) throws ParseException {
            return parseTimeMinutes(str, pos);
        }
    }

    private static class HoursTimeFormat extends TimeFormat {
        @Override
        public StringBuilder format(long value, StringBuilder str) {
            formatTimeHours(value, str);
            return str;
        }
        @Override
        public long parse(CharSequence str, ParsePosition pos) throws ParseException {
            return parseTimeHours(str, pos);
        }
    }

    private static class DateOnlyFormat extends TimeFormat {
        private final int epochDaysSince1970;

        DateOnlyFormat(int epochDaysSince1970) {
            this.epochDaysSince1970 = epochDaysSince1970;
        }

        @Override
        public StringBuilder format(long value, StringBuilder str) {
            formatUnixDate((int) value + epochDaysSince1970, str);
            return str;
        }
        @Override
        public long parse(CharSequence str, ParsePosition pos) throws ParseException {
            return parseUnixDate(str, pos) - epochDaysSince1970;
        }
    }

    private static class DateTimeFormat extends TimeFormat {
        private final TimeFormat timeFormat;
        private final int epochDaysSince1970;
        private final long timePerDay;

        private DateTimeFormat(TimeFormat timeFormat, int epochDaysSince1970, long timePerDay) {
            this.timeFormat = timeFormat;
            this.epochDaysSince1970 = epochDaysSince1970;
            this.timePerDay = timePerDay;
        }
        
        @Override
        public StringBuilder format(long value, StringBuilder str) {
            int days = (int) (value / timePerDay + epochDaysSince1970);
            if (value < 0) {
                days--;
            }
            formatUnixDate(days, str);
            str.append(' ');
            timeFormat.format(value, str);
            return str;
        }
        @Override
        public long parse(CharSequence str, ParsePosition pos) throws ParseException {
            long days = parseUnixDate(str, pos);
            parseCheck(str, ' ', pos);
            long time = timeFormat.parse(str, pos);
            return (days - epochDaysSince1970) * timePerDay + time;
        }
    }

}
