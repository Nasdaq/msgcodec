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
