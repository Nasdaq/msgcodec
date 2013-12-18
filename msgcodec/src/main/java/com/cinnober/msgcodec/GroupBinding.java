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

import java.util.Objects;

/**
 * Group binding.
 * GroupBinding is immutable.
 *
 *
 * @author mikael.brannstrom
 *
 */
public class GroupBinding {
    private final Object groupType;
    private final Factory<?> factory;

    /** Create a group binding.
     * @param factory the group factory, not null.
     * @param groupType the group key, not null.
     */
    public GroupBinding(Factory<?> factory, Object groupType) {
        this.factory = Objects.requireNonNull(factory);
        this.groupType = Objects.requireNonNull(groupType);
    }

    /** Returns the group factory.
     *
     * @return the factory, not null.
     */
    public Factory<?> getFactory() {
        return factory;
    }

    /** Returns the group type.
     *groupType
     * @return the group type, not null.
     * @see GroupTypeAccessor
     */
    public Object getGroupType() {
        return groupType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(GroupBinding.class)) {
            return false;
        }
        GroupBinding other = (GroupBinding) obj;
        return
            Objects.equals(factory, other.factory) &&
            Objects.equals(groupType, other.groupType);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.groupType);
        hash = 43 * hash + Objects.hashCode(this.factory);
        return hash;
    }
}
