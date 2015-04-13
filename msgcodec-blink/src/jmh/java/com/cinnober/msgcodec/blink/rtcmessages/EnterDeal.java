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
import java.util.concurrent.TimeUnit;

import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.SmallDecimal;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 * <p>Enter a deal to the clearing system.</p>
 * <p>The response is a {@link EnterDealRsp} if successful, or a {@link Error}. The request is rejected if:
 * <ul>
 * <li>The instrument does not exist.</li>
 * <li>The price is negative.</li>
 * <li>The quantity is zero or negative.</li>
 * <li>Any side lacks allocations that add up to the trade quantity.</li>
 * <li>The allocation information refers to non-existing accounts.</li>
 * <li>The clientDealId is not unique for this user and instrument.</li>
 * </ul>
 *
 * <p>If the request is accepted, trades are booked to accounts according to
 * the allocation information in the request.</p>
 *
 * @author hannes, Cinnober Financial Technology
 */
public class EnterDeal extends Request {
    /**
     * Client assigned deal id, e.g. the market place assigned trade id.
     */
    @Required
    public String clientDealId;

    /** The instrument the trade applies to. */
    @Unsigned
    public long instrumentKey;

    /** The trade price. */
    @Required @SmallDecimal
    public BigDecimal price;

    /** The trade quantity. */
    @Required
    public BigDecimal quantity;

    /** The business date the trade occurred. */
    @Time(unit=TimeUnit.DAYS, timeZone="")
    public int tradeBusinessDate;

    /** The buy side. */
    @Required
    public IncomingTradeSide buy;

    /** The sell side. */
    @Required
    public IncomingTradeSide sell;

    /** Container for custom attributes. Should be used by customer projects that wishes to add custom fields to
     * this message */
    public TradeExternalData tradeExternalData;

}
