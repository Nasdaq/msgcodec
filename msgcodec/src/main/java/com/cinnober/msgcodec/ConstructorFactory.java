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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * The constructor factory is a factory backed by a no-arg constructor.
 * ConstructorFactory is immutable.
 *
 * Msgcodec implementations may check if a factory is <code>instanceof ConstructorFactory</code>
 * and generate optimized code in that case.
 *
 * @author mikael.brannstrom
 * @param <T> the type of object to create.
 */
public final class ConstructorFactory<T> implements Factory<T> {
    private final Constructor<T> constructor;

    /**
     * Create a new constructor factory.
     * @param constructor the no-arg constructor, not null.
     */
    public ConstructorFactory(Constructor<T> constructor) {
        if (constructor.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Expected no-arg constructor");
        }
        this.constructor = constructor;
        this.constructor.setAccessible(true);
    }

    @Override
    public T newInstance() {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new ObjectInstantiationException("Cannot instantiate abstract class " +
                    constructor.getDeclaringClass().getCanonicalName(), e);
        } catch (IllegalAccessException e) {
            throw new Error("Should not happen", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Constructor throwed exception", e);
        }
    }

    /**
     * Returns the underlying no-arg constructor.
     * @return the underlying no-arg constructor, not null.
     */
    public Constructor<T> getConstructor() {
        return constructor;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.constructor);
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
        final ConstructorFactory<?> other = (ConstructorFactory<?>) obj;
        return Objects.equals(this.constructor, other.constructor);
    }

    
}
