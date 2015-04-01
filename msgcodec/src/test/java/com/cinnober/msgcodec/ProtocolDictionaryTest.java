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

package com.cinnober.msgcodec;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikael.brannstrom
 */
public class ProtocolDictionaryTest {

    public ProtocolDictionaryTest() {
    }

    @Test
    public void testEqualsAndHashCode() {
        Schema dict1 = new SchemaBuilder().build(FooMessage.class, BarMessage.class);
        Schema dict2 = new SchemaBuilder().build(FooMessage.class, BarMessage.class);

        assertEquals(dict1, dict2);
        assertEquals(dict1.hashCode(), dict2.hashCode());
        assertNotEquals(dict1.getUID(), dict2.getUID());
    }

}
