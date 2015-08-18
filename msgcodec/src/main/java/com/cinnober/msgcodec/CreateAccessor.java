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

/**
 * The field accessor is an accessor for an creating a default field.
 * CreateAccessor is immutable. 
 *
 * The "inverse" of ignoreAccessor, for cases where we want to create 
 * a default value for a field
 *
 */
public final class CreateAccessor<O, V> implements Accessor<O, V> {

    private final Field field;
    
    @SuppressWarnings("rawtypes")
//    private static final CreateAccessor INSTANCE = new CreateAccessor();

    /**
     * Returns an ignore accessor.
     * @param <O> the object type, that contains the field.
     * @param <V> the field value type.
     * @return the ignore accessor, not null.
     */
//    @SuppressWarnings("unchecked")
//    public static <O,V> CreateAccessor<O, V> instance() {
//        return INSTANCE;
//    }

    /**
     * Bind the specified field to be ignored during decoding.
     * @param field the field to be bound, not null.
     * @return the bound field, not null.
     */
//    public static FieldDef bindField(FieldDef field) {
//        return field.bind(new FieldBinding(instance(), field.getType().getDefaultJavaType(),
//                field.getType().getDefaultJavaComponentType()));
//    }

    
    public FieldDef bindField(FieldDef field) {
        return field.bind(new FieldBinding(this, field.getType().getDefaultJavaType(),
                field.getType().getDefaultJavaComponentType()));
    }
    
    /**
     * Create a new ignore accessor.
     */
    public CreateAccessor(Field field) {
        this.field = field;
    }

    /**
     * Returns the underlying field.
     * @return the underlying field, not null.
     */
    public Field getField() {
        return field;
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
