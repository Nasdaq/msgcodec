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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.cinnober.msgcodec.TypeDef.Symbol;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Enumeration;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.SmallDecimal;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;
import java.lang.annotation.Annotation;

/**
 * The protocol dictionary builder can build a protocol dictionary from a collection of java classes.
 * <p>All non-static fields of the Java classes must be annotated properly, see the com.cinnober.msgcodec.anot package.
 * The default constructor must exist.
 *
 * <p>Example:
 * <pre>
 * {@literal @}Id(123) // optional message id (recommended)
 * public class MyMessage extends MsgObj { // extend MsgObject to get toString
 *     {@literal @}Id(1) // optional field id (recommended)
 *     {@literal @}Unsigned // negative values are treated as the positive values 2^31 up to 2^32 - 1.
 *     public int id;
 *
 *     {@literal @}Id(2)
 *     {@literal @}Required // null values are not permitted, checked by the message codec
 *     public String name;
 *
 *     {@literal @}Id(3)
 *     public Long optionalValue;
 *
 *     {@literal @}Id(4)
 *     {@literal @}Static // only instances of MyMessage are permitted, not sub classes
 *     public MyMessage embedded;
 *
 *     public MyMessage() {} // default constructor must exist
 * }
 * </pre>
 *
 * @author mikael.brannstrom
 *
 */
public class ProtocolDictionaryBuilder {

    private static final Set<Class<?>> nativeTypes =
            new HashSet<>(new ArrayList<Class<?>>(Arrays.asList(
                    byte.class, Byte.class,
                    short.class, Short.class,
                    int.class, Integer.class,
                    long.class, Long.class,
                    float.class, Float.class,
                    double.class, Double.class,
                    BigDecimal.class,
                    BigInteger.class,
                    Date.class,
                    boolean.class, Boolean.class,
                    String.class,
                    byte[].class
                    )));

    private boolean strict;

    /**
     * Create a protocol dictionary builder with default behaviour.
     */
    public ProtocolDictionaryBuilder() {
    }
    
    /** 
     * Create a protocol dictionary builder.
     * 
     * @param strict true if unecessary annotations should be checked for, otherwise false (default).
     */
    public ProtocolDictionaryBuilder(boolean strict) {
        this.strict = strict;
    }
    
    /** Build a protocol dictionary from the specified Java classes.
     *
     * <p>The protocol dictionary built is bound to the specified classes.
     *
     * @param messageTypes the Java classes that should be included in the protocol dictionary.
     * Any component groups or enumerations that are referred to will be automatically included.
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
     * Any component groups or enumerations that are referred to will be automatically included.
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

        Map<String, NamedType> namedTypes = new LinkedHashMap<>();

        // scan groups for referenced component groups
        LinkedList<GroupMeta> groupsToScan = new LinkedList<>(groups.values());
        while (!groupsToScan.isEmpty()) {
            GroupMeta group = groupsToScan.removeFirst();
            scanGroup(group, groups, namedTypes, groupsToScan);
        }

        // infer group names, ids and inheritance
        for (GroupMeta group : groups.values()) {
            inferGroupName(group);
            inferGroupId(group);
            group.setAnnotations(toAnnotationsMap(group.getJavaClass().getAnnotation(Annotate.class)));
            inferGroupInheritance(group, groups);
        }

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


    /**
     * Scan all field types to automatically add GroupMeta objects for all group types.
     *
     * @param group the group to scan, not null.
     * @param groups all groups, not null. Newly found groups will be added here.
     * @param namedTypes the named types, not null
     * @param groupsToScan the groups to be scanned, not null. Newly found groups will be added here.
     */
    private void scanGroup(GroupMeta group,
            Map<Class<?>, GroupMeta> groups,
            Map<String, NamedType> namedTypes,
            LinkedList<GroupMeta> groupsToScan) {
        HashMap<TypeVariable<?>, Class<?>> genericParameters = new HashMap<>();

        Class<?> javaClass = group.getJavaClass();
        do {
            for (Field field : javaClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                scanType(getType(field, genericParameters),
                        field.getAnnotation(Sequence.class), groups, namedTypes, groupsToScan);
            }

            updateGenericParameters(javaClass, genericParameters);
            javaClass = javaClass.getSuperclass();
        } while (javaClass != null);
    }



    /**
     * Scan the type to automatically add GroupMeta objects for group types.
     *
     * If the type is a group type, a new GroupMeta is added to groups and groupsToScan.
     *
     * @param type the type, not null
     * @param sequenceAnot sequence annotation, or null
     * @param groups all groups, not null. Newly found groups will be added here.
     * @param namedTypes the named types, not null
     * @param groupsToScan the groups to be scanned, not null. Newly found groups will be added here.
     */
    private void scanType(Class<?> type, Sequence sequenceAnot,
            Map<Class<?>,
            GroupMeta> groups,
            Map<String, NamedType> namedTypes,
            LinkedList<GroupMeta> groupsToScan) {
        if (nativeTypes.contains(type) || type.isEnum()) {
            return;
        }

        if (type.isArray()) {
            scanType(type.getComponentType(), null, groups, namedTypes, groupsToScan);
        } else if (sequenceAnot != null) {
            scanType(sequenceAnot.value(), null, groups, namedTypes, groupsToScan);
        } else {
            if (type.equals(Object.class)) {
                return; // placeholder for any type
            }

            if (!groups.containsKey(type)) {
                GroupMeta group = new GroupMeta(type);
                groups.put(type, group);
                groupsToScan.add(group);
            }
        }
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
        findFields(group, group.getJavaClass(), new HashMap<TypeVariable<?>, Class<?>>(), namedTypes, groupsByClass);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void findFields(GroupMeta group, Class<?> javaClass,
            Map<TypeVariable<?>, Class<?>> genericParameters,
            Map<String, NamedType> namedTypes,
            Map<Class<?>, GroupMeta> groupsByClass) {
        if (javaClass == null ||
            (group.getParent() != null && group.getParent().getJavaClass().equals(javaClass))) {
            return;
        }

        updateGenericParameters(javaClass, genericParameters);
        findFields(group, javaClass.getSuperclass(), genericParameters, namedTypes, groupsByClass); // TODO: pass on generic type info here

        // fields are not sorted in any order, not even as they appear in the source code
        // instead we sort the fields according to id and name (later on)

        Field[] fields = javaClass.getDeclaredFields();
        ArrayList<FieldDef> fieldDefs = new ArrayList<>();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String name = field.getName();
            Class type = getType(field, genericParameters);
            Id idAnot = field.getAnnotation(Id.class);
            Name nameAnot = field.getAnnotation(Name.class);

            if (nameAnot != null) {
                name = nameAnot.value();
            }
            int id = -1;
            if (idAnot != null) {
                id = idAnot.value();
            }
            Accessor<Object, Object> accessor = new FieldAccessor(field);

            Required requiredAnot = field.getAnnotation(Required.class);
            if (strict && requiredAnot != null && type.isPrimitive()) {
                throw new IllegalArgumentException("@Required is not needed for primitives. " + field.toString());
            }
            boolean required = type.isPrimitive() || requiredAnot != null;

            Enumeration enumAnot = field.getAnnotation(Enumeration.class);
            Time timeAnot = field.getAnnotation(Time.class);
            Sequence sequenceAnot = field.getAnnotation(Sequence.class);
            Dynamic dynamicAnot = field.getAnnotation(Dynamic.class);
            Unsigned unsignedAnot = field.getAnnotation(Unsigned.class);
            SmallDecimal smallDecimalAnot = field.getAnnotation(SmallDecimal.class);
            Class<?> componentType = sequenceAnot != null ? sequenceAnot.value() : type.getComponentType();
            Annotate annotateAnot = field.getAnnotation(Annotate.class);

            TypeDef typeDef = getTypeDef(type, sequenceAnot, enumAnot, timeAnot,
                    dynamicAnot, unsignedAnot, smallDecimalAnot,
                    namedTypes, groupsByClass);
            FieldDef fieldDef = new FieldDef(name, id, required, typeDef,
                    toAnnotationsMap(annotateAnot),
                    new FieldBinding(accessor, type, componentType));
            fieldDefs.add(fieldDef);
        }

        // sort fields according to id and then name
        Collections.sort(fieldDefs, new FieldDefComparator());
        for (FieldDef field : fieldDefs) {
            group.addField(field);
        }
    }

    /**
     * Returns the field type, by also resolving any generic type variables using
     * the supplied generic parameters map.
     *
     * @param field the field to return the type of, not null
     * @param genericParameters the map, not null
     * @return the field type, not null
     */
    private Class<?> getType(Field field, Map<TypeVariable<?>, Class<?>> genericParameters) {
        Type genericType = field.getGenericType();
        if (genericType instanceof TypeVariable) {
            TypeVariable<?> genericVar = (TypeVariable<?>) genericType;
            Class<?> type = genericParameters.get(genericVar);
            if (type != null) {
                return type;
            }
        }
        return field.getType();
    }

    /**
     * Update the generic parameters map for any type parameters set in the <b>extends</b>
     * part of this class.
     *
     * For example,
     * we have the classes <code>Base&lt;T&gt;</code> and <code>Foo extends Base&gt;Bar&lt;</code>.
     * Then this method is invoked on Foo, which means that the type parameter T is mapped to Bar.class.
     * When the type parameter T is referenced in Base this will be replaced with Bar in this context.
     *
     * @param javaClass the class, not null.
     * @param genericParameters the parameters map, not null. This map will be updated
     */
    private void updateGenericParameters(Class<?> javaClass,
            Map<TypeVariable<?>, Class<?>> genericParameters) {
        Class<?> superclass = javaClass.getSuperclass();
        if (superclass == null) {
            return;
        }
        Type genericSuperclass = javaClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parametrizedSuperclass = (ParameterizedType) genericSuperclass;
            TypeVariable<?>[] typeParameters = superclass.getTypeParameters();
            Type[] actualTypeArguments = parametrizedSuperclass.getActualTypeArguments();
            for (int i=0; i<typeParameters.length; i++) {
                if (actualTypeArguments[i] instanceof Class) {
                    genericParameters.put(typeParameters[i], (Class<?>) actualTypeArguments[i]);
                } else if (actualTypeArguments[i] instanceof TypeVariable) {
                    Class<?> actualType = genericParameters.get((TypeVariable)actualTypeArguments[i]);
                    if (actualType != null) {
                        genericParameters.put(typeParameters[i], actualType);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TypeDef getTypeDef(Class<?> type, Sequence sequenceAnot, Enumeration enumAnot, Time timeAnot,
            Dynamic dynamicAnot, Unsigned unsignedAnot, SmallDecimal smallDecimalAnot,
            Map<String, NamedType> namedTypes, Map<Class<?>, GroupMeta> groups) {
        // sequence
        if (sequenceAnot != null || (type.isArray() && !type.equals(byte[].class))) {
            if (!Collection.class.isAssignableFrom(type) && !type.isArray()) {
                throw new IllegalArgumentException(
                        "Illegal @Sequence. Field type must be Collection or array.");
            }
            if (type.isArray() && sequenceAnot != null && !type.getComponentType().equals(sequenceAnot.value())) {
                throw new IllegalArgumentException(
                        "Illegal @Sequence. Field type must be array of " +
                type.getComponentType().getName());
            }
            if (strict && sequenceAnot != null && type.isArray() && !type.equals(byte[].class)) {
                throw new IllegalArgumentException("@Sequence is not needed for arrays.");
            }

            Class<?> componentType = sequenceAnot != null ? sequenceAnot.value() : type.getComponentType();
            TypeDef elementType = getTypeDef(componentType, null, enumAnot, timeAnot,
                    dynamicAnot, unsignedAnot, smallDecimalAnot, namedTypes, groups);
            if (elementType.getType() == TypeDef.Type.SEQUENCE) {
                throw new IllegalArgumentException("Sequence of sequence is not allowed");
            }
            return new TypeDef.Sequence(elementType);
        }
        
        // enumeration
        if (type.isEnum() || enumAnot != null) {
            if (enumAnot != null && !type.equals(int.class) && !type.equals(Integer.class) &&
                !type.equals(enumAnot.value())) {
                throw new IllegalArgumentException(
                        "Illegal @Enum. Field type must be int, Integer or " +
                enumAnot.value().getName());
            }
            if (strict && enumAnot != null && type.isEnum()) {
                throw new IllegalArgumentException("@Enum is not needed for Java enum types.");
            }
            Class enumType = enumAnot != null ? enumAnot.value() : type;

            Collection<Symbol> symbols = EnumSymbols.createSymbolMap(enumType).values();
            NamedType namedType = new NamedType(enumType.getSimpleName(), new TypeDef.Enum(symbols), null);
            namedTypes.put(namedType.getName(), namedType);

            assertNotAnnotated("Enum", timeAnot, dynamicAnot, unsignedAnot, smallDecimalAnot);
            
            return new TypeDef.Reference(enumType.getSimpleName());
        }

        // time
        if (type.equals(Date.class) || timeAnot != null) {
            assertNotAnnotated("Time", dynamicAnot, unsignedAnot, smallDecimalAnot);
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
        boolean isUnsigned = unsignedAnot != null;
        if (type.equals(byte.class) || type.equals(Byte.class)) {
            assertNotAnnotated(type.getName(), dynamicAnot, smallDecimalAnot);
            return isUnsigned ? TypeDef.UINT8 : TypeDef.INT8;
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            assertNotAnnotated(type.getName(), dynamicAnot, smallDecimalAnot);
            return isUnsigned ? TypeDef.UINT16 : TypeDef.INT16;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            assertNotAnnotated(type.getName(), dynamicAnot, smallDecimalAnot);
            return isUnsigned ? TypeDef.UINT32 : TypeDef.INT32;
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            assertNotAnnotated(type.getName(), dynamicAnot, smallDecimalAnot);
            return isUnsigned ? TypeDef.UINT64 : TypeDef.INT64;
        } else if (type.equals(BigInteger.class)) {
            assertNotAnnotated("BigInteger", unsignedAnot, dynamicAnot, smallDecimalAnot);
            return TypeDef.BIGINT;
        } else if (type.equals(BigDecimal.class)) {
            assertNotAnnotated("BigInteger", unsignedAnot, dynamicAnot);
            if (smallDecimalAnot != null) {
                return TypeDef.DECIMAL;
            } else {
                return TypeDef.BIGDECIMAL;
            }
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot);
            return TypeDef.FLOAT32;
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot);
            return TypeDef.FLOAT64;
        } else if (type.equals(String.class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot);
            return TypeDef.STRING;
        } else if (type.equals(byte[].class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot);
            return TypeDef.BINARY;
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot);
            return TypeDef.BOOLEAN;
        }

        // reference
        assertNotAnnotated("Group", unsignedAnot, smallDecimalAnot);
        if (type.equals(Object.class)) {
            if (strict && dynamicAnot != null) {
                throw new IllegalArgumentException("@Dynamic is not needed for Object.");
            }
            return new TypeDef.DynamicReference(null);
        }
        GroupMeta group = groups.get(type);
        if (group != null) {
            if (dynamicAnot == null) {
                return new TypeDef.Reference(group.getName());
            } else {
                return new TypeDef.DynamicReference(group.getName());
            }
        }

        throw new IllegalArgumentException("Illegal field type. " + type.getName());
    }

    private static void assertNotAnnotated(String notApplicableFor, Annotation ... annotations) {
        for (Annotation annotation : annotations) {
            if (annotation != null) {
                throw new IllegalArgumentException("Illegal @" + annotation.annotationType().getSimpleName() + 
                        ". Not applicable for " + notApplicableFor + ".");
            }
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
    private static class FieldAccessor implements Accessor {
        private final Field field;
        public FieldAccessor(Field field) {
            this.field = field;
            field.setAccessible(true);
        }
        @Override
        public Object getValue(Object obj) {
            try {
                return field.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new Error("Should not happen", e);
            }
        }
        @Override
        public void setValue(Object obj, Object value) {
            try {
                field.set(obj, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new Error("Should not happen", e);
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
