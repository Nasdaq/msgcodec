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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.cinnober.msgcodec.TypeDef.Ref;
import com.cinnober.msgcodec.messages.MetaGroupDef;
import com.cinnober.msgcodec.messages.MetaNamedType;
import com.cinnober.msgcodec.messages.MetaProtocolDictionary;

/**
 * The protocol dictionary defines the messages of a protocol.
 * Protocol dictionary is immutable.
 *
 * <p>A protocol dictionary consists of a collection of message definitions ({@link GroupDef}),
 * and any named types (a mapping from String to {@link TypeDef}) that may be referred to by
 * message fields ({@link FieldDef}).
 *
 * @author mikael.brannstrom
 *
 */
public class ProtocolDictionary implements Annotatable<ProtocolDictionary> {
    private final Map<String, NamedType> typesByName;

    private final Map<String, GroupDef> groupsByName;
    private final Map<Integer, GroupDef> groupsById;
    private final Map<Object, GroupDef> groupsByType;
    private final Collection<GroupDef> sortedGroups;
    private final Map<String, String> annotations;

    private final ProtocolDictionaryBinding binding;
    private Boolean bound;

    /**
     * Creates a protocol dictionary.
     *
     * @param groups the group definitions (the protocol messages), not null.
     * @param namedTypes any named types, or null if none.
     */
    public ProtocolDictionary(Collection<GroupDef> groups, Collection<NamedType> namedTypes) {
        this(groups, namedTypes, null, null);
    }

    /**
     * Creates a protocol dictionary.
     *
     * @param groups the group definitions (the protocol messages), not null.
     * @param namedTypes any named types, or null if none.
     * @param binding the protocol dictionary binding, or null if unbound
     */
    public ProtocolDictionary(Collection<GroupDef> groups, Collection<NamedType> namedTypes,
        ProtocolDictionaryBinding binding) {
        this(groups, namedTypes, null, binding);
    }

    /**
     * Creates a protocol dictionary.
     *
     * @param groups the group definitions (the protocol messages), not null.
     * @param namedTypes any named types, or null if none.
     * @param annotations the group annotations.
     * @param binding the protocol dictionary binding, or null if unbound
     */
    public ProtocolDictionary(Collection<GroupDef> groups, Collection<NamedType> namedTypes,
        Map<String, String> annotations, ProtocolDictionaryBinding binding) {
        if (annotations == null || annotations.isEmpty()) {
            this.annotations = Collections.emptyMap();
        } else{
            this.annotations = Collections.unmodifiableMap(new LinkedHashMap<>(annotations));
        }
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
            if (group.getGroupType() != null) {
                if (groupsByType.put(group.getGroupType(), group) != null) {
                    throw new IllegalArgumentException("Duplicate group type: " + group.getGroupType());
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
                if (binding != null && binding.getGroupTypeAccessor() instanceof GroupTypeAccessor.JavaClass) {
                    Class<?> superGroupClass = (Class<?>) superGroup.getGroupType();
                    Class<?> groupClass = (Class<?>) group.getGroupType();
                    if (!superGroupClass.isAssignableFrom(groupClass)) {
                        throw new IllegalArgumentException("Java inheritance does not match super group in group: " +
                            group.getName());
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
        ArrayList<GroupDef> sortedGroupsTemp = new ArrayList<GroupDef>(groups);
        Collections.sort(sortedGroupsTemp, new GroupDefComparator());
        sortedGroups = Collections.unmodifiableCollection(sortedGroupsTemp);
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
    public ProtocolDictionaryBinding getBinding() {
        return binding;
    }

    /** Returns true if this dictionary is completely bound.
     * @return true if completely bound, otherwise false.
     */
    public boolean isBound() {
        if (bound == null) {
            boolean allBound = binding != null;
            for (GroupDef group : sortedGroups) {
                allBound &= group.isBound();
            }
            this.bound = allBound;
        }
        return bound;
    }

    public ProtocolDictionary bind(ProtocolDictionaryBinding binding, Collection<GroupDef> boundGroups) {
        return new ProtocolDictionary(boundGroups, typesByName.values(), annotations, binding);
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
     * <p>If <code>pFull</code> is true, then instances of DynamicReference and
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
     * GroupDef. If <code>pFull</code> is true, then DynamicReference will point <em>directly</em> to a GroupDef,
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
        return groupsByType.get(groupType);
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

    /** Return groups that are "instanceof" the specified group.
     *
     * @param name the group name, or null to match all groups.
     * @return the groups that have this name or is a (direct or indirect) sub group to the super group, never null.
     */
    public Collection<GroupDef> getDynamicGroups(String name) {
        if (name == null) {
            return getGroups();
        }
        Set<String> superNames = new HashSet<String>();
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


    /**
     * Returns a human readable string representation of this protocol dictionary.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> annotation : annotations.entrySet()) {
            str.append("schema <- " + Annotations.toString(annotation.getKey(), annotation.getValue())).append("\n");
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

    public MetaProtocolDictionary toMessage() {
        Collection<MetaGroupDef> msgGroups = new ArrayList<>(sortedGroups.size());
        for (GroupDef group : sortedGroups) {
            msgGroups.add(group.toMessage());
        }

        Collection<MetaNamedType> msgTypes = new ArrayList<>(typesByName.size());
        for (NamedType namedType : typesByName.values()) {
            msgTypes.add(namedType.toMessage());
        }

        MetaProtocolDictionary message = new MetaProtocolDictionary(msgGroups, msgTypes);
        message.setAnnotations(annotations.isEmpty() ? null : annotations);
        return message;
    }

    /** Replace all annotations in this object with the specified annotations.
     *
     * @param annotations the annotations.
     * @return a new copy of this object, with the specified annotations set.
     */
    @Override
    public ProtocolDictionary replaceAnnotations(Annotations annotations) {
        Collection<GroupDef> newGroups = new ArrayList<>(sortedGroups.size());
        for(GroupDef group : sortedGroups){
            newGroups.add(group.replaceAnnotations(annotations.path(group.getName())));
        }
        Collection<NamedType> newNamedTypes = new ArrayList<>(typesByName.size());
        for (NamedType namedType : typesByName.values()) {
            newNamedTypes.add(namedType.replaceAnnotations(annotations.path(namedType.getName())));
        }
        Map<String, String> newAnnotations = annotations.map();
        return new ProtocolDictionary(newGroups, newNamedTypes, newAnnotations, binding);
    }

    @Override
    public ProtocolDictionary addAnnotations(Annotations annotations) {
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
        return new ProtocolDictionary(newGroups, newNamedTypes, newAnnotations, binding);
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
     * Gets node in the protocol dictionary. A node can be a group, field, type or the dictionary it self
     * @param path path to node
     * @return the requested node, or null if the path does not exist in this protocol dictionary
     */
    public Object getNode(String... path) {
        if (path.length == 0){
            return this;
        } else if (path.length == 1) {
            NamedType typeDef = typesByName.get(path[0]);
            if (typeDef != null){
                return typeDef;
            }
            GroupDef groupDef = groupsByName.get(path[0]);
            if (groupDef != null){
                return groupDef;
            }
        } else if (path.length == 2) {
            GroupDef groupDef = groupsByName.get(path[0]);
            if (groupDef != null){
                FieldDef fieldDef = groupDef.getField(path[1]);
                if (fieldDef != null) {
                    return fieldDef;
                }
            }
        }

        return null; // not found
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
