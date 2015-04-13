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

import java.util.List;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;


/**
 * Component block used in the {@link EnterDeal} message that contains
 * member information about one side in a deal. The information contains
 * identities of the trading and clearing member as well as instructions on
 * which accounts to book the Trade objects created from the deal.
 *
 * @author hannes, Cinnober Financial Technology
 */
public class IncomingTradeSide extends MsgObject {
    /**
     * A optional reference set by the trading member to backtrack the trade to
     * an order at the trading venue.
     */
    public String clientOrderId;

    /**
     * The allocations to accounts. The allocated quantities must sum up to the trade quantity.
     */
    @Required @Sequence(TradeDestination.class)
    public List<TradeDestination> allocations;
}
