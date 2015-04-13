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
@CustomAnnotation("FooMessage")
@Id(1001)
public class FooMessage extends MsgObject {
    @CustomAnnotation("myByte")
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
