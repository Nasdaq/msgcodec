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
package com.cinnober.msgcodec;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 * @author mikael.brannstrom
 *
 */
@Id(100)
public class FooMessage extends MsgObject {
    @Id(1)
    public byte myByte;
    @Id(2)
    public short myShort;
    @Id(3)
    public int myInteger;
    @Id(4)
    public long myLong;

    @Unsigned
    @Id(5)
    public byte myByteU;
    @Unsigned
    @Id(6)
    public short myShortU;
    @Unsigned
    @Id(7)
    public int myIntegerU;
    @Unsigned
    @Id(8)
    public long myLongU;

    @Id(9)
    public float myFloat;
    @Id(10)
    public double myDouble;
    @Id(11)
    public boolean myBoolean;

    @Id(12)
    public Byte myByteObj;
    @Id(13)
    public Short myShortObj;
    @Id(14)
    public Integer myIntegerObj;
    @Id(15)
    public Long myLongObj;

    @Unsigned
    @Id(16)
    public Byte myByteUObj;
    @Unsigned
    @Id(17)
    public Short myShortUObj;
    @Unsigned
    @Id(18)
    public Integer myIntegerUObj;
    @Unsigned
    @Id(19)
    public Long myLongUObj;

    @Id(20)
    public Float myFloatObj;
    @Id(21)
    public Double myDoubleObj;
    @Id(22)
    public Boolean myBooleanObj;

    @Id(23)
    public String myString;
    @Required
    @Id(24)
    public String myStringReq;

    @Id(25)
    public byte[] myBinary;
    @Required
    @Id(26)
    public byte[] myBinaryReq;

    @Id(27)
    public BigDecimal myDecimal;
    @Required
    @Id(28)
    public BigDecimal myDecimalReq;

    @Id(29)
    public Object myAnything;
    @Required
    @Id(30)
    public Object myAnythingReq;

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
