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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Group is a generic group object.
 * Group implements the Map interface where field name is used as key.
 *
 * @author mikael.brannstrom
 *
 */
public class Group implements Map<String, Object> {
    private final String groupName;
    private final Map<String, Object> fieldValues;


    /** Create a new group.
     *
     * @param groupName the group name, not null.
     */
    public Group(String groupName) {
        this.groupName = Objects.requireNonNull(groupName);
        this.fieldValues = new HashMap<>();
    }

    /** Returns the group name.
     * @return the group name, not null.
     */
    public String getGroupName() {
        return groupName;
    }

    @Override
    public int size() {
        return fieldValues.size();
    }
    @Override
    public boolean isEmpty() {
        return fieldValues.isEmpty();
    }
    @Override
    public boolean containsKey(Object key) {
        return fieldValues.containsKey(key);
    }
    @Override
    public boolean containsValue(Object value) {
        return fieldValues.containsValue(value);
    }
    @Override
    public Object get(Object key) {
        return fieldValues.get(key);
    }
    @Override
    public Object put(String key, Object value) {
        return fieldValues.put(key, value);
    }
    @Override
    public Object remove(Object key) {
        return fieldValues.remove(key);
    }
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        fieldValues.putAll(m);
    }
    @Override
    public void clear() {
        fieldValues.clear();
    }
    @Override
    public Set<String> keySet() {
        return fieldValues.keySet();
    }
    @Override
    public Collection<Object> values() {
        return fieldValues.values();
    }
    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return fieldValues.entrySet();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(groupName).append(" [");
        boolean first = true;
        for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
            if (first) {
                first = false;
            } else {
                str.append(", ");
            }
            str.append(entry.getKey()).append("=").append(entry.getValue().toString());
        }
        str.append("]");
        return str.toString();
    }

    private static GroupDef bind(GroupDef group, ProtocolDictionary dictionary) {
        Map<String, FieldBinding> fieldBindings = new HashMap<>(group.getFields().size());
        for (FieldDef field : group.getFields()) {
            FieldNameAccessor accessor = new FieldNameAccessor(field.getName());
            TypeDef type = dictionary.resolveToType(field.getType(), true);
            FieldBinding binding =
                    new FieldBinding(accessor, type.getDefaultJavaType(), type.getDefaultJavaComponentType());
            fieldBindings.put(field.getName(), binding);
        }
        String groupName = group.getName();
        GroupBinding groupBinding = new GroupBinding(new GroupFactory(groupName), groupName);
        return group.bind(groupBinding, fieldBindings);
    }

    /** Bind the protocol dictionary to {@link Group} instances for group objects.
     *
     * @param dictionary the dictionary to be bound
     * @return the bound dictionary
     */
    public static ProtocolDictionary bind(ProtocolDictionary dictionary) {
        List<GroupDef> groups = new ArrayList<>(dictionary.getGroups().size());
        for (GroupDef group : dictionary.getGroups()) {
            groups.add(bind(group, dictionary));
        }
        ProtocolDictionaryBinding binding = new ProtocolDictionaryBinding(GroupTypeAccessor.GROUP_NAME);
        return new ProtocolDictionary(groups, dictionary.getNamedTypes(), dictionary.getAnnotations(), binding);
    }

    private static class GroupFactory implements Factory<Group> {
        private final String groupName;
        /**
         * @param groupName
         */
        public GroupFactory(String groupName) {
            this.groupName = groupName;
        }

        @Override
        public Group newInstance() {
            return new Group(groupName);
        }

    }

    private static class FieldNameAccessor extends MapAccessor<String, Object> {
        public FieldNameAccessor(String fieldName) {
            super(fieldName);
        }
    }

    private static class MapAccessor<K, V> implements Accessor<Map<K, V>, V> {
        private final K key;
        /**
         * @param key
         */
        public MapAccessor(K key) {
            this.key = key;
        }

        @Override
        public V getValue(Map<K, V> obj) {
            return obj.get(key);
        }

        @Override
        public void setValue(Map<K, V> obj, V value) {
            obj.put(key, value);
        }
    }
}