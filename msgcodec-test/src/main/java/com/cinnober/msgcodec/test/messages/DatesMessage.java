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
package com.cinnober.msgcodec.test.messages;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Time;

/**
 * @author mikael.brannstrom
 *
 */
@Id(101)
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


    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, DatesMessage> createMessages() {
        Map<String, DatesMessage> messages = new LinkedHashMap<>();

        DatesMessage msg;

        msg = new DatesMessage();
        messages.put("clean", msg);

        msg = new DatesMessage();
        messages.put("zero", msg);
        msg.dateTimestamp = new Date(0L);

        msg = new DatesMessage();
        messages.put("border1", msg);
        msg.localTimeMillis = 1;

        msg = new DatesMessage();
        messages.put("border2", msg);
        msg.localTimeMillis = 24 * 3600 * 1000 - 1;

        msg = new DatesMessage();
        messages.put("2013-10-15 12:00:00.123456789", msg);
        // 1381856400 = 2013-10-15 12:00:00
        msg.dateTimestamp = new Date(1381856400123L);
        msg.utcMillis = 1381856400123L;
        msg.utcNanos = 1381856400123456789L;
        msg.businessDate = 1381856400L / (24 * 3600);
        msg.localTimeMillis = 12 * 3600 * 1000 + 123;

        return messages;
    }
}
