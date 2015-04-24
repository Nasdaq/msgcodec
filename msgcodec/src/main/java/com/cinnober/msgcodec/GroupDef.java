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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.cinnober.msgcodec.messages.MetaFieldDef;
import com.cinnober.msgcodec.messages.MetaGroupDef;

/**
 * Definition of a group. A group is a message or a component contained in another message.
 * A group is represented by a Java Object.
 * GroupDef is immutable.
 *
 * <p>A group has a string name and a optional numeric id. Both must be unique within a {@link Schema}.
 *
 * @author mikael.brannstrom
 *
 */
public class GroupDef implements Annotatable<GroupDef> {
    private final String name;
    /**
     * -1 means unspecified.
     */
    private final int id;
    private final String superGroup;
    private final List<FieldDef> fields;
    private final Map<String, String> annotations;
    private final GroupBinding binding;
    private BindingStatus bindingStatus;

    /**
     * Create a group definition.
     *
     * @param name the group name, not null.
     * @param id the group id, or -1 if unspecified.
     * @param superGroup the name of the super group, or null if none.
     * @param fields the fields declared in this group, not null.
     * @param annotations the annotations, or null
     * @param binding the group binding, or null
     */
    public GroupDef(String name, int id, String superGroup,
            List<FieldDef> fields, Map<String, String> annotations,
            GroupBinding binding) {
        this.name = Objects.requireNonNull(name);
        this.id = id;
        this.superGroup = superGroup;
        this.fields = Collections.unmodifiableList(Objects.requireNonNull(fields));
        if (annotations == null || annotations.isEmpty()) {
            this.annotations = Collections.emptyMap();
        } else{
            this.annotations = Collections.unmodifiableMap(new LinkedHashMap<>(annotations));
        }
        this.binding = binding;
    }

    /**
     * Returns the group name.
     *
     * @return the name, not null.
     */
    public String getName() {
        return name;
    }

    /** Returns the group id.
     *
     * @return the id, or -1 if unspecified.
     */
    public int getId() {
        return id;
    }

    /** Returns the name of the super group, which this groups inherits from.
     *
     * @return the name of the super group, or null if this group has no super group.
     */
    public String getSuperGroup() {
        return superGroup;
    }

    /**
     * @return the binding
     */
    public GroupBinding getBinding() {
        return binding;
    }

    /**
     * Returns true if this group is completely bound.
     * @return true if this group is completely bound, otherwise false.
     */
    public boolean isBound() {
        return getBindingStatus() == BindingStatus.BOUND;
    }

    /**
     * Returns true if this group is completely unbound.
     * @return true if this group is completely unbound, otherwise false.
     */
    public boolean isUnbound() {
        return getBindingStatus() == BindingStatus.UNBOUND;
    }

    BindingStatus getBindingStatus() {
        if (bindingStatus == null) {
            BindingStatus status = binding != null ? BindingStatus.BOUND : BindingStatus.UNBOUND;
            for (FieldDef field : fields) {
                status = status.combine(field.getBindingStatus());
            }
            bindingStatus = status;
        }
        return bindingStatus;
    }

    /** Bind this group using the specified binding.
    *
    * @param groupBinding the new binding, not null.
    * @param fieldBindings the field bindings by field name, not null.
    * @return the bound group.
    */
    public GroupDef bind(GroupBinding groupBinding, Map<String, FieldBinding> fieldBindings) {
        List<FieldDef> newFields = new ArrayList<>(fields.size());
        for (FieldDef field : fields) {
            FieldBinding fieldBinding = fieldBindings.get(field.getName());
            if (fieldBinding == null) {
                throw new IllegalArgumentException("Missing field binding for field " + field.getName());
            }
            newFields.add(field.bind(fieldBinding));
        }
        return new GroupDef(name, id, superGroup, newFields, annotations, Objects.requireNonNull(groupBinding));
    }

    /**
     * Remove any binding from this group.
     *
     * @return the unbound group.
     */
    public GroupDef unbind() {
        if (isUnbound()) {
            return this;
        }
        List<FieldDef> newFields = new ArrayList<>(fields.size());
        for (FieldDef field : fields) {
            newFields.add(field.unbind());
        }
        return new GroupDef(name, id, superGroup, newFields, annotations, null);
    }

    /**
     * Returns the group type of this group.
     *
     * @return the group type, not null.
     */
    public Object getGroupType() {
        return binding != null ? binding.getGroupType() : null;
    }

    /**
     * Returns the group factory.
     *
     * @return the factory, not null.
     */
    public Factory<?> getFactory() {
        return binding != null ? binding.getFactory() : null;
    }

    /** Returns the declared fields of this group. Inherited fields are not included.
     *
     * @return the fields, not null.
     */
    public List<FieldDef> getFields() {
        return fields;
    }

    /**
     * Returns a human readable string representation of this group definition.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if(!annotations.isEmpty()){
            for (Entry<String, String> ann : annotations.entrySet()) {
                str.append(Annotations.toString(ann.getKey(), ann.getValue()));
                str.append('\n');
            }
        }
        str.append(name);
        if (id != -1) {
            str.append("/").append(id);
        }
        if (superGroup != null) {
            str.append(" : ").append(superGroup);
        }
        if (!fields.isEmpty()) {
            str.append(" -> ");

            boolean first = true;
            for (FieldDef field : fields) {
                if (first) {
                    first = false;
                } else {
                    str.append(",");
                }
                str.append("\n  ").append(field.toString());
            }
        }

        str.append("\n");
        return str.toString();
    }

    public MetaGroupDef toMessage() {
        List<MetaFieldDef> msgFields = new ArrayList<>(fields.size());
        for (FieldDef field : fields) {
            msgFields.add(field.toMessage());
        }
        MetaGroupDef message = new MetaGroupDef(name, id != -1 ? id : null, superGroup, msgFields);
        message.setAnnotations(annotations.isEmpty() ? null : annotations);
        return message;
    }

    /**
     * Replace all annotations in this object with the specified annotations.
     *
     * @param annotations the annotations.
     * @return a new copy of this object, with the specified annotations set.
     */
    @Override
    public GroupDef replaceAnnotations(Annotations annotations){
        List<FieldDef> newFields = new ArrayList<>(fields.size());
        for(FieldDef f : fields){
            newFields.add(f.replaceAnnotations(annotations.path(f.getName())));
        }
        return new GroupDef(name, id, superGroup, newFields, annotations.map(), binding);
    }

    @Override
    public GroupDef addAnnotations(Annotations annotations) {
        List<FieldDef> newFields = new ArrayList<>(fields.size());
        for(FieldDef f : fields){
            newFields.add(f.addAnnotations(annotations.path(f.getName())));
        }
        Map<String, String> newAnnotations = new HashMap<>(this.annotations);
        newAnnotations.putAll(annotations.map());
        return new GroupDef(name, id, superGroup, newFields, newAnnotations, binding);
    }

    /**
     * Returns the annotation value for the specified annotation name.
     *
     * @param name the annotation name, not null.
     * @return the annotation value, or null if not found.
     */
    @Override
    public String getAnnotation(String name) {
        return annotations.get(name);
    }

    /**
     * Get all annotations as an un-modifiable map.
     *
     * @return a map of annotation name-value pairs, not null.
     */
    @Override
    public Map<String, String> getAnnotations() {
        return annotations;
    }

    /**
     * Returns declared field with specified name.
     * @param fieldName the field name, not null.
     * @return the requested field, or null if this group has no field with specified name.
     */
    public FieldDef getField(String fieldName) {
        for (FieldDef f : fields) {
            if (f.getName().equals(fieldName)){
                return f;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(GroupDef.class)) {
            return false;
        }
        GroupDef other = (GroupDef) obj;
        return id == other.id &&
            Objects.equals(name, other.name) &&
            Objects.equals(superGroup, other.superGroup) &&
            Objects.equals(fields, other.fields) &&
            Objects.equals(annotations, other.annotations) &&
            Objects.equals(binding, other.binding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, superGroup, fields, annotations, binding);
    }

}
