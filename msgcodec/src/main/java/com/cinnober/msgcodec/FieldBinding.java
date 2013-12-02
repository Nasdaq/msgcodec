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
 * Field binding.
 * FieldBinding is immutable.
 *
 * @author mikael.brannstrom
 *
 */
public class FieldBinding {
    private final Accessor<?, ?> accessor;
    private final Class<?> javaClass;
    private final Class<?> componentJavaClass;

    /**
     * Create a new field binding.
     * @param accessor the accessor of the field, not null.
     * @param javaClass the java class of the field, not null.
     * @param componentJavaClass the java class of the component if sequence, or null.
     */
    public FieldBinding(Accessor<?, ?> accessor, Class<?> javaClass,
        Class<?> componentJavaClass) {
        this.accessor = Objects.requireNonNull(accessor);
        this.javaClass = javaClass;
        this.componentJavaClass = componentJavaClass;
    }

    /** Returns the field accessor.
     *
     * @return the field accessor, not null.
     */
    public Accessor<?, ?> getAccessor() {
        return accessor;
    }

    /** Returns the Java class of this field.
     *
     * @return the Java class, not null.
     */
    public Class<?> getJavaClass() {
        return javaClass;
    }

    /** Returns the component Java class of this field, for sequence fields.
     *
     * @return the Java class of the sequence component, or null if not a sequence field.
     */
    public Class<?> getComponentJavaClass() {
        return componentJavaClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(FieldBinding.class)) {
            return false;
        }
        FieldBinding other = (FieldBinding) obj;
        return
            Objects.equals(accessor, other.accessor) &&
            Objects.equals(javaClass, other.javaClass) &&
            Objects.equals(componentJavaClass, other.componentJavaClass);
    }

}
