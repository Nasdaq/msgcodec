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
package com.cinnober.msgcodec.test.messages;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;

/**
 * @author mikael.brannstrom
 *
 */
@Id(106)
public class MiscMessage extends MsgObject {
    @Id(1)
    public boolean myBoolean;
    @Id(2)
    public Boolean myBooleanObj;

    @Id(3)
    public Object myAny;

    @Id(4) @Dynamic
    public Person myDynPerson;
    @Id(5)
    public Person myStatPerson;

    @Id(6)
    @Required
    public Object myAnyReq;
    @Id(7)
    @Required @Dynamic
    public Person myDynPersonReq;
    @Id(8)
    @Required
    public Person myStatPersonReq;
}
