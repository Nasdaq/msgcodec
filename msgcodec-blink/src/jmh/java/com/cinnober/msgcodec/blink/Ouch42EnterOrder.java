/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 *
 * @author mikael.brannstrom
 */
@Id('O')
@Name("EnterOrder")
public class Ouch42EnterOrder extends MsgObject {
    @Annotate("maxLength=14")
    @Required
    String token;
    @Unsigned
    byte buySell;
    @Unsigned
    int shares;
    @Annotate("maxLength=8")
    @Required
    String stock;
    @Unsigned
    int price;
    @Unsigned
    int timeInForce;
    @Annotate("maxLength=4")
    @Required
    String firm;
    @Unsigned
    byte display;
    @Unsigned
    byte capacity;
    @Unsigned
    byte intermarketSweep;
    @Unsigned
    int minimumQuantity;
    @Unsigned
    byte crossType;
    @Unsigned
    byte customerType;


}
