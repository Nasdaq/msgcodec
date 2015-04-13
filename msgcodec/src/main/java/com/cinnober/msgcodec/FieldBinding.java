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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.accessor);
        hash = 79 * hash + Objects.hashCode(this.javaClass);
        hash = 79 * hash + Objects.hashCode(this.componentJavaClass);
        return hash;
    }
}
