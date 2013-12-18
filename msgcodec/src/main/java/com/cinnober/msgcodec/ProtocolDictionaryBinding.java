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
 * Protocol dictionary binding.
 * ProtocolDictionaryBinding is immutable.
 *
 * @author mikael.brannstrom
 *
 */
public class ProtocolDictionaryBinding {
    private final GroupTypeAccessor groupTypeAccessor;

    /**
     * Create a protocol dictionary binding.
     *
     * @param groupTypeAccessor the group type accessor, not null
     */
    public ProtocolDictionaryBinding(GroupTypeAccessor groupTypeAccessor) {
        this.groupTypeAccessor = Objects.requireNonNull(groupTypeAccessor);
    }

    /**
     * @return the groupTypeAccessor
     */
    public GroupTypeAccessor getGroupTypeAccessor() {
        return groupTypeAccessor;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(ProtocolDictionaryBinding.class)) {
            return false;
        }
        ProtocolDictionaryBinding other = (ProtocolDictionaryBinding) obj;
        return groupTypeAccessor.equals(other.groupTypeAccessor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.groupTypeAccessor);
    }
}
