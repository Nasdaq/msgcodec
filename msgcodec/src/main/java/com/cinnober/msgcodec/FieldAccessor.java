/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 The MsgCodec Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
