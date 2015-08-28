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

import com.cinnober.msgcodec.TypeDef.Type;

/**
 * The field accessor is an accessor for an creating a default field.
 * CreateAccessor is immutable. 
 *
 * The "inverse" of ignoreAccessor, for cases where we want to create 
 * a default value for a field
 *
 */
public final class CreateAccessor<O, V> implements Accessor<O, V> {

    private final FieldDef field;
    private final V value;
    
    public FieldDef bindField(FieldDef field) {
        return field.bind(new FieldBinding(this, field.getType().getDefaultJavaType(),
                field.getType().getDefaultJavaComponentType(), null));
    }
    
    /**
     * Create a new create accessor.
     * 
     * @param field The affected field
     */
    @SuppressWarnings("unchecked")
    public CreateAccessor(FieldDef field) {
        this.field = field;

        if(field.getJavaClass() == null) {
            if(field.isRequired()) {
                if(field.getType().getType() == Type.INT16 || field.getType().getType() == Type.UINT16) {
                    value = (V) Short.valueOf((short) 0);
                }
                else if(field.getType().getType() == Type.BOOLEAN) {
                    value = (V) Boolean.valueOf(false);
                }
                else if(field.getType().getType() == Type.INT8 || field.getType().getType() == Type.UINT8) {
                    value = (V) Byte.valueOf((byte) 0);
                }
                else if(field.getType().getType() == Type.INT32 || field.getType().getType() == Type.UINT32) {
                    value = (V) Integer.valueOf(0);
                }
                else if(field.getType().getType() == Type.FLOAT32) {
                    value = (V) Float.valueOf(0.0f);
                }
                else if(field.getType().getType() == Type.FLOAT64) {
                    value = (V) Double.valueOf(0.0);
                }
                else {
                    value = null;
                }
                return;
            }
            value = null;
            return;
        }
        
        if(field.getJavaClass() != null && field.getJavaClass().isPrimitive()) {
            if(field.getJavaClass() == short.class) {
                value = (V) Short.valueOf((short) 0);
            }
            else if(field.getJavaClass() == int.class) {
                value = (V) Integer.valueOf(0);
            }
            else if(field.getJavaClass() == boolean.class) {
                value = (V) Boolean.valueOf(false);
            }
            else if(field.getJavaClass() == byte.class) {
                value = (V) Byte.valueOf((byte) 0);
            }
            else if(field.getJavaClass() == double.class) {
                value = (V) Double.valueOf(0.0);
            }
            else if(field.getJavaClass() == float.class) {
                value = (V) Float.valueOf(0.0f);
            }
            else {
                value = null;
            }
            return;
        }
        value = null;
    }

    /**
     * Returns the underlying field.
     * @return the underlying field, not null.
     */
    public FieldDef getField() {
        return field;
    }
    
    /**
     * Returns the value, which is always null.
     * @param obj the object, here ignored.
     * @return null.
     */
    @Override
    public V getValue(O obj) {
        return value;
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
}
