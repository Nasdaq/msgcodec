/*
 * Copyright (c) 2014 Cinnober Financial Technology AB, Stockholm,
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
