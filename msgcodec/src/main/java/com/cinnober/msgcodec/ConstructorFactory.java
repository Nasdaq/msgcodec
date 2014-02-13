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
            throw new Error("Should not happen", e); // abstract class
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
