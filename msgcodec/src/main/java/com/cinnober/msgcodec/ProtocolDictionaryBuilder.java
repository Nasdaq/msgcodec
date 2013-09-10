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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.cinnober.msgcodec.TypeDef.Symbol;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Enumeration;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.SmallDecimal;
import com.cinnober.msgcodec.anot.Static;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 * The protocol dictionary builder can build a protocol dictionary from a collection of java classes.
 * <p>The methods of the Java classes must be annotated properly. See the com.cinnober.msgcodec.anot package.
 *
 * <p>Example:
 * <pre>
 * {@literal @}Id(123) // optional message id (recommended)
 * public class MyMessage {
 *     private int id;
 *     private String name;
 *     private Long optionalValue;
 *     private MyMessage embedded;
 *
 *     public MyMessage() {} // default constructor must exist
 *
 *     {@literal @}Id(1) // optional field id (recommended)
 *     {@literal @}Unsigned // negative values are treated as the positive values 2^31 up to 2^32 - 1.
 *     public int getId() { return id; }
 *     public void setId(int id) { this.id = id; }
 *
 *     {@literal @}Id(2)
 *     {@literal @}Required // null values are not permitted, checked by the message codec
 *     public String getName() { return name; }
 *     public void setName(String name) { this.name = name; }
 *
 *     {@literal @}Id(3)
 *     public Long getOptionalValue() { return optionalValue; }
 *     public void setOptionalValue(Long optionalValue) { this.optionalValue = optionalValue; }
 *
 *     {@literal @}Id(4)
 *     {@literal @}Static // only instances of MyMessage are permitted, not sub classes
 *     public MyMessage getEmbedded() { return embedded; }
 *     public void setEmbedded(MyMessage embedded) { this.embedded = embedded; }
 * }
 * </pre>
 *
 * @author mikael.brannstrom
 *
 */
public class ProtocolDictionaryBuilder {

    /** Build a protocol dictionary from the specified Java classes.
     *
     * <p>The protocol dictionary built is bound to the specified classes.
     *
     * @param messageTypes the Java classes that should be included in the protocol dictionary.
     * @return the created protocol dictionary.
     * @throws IllegalArgumentException if the protocol dictionary could not be build due to wrong input.
     * E.g. wrong annotations etc.
     */
    public ProtocolDictionary build(Class<?> ... messageTypes) throws IllegalArgumentException {
        Map<Class<?>, GroupMeta> groups = new HashMap<Class<?>, GroupMeta>(messageTypes.length * 2);
        for (Class<?> messageType : messageTypes) {
            groups.put(messageType, new GroupMeta(messageType));
        }
        return internalBuild(groups);
    }

    /** Build a protocol dictionary from the specified Java classes.
     *
     * <p>The protocol dictionary built is bound to the specified classes.
     *
     * @param messageTypes the Java classes that should be included in the protocol dictionary.
     * @return the created protocol dictionary.
     * @throws IllegalArgumentException if the protocol dictionary could not be build due to wrong input.
     * E.g. wrong annotations etc.
     */
    public ProtocolDictionary build(Collection<Class<?>> messageTypes) throws IllegalArgumentException {
        Map<Class<?>, GroupMeta> groups = new HashMap<Class<?>, GroupMeta>(messageTypes.size() * 2);
        for (Class<?> messageType : messageTypes) {
            groups.put(messageType, new GroupMeta(messageType));
        }
        return internalBuild(groups);
    }

    /** Build a protocol dictionary from the specified groups.
     * @param groups group meta objects where only the java class is initialized, mapped by the java classes.
     * @return the created protocol dictionary.
     * @throws IllegalArgumentException if the protocol dictionary could not be build due to wrong input.
     * E.g. wrong annotations etc.
     */
    @SuppressWarnings({ "rawtypes" })
    private ProtocolDictionary internalBuild(Map<Class<?>, GroupMeta> groups) throws IllegalArgumentException {

        // infer group names, ids and inheritance
        for (GroupMeta group : groups.values()) {
            inferGroupName(group);
            inferGroupId(group);
            group.setAnnotations(toAnnotationsMap(group.getJavaClass().getAnnotation(Annotate.class)));
            inferGroupInheritance(group, groups);
        }

        Map<String, NamedType> namedTypes = new LinkedHashMap<>();

        // find fields, traverse groups starting from the top (inheritance wise)
        ArrayList<GroupMeta> sortedGroups = new ArrayList<GroupMeta>(groups.values());
        Collections.sort(sortedGroups, new GroupMetaComparator());
        for (GroupMeta group : sortedGroups) {
            findFields(group, namedTypes, groups);
        }

        // build group definitions
        Collection<GroupDef> groupDefs = new ArrayList<GroupDef>(sortedGroups.size());
        for (GroupMeta group : sortedGroups) {
            Constructor constructor;
            try {
                constructor = group.getJavaClass().getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No default constructor found for class " +
                        group.getJavaClass().getName(), e);
            } catch (SecurityException e) {
                throw new IllegalArgumentException("No public default constructor found for class " +
                        group.getJavaClass().getName(), e);
            }

            ConstructorFactory factory = new ConstructorFactory(constructor);
            GroupDef groupDef = new GroupDef(
                    group.getName(),
                    group.getId(),
                    group.getParent() != null ? group.getParent().getName() : null,
                    group.getFields(),
                    group.getAnnotations(),
                    new GroupBinding(factory, group.getJavaClass())
                    );
            groupDefs.add(groupDef);
        }

        ProtocolDictionaryBinding binding = new ProtocolDictionaryBinding(new GroupTypeAccessor.JavaClass());
        return new ProtocolDictionary(groupDefs, namedTypes.values(), binding);
    }


    /** Infer and set the group name.
     * @param group
     */
    private void inferGroupName(GroupMeta group) {
        String name = group.getJavaClass().getSimpleName();
        Name nameAnot = group.getJavaClass().getAnnotation(Name.class);
        if (nameAnot != null) {
            name = nameAnot.value();
        }
        group.setName(name);
    }
    /** Infer and set the group id. The name must be set.
     * @param group
     */
    private void inferGroupId(GroupMeta group) {
        int id = -1;
        Id idAnot = group.getJavaClass().getAnnotation(Id.class);
        if (idAnot != null) {
            id = idAnot.value();
        }
        group.setId(id);
    }

    private Map<String, String> toAnnotationsMap(Annotate annotateAnot) {
        if (annotateAnot == null) {
            return Collections.emptyMap();
        } else {
            Map<String, String> map = new HashMap<>(annotateAnot.value().length * 2);
            for (String keyValue : annotateAnot.value()) {
                int idx = keyValue.indexOf('=');
                if (idx == -1) {
                    throw new IllegalArgumentException("Illegal annotation \"" + keyValue + "\"");
                }
                String key = keyValue.substring(0, idx);
                String value = keyValue.substring(idx + 1);
                map.put(key, value);
            }
            return map;
        }

    }

    /**
     * @param group
     * @param groups
     */
    private void inferGroupInheritance(GroupMeta group,
            Map<Class<?>, GroupMeta> groups) {
        Class<?> superClass = group.getJavaClass().getSuperclass();
        while(superClass != null) {
            GroupMeta superGroup = groups.get(superClass);
            if(superGroup != null) {
                group.setParent(superGroup);
                break;
            }
            superClass = superClass.getSuperclass();
        }
    }

    /**
     * Find all fields up to, but not including, the parent group.
     * A field consists of a getter/setter pair.
     *
     * @param group
     */
    private void findFields(GroupMeta group, Map<String, NamedType> namedTypes, Map<Class<?>, GroupMeta> groupsByClass) {
        findFields(group, group.getJavaClass(), namedTypes, groupsByClass);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void findFields(GroupMeta group, Class<?> javaClass, Map<String, NamedType> namedTypes, Map<Class<?>, GroupMeta> groupsByClass) {
        if (javaClass == null ||
            (group.getParent() != null && group.getParent().getJavaClass().equals(javaClass))) {
            return;
        }
        findFields(group, javaClass.getSuperclass(), namedTypes, groupsByClass);

        // methods are not sorted in any order, not even as they appear in the source code
        // instead we sort the fields according to id and name (later on)
        Method[] methods = javaClass.getDeclaredMethods();
        Map<String, Method> getMethodsByPropertyName = new HashMap<String, Method>(methods.length);
        Map<String, Method> setMethodsByPropertyName = new HashMap<String, Method>(methods.length);

        // clear all non-public or static methods
        for (int i=0; i<methods.length; i++) {
            if (!Modifier.isPublic(methods[i].getModifiers()) ||
                Modifier.isStatic(methods[i].getModifiers())) {
                methods[i] = null;
            }
        }

        // find get methods
        for (Method method : methods) {
            if (method == null) {
                continue;
            }
            if (method.getParameterTypes().length != 0) {
                continue; // must not have any parameters
            }
            if (method.getReturnType().equals(void.class)) {
                continue; // must have a return type
            }
            // check prefix
            String propertyName = null;
            if (method.getName().startsWith("get")) {
                propertyName = toPropertyName(method.getName().substring(3));
            } else if (method.getName().startsWith("is") &&
                       (method.getReturnType().equals(boolean.class) ||
                        method.getReturnType().equals(Boolean.class))) {
                propertyName = toPropertyName(method.getName().substring(2));
            }

            if (propertyName != null) {
                getMethodsByPropertyName.put(propertyName, method);
            }
        }

        // find set methods
        for (Method method : methods) {
            if (method == null) {
                continue;
            }
            if (method.getParameterTypes().length != 1) {
                continue; // must have one parameter
            }
            // relax: ignore return type (should be void)

            // check prefix
            if (method.getName().startsWith("set")) {
                String propertyName = toPropertyName(method.getName().substring(3));
                Method getMethod = getMethodsByPropertyName.get(propertyName);
                if (getMethod == null) {
                    continue; // no matching get method
                }
                if (!getMethod.getReturnType().equals(method.getParameterTypes()[0])) {
                    continue; // parameter type of set-method must match return type of get-method
                }
                setMethodsByPropertyName.put(propertyName, method);
            }
        }

        ArrayList<FieldDef> fields = new ArrayList<FieldDef>(setMethodsByPropertyName.size());
        for (Map.Entry<String, Method> entry : setMethodsByPropertyName.entrySet()) {
            Method setMethod = entry.getValue();
            Method getMethod = getMethodsByPropertyName.get(entry.getKey());
            String name = entry.getKey();
            Class type = getMethod.getReturnType();
            Id idAnot = getAnnotation(Id.class, getMethod, setMethod);
            Name nameAnot = getAnnotation(Name.class, getMethod, setMethod);

            if (nameAnot != null) {
                name = nameAnot.value();
            }
            int id = -1;
            if (idAnot != null) {
                id = idAnot.value();
            }
            Accessor<Object, Object> accessor = new MethodAccessor(getMethod, setMethod);

            Required requiredAnot = getAnnotation(Required.class, getMethod, setMethod);
            boolean required = type.isPrimitive() || requiredAnot != null;

            Enumeration enumAnot = getAnnotation(Enumeration.class, getMethod, setMethod);
            Time timeAnot = getAnnotation(Time.class, getMethod, setMethod);
            Sequence sequenceAnot = getAnnotation(Sequence.class, getMethod, setMethod);
            Static staticAnot = getAnnotation(Static.class, getMethod, setMethod);
            Unsigned unsignedAnot = getAnnotation(Unsigned.class, getMethod, setMethod);
            SmallDecimal smallDecimalAnot = getAnnotation(SmallDecimal.class, getMethod, setMethod);
            Class<?> componentType = sequenceAnot != null ? sequenceAnot.value() : type.getComponentType();
            Annotate annotateAnot = getAnnotation(Annotate.class, getMethod, setMethod);

            TypeDef typeDef = getTypeDef(type, sequenceAnot, enumAnot, timeAnot,
                    staticAnot != null, unsignedAnot != null, smallDecimalAnot != null,
                    namedTypes, groupsByClass);
            FieldDef fieldDef = new FieldDef(name, id, required, typeDef,
                    toAnnotationsMap(annotateAnot),
                    new FieldBinding(accessor, type, componentType));
            fields.add(fieldDef);
        }

        // sort fields according to id and then name
        Collections.sort(fields, new FieldDefComparator());
        for (FieldDef field : fields) {
            group.addField(field);
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TypeDef getTypeDef(Class<?> type, Sequence sequenceAnot, Enumeration enumAnot, Time timeAnot,
            boolean isStatic, boolean isUnsigned, boolean isSmallDecimal,
            Map<String, NamedType> namedTypes, Map<Class<?>, GroupMeta> groups) {
        // sequence
        if (sequenceAnot != null) {
            if (!Collection.class.isAssignableFrom(type) && !type.isArray()) {
                throw new IllegalArgumentException(
                        "Illegal Sequence annotation. Field type must be Collection (or subclass) or array.");
            }
            if (type.isArray() && !type.getComponentType().equals(sequenceAnot.value())) {
                throw new IllegalArgumentException(
                        "Illegal sequence annotation. Field type must be array of " +
                type.getComponentType().getName());
            }

            TypeDef elementType = getTypeDef(sequenceAnot.value(), null, enumAnot, timeAnot,
                    isStatic, isUnsigned, isSmallDecimal, namedTypes, groups);
            return new TypeDef.Sequence(elementType);
        }

        // enumeration
        if (type.isEnum() || enumAnot != null) {
            if (enumAnot != null && !type.equals(int.class) && !type.equals(Integer.class) &&
                    !type.equals(enumAnot.value())) {
                    throw new IllegalArgumentException(
                            "Illegal Enum annotation. Field type must be int, Integer or " +
                    enumAnot.value().getName());
                }
            Class enumType = enumAnot != null ? enumAnot.value() : type;

            Collection<Symbol> symbols = EnumSymbols.createSymbolMap(enumType).values();
            NamedType namedType = new NamedType(enumType.getSimpleName(), new TypeDef.Enum(symbols), null);
            namedTypes.put(namedType.getName(), namedType);

            return new TypeDef.Reference(enumType.getSimpleName());
        }

        // time
        if (type.equals(Date.class) || timeAnot != null) {
            if (timeAnot == null) {
                return TypeDef.DATETIME_MILLIS_UTC;
            } else {
                TimeZone zone = null;
                if (!timeAnot.timeZone().equals("")) {
                    zone = TimeZone.getTimeZone(timeAnot.timeZone());
                }
                return new TypeDef.Time(timeAnot.unit(), timeAnot.epoch(), zone);
            }
        }

        // basic types
        if (type.equals(byte.class) || type.equals(Byte.class)) {
            return isUnsigned ? TypeDef.UINT8 : TypeDef.INT8;
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            return isUnsigned ? TypeDef.UINT16 : TypeDef.INT16;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return isUnsigned ? TypeDef.UINT32 : TypeDef.INT32;
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return isUnsigned ? TypeDef.UINT64 : TypeDef.INT64;
        } else if (type.equals(BigInteger.class)) {
            return TypeDef.BIGINT;
        } else if (type.equals(BigDecimal.class)) {
            if (isSmallDecimal) {
                return TypeDef.DECIMAL;
            } else {
                return TypeDef.BIGDECIMAL;
            }
        } else if (type.equals(BigDecimal.class)) {
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return TypeDef.FLOAT32;
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return TypeDef.FLOAT64;
        } else if (type.equals(String.class)) {
            return TypeDef.STRING;
        } else if (type.equals(byte[].class)) {
            return TypeDef.BINARY;
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return TypeDef.BOOLEAN;
        }

        // reference
        if (type.equals(Object.class)) {
            if (isStatic) {
                throw new IllegalArgumentException("Illegal Static annotation. Object references cannot be static.");
            }
            return new TypeDef.DynamicReference(null);
        }
        GroupMeta group = groups.get(type);
        if (group != null) {
            if (isStatic) {
                return new TypeDef.Reference(group.getName());
            } else {
                return new TypeDef.DynamicReference(group.getName());
            }
        }

        throw new IllegalArgumentException("Illegal field type. " + type.getName());
    }

     private <T extends Annotation> T getAnnotation(Class<T> annotationClass, Method ... methods) {
        for (Method method : methods) {
            T annotation = method.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private String toPropertyName(String name) {
        if (name.length() == 0) {
            return "";
        } else {
            StringBuilder str = new StringBuilder(name);
            str.setCharAt(0, Character.toLowerCase(str.charAt(0)));
            return str.toString();
        }
    }


    private static class GroupMeta {
        private final Class<?> javaClass;
        private int id;
        private String name;
        private GroupMeta parent;
        private List<FieldDef> fields = new LinkedList<FieldDef>();
        private Map<String, String> annotations;
        public GroupMeta(Class<?> javaClass) {
            if (Modifier.isAbstract(javaClass.getModifiers())) {
                throw new IllegalArgumentException("Java class must not be abstract: " + javaClass.getName());
            }
            this.javaClass = javaClass;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public GroupMeta getParent() {
            return parent;
        }
        public void setParent(GroupMeta parent) {
            this.parent = parent;
        }
        public Class<?> getJavaClass() {
            return javaClass;
        }
        public void addField(FieldDef field) {
            fields.add(field);
        }
        public List<FieldDef> getFields() {
            return fields;
        }
        public Map<String, String> getAnnotations() {
            return annotations;
        }
        public void setAnnotations(Map<String, String> annotations) {
            this.annotations = annotations;
        }
    }

    /** Compares groups with ascending parent count, followed by group id.
     *
     * @author mikael.brannstrom
     *
     */
    private static class GroupMetaComparator implements Comparator<GroupMeta> {
        @Override
        public int compare(GroupMeta group1, GroupMeta group2) {
            int parentCount1 = parentCount(group1);
            int parentCount2 = parentCount(group1);

            if (parentCount1 < parentCount2) {
                return -1;
            } else if (parentCount1 > parentCount2) {
                return 1;
            } else {
                return Integer.compare(group1.getId(), group2.getId());
            }
        }

        private int parentCount(GroupMeta group) {
            if (group.getParent() == null) {
                return 0;
            } else {
                return 1 + parentCount(group.getParent());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static class MethodAccessor implements Accessor {
        private final Method getMethod;
        private final Method setMethod;
        public MethodAccessor(Method getMethod, Method setMethod) {
            this.getMethod = getMethod;
            this.setMethod = setMethod;
        }
        @Override
        public Object getValue(Object obj) {
            try {
                return getMethod.invoke(obj);
            } catch (IllegalAccessException e) {
                throw new Error("Should not happen", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Getter throwed exception", e);
            }
        }
        @Override
        public void setValue(Object obj, Object value) {
            try {
                setMethod.invoke(obj, value);
            } catch (IllegalAccessException e) {
                throw new Error("Should not happen", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Setter throwed exception", e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static class ConstructorFactory implements Factory {
        private final Constructor constructor;
        public ConstructorFactory(Constructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public Object newInstance() {
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

    }

    private static class FieldDefComparator implements Comparator<FieldDef> {
        @Override
        public int compare(FieldDef field1, FieldDef field2) {
            int id1 = field1.getId();
            int id2 = field2.getId();
            if (id1 == -1) {
                if (id2 == -1) {
                    return field1.getName().compareTo(field2.getName());
                } else {
                    return 1;
                }
            } else if (id2 == -1) {
                return -1;
            } else {
                return Long.compare(0xffffffffL & id1, 0xffffffffL & id2);
            }
        }
    }

}
