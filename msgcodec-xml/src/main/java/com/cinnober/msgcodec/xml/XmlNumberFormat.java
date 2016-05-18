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
package com.cinnober.msgcodec.xml;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * @author mikael.brannstrom
 *
 */
abstract class XmlNumberFormat<T> implements XmlFormat<T> {

    public static final Int8NumberFormat INT8 = new Int8NumberFormat();
    public static final Int16NumberFormat INT16 = new Int16NumberFormat();
    public static final Int32NumberFormat INT32 = new Int32NumberFormat();
    public static final Int64NumberFormat INT64 = new Int64NumberFormat();

    public static final UInt8NumberFormat UINT8 = new UInt8NumberFormat();
    public static final CharacterNumberFormat CHAR = new CharacterNumberFormat();
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
    public static class CharacterNumberFormat extends XmlNumberFormat<Character> {
        @Override
        public String format(Character value) {
            return formatInt(0xffffL & value);
        }
        @Override
        public Character parse(String str) throws FormatException {
            return (char) parseUInt(str);
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
