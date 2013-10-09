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
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.cinnober.msgcodec.Epoch;

/**
 * Specifies that the field is of a time type.
 * The type of the field may be int or {@link Date}.
 *
 * <p>When applied to a sequence, the meaning of this annotation is transferred to the element type of the sequence.
 *
 * @author mikael.brannstrom
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Time {
    /** The granularity of the time. */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
    /** The epoch defining zero time. */
    Epoch epoch() default Epoch.UNIX;
    /** The time zone, or the empty string for local/unspecified time zone. */
    String timeZone() default "UTC";
}
