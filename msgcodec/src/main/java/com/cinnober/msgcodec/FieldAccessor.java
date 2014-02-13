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

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * The field accessor is an accessor backed by a {@link java.lang.reflect.Field}.
 * FieldAccessor is immutable.
 *
 * Msgcodec implementations may check if an accessor is <code>instanceof FieldAccessor</code>
 * and generate optimized code in that case.
 *
 * @author mikael.brannstrom
 */
public final class FieldAccessor implements Accessor<Object, Object> {
    private final Field field;

    /**
     * Create a new field accessor.
     * @param field the field, not null.
     */
    public FieldAccessor(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    public Object getValue(Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new Error("Should not happen", e);
        }
    }

    @Override
    public void setValue(Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new Error("Should not happen", e);
        }
    }

    /**
     * Returns the underlying field.
     * @return the underlying field, not null.
     */
    public Field getField() {
        return field;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.field);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldAccessor other = (FieldAccessor) obj;
        return Objects.equals(this.field, other.field);
    }

    
}
