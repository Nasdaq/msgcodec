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

import com.cinnober.msgcodec.TypeDef.Ref;
import com.cinnober.msgcodec.messages.MetaGroupDef;
import com.cinnober.msgcodec.messages.MetaNamedType;
import com.cinnober.msgcodec.messages.MetaSchema;
import com.cinnober.msgcodec.visitor.FieldDefVisitor;
import com.cinnober.msgcodec.visitor.GroupDefVisitor;
import com.cinnober.msgcodec.visitor.NamedTypeVisitor;
import com.cinnober.msgcodec.visitor.SchemaVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The schema defines the messages of a protocol.
 * Schema is immutable.
 *
 * <p>A schema consists of a collection of message definitions ({@link GroupDef}),
 * and any named types (a mapping from String to {@link TypeDef}) that may be referred to by
 * message fields ({@link FieldDef}).
 *
 * @author mikael.brannstrom
 *
 */
public class Schema implements Annotatable<Schema> {
    private final Object UID = new Object();
    private final Map<String, NamedType> typesByName;

    private final Map<String, GroupDef> groupsByName;
    private final Map<Integer, GroupDef> groupsById;
    private final Map<Object, GroupDef> groupsByType;
    private final Collection<GroupDef> sortedGroups;
    private final Map<String, String> annotations;
    private final Map<Object,Integer> remappedClasses;

    private final SchemaBinding binding;
    private BindingStatus bindingStatus;

    /**
     * Creates a schema.
     *
     * @param groups the group definitions (the protocol messages), not null.
     * @param namedTypes any named types, or null if none.
     */
    public Schema(Collection<GroupDef> groups, Collection<NamedType> namedTypes) {
        this(groups, namedTypes, null, null);
    }

    /**
     * Creates a schema.
     *
     * @param groups the group definitions (the protocol messages), not null.
     * @param namedTypes any named types, or null if none.
     * @param binding the protocol schema binding, or null if unbound
     */
    public Schema(Collection<GroupDef> groups, Collection<NamedType> namedTypes,
        SchemaBinding binding) {
        this(groups, namedTypes, null, binding);
    }

    private boolean isSuperGroup(GroupDef parent, GroupDef child) {
        if (child.getSuperGroup() == null) {
            return false;
        }
        if (child.getSuperGroup().equals(parent.getName())) {
            return true;
        }

        // should always exist in groupsByName
        return isSuperGroup(parent, groupsByName.get(child.getSuperGroup()));
    }

    /**
     * Creates a schema.
     *
     * @param groups the group definitions (the protocol messages), not null.
     * @param namedTypes any named types, or null if none.
     * @param annotations the group annotations.
     * @param binding the protocol schema binding, or null if unbound
     */
    public Schema(Collection<GroupDef> groups, Collection<NamedType> namedTypes,
                  Map<String, String> annotations, SchemaBinding binding) {
        this(groups, namedTypes, annotations, binding, Collections.EMPTY_MAP);
    }

    /**
     * Creates a schema.
     *
     * @param groups the group definitions (the protocol messages), not null.
     * @param namedTypes any named types, or null if none.
     * @param annotations the group annotations.
     * @param binding the protocol schema binding, or null if unbound
     * @param remappedClasses altered mapping for classes that are not included in the schema (replaces a class with another)
     */
    public Schema(Collection<GroupDef> groups, Collection<NamedType> namedTypes,
        Map<String, String> annotations, SchemaBinding binding, Map<Object,Integer> remappedClasses) {
        if (annotations == null || annotations.isEmpty()) {
            this.annotations = Collections.emptyMap();
        } else{
            this.annotations = Collections.unmodifiableMap(new LinkedHashMap<>(annotations));
        }
        this.remappedClasses = Collections.unmodifiableMap(remappedClasses);
        this.binding = binding;
        if (namedTypes != null) {
            LinkedHashMap<String, NamedType> tempMap = new LinkedHashMap<>(namedTypes.size() * 2);
            for (NamedType type : namedTypes) {
                if (tempMap.put(type.getName(), type) != null) {
                    throw new IllegalArgumentException("Duplicate named type: " + type.getName());
                }
            }
            typesByName = Collections.unmodifiableMap(tempMap);
        } else {
            typesByName = Collections.emptyMap();
        }

        groupsByName = new HashMap<>(groups.size() * 2);
        groupsById = new HashMap<>(groups.size() * 2);
        groupsByType = new HashMap<>(groups.size() * 2);

        for (GroupDef group : groups) {
            if (typesByName.containsKey(group.getName())) {
                throw new IllegalArgumentException("Duplicate group/type name: " + group.getName());
            }
            if (groupsByName.put(group.getName(), group) != null) {
                throw new IllegalArgumentException("Duplicate group name: " + group.getName());
            }
            if (group.getId() != -1) {
                if (groupsById.put(group.getId(), group) != null) {
                    throw new IllegalArgumentException("Duplicate group id: " + group.getId());
                }
            }
            if (group.getGroupType() != null && group.getGroupType() != Object.class) {
                GroupDef existingGroup = groupsByType.put(group.getGroupType(), group);

                if (existingGroup != null) {
                    if (isSuperGroup(existingGroup, group)) {
                        groupsByType.put(group.getGroupType(), existingGroup);
                    } else if (isSuperGroup(group, existingGroup)) {
                        groupsByType.put(group.getGroupType(), group);
                    } else {
                        throw new IllegalArgumentException("Duplicate group type: " + group.getGroupType());
                    }

                }
            }
        }

        // validate super groups
        for (GroupDef group : groups) {
            if (group.getSuperGroup() != null) {
                GroupDef superGroup = groupsByName.get(group.getSuperGroup());
                if (superGroup == null) {
                    throw new IllegalArgumentException("Unknown super group: " + group.getSuperGroup());
                }
                if (binding != null && binding.getGroupTypeAccessor() instanceof JavaClassGroupTypeAccessor) {
                    if (superGroup.getGroupType() instanceof Class<?> &&
                            group.getGroupType() instanceof Class<?>) {
                        Class<?> superGroupClass = (Class<?>) superGroup.getGroupType();
                        Class<?> groupClass = (Class<?>) group.getGroupType();
                        if (groupClass != null && superGroupClass != null &&
                                !superGroupClass.isAssignableFrom(groupClass)) {
                            throw new IllegalArgumentException("Java inheritance does not match super group in group: " +
                                    group.getName());
                        }
                    }
                }
            }
        }

        // TODO: validate that group does not shadow fields in super group

        // validate TypeDef references and cycles
        final int cycleLimit = typesByName.size();
        // - named types
        for (NamedType type : typesByName.values()) {
            checkTypeReferenceAndCycles(type.getType(), cycleLimit);
        }
        // - field types
        for (GroupDef group : groups) {
            for (FieldDef field : group.getFields()) {
                checkTypeReferenceAndCycles(field.getType(), cycleLimit);
            }
        }

        // validate that dynamic references eventually point to a GroupDef
        // validate that we do not have a sequence of sequence (without a GroupDef in between)
        // - named types
        for (NamedType type : typesByName.values()) {
            checkDynamiceReferencesAndSequences(type.getType());
        }
        // - field types
        for (GroupDef group : groups) {
            for (FieldDef tField : group.getFields()) {
                checkDynamiceReferencesAndSequences(tField.getType());
            }
        }


        // sort groups with increasing super class count, i.e. groups with no superclass comes first
        ArrayList<GroupDef> sortedGroupsTemp = new ArrayList<>(groups);
        Collections.sort(sortedGroupsTemp, new GroupDefComparator());
        sortedGroups = Collections.unmodifiableList(sortedGroupsTemp);
    }

    private void checkTypeReferenceAndCycles(TypeDef.Ref type, int cycleCounter) {
        String ref = type.getRefType();
        if (ref == null) {
            return; // ok (dynamic reference)
        }
        if (groupsByName.containsKey(ref)) {
            return; // ok
        }

        NamedType refType = typesByName.get(ref);
        if (refType == null) {
            throw new IllegalArgumentException("Unknown TypeDef reference: " + ref);
        }
        if (cycleCounter == 0) {
            throw new IllegalArgumentException("Cyclic TypeDef references detected: " + ref);
        }

        checkTypeReferenceAndCycles(refType.getType(), cycleCounter - 1);
    }
    private void checkTypeReferenceAndCycles(TypeDef.Sequence type, int cycleCounter) {
        final int cycleLimit = typesByName.size();
        checkTypeReferenceAndCycles(type.getComponentType(), cycleLimit);
    }
    private void checkTypeReferenceAndCycles(TypeDef type, int cycleCounter) {
        if (type instanceof TypeDef.Ref) {
            checkTypeReferenceAndCycles((TypeDef.Ref) type, cycleCounter);
        } else if (type instanceof TypeDef.Sequence) {
            checkTypeReferenceAndCycles((TypeDef.Sequence) type, cycleCounter);
        }
        // else ok
    }

    private void checkDynamiceReferencesAndSequences(TypeDef type) {
        if (type instanceof TypeDef.DynamicReference) {
            checkDynamiceReferencesAndSequences((TypeDef.DynamicReference) type);
        } else if (type instanceof TypeDef.Sequence) {
            checkDynamiceReferencesAndSequences((TypeDef.Sequence) type);
        }
        // else ok
    }
    private void checkDynamiceReferencesAndSequences(TypeDef.DynamicReference type) {
        if (type.getRefType() != null && resolveToGroup(type) == null) {
            throw new IllegalArgumentException(
                    "Dynamic reference must resolve to a GroupDef: " + type.getRefType());
        }
    }
    private void checkDynamiceReferencesAndSequences(TypeDef.Sequence type) {
        TypeDef resolvedSequenceType = resolveToType(type.getComponentType(), false);
        if (resolvedSequenceType instanceof TypeDef.Sequence) {
            throw new IllegalArgumentException("Sequence of sequence is not allowed: " + type);
        }
        checkDynamiceReferencesAndSequences(resolvedSequenceType);
    }

    /**
     * @return the binding
     */
    public SchemaBinding getBinding() {
        return binding;
    }

    /**
     * Returns true if this schema is completely bound.
     * @return true if this schema is completely bound, otherwise false.
     */
    public boolean isBound() {
        return getBindingStatus() == BindingStatus.BOUND;
    }

    /**
     * Returns true if this schema is completely unbound.
     * @return true if this schema is completely unbound, otherwise false.
     */
    public boolean isUnbound() {
        return getBindingStatus() == BindingStatus.UNBOUND;
    }

    BindingStatus getBindingStatus() {
        if (bindingStatus == null) {
            BindingStatus status = binding != null ? BindingStatus.BOUND : BindingStatus.UNBOUND;
            for (GroupDef group : sortedGroups) {
                status = status.combine(group.getBindingStatus());
            }
            bindingStatus = status;
        }
        return bindingStatus;
    }

    /**
     * Returns the unbound version of this schema.
     * @return the unbound version of this schema.
     */
    public Schema unbind() {
        if (isUnbound()) {
            return this;
        }
        Collection<GroupDef> unboundGroups = new ArrayList<>(sortedGroups.size());
        for (GroupDef group : sortedGroups) {
            unboundGroups.add(group.unbind());
        }
        return new Schema(unboundGroups, typesByName.values(), annotations, null);
    }

    /** Resolves the TypeDef to a GroupDef, or null if it cannot be resolved to a GroupDef.
     *
     * <p>A DynamicReference will always be resolved to a GroupDef, eventually, unless it
     * is references any type.
     * A Reference may be resolved to a GroupDef. Other types cannot be resolved to a GroupDef.
     *
     * @param type the type to be resolved
     * @return the GroupDef or null of it cannot be resolved.
     */
    public GroupDef resolveToGroup(TypeDef type) {
        if (!(type instanceof TypeDef.Ref)) {
            return null;
        }
        String refType = ((Ref) type).getRefType();
        if (refType == null) {
            return null; // dynamic ref to any group
        }
        GroupDef groupDef = groupsByName.get(refType);
        if (groupDef != null) {
            return groupDef;
        }
        NamedType namedType = typesByName.get(refType);
        return resolveToGroup(namedType != null ? namedType.getType() : null);
    }

    /**
     * Replace instances of Reference that points to another TypeDef, with the target.
     *
     * <p>If <code>full</code> is true, then instances of DynamicReference and
     * Sequence are resolved further, as follows:
     * <ul>
     * <li>DynamicReference is replaced (if needed) with a direct reference to a group,
     * without any intermediate Reference pointers.</li>
     * <li>Sequence is replaced (if needed) where the sequence type is fully resolved.
     * </ul>
     *
     * @param type the type to be resolved
     * @param full true if targets of DymanicReference and Sequence should be resolved (recursively)
     * @return the resolved type. If a Reference is returned, then it is guaranteed that it points to a
     * GroupDef. If <code>full</code> is true, then DynamicReference will point <em>directly</em> to a GroupDef,
     * and Sequence will have a type which is fully resolved.
     * @see #resolveToGroup(TypeDef)
     */
    public TypeDef resolveToType(TypeDef type, boolean full) {
        if (type instanceof TypeDef.Reference) {
            String refType = ((TypeDef.Reference) type).getRefType();
            if (groupsByName.containsKey(refType)) {
                return type;
            }
            NamedType namedType = typesByName.get(refType);
            return resolveToType(namedType != null ? namedType.getType() : null, full);
        } else if (type instanceof TypeDef.DynamicReference) {
            String refType = ((TypeDef.DynamicReference) type).getRefType();
            if (!full || refType == null || groupsByName.containsKey(refType)) {
                return type;
            }
            // tRefType points to a TypeDef.Reference that need to be resolved
            NamedType namedType = typesByName.get(refType);
            refType = resolveToGroup(namedType != null ? namedType.getType() : null).getName();
            return new TypeDef.DynamicReference(refType);
        } else if (type instanceof TypeDef.Sequence) {
            TypeDef seqType = ((TypeDef.Sequence) type).getComponentType();
            if (!full) {
                return type;
            }
            TypeDef resolvedSeqType = resolveToType(seqType, full);
            if (seqType == resolvedSeqType) {
                return type;
            }
            return new TypeDef.Sequence(resolvedSeqType);
        } else {
            // basic type
            return type;
        }
    }

    /**
     * @return the typesByName
     */
    public Collection<NamedType> getNamedTypes() {
        return typesByName.values();
    }

    /** Returns the group definition for the specified group type.
     *
     * @param groupType the group type.
     * @return the group definition, or null if not found.
     */
    public GroupDef getGroup(Object groupType) {
        GroupDef g = groupsByType.get(groupType);
        if (g != null) {
            return g;
        } else {
            Integer gId = remappedClasses.get(groupType);
            return gId != null ? groupsById.get(gId) : null;
        }
    }

    /** Returns the group definition for the specified group name.
     *
     * @param name the name of the group.
     * @return the group definition, or null if not found.
     */
    public GroupDef getGroup(String name) {
        return groupsByName.get(name);
    }

    /** Returns the group definition for the specified group id.
     *
     * @param id the id of the group.
     * @return the group definition, or null if not found.
     */
    public GroupDef getGroup(int id) {
        return groupsById.get(id);
    }

    /**
     * Return groups that are "instanceof" the specified group.
     *
     * @param name the group name, or null to match all groups.
     * @return the groups that have this name or is a (direct or indirect) sub group to the super group, never null.
     */
    public Collection<GroupDef> getDynamicGroups(String name) {
        if (name == null) {
            return getGroups();
        }
        HashSet<String> superNames = new HashSet<>();
        LinkedList<GroupDef> subGroups = new LinkedList<>();
        for (GroupDef group : sortedGroups) {
            if (group.getName().equals(name)) {
                superNames.add(name);
                subGroups.add(group);
            } else if (superNames.contains(group.getSuperGroup())) {
                subGroups.add(group);
            }
        }
        return subGroups;
    }

    /** Returns all groups.
     * The groups are sorted with increasing super group count, then by increasing id values.
     * This means that any super groups will always appear before any extending groups.
     *
     * @return all groups.
     */
    public Collection<GroupDef> getGroups() {
        return sortedGroups;
    }

    /** Assign group ids for all groups with unassigned group id.
     * The {@link String#hashCode()} of the name is used. If the hash code is -1 then zero is chosen.
     * @return a new Schema with group identifiers assigned.
     * @throws IllegalArgumentException if duplicate identifiers were generated
     */
    public Schema assignGroupIds() {
        Collection<GroupDef> newSortedGroups = new ArrayList<>(sortedGroups.size());
        for (GroupDef group : sortedGroups) {
            if (group.getId() == -1) {
                int newGroupId = group.getName().hashCode();
                if (newGroupId == -1) {
                    newGroupId = 0;
                }
                group = new GroupDef(
                        group.getName(),
                        newGroupId,
                        group.getSuperGroup(),
                        group.getFields(),
                        group.getAnnotations(),
                        group.getBinding());
            }
            newSortedGroups.add(group);
        }

        return new Schema(newSortedGroups, typesByName.values(), annotations, binding);
    }

    /**
     * Returns a unique identifier for this schema instance.
     * It is guaranteed that the UID object of two schema are equal
     * iff the schemas are the same instance.
     * @return the UID, not null.
     */
    public Object getUID() {
        return UID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(Schema.class)) {
            return false;
        }
        final Schema other = (Schema) obj;
        return Objects.equals(this.sortedGroups, other.sortedGroups) &&
                Objects.equals(this.typesByName, other.typesByName) &&
                Objects.equals(this.annotations, other.annotations) &&
                Objects.equals(this.binding, other.binding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortedGroups, typesByName, annotations, binding);
    }

    /**
     * Returns a human readable string representation of this schema.
     * @see SchemaParser
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> annotation : annotations.entrySet()) {
            str.append(". <- ").append(Annotations.toString(annotation.getKey(), annotation.getValue()));
            str.append("\n");
        }

        for (NamedType namedType : typesByName.values()) {
            str.append(namedType.toString());
        }
        for (GroupDef group : sortedGroups) {
            str.append("\n");
            str.append(group.toString());
        }

        return str.toString();
    }

    public MetaSchema toMessage() {
        List<MetaGroupDef> msgGroups = new ArrayList<>(sortedGroups.size());
        for (GroupDef group : sortedGroups) {
            msgGroups.add(group.toMessage());
        }

        List<MetaNamedType> msgTypes = new ArrayList<>(typesByName.size());
        for (NamedType namedType : typesByName.values()) {
            msgTypes.add(namedType.toMessage());
        }

        MetaSchema message = new MetaSchema(msgGroups, msgTypes);
        message.setAnnotations(annotations.isEmpty() ? null : annotations);
        return message;
    }

    /**
     * Replace all annotations in this object with the specified annotations.
     *
     * @param annotations the annotations.
     * @return a new copy of this schema, with the specified annotations set.
     */
    @Override
    public Schema replaceAnnotations(Annotations annotations) {
        Collection<GroupDef> newGroups = new ArrayList<>(sortedGroups.size());
        for(GroupDef group : sortedGroups){
            newGroups.add(group.replaceAnnotations(annotations.path(group.getName())));
        }
        Collection<NamedType> newNamedTypes = new ArrayList<>(typesByName.size());
        for (NamedType namedType : typesByName.values()) {
            newNamedTypes.add(namedType.replaceAnnotations(annotations.path(namedType.getName())));
        }
        Map<String, String> newAnnotations = annotations.map();
        return new Schema(newGroups, newNamedTypes, newAnnotations, binding);
    }

    /**
     * Add the specified annotations to this object.
     *
     * @param annotations the annotations.
     * @return a new copy of this schema, with the specified annotations set.
     */
    @Override
    public Schema addAnnotations(Annotations annotations) {
        Collection<GroupDef> newGroups = new ArrayList<>(sortedGroups.size());
        for(GroupDef group : sortedGroups){
            newGroups.add(group.addAnnotations(annotations.path(group.getName())));
        }
        Collection<NamedType> newNamedTypes = new ArrayList<>(typesByName.size());
        for (NamedType namedType : typesByName.values()) {
            newNamedTypes.add(namedType.replaceAnnotations(annotations.path(namedType.getName())));
        }
        Map<String, String> newAnnotations = new HashMap<>(this.annotations);
        newAnnotations.putAll(annotations.map());
        return new Schema(newGroups, newNamedTypes, newAnnotations, binding);
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
     * Visit this schema with the specified schema visitor.
     * @param sv the schema visitor, not null.
     */
    public void visit(SchemaVisitor sv) {
        visit(this, sv);
    }

    private static void visit(Schema schema, SchemaVisitor sv) {
        sv.visit(schema.getBinding());
        schema.getAnnotations().forEach(sv::visitAnnotation);
        schema.getNamedTypes().forEach(t -> {
            NamedTypeVisitor tv = sv.visitNamedType(t.getName(), t.getType());
            if (tv != null) {
                t.getAnnotations().forEach(tv::visitAnnotation);
                tv.visitEnd();
            }
        });
        schema.getGroups().forEach(g -> {
            GroupDefVisitor gv = sv.visitGroup(
                    g.getName(),
                    g.getId(),
                    g.getSuperGroup(),
                    g.getBinding());
            if (gv != null) {
                g.getAnnotations().forEach(gv::visitAnnotation);
                g.getFields().forEach(f -> {
                    FieldDefVisitor fv = gv.visitField(
                            f.getName(),
                            f.getId(),
                            f.isRequired(),
                            f.getType(),
                            f.getBinding());
                    if (fv != null) {
                        f.getAnnotations().forEach(fv::visitAnnotation);
                    }
                    fv.visitEnd();
                });
                gv.visitEnd();
            }
        });
        sv.visitEnd();
    }

    /** Sort GroupDefs by parentCount ascending, followed by id ascending.
     *
     * @author mikael.brannstrom
     */
    private class GroupDefComparator implements Comparator<GroupDef> {
        @Override
        public int compare(GroupDef group1, GroupDef group2) {
            int parentCount1 = parentCount(group1, 0);
            int parentCount2 = parentCount(group2, 0);

            if (parentCount1 < parentCount2) {
                return -1;
            } else if (parentCount1 > parentCount2) {
                return 1;
            } else if (group1.getId() < group2.getId()) {
                return -1;
            } else if (group1.getId() > group2.getId()) {
                return 1;
            } else {
                return group1.getName().compareTo(group2.getName());
            }
        }

        private int parentCount(GroupDef group, int count) {
            if (group == null) {
                return count - 1;
            } else {
                return parentCount(groupsByName.get(group.getSuperGroup()), count + 1);
            }
        }
    }


}
