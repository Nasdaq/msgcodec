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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.cinnober.msgcodec.TypeDef.Type;

/**
 * Group is a generic group object.
 *
 * @author mikael.brannstrom
 *
 */
public class Group {

    /**
     * Synchronize on this object when accessing.
     */
    private static final WeakHashMap<Object, SchemaInfo> schemaInfoBySchemaUID = new WeakHashMap<>();
    private static final GroupTypeAccessorImpl GROUP_TYPE_ACCESSOR = new GroupTypeAccessorImpl();

    private final GroupInfo groupInfo;
    private final Object[] fieldValues;


    private static GroupInfo getGroupInfo(Schema schema, String groupName) {
        SchemaInfo dictInfo;
        synchronized (schemaInfoBySchemaUID) {
            dictInfo = schemaInfoBySchemaUID.get(schema.getUID());
        }
        if (dictInfo == null) {
            throw new IllegalArgumentException("Schema not bound to Group");
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
     * @param schema the schema bound to {@link Group}, not null.
     * @param groupName the group name, not null.
     */
    public Group(Schema schema, String groupName) {
        this(getGroupInfo(schema, groupName));
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
     * @return this group instance, for chaining.
     * @throws IllegalArgumentException if the field name does not exist.
     */
    public Group set(String fieldName, Object value) throws IllegalArgumentException {
        fieldValues[groupInfo.getFieldIndex(fieldName)] = value;
        return this;
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
     * Bind the protocol schema to {@link Group} instances for group objects.
     *
     * @param schema the schema to be bound, not null.
     * @return the bound schema
     */
    public static Schema bind(Schema schema) {
        Map<String, GroupInfo> groupInfos = new HashMap<>();
        ArrayList<FieldIndexAccessor> fieldAccessors = new ArrayList<>();
        List<GroupDef> groups = new ArrayList<>(schema.getGroups().size());
        for (GroupDef group : schema.getGroups()) {
            groups.add(bind(group, schema, groupInfos, fieldAccessors));
        }
        SchemaBinding binding = new SchemaBinding(GROUP_TYPE_ACCESSOR);
        Schema boundSchema =
                new Schema(groups, schema.getNamedTypes(), schema.getAnnotations(), binding);

        SchemaInfo schemaInfo = new SchemaInfo(groupInfos);
        synchronized (schemaInfoBySchemaUID) {
            schemaInfoBySchemaUID.put(boundSchema.getUID(), schemaInfo);
        }
        return boundSchema;
    }

    private static FieldIndexAccessor getAccessor(List<FieldIndexAccessor> accessors, int index) {
        while (accessors.size() <= index) {
            accessors.add(new FieldIndexAccessor(accessors.size()));
        }
        return accessors.get(index);
    }

    private static GroupDef bind(GroupDef group, Schema schema,
            Map<String, GroupInfo> groupInfoByName, List<FieldIndexAccessor> accessors) {
        GroupInfo superGroupInfo = group.getSuperGroup() != null ? groupInfoByName.get(group.getSuperGroup()) : null;
        List<FieldDef> allFields = new ArrayList<>();
        List<FieldDef> declaredFields = new ArrayList<>();
        if (superGroupInfo != null) {
            allFields.addAll(Arrays.asList(superGroupInfo.fieldDefs()));
        }
        for (FieldDef field : group.getFields()) {
            TypeDef type = schema.resolveToType(field.getType(), true);
            SymbolMapping<?> symbolMapping = null;
            
            if (type.getType() == Type.ENUM) {
                symbolMapping = new SymbolMapping.IdentityIntegerEnumMapping((TypeDef.Enum) type);
            } else if (type.getType() == Type.SEQUENCE) {
                TypeDef componentType = schema.resolveToType(((TypeDef.Sequence) type).getComponentType(), false);
                if (componentType != null && componentType.getType() == Type.ENUM) {
                    symbolMapping = new SymbolMapping.IdentityIntegerEnumMapping((TypeDef.Enum) componentType);
                }
            }
            
            FieldDef boundField = field.bind(new FieldBinding(getAccessor(accessors, allFields.size()),
                    type.getDefaultJavaType(), type.getDefaultJavaComponentType(), symbolMapping));
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


    private static class SchemaInfo {
        private final Map<String, GroupInfo> groupsByName;

        public SchemaInfo(Map<String, GroupInfo> groupsByName) {
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
