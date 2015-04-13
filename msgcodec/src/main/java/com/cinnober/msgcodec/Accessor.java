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

/**
 * An accessor enables getting and setting of a field value in an object.
 * 
 * <p>Conceptually this can be seen as a getter/setter pair.
 * 
 * @author mikael.brannstrom
 *
 * @param <O> the object type, that contains the field.
 * @param <V> the field value type.
 */
public interface Accessor<O, V> {
    /**
     * Returns the value for the field in the specified object.
     * @param obj the object to get the field value from, not null.
     * @return the field value.
     */
    V getValue(O obj);

    /**
     * Set the value for the field in the spcecified object.
     * @param obj the object to set the field value in, not null.
     * @param value the field value.
     */
    void setValue(O obj, V value);
}
