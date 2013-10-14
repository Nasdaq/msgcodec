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
package com.cinnober.msgcodec.test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Time;

/**
 * @author mikael.brannstrom
 *
 */
@Id(100)
public class DatesMessage extends MsgObject {
    @Id(31) // default: Date -> UTC timestamp millis
    public Date dateTimestamp;
    @Id(32)
    @Time // UTC timestamp millis
    public long utcMillis;
    @Id(33)
    @Time(unit=TimeUnit.NANOSECONDS)
    public long utcNanos;
    @Id(34)
    @Time(unit=TimeUnit.DAYS, timeZone="")
    public long businessDate;
    @Id(35)
    @Time(epoch=Epoch.MIDNIGHT, timeZone="")
    public long localTimeMillis;

}
