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
package com.cinnober.msgcodec.examples.messages;

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
public class Numbers {
    @Id(1)
    private int signedReq;
    @Id(2)
    @Unsigned // <- negative numbers are interpreted as the high values, e.g. -1 = 2^32-1
    private int unsignedReq;
    @Id(3)
    private Integer signed;
    @Id(4)
    @Unsigned
    private Integer unsigned;

    @Id(5)
    @SmallDecimal // <- not big decimal, limited to 64-bit integer as mantissa
    private BigDecimal decimal;
    @Id(6)
    private BigDecimal bigDecimal;
    @Id(7)
    private BigInteger bigInt;
    @Id(8)
    @Required // <- make it required
    private BigInteger bigIntReq;
    public int getSignedReq() {
        return signedReq;
    }
    public void setSignedReq(int signedReq) {
        this.signedReq = signedReq;
    }
    public int getUnsignedReq() {
        return unsignedReq;
    }
    public void setUnsignedReq(int unsignedReq) {
        this.unsignedReq = unsignedReq;
    }
    public Integer getSigned() {
        return signed;
    }
    public void setSigned(Integer signed) {
        this.signed = signed;
    }
    public Integer getUnsigned() {
        return unsigned;
    }
    public void setUnsigned(Integer unsigned) {
        this.unsigned = unsigned;
    }
    public BigDecimal getDecimal() {
        return decimal;
    }
    public void setDecimal(BigDecimal decimal) {
        this.decimal = decimal;
    }
    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }
    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }
    public BigInteger getBigInt() {
        return bigInt;
    }
    public void setBigInt(BigInteger bigInt) {
        this.bigInt = bigInt;
    }
    public BigInteger getBigIntReq() {
        return bigIntReq;
    }
    public void setBigIntReq(BigInteger bigIntReq) {
        this.bigIntReq = bigIntReq;
    }
}
