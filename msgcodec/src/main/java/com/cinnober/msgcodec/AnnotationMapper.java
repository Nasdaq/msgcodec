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

import java.lang.annotation.Annotation;

/**
 * The annotation mapper converts a Java annotation to a msgcodec "key=value" string annotation.
 *
 * @author mikael.brannstrom
 */
public interface AnnotationMapper<T extends Annotation> {
    /**
     * Returns a "key=value" string annotation value from the specified annotation.
     * @param annotation the Java annotation, not null.
     * @return the "key=value" string annotation value, not null.
     */
    String map(T annotation);
}
