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
