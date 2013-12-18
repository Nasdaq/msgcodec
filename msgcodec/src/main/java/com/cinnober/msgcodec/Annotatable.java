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

import java.util.Map;

/**
 * An (immutable) object that can be annotated with string name-value pairs.
 *
 * @author mikael.brannstrom
 * @param <T> the type of the annotatable class, used as return type.
 *
 */
public interface Annotatable<T> {
    /**
     * Replace all annotations in this object with the specified annotations.
     *
     * @param annotations the annotations.
     * @return a new copy of this object, with the specified annotations set.
     */
    T replaceAnnotations(Annotations annotations);

    /**
     * Add the specified annotations to this object.
     * Any duplicate annotations will be replaced.
     *
     * @param annotations the annotations.
     * @return a new copy of this object, with the specified annotations added.
     */
    T addAnnotations(Annotations annotations);

    /**
     * Returns the annotation value for the specified annotation name.
     *
     * @param name the annotation name, not null.
     * @return the annotation value, or null if not found.
     */
    String getAnnotation(String name);

    /**
     * Get all annotations as an un-modifiable map.
     *
     * @return a map of annotation name-value pairs, not null.
     */
    Map<String, String> getAnnotations();
}
