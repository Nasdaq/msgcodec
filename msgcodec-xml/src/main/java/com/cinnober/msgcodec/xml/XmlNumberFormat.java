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
package com.cinnober.msgcodec.xml;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * @author mikael.brannstrom
 *
 */
public abstract class XmlNumberFormat<T> implements XmlFormat<T> {

    public static final Int8NumberFormat INT8 = new Int8NumberFormat();
    public static final Int16NumberFormat INT16 = new Int16NumberFormat();
    public static final Int32NumberFormat INT32 = new Int32NumberFormat();
    public static final Int64NumberFormat INT64 = new Int64NumberFormat();

    public static final UInt8NumberFormat UINT8 = new UInt8NumberFormat();
    public static final UInt16NumberFormat UINT16 = new UInt16NumberFormat();
    public static final UInt32NumberFormat UINT32 = new UInt32NumberFormat();
    public static final UInt64NumberFormat UINT64 = new UInt64NumberFormat();

    public static final BigIntNumberFormat BIGINT = new BigIntNumberFormat();
    public static final FloatNumberFormat FLOAT32 = new FloatNumberFormat();
    public static final DoubleNumberFormat FLOAT64 = new DoubleNumberFormat();
    public static final DecimalNumberFormat DECIMAL = new DecimalNumberFormat();
    public static final BigDecimalNumberFormat BIGDECIMAL = new BigDecimalNumberFormat();

    protected String formatInt(long value) {
        return Long.toString(value);
    }
    protected long parseInt(String str) {
        return Long.parseLong(str);
    }
    protected long parseUInt(String str) throws FormatException {
        if (str.startsWith("-")) {
            throw new FormatException("Expected unsigned value: " + str);
        }
        return Long.parseLong(str);
    }
    protected String formatBigInt(BigInteger value) {
        return value.toString();
    }
    protected BigInteger parseBigInt(String str) {
        return new BigInteger(str);
    }
    protected float parseFloat(String str) {
        return Float.parseFloat(str);
    }
    protected double parseDouble(String str) {
        return Double.parseDouble(str);
    }
    protected String formatFloat(float value) {
        return Float.toString(value);
    }
    protected String formatDouble(double value) {
        return Double.toString(value);
    }
    protected String formatBigDecimal(BigDecimal value) {
        return value.toString();
    }
    protected BigDecimal parseBigDecimal(String str) {
        return new BigDecimal(str);
    }


    public static class Int64NumberFormat extends XmlNumberFormat<Long> {
        @Override
        public String format(Long value) {
            return formatInt(value);
        }
        @Override
        public Long parse(String str) throws FormatException {
            return parseInt(str);
        }
    }
    public static class Int32NumberFormat extends XmlNumberFormat<Integer> {
        @Override
        public String format(Integer value) {
            return formatInt(value);
        }
        @Override
        public Integer parse(String str) throws FormatException {
            return (int) parseInt(str);
        }
    }
    public static class Int16NumberFormat extends XmlNumberFormat<Short> {
        @Override
        public String format(Short value) {
            return formatInt(value);
        }
        @Override
        public Short parse(String str) throws FormatException {
            return (short) parseInt(str);
        }
    }
    public static class Int8NumberFormat extends XmlNumberFormat<Byte> {
        @Override
        public String format(Byte value) {
            return formatInt(value);
        }
        @Override
        public Byte parse(String str) throws FormatException {
            return (byte) parseInt(str);
        }
    }
    public static class UInt64NumberFormat extends XmlNumberFormat<Long> {
        @Override
        public String format(Long valueObj) {
            long value = valueObj.longValue();
            if (value >= 0) {
                return formatInt(value);
            } else {
                return formatBigInt(BigInteger.valueOf(value & 0x7fffffffffffffffL).setBit(63));
            }
        }
        @Override
        public Long parse(String str) throws FormatException {
            return parseUInt(str);
        }
    }

    public static class UInt32NumberFormat extends XmlNumberFormat<Integer> {
        @Override
        public String format(Integer value) {
            return formatInt(0xffffffffL & value);
        }
        @Override
        public Integer parse(String str) throws FormatException {
            return (int) parseUInt(str);
        }
    }
    public static class UInt16NumberFormat extends XmlNumberFormat<Short> {
        @Override
        public String format(Short value) {
            return formatInt(0xffffL & value);
        }
        @Override
        public Short parse(String str) throws FormatException {
            return (short) parseUInt(str);
        }
    }
    public static class UInt8NumberFormat extends XmlNumberFormat<Byte> {
        @Override
        public String format(Byte value) {
            return formatInt(0xff & value);
        }
        @Override
        public Byte parse(String str) throws FormatException {
            return (byte) parseUInt(str);
        }
    }

    public static class BigIntNumberFormat extends XmlNumberFormat<BigInteger> {
        @Override
        public String format(BigInteger value) {
            return formatBigInt(value);
        }
        @Override
        public BigInteger parse(String str) throws FormatException {
            return parseBigInt(str);
        }
    }

    public static class FloatNumberFormat extends XmlNumberFormat<Float> {
        @Override
        public String format(Float value) {
            return formatFloat(value);
        }
        @Override
        public Float parse(String str) throws FormatException {
            return parseFloat(str);
        }
    }
    public static class DoubleNumberFormat extends XmlNumberFormat<Double> {
        @Override
        public String format(Double value) {
            return formatDouble(value);
        }
        @Override
        public Double parse(String str) throws FormatException {
            return parseDouble(str);
        }
    }
    public static class DecimalNumberFormat extends XmlNumberFormat<BigDecimal> {
        @Override
        public String format(BigDecimal value) {
            return formatBigDecimal(value);
        }
        @Override
        public BigDecimal parse(String str) throws FormatException {
            return parseBigDecimal(str);
        }
    }
    public static class BigDecimalNumberFormat extends XmlNumberFormat<BigDecimal> {
        @Override
        public String format(BigDecimal value) {
            return formatBigDecimal(value);
        }
        @Override
        public BigDecimal parse(String str) throws FormatException {
            return parseBigDecimal(str);
        }
    }

}
