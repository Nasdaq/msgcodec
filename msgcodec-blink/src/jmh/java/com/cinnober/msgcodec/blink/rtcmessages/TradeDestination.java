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
package com.cinnober.msgcodec.blink.rtcmessages;

import java.math.BigDecimal;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 * <p>This structure describes on which account a trade should be booked and at what quantity. It is used
 * both to handle a leg of an incoming deal and when moving trades between accounts.
 * It also specifies whether it should open a new position or close out an existing position.</p>
 *
 *
 * @author mikael.brannstrom
 *
 */
public class TradeDestination extends MsgObject {

    /** The account to book a Trade to. */
    @Unsigned
    public long accountId;

    /** The quantity to book in the account, expressed in nominal quantity. */
    @Required
    public BigDecimal quantity;

    /** An optional free text field. The field will be set in field reference in trade history and in trade */
    public String reference;

    /**
     * Specifies whether this allocation is meant to open a new position
     * or close an old position.
     * Returns the openOrClose.
     *
     * Determines if the position should be opened or closed.
     * Note: Only open is currently supported.
     */
    @Required
    public OpenOrClose openOrClose;

    public TradeDestination() {
    }

    public TradeDestination(long accountId, BigDecimal quantity) {
        this.accountId = accountId;
        this.quantity = quantity;
        this.openOrClose = OpenOrClose.OPEN;
    }


}
