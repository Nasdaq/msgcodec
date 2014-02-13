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
 * The field accessor is an accessor for an ignored field.
 * IgnoreAccessor is immutable. The ignore accessor is mostly useful for decoding.
 * During encoding null values will be used, which might not be valid if the field is required.
 *
 * Msgcodec implementations may check if an accessor is <code>instanceof IgnoreAccessor</code>
 * and generate optimized code in that case.
 *
 * @author mikael.brannstrom
 */
public final class IgnoreAccessor<O, V> implements Accessor<O, V> {

    private static final IgnoreAccessor INSTANCE = new IgnoreAccessor();

    /**
     * Returns an ignore accessor.
     * @param <O> the object type, that contains the field.
     * @param <V> the field value type.
     * @return the ignore accessor, not null.
     */
    @SuppressWarnings("unchecked")
    public static <O,V> IgnoreAccessor<O, V> instance() {
        return INSTANCE;
    }

    /**
     * Bind the specified field to be ignored during decoding.
     * @param field the field to be bound, not null.
     * @return the bound field, not null.
     */
    public static FieldDef bindField(FieldDef field) {
        return field.bind(new FieldBinding(instance(), field.getType().getDefaultJavaType(),
                field.getType().getDefaultJavaComponentType()));
    }

    /**
     * Create a new ignore accessor.
     */
    private IgnoreAccessor() {
    }

    /**
     * Returns the value, which is always null.
     * @param obj the object, here ignored.
     * @return null.
     */
    @Override
    public V getValue(O obj) {
        return null;
    }

    /**
     * Set the value, which is ignored.
     * @param obj the object, here ignored.
     * @param value the value, here ignored.
     */
    @Override
    public void setValue(Object obj, Object value) {
        // ignore
    }

    // equals and hashCode can be inherited from Object, since we have a single instance.
}
