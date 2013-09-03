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
 * Extracts the group type from a group instance.
 * The group type corresponds to the value in {@link GroupBinding#getGroupType()}
 *
 * @author mikael.brannstrom
 */
public abstract class GroupTypeAccessor {
    /** Returns the group type of the specified group value.
     *
     * @param groupValue the group value, not null.
     * @return the group type.
     */
    public abstract Object getGroupType(Object groupValue);

    /** A group type accessor that uses the java class as a group type.
     * Useful when each groups have been bound to a separate java class.
     */
    public static class JavaClass extends GroupTypeAccessor {
        /**
         * Returns <code>groupValue.getClass()</code>.
         */
        @Override
        public Object getGroupType(Object groupValue) {
            return groupValue.getClass();
        }
    }

    /** A group type accessor that uses the group name as group type.
     * This accessor only works with instances of {@link Group} objects
     * for group values.
     *
     * @author mikael.brannstrom
     */
    public static class GroupName extends GroupTypeAccessor {
        @Override
        public Object getGroupType(Object groupValue) {
            return ((Group) groupValue).getGroupName();
        }

    }
}
