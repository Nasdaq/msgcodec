/*
 * $Id: codetemplates.xml,v 1.4 2006/04/05 12:25:17 maal Exp $
 *
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
