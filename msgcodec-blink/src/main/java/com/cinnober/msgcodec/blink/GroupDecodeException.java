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

import com.cinnober.msgcodec.DecodeException;
import java.util.Objects;

/**
 * Decoding error related to a group.
 *
 * @author mikael.brannstrom
 */
public class GroupDecodeException extends DecodeException {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new group decode exception.
     * @param groupName the group name, not null.
     * @param cause the cause, not null.
     */
    public GroupDecodeException(String groupName, Throwable cause) {
        super(Objects.requireNonNull(groupName), Objects.requireNonNull(cause));
    }

    public String getGroupName() {
        return getMessage();
    }

}
