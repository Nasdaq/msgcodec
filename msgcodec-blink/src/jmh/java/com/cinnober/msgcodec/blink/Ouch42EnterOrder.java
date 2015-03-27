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
    @Id(1)
    @Annotate("maxLength=14")
    @Required
    public String token;
    @Id(2)
    @Unsigned
    public byte buySell;
    @Id(3)
    @Unsigned
    public int shares;
    @Id(4)
    @Annotate("maxLength=8")
    @Required
    public String stock;
    @Id(5)
    @Unsigned
    public int price;
    @Id(6)
    @Unsigned
    public int timeInForce;
    @Id(7)
    @Annotate("maxLength=4")
    @Required
    public String firm;
    @Id(8)
    @Unsigned
    public byte display;
    @Id(9)
    @Unsigned
    public byte capacity;
    @Id(10)
    @Unsigned
    public byte intermarketSweep;
    @Id(11)
    @Unsigned
    public int minimumQuantity;
    @Id(12)
    @Unsigned
    public byte crossType;
    @Id(13)
    @Unsigned
    public byte customerType;


}
