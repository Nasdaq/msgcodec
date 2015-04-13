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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.Epoch;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for time field of type Date.
 * Conversion between time units and epochs.
 *
 * <p>To convert between time values sent on the wire and time values used in the date class, see:
 * <pre>
 * wireTime = (dateTime - epochOffset) / timeUnitInMillis);
 * dateTime = wireTime * timeUnitInMillis + epochOffset;
 * </pre>
 *
 * 
 * @author mikael.brannstrom
 */
class DateUtil {
    static long getTimeInMillis(TimeUnit unit) {
        switch (unit) {
        case MILLISECONDS:
        case SECONDS:
        case MINUTES:
        case HOURS:
        case DAYS:
            return unit.toMillis(1L);
        default:
            throw new IllegalArgumentException("Date does not support " + unit);
        }
    }
    static long getEpochOffset(Epoch epoch) {
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

}
