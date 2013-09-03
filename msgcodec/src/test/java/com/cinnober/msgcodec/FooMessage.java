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

import com.cinnober.msgcodec.Epoch;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 * @author mikael.brannstrom
 *
 */
@Id(100)
public class FooMessage {
    private byte myByte;
    private short myShort;
    private int myInteger;
    private long myLong;

    private byte myByteU;
    private short myShortU;
    private int myIntegerU;
    private long myLongU;

    private float myFloat;
    private double myDouble;
    private boolean myBoolean;

    private Byte myByteObj;
    private Short myShortObj;
    private Integer myIntegerObj;
    private Long myLongObj;

    private Byte myByteUObj;
    private Short myShortUObj;
    private Integer myIntegerUObj;
    private Long myLongUObj;

    private Float myFloatObj;
    private Double myDoubleObj;
    private Boolean myBooleanObj;

    private String myString;
    private String myStringReq;

    private byte[] myBinary;
    private byte[] myBinaryReq;

    private BigDecimal myDecimal;
    private BigDecimal myDecimalReq;

    private Object myAnything;
    private Object myAnythingReq;

    private Date dateTimestamp;
    private long utcMillis;
    private long utcNanos;
    private long businessDate;
    private long localTimeMillis;

    @Id(1)
    public byte getMyByte() {
        return myByte;
    }
    public void setMyByte(byte myByte) {
        this.myByte = myByte;
    }
    @Id(2)
    public short getMyShort() {
        return myShort;
    }
    public void setMyShort(short myShort) {
        this.myShort = myShort;
    }
    @Id(3)
    public int getMyInteger() {
        return myInteger;
    }
    public void setMyInteger(int myInteger) {
        this.myInteger = myInteger;
    }
    @Id(4)
    public long getMyLong() {
        return myLong;
    }
    public void setMyLong(long myLong) {
        this.myLong = myLong;
    }
    @Unsigned
    @Id(5)
    public byte getMyByteU() {
        return myByteU;
    }
    public void setMyByteU(byte myByteU) {
        this.myByteU = myByteU;
    }
    @Unsigned
    @Id(6)
    public short getMyShortU() {
        return myShortU;
    }
    public void setMyShortU(short myShortU) {
        this.myShortU = myShortU;
    }
    @Unsigned
    @Id(7)
    public int getMyIntegerU() {
        return myIntegerU;
    }
    public void setMyIntegerU(int myIntegerU) {
        this.myIntegerU = myIntegerU;
    }
    @Unsigned
    @Id(8)
    public long getMyLongU() {
        return myLongU;
    }
    public void setMyLongU(long myLongU) {
        this.myLongU = myLongU;
    }
    @Id(9)
    public float getMyFloat() {
        return myFloat;
    }
    public void setMyFloat(float myFloat) {
        this.myFloat = myFloat;
    }
    @Id(10)
    public double getMyDouble() {
        return myDouble;
    }
    public void setMyDouble(double myDouble) {
        this.myDouble = myDouble;
    }
    @Id(11)
    public boolean isMyBoolean() {
        return myBoolean;
    }
    public void setMyBoolean(boolean myBoolean) {
        this.myBoolean = myBoolean;
    }
    @Id(12)
    public Byte getMyByteObj() {
        return myByteObj;
    }
    public void setMyByteObj(Byte myByteObj) {
        this.myByteObj = myByteObj;
    }
    @Id(13)
    public Short getMyShortObj() {
        return myShortObj;
    }
    public void setMyShortObj(Short myShortObj) {
        this.myShortObj = myShortObj;
    }
    @Id(14)
    public Integer getMyIntegerObj() {
        return myIntegerObj;
    }
    public void setMyIntegerObj(Integer myIntegerObj) {
        this.myIntegerObj = myIntegerObj;
    }
    @Id(15)
    public Long getMyLongObj() {
        return myLongObj;
    }
    public void setMyLongObj(Long myLongObj) {
        this.myLongObj = myLongObj;
    }
    @Unsigned
    @Id(16)
    public Byte getMyByteUObj() {
        return myByteUObj;
    }
    public void setMyByteUObj(Byte myByteUObj) {
        this.myByteUObj = myByteUObj;
    }
    @Unsigned
    @Id(17)
    public Short getMyShortUObj() {
        return myShortUObj;
    }
    public void setMyShortUObj(Short myShortUObj) {
        this.myShortUObj = myShortUObj;
    }
    @Unsigned
    @Id(18)
    public Integer getMyIntegerUObj() {
        return myIntegerUObj;
    }
    public void setMyIntegerUObj(Integer myIntegerUObj) {
        this.myIntegerUObj = myIntegerUObj;
    }
    @Unsigned
    @Id(19)
    public Long getMyLongUObj() {
        return myLongUObj;
    }
    public void setMyLongUObj(Long myLongUObj) {
        this.myLongUObj = myLongUObj;
    }
    @Id(20)
    public Float getMyFloatObj() {
        return myFloatObj;
    }
    public void setMyFloatObj(Float myFloatObj) {
        this.myFloatObj = myFloatObj;
    }
    @Id(21)
    public Double getMyDoubleObj() {
        return myDoubleObj;
    }
    public void setMyDoubleObj(Double myDoubleObj) {
        this.myDoubleObj = myDoubleObj;
    }
    @Id(22)
    public Boolean getMyBooleanObj() {
        return myBooleanObj;
    }
    public void setMyBooleanObj(Boolean myBooleanObj) {
        this.myBooleanObj = myBooleanObj;
    }
    @Id(23)
    public String getMyString() {
        return myString;
    }
    public void setMyString(String myString) {
        this.myString = myString;
    }
    @Required
    @Id(24)
    public String getMyStringReq() {
        return myStringReq;
    }
    public void setMyStringReq(String myStringReq) {
        this.myStringReq = myStringReq;
    }
    @Id(25)
    public byte[] getMyBinary() {
        return myBinary;
    }
    public void setMyBinary(byte[] myBinary) {
        this.myBinary = myBinary;
    }
    @Required
    @Id(26)
    public byte[] getMyBinaryReq() {
        return myBinaryReq;
    }
    public void setMyBinaryReq(byte[] myBinaryReq) {
        this.myBinaryReq = myBinaryReq;
    }
    @Id(27)
    public BigDecimal getMyDecimal() {
        return myDecimal;
    }
    public void setMyDecimal(BigDecimal myDecimal) {
        this.myDecimal = myDecimal;
    }
    @Required
    @Id(28)
    public BigDecimal getMyDecimalReq() {
        return myDecimalReq;
    }
    public void setMyDecimalReq(BigDecimal myDecimalReq) {
        this.myDecimalReq = myDecimalReq;
    }
    @Id(29)
    public Object getMyAnything() {
        return myAnything;
    }
    public void setMyAnything(Object myAnything) {
        this.myAnything = myAnything;
    }
    @Required
    @Id(30)
    public Object getMyAnythingReq() {
        return myAnythingReq;
    }
    public void setMyAnythingReq(Object myAnythingReq) {
        this.myAnythingReq = myAnythingReq;
    }
    @Id(31) // default: Date -> UTC timestamp millis
    public Date getDateTimestamp() {
        return dateTimestamp;
    }
    public void setDateTimestamp(Date dateTimestamp) {
        this.dateTimestamp = dateTimestamp;
    }

    @Id(32)
    @Time // UTC timestamp millis
    public long getUtcMillis() {
        return utcMillis;
    }
    public void setUtcMillis(long utcMillis) {
        this.utcMillis = utcMillis;
    }
    @Id(33)
    @Time(unit=TimeUnit.NANOSECONDS)
    public long getUtcNanos() {
        return utcNanos;
    }
    public void setUtcNanos(long utcNanos) {
        this.utcNanos = utcNanos;
    }
    @Id(34)
    @Time(unit=TimeUnit.DAYS, timeZone="")
    public long getBusinessDate() {
        return businessDate;
    }
    public void setBusinessDate(long businessDate) {
        this.businessDate = businessDate;
    }
    @Id(35)
    @Time(epoch=Epoch.MIDNIGHT, timeZone="")
    public long getLocalTimeMillis() {
        return localTimeMillis;
    }
    public void setLocalTimeMillis(long localTimeMillis) {
        this.localTimeMillis = localTimeMillis;
    }



}
