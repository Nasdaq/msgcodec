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
package com.cinnober.msgcodec.anot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the name of the group, field or name.
 *
 * <dl>
 * <dt>Group</dt><dd>The name of a group must be unique within the protocol dictionary.
 * The default value is the unqualified name of the class.</dd>
 *
 * <dt>Field</dt><dd>The id of a field must be unique within a group, including any super groups.
 * The default value is extracted from the the method name according to the Java Beans property
 * naming convention. Annotate a get or set method.</dd>
 *
 * <dt>Enum constant</dt><dd>The name must be unique within the enum definition.
 * The default is the enum constant name.</dd>
 *
 * </dl>
 *
 * @author mikael.brannstrom
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Name {
    /** The name. */
    String value();
}
