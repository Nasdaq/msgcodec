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

/**
 * An accessor enables getting and setting of a field value in an object.
 * 
 * <p>Conceptually this can be seen as a getter/setter pair.
 * 
 * @author mikael.brannstrom
 *
 * @param <O> the object type, that contains the field.
 * @param <V> the field value type.
 */
public interface Accessor<O, V> {
    /**
     * Returns the value for the field in the specified object.
     * @param obj the object to get the field value from, not null.
     * @return the field value.
     */
    V getValue(O obj);

    /**
     * Set the value for the field in the spcecified object.
     * @param obj the object to set the field value in, not null.
     * @param value the field value.
     */
    void setValue(O obj, V value);
}
