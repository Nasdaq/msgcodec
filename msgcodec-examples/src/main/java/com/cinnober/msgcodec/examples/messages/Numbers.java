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
package com.cinnober.msgcodec.examples.messages;

import com.cinnober.msgcodec.MsgObject;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.SmallDecimal;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 * @author Mikael Brannstrom
 *
 */
@Id(2)
public class Numbers extends MsgObject {
    @Id(1)
    public int signedReq;
    @Id(2)
    @Unsigned // <- negative numbers are interpreted as the high values, e.g. -1 = 2^32-1
    public int unsignedReq;
    @Id(3)
    public Integer signed;
    @Id(4)
    @Unsigned
    public Integer unsigned;

    @Id(5)
    @SmallDecimal // <- not big decimal, limited to 64-bit integer as mantissa
    public BigDecimal decimal;
    @Id(6)
    public BigDecimal bigDecimal;
    @Id(7)
    public BigInteger bigInt;
    @Id(8)
    @Required // <- make it required
    public BigInteger bigIntReq;
}
