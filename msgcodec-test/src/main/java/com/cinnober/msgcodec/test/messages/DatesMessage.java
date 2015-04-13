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
    @Id(36)
    @Time(unit=TimeUnit.DAYS, timeZone="")
    public Date dateDate;


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
        msg.dateDate = new Date(0L);

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
        msg.businessDate = 15993L;
        msg.localTimeMillis = 12 * 3600 * 1000 + 123;
        msg.dateDate = new Date(1381795200000L);

        return messages;
    }
}
