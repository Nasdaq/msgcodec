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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.cinnober.msgcodec.messages.MetaFieldDef;

/**
 * Definition of a field.
 * FieldDef is immutable.
 *
 * <p>A field has a string name and an optional numeric id. Both must be unique within a {@link GroupDef}.
 *
 * @author mikael.brannstrom
 *
 */
public class FieldDef implements Annotatable<FieldDef> {
    private final String name;
    /**
     * -1 means unspecified.
     */
    private final int id;
    private final boolean required;
    private final TypeDef type;
    private final Map<String, String> annotations;
    private final FieldBinding binding;

    /**
     * Create a new field definition.
     *
     * @param name the field name, not null.
     * @param id the field id, or -1 if unspecified.
     * @param required true if the field is required, false if optional.
     * @param type the type definition of the field, not null.
     * @param annotations the annotations of the field, or null
     * @param binding the field binding, or null of unbound
     */
    public FieldDef(String name, int id, boolean required, TypeDef type, Map<String, String> annotations,
            FieldBinding binding) {
        this.name = Objects.requireNonNull(name);
        this.id = id;
        this.type = Objects.requireNonNull(type);
        this.required = required;
        if (annotations == null || annotations.isEmpty()) {
            this.annotations = Collections.emptyMap();
        } else{
            this.annotations = Collections.unmodifiableMap(new LinkedHashMap<>(annotations));
        }
        this.binding = binding;
    }

    /** Returns the field name.
     *
     * @return the field name, not null.
     */
    public String getName() {
        return name;
    }

    /** Returns the field id.
     *
     * @return the field id, or -1 if unspecified.
     */
    public int getId() {
        return id;
    }

    /** Returns true if the field is required.
     *
     * @return true if required, false if optional.
     */
    public boolean isRequired() {
        return required;
    }

    /** Returns the type definition.
     *
     * @return the type definition, not null.
     */
    public TypeDef getType() {
        return type;
    }

    /**
     * @return the binding
     */
    public FieldBinding getBinding() {
        return binding;
    }

    public boolean isBound() {
        return binding != null;
    }

    BindingStatus getBindingStatus() {
        return binding != null ? BindingStatus.BOUND : BindingStatus.UNBOUND;
    }

    /** Bind this field using the specified binding.
     *
     * @param binding the new binding, not null.
     * @return the bound field.
     */
    public FieldDef bind(FieldBinding binding) {
        return new FieldDef(name, id, required, type, annotations, Objects.requireNonNull(binding));
    }

    /**
     * Remove any binding from this field.
     *
     * @return the unbound field.
     */
    public FieldDef unbind() {
        if (binding == null) {
            return this;
        } else {
            return new FieldDef(name, id, required, type, annotations, null);
        }
    }

    /** Returns the field accessor.
     *
     * @return the field accessor, not null.
     */
    public Accessor<?, ?> getAccessor() {
        return binding != null ? binding.getAccessor() : null;
    }

    /** Returns the Java class of this field.
     *
     * @return the Java class, not null.
     */
    public Class<?> getJavaClass() {
        return binding != null ? binding.getJavaClass() : null;
    }

    /** Returns the component Java class of this field, for sequence fields.
     *
     * @return the Java class of the sequence component, or null if not a sequence field.
     */
    public Class<?> getComponentJavaClass() {
        return binding != null ? binding.getComponentJavaClass() : null;
    }

    /** Replace all annotations in this object with the specified annotations.
     *
     * @param annotations the annotations.
     * @return a new copy of this object, with the specified annotations set.
     */
    @Override
    public FieldDef replaceAnnotations(Annotations annotations){
        return new FieldDef(name, id, required, type, annotations.map(), binding);
    }

    @Override
    public FieldDef addAnnotations(Annotations annotations) {
        Map<String, String> newAnnotations = new HashMap<>(this.annotations);
        newAnnotations.putAll(annotations.map());
        return new FieldDef(name, id, required, type, newAnnotations, binding);
    }

    /** Returns the annotation value for the specified annotation name.
     *
     * @param name the annotation name, not null.
     * @return the annotation value, or null if not found.
     */
    @Override
    public String getAnnotation(String name) {
        return annotations.get(name);
    }

    /** Get all annotations as an un-modifiable map.
     *
     * @return a map of annotation name-value pairs, not null.
     */
    @Override
    public Map<String, String> getAnnotations() {
        return annotations;
    }

    /**
     * Returns a human readable string representation of this field definition.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (!annotations.isEmpty()){
            for (Entry<String, String> annotation : annotations.entrySet()) {
                str.append(Annotations.toString(annotation.getKey(), annotation.getValue()));
                str.append("\n  ");
            }
        }
        str.append(type.toString());
        if (!required) {
            str.append("?");
        }
        str.append(" ").append(name);
        if (id != -1) {
            str.append("/").append(id);
        }
        return str.toString();
    }

    public MetaFieldDef toMessage() {
        MetaFieldDef message = new MetaFieldDef(name, id != -1 ? id : null, required, type.toMessage());
        message.setAnnotations(annotations.isEmpty() ? null : annotations);
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(FieldDef.class)) {
            return false;
        }
        FieldDef other = (FieldDef) obj;
        return id == other.id &&
                required == other.required &&
            Objects.equals(name, other.name) &&
            Objects.equals(type, other.type) &&
            Objects.equals(annotations, other.annotations) &&
            Objects.equals(binding, other.binding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, required, type, annotations, binding);
    }
}
