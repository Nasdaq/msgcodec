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

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mikael.brannstrom
 *
 */
abstract class TimeFormat {
    abstract String format(long value);
    abstract long parse(String str);

    static void formatUnixDate(int daysSinceUnixEpoch, StringBuilder appendTo) {
        // TODO: optimize this
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        appendTo.append(sdf.format(new Date(24L * 3600 * 1000 * daysSinceUnixEpoch)));
    }

    static int parseUnixDate(String str, ParsePosition position) throws ParseException {
        // TODO: optimize this
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateObj = sdf.parse(str, position);
        return (int) (dateObj.getTime() / (24L * 3600 * 1000));
    }

    static void formatHours(int hours, StringBuilder appendTo) {
        formatInt(wrap(hours, 24), 2, appendTo);
    }
    static void formatMinutes(int minutes, StringBuilder appendTo) {
        formatInt(wrap(minutes, 60), 2, appendTo);
    }
    static void formatSeconds(int seconds, StringBuilder appendTo) {
        formatInt(wrap(seconds, 60), 2, appendTo);
    }
    static void formatMillis(int millis, StringBuilder appendTo) {
        formatInt(millis, 3, appendTo);
    }
    static void formatMicros(int micros, StringBuilder appendTo) {
        formatInt(micros, 6, appendTo);
    }
    static void formatNanos(int nanos, StringBuilder appendTo) {
        formatInt(nanos, 9, appendTo);
    }

    static int wrap(int value, int wrap) {
        if (0 <= value && value < wrap) {
            return value; // optimize for common case
        }
        value = value % wrap;
        if (value < 0) {
            value += wrap;
        }
        return value;
    }

    static void formatInt(int value, int digits, StringBuilder appendTo) {
        // PENDING: optimize
        String str = Integer.toString(value);
        int prefixCount = digits - str.length();
        if (prefixCount < 0) {
            appendTo.append(str.substring(str.length() + prefixCount));
        } else {
            for (int i=0; i<prefixCount; i++) {
                appendTo.append('0');
            }
            appendTo.append(str);
        }
    }


}
