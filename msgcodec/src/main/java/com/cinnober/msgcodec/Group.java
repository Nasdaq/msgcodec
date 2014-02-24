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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Group is a generic group object.
 *
 * @author mikael.brannstrom
 *
 */
public class Group {

    private static final WeakHashMap<Object, DictionaryInfo> dictInfoByDictUID = new WeakHashMap<>();
    private static final GroupTypeAccessorImpl GROUP_TYPE_ACCESSOR = new GroupTypeAccessorImpl();

    private final GroupInfo groupInfo;
    private final Object[] fieldValues;


    private static GroupInfo getGroupInfo(ProtocolDictionary dictionary, String groupName) {
        DictionaryInfo dictInfo = dictInfoByDictUID.get(dictionary.getUID());
        if (dictInfo == null) {
            throw new IllegalArgumentException("Dictionary not bound to Group");
        }
        GroupInfo groupInfo = dictInfo.getGroupInfo(groupName);
        if (groupInfo == null) {
            throw new IllegalArgumentException("Unknown group name \"" + groupName + "\"");
        }
        return groupInfo;
    }

    /**
     * Create a new group.
     *
     * @param dictionary the dictionary bound to {@link Group}, not null.
     * @param groupName the group name, not null.
     */
    public Group(ProtocolDictionary dictionary, String groupName) {
        this(getGroupInfo(dictionary, groupName));
    }

    private Group(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
        this.fieldValues = new Object[groupInfo.size()];
    }

    /**
     * Returns the group type.
     * @return the group type (an opaque group type identifier), not null.
     * @see GroupTypeAccessor#getGroupType(java.lang.Object)
     * @see GroupBinding#getGroupType()
     */
    public Object getGroupType() {
        return groupInfo;
    }

    /**
     * Returns the group name.
     * @return the group name, not null.
     */
    public String getGroupName() {
        return groupInfo.name();
    }

    /**
     * Returns the value for the specified field.
     * @param fieldName the field name, not null.
     * @return the field value (including null).
     * @throws IllegalArgumentException if the field name does not exist.
     */
    public Object get(String fieldName) throws IllegalArgumentException {
        return fieldValues[groupInfo.getFieldIndex(fieldName)];

    }
    /**
     * Set the value for the specified field.
     * @param fieldName the field name, not null.
     * @param value the field value (including null).
     * @throws IllegalArgumentException if the field name does not exist.
     */
    public void set(String fieldName, Object value) throws IllegalArgumentException {
        fieldValues[groupInfo.getFieldIndex(fieldName)] = value;
    }
    private Object get(int fieldIndex) {
        return fieldValues[fieldIndex];
    }
    private void set(int fieldIndex, Object value) {
        fieldValues[fieldIndex] = value;
    }
    /**
     * Clear all field values (set to null).
     */
    public void clear() {
        for (int i=0; i<fieldValues.length; i++) {
            fieldValues[i] = null;
        }
    }

    @Override
    public String toString() {
        return groupInfo.toString(fieldValues);
    }

    /**
     * Bind the protocol dictionary to {@link Group} instances for group objects.
     *
     * @param dictionary the dictionary to be bound, not null.
     * @return the bound dictionary
     */
    public static ProtocolDictionary bind(ProtocolDictionary dictionary) {
        Map<String, GroupInfo> groupInfos = new HashMap<>();
        ArrayList<FieldIndexAccessor> fieldAccessors = new ArrayList<>();
        List<GroupDef> groups = new ArrayList<>(dictionary.getGroups().size());
        for (GroupDef group : dictionary.getGroups()) {
            groups.add(bind(group, dictionary, groupInfos, fieldAccessors));
        }
        ProtocolDictionaryBinding binding = new ProtocolDictionaryBinding(GROUP_TYPE_ACCESSOR);
        ProtocolDictionary boundDict =
                new ProtocolDictionary(groups, dictionary.getNamedTypes(), dictionary.getAnnotations(), binding);

        DictionaryInfo dictInfo = new DictionaryInfo(groupInfos);
        dictInfoByDictUID.put(boundDict.getUID(), dictInfo);
        return boundDict;
    }

    private static FieldIndexAccessor getAccessor(List<FieldIndexAccessor> accessors, int index) {
        while (accessors.size() <= index) {
            accessors.add(new FieldIndexAccessor(accessors.size()));
        }
        return accessors.get(index);
    }

    private static GroupDef bind(GroupDef group, ProtocolDictionary dictionary,
            Map<String, GroupInfo> groupInfoByName, List<FieldIndexAccessor> accessors) {
        GroupInfo superGroupInfo = group.getSuperGroup() != null ? groupInfoByName.get(group.getSuperGroup()) : null;
        List<FieldDef> allFields = new ArrayList<>();
        List<FieldDef> declaredFields = new ArrayList<>();
        if (superGroupInfo != null) {
            for (FieldDef field : superGroupInfo.fieldDefs()) {
                allFields.add(field);
            }
        }
        for (FieldDef field : group.getFields()) {
            TypeDef type = dictionary.resolveToType(field.getType(), true);
            FieldDef boundField = field.bind(new FieldBinding(getAccessor(accessors, allFields.size()),
                    type.getDefaultJavaType(), type.getDefaultJavaComponentType()));
            allFields.add(boundField);
            declaredFields.add(boundField);
        }

        GroupInfo groupInfo = new GroupInfo(allFields.toArray(new FieldDef[allFields.size()]));
        GroupBinding groupBinding = new GroupBinding(groupInfo, groupInfo);
        GroupDef boundGroup = new GroupDef(group.getName(), group.getId(), group.getSuperGroup(), declaredFields,
                group.getAnnotations(), groupBinding);

        groupInfo.initGroupDef(boundGroup);
        groupInfoByName.put(groupInfo.name(), groupInfo);

        return boundGroup;
    }


    private static class DictionaryInfo {
        private final Map<String, GroupInfo> groupsByName;

        public DictionaryInfo(Map<String, GroupInfo> groupsByName) {
            this.groupsByName = groupsByName;
        }

        private GroupInfo getGroupInfo(String groupName) {
            return groupsByName.get(groupName);
        }

    }

    private static class GroupInfo implements Factory<Group> {
        private GroupDef groupDef;
        private final FieldDef[] fieldDefs;
        private final Map<String, Integer> fieldIndexByName;

        public GroupInfo(FieldDef[] fieldDefs) {
            this.fieldDefs = fieldDefs;
            this.fieldIndexByName = new HashMap<>(fieldDefs.length*2);
            int index = 0;
            for (FieldDef field : fieldDefs) {
                fieldIndexByName.put(field.getName(), index++);
            }
        }
        GroupDef groupDef() {
            return groupDef;
        }

        void initGroupDef(GroupDef groupDef) {
            this.groupDef = groupDef;
        }


        FieldDef[] fieldDefs() {
            return fieldDefs;
        }

        int size() {
            return fieldDefs.length;
        }
        String name() {
            return groupDef.getName();
        }

        int getFieldIndex(String fieldName) {
            Integer index = fieldIndexByName.get(fieldName);
            if (index == null) {
                throw new IllegalArgumentException("No such field \"" + fieldName+"\" in group \""+name()+"\"");
            }
            return index.intValue();
        }

        @Override
        public Group newInstance() {
            return new Group(this);
        }

        @Override
        public String toString() {
            return name();
        }
        String toString(Object[] values) {
            StringBuilder str = new StringBuilder();
            str.append(name()).append(" [");
            int len = fieldDefs.length;
            boolean comma = false;
            for (int i=0; i<len; i++) {
                Object value = values[i];
                if (value != null) {
                    if (comma) {
                        str.append(", ");
                    } else {
                        comma = true;
                    }
                    str.append(fieldDefs[i].getName()).append('=').append(value.toString());
                }
            }
            str.append("]");
            return str.toString();
        }
    }

    private static class FieldIndexAccessor implements Accessor<Group, Object> {
        private final int index;
        public FieldIndexAccessor(int index) {
            this.index = index;
        }

        @Override
        public Object getValue(Group obj) {
            return obj.get(index);
        }

        @Override
        public void setValue(Group obj, Object value) {
            obj.set(index, value);
        }
    }

    private static class GroupTypeAccessorImpl implements GroupTypeAccessor {
        @Override
        public Object getGroupType(Object groupValue) {
            return ((Group)groupValue).getGroupType();
        }
    }
}