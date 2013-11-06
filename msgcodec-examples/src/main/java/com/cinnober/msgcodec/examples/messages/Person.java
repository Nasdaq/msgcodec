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

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;

/**
 * @author Mikael Brannstrom
 *
 */
@Id(3)
public class Person extends MsgObject {
    @Id(1)
    @Required
    public String name;
    @Id(2)
    @Dynamic // <- this is a dynamic reference, i.e. can contain refer to a subclass of person
    public Person mom;
    @Id(3)
    @Dynamic // <- this is a dynamic reference, i.e. can contain refer to a subclass of person
    public Person dad;
}