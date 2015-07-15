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

import com.cinnober.msgcodec.TypeDef.Symbol;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Enumeration;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.MaxSize;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.SmallDecimal;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;
import com.cinnober.msgcodec.visitor.AnnotatedVisitor;
import com.cinnober.msgcodec.visitor.FieldDefVisitor;
import com.cinnober.msgcodec.visitor.GroupDefVisitor;
import com.cinnober.msgcodec.visitor.SchemaProducer;
import com.cinnober.msgcodec.visitor.SchemaVisitor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Supplier;

/**
 * The schema builder can build a schema from a collection of java classes.
 * <p>All non-static fields of the Java classes must be annotated properly, see the com.cinnober.msgcodec.anot package.
 * The default constructor must exist, but it need not be public.
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
public class SchemaBuilder {

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
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Annotation>, AnnotationMapper> annotationMappers = new HashMap<>();
    private final Set<Class<?>> messageClasses = new LinkedHashSet<>();

    /**
     * Create a schema builder.
     */
    public SchemaBuilder() {
        this(false);
    }

    /**
     * Create a schema builder.
     *
     * @param strict true if unecessary annotations should be checked for, otherwise false (default).
     */
    public SchemaBuilder(boolean strict) {
        this.strict = strict;
    }

    /**
     * Set strict validation mode.
     * 
     * @param strict true if unecessary annotations should be checked for, otherwise false (default).
     * @return this
     */
    public SchemaBuilder setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    /**
     * Returns true if strict validation should be applied.
     * 
     * @return true if unecessary annotations should be checked for, otherwise false.
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Add an annotation mapper to automatically import the specified annotation type.
     * The specified annotation type will be imported from classes and fields and converted to a "key=value" string.
     *
     * @param <T> the annotation type
     * @param annotationType the annotation type, not null
     * @param mapper the mapper that can convert the annotation to a "key=value" string.
     * @return this
     */
    public <T extends Annotation> SchemaBuilder addAnnotationMapper(
            Class<T> annotationType, AnnotationMapper<T> mapper) {
        annotationMappers.put(Objects.requireNonNull(annotationType), Objects.requireNonNull(mapper));
        return this;
    }


    /**
     * Add a set of message classes to this builder.
     * @param messageClasses the Java classes that should be included in the schema
     * @return this
     */
    public SchemaBuilder addMessages(Collection<Class<?>>  messageClasses) {
        this.messageClasses.addAll(messageClasses);
        return this;
    }

    /**
     * Add a set of message classes to this builder.
     * @param messageClasses the Java classes that should be included in the schema
     * @return this
     */
    public SchemaBuilder addMessages(Class<?> ... messageClasses) {
        return addMessages(Arrays.asList(messageClasses));
    }

    /**
     * Build a schema from the specified Java classes.
     *
     * <p>This is a shorthand for:<br>
     * <code>builder.addMessages(messageTypes).build()</code>
     *
     * @param messageTypes the Java classes that should be included in the schema.
     * @return the schema.
     * @throws IllegalArgumentException if the schema could not be built due to wrong input.
     * E.g. wrong annotations etc.
     * @see #build()
     * @see #addMessages(java.util.Collection) 
     */
    public Schema build(Collection<Class<?>> messageTypes) throws IllegalArgumentException {
        return addMessages(messageTypes).build();
    }

    /**
     * Build a schema from the specified Java classes.
     *
     * <p>This is a shorthand for:<br>
     * <code>builder.addMessages(messageTypes).build()</code>
     *
     * @param messageClasses the Java classes that should be included in the schema.
     * @return the schema.
     * @throws IllegalArgumentException if the schema could not be built due to wrong input.
     * E.g. wrong annotations etc.
     * @see #build()
     * @see #addMessages(java.lang.Class...) 
     */
    public Schema build(Class<?> ... messageClasses) throws IllegalArgumentException {
        return addMessages(messageClasses).build();
    }

    /**
     * Build a schema from the added message classes.
     * Any component groups or enumerations that are referred to will be automatically included in the schema.
     *
     * <p>The schema is bound the the messages classes.
     *
     * @return the schema
     * @throws IllegalArgumentException if the schema could not be built due to wrong input.
     * E.g. wrong annotations etc.
     */
    public Schema build() {
        SchemaProducer sp = new SchemaProducer();
        visit(sp);
        return sp.getSchema();
    }

    /**
     * Visit the schema from the added message classes.
     * Any component groups or enumerations that are referred to will be automatically included in the schema.
     *
     * <p>The schema is bound the the messages classes.
     * 
     * @param sv the schema visitor, not null.
     */
    public void visit(SchemaVisitor sv) {
        visit(findAllGroups(messageClasses), sv);
    }

    /**
     * Find all groups referenced by, and including the specified groups.
     * @param groups the groups to scan, not null.
     * @return all groups found, not null.
     */
    private Set<Class<?>> findAllGroups(Set<Class<?>> groups) {
        groups = new LinkedHashSet<>(groups);
        // scan groups for referenced component groups
        LinkedList<Class<?>> groupsToScan = new LinkedList<>(groups);
        while (!groupsToScan.isEmpty()) {
            Class<?> group = groupsToScan.removeFirst();
            scanGroup(group, groups, groupsToScan);
        }
        return groups;
    }

    /**
     * Scan all field types to automatically find all group types.
     *
     * @param group the group to scan, not null.
     * @param groups all groups, not null. Newly found groups will be added here.
     * @param groupsToScan the groups to be scanned, not null. Newly found groups will be added here.
     */
    private void scanGroup(
            Class<?> group,
            Set<Class<?>> groups,
            LinkedList<Class<?>> groupsToScan) {
        HashMap<Type, Class<?>> genericParameters = new HashMap<>();

        Class<?> javaClass = group;
        do {
            for (Field field : javaClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Class<?> type = getType(field, genericParameters);
                final Sequence sequenceAnot = field.getAnnotation(Sequence.class);
                if (type.isArray()) {
                    type = getComponentType(field, genericParameters);
                } else if (sequenceAnot != null) {
                    Class<?> listElementType = getListComponentType(field, genericParameters);
                    type = listElementType != null ? listElementType : sequenceAnot.value();
                }
                scanType(type, groups, groupsToScan);
            }

            updateGenericParameters(javaClass, genericParameters);
            javaClass = javaClass.getSuperclass();
        } while (javaClass != null);
    }

    /**
     * Scan the type to automatically add group types.
     *
     * If the type is a group type, a new group is added to groups and groupsToScan.
     *
     * @param type the type, not null
     * @param groups all groups, not null. Newly found groups will be added here.
     * @param groupsToScan the groups to be scanned, not null. Newly found groups will be added here.
     */
    private void scanType(
            Class<?> type,
            Set<Class<?>> groups,
            LinkedList<Class<?>> groupsToScan) {
        if (nativeTypes.contains(type) || type.isEnum()) {
            return;
        }

        if (type.equals(Object.class)) {
            return; // placeholder for 'any' type
        }

        if (!groups.contains(type)) {
            groups.add(type);
            groupsToScan.add(type);
        }
    }

    /**
     * Visit the schema for the specified groups.
     * @param groups java class of the groups to visit.
     * @throws IllegalArgumentException if the schema could not be visited due to wrong input.
     * E.g. wrong annotations etc.
     */
    @SuppressWarnings({ "rawtypes" })
    private void visit(Set<Class<?>> groups, SchemaVisitor sv) throws IllegalArgumentException {

        Map<String, NamedType> namedTypes = new LinkedHashMap<>();

        SchemaBinding binding = new SchemaBinding(JavaClassGroupTypeAccessor.INSTANCE);
        sv.visit(binding);

        // infer group names, ids and inheritance
        for (Class<?> group : groups) {
            Constructor constructor = null;
            for (Constructor constr : group.getDeclaredConstructors()) {
                if (constr.getParameterTypes().length == 0) {
                    constructor = constr;
                    break;
                }
            }
            if (constructor == null) {
                throw new IllegalArgumentException("No default constructor found for class " +
                        group.getName());
            }

            @SuppressWarnings("unchecked")
            ConstructorFactory factory = new ConstructorFactory(constructor);

            Class<?> superGroup = findSuperGroup(group, groups);

            GroupDefVisitor gv = sv.visitGroup(
                    getName(group),
                    getId(group),
                    superGroup != null ? getName(superGroup) : null,
                    new GroupBinding(factory, group));

            visitAnnotations(group, gv);
            visitFields(gv, group, superGroup, namedTypes, groups);
        }

        namedTypes.values().forEach(nt -> sv.visitNamedType(nt.getName(), nt.getType()));

        sv.visitEnd();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, String> visitAnnotations(AnnotatedElement element, AnnotatedVisitor av) {
        Map<String, String> map = new HashMap<>();
        Annotate annotateAnot = element.getAnnotation(Annotate.class);
        if (annotateAnot != null) {
            for (String keyValue : annotateAnot.value()) {
                visitAnnotation(map, keyValue, av);
            }
        }
        for (Map.Entry<Class<? extends Annotation>, AnnotationMapper> entry : annotationMappers.entrySet()) {
            Annotation anot = element.getAnnotation(entry.getKey());
            if (anot != null) {
                visitAnnotation(map, entry.getValue().map(anot), av);
            }
        }
        return map;
    }
    private static void visitAnnotation(Map<String, String> map, String keyValue, AnnotatedVisitor av) {
        int idx = keyValue.indexOf('=');
        if (idx == -1) {
            throw new IllegalArgumentException("Illegal annotation \"" + keyValue + "\"");
        }
        String key = keyValue.substring(0, idx);
        String value = keyValue.substring(idx + 1);
        map.put(key, value);
        if (av != null) {
            av.visitAnnotation(key, value);
        }
    }

    private Class<?> findSuperGroup(Class<?> group, Set<Class<?>> groups) {
        Class<?> superClass = group.getSuperclass();
        while(superClass != null) {
            if(groups.contains(superClass)) {
                return superClass;
            }
            superClass = superClass.getSuperclass();
        }
        return null;
    }

    /**
     * Visit all fields up to, but not including, the parent group.
     *
     * @param group
     */
    private void visitFields(
            GroupDefVisitor gv,
            Class<?> group,
            Class<?> parentGroup,
            Map<String, NamedType> namedTypes,
            Set<Class<?>> groups) {
        visitFields(gv, group, parentGroup, group, new HashMap<>(), namedTypes, groups);
    }

    @SuppressWarnings({ "rawtypes" })
    private void visitFields(
            GroupDefVisitor gv,
            Class<?> group,
            Class<?> parentGroup,
            Class<?> javaClass,
            Map<Type, Class<?>> genericParameters,
            Map<String, NamedType> namedTypes,
            Set<Class<?>> groups) {
        if (javaClass == null ||
            (parentGroup != null && parentGroup.equals(javaClass))) {
            return;
        }

        updateGenericParameters(javaClass, genericParameters);
        visitFields(gv, group, parentGroup, javaClass.getSuperclass(), genericParameters, namedTypes, groups);

        // fields are not sorted in any order, not even as they appear in the source code
        // instead we sort the fields according to id and name (later on)

        Field[] fields = javaClass.getDeclaredFields();

        // sort fields according to id and then name
        Arrays.sort(fields, new FieldComparator());

        for (Field field : fields) {
            try {
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
                    throw new IllegalArgumentException("@Required is not needed for primitives.");
                }
                boolean required = type.isPrimitive() || requiredAnot != null;

                Enumeration enumAnot = field.getAnnotation(Enumeration.class);
                Time timeAnot = field.getAnnotation(Time.class);
                Sequence sequenceAnot = field.getAnnotation(Sequence.class);
                Dynamic dynamicAnot = field.getAnnotation(Dynamic.class);
                Unsigned unsignedAnot = field.getAnnotation(Unsigned.class);
                SmallDecimal smallDecimalAnot = field.getAnnotation(SmallDecimal.class);
                MaxSize maxSizeAnot = field.getAnnotation(MaxSize.class);
                Class<?> componentType;
                if (sequenceAnot != null) {
                    Class<?> listComponentType = getListComponentType(field, genericParameters);
                    componentType = listComponentType != null ? listComponentType : sequenceAnot.value();
                } else {
                    componentType = getComponentType(field, genericParameters);
                }

                TypeDef typeDef = getTypeDef(type, componentType, sequenceAnot, enumAnot, timeAnot,
                        dynamicAnot, unsignedAnot, smallDecimalAnot, maxSizeAnot,
                        namedTypes, groups);
                
                FieldDefVisitor fv = null;
                if (gv != null) {
                    fv = gv.visitField(
                        name,
                        id,
                        required,
                        typeDef,
                        new FieldBinding(accessor, type, componentType));
                }

                visitAnnotations(field, fv);
                if (fv != null) {
                    fv.visitEnd();
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Illegal field type and/or annotations: " +
                    field.getDeclaringClass().getName() + "." + field.getName(), e);
            }
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
    private static Class<?> getType(Field field, Map<Type, Class<?>> genericParameters) {
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
     * Returns the field component type, by also resolving any generic type variables using
     * the supplied generic parameters map.
     *
     * @param field the field to return the type of, not null
     * @param genericParameters the map, not null
     * @return the field type, not null
     */
    private static Class<?> getComponentType(Field field, Map<Type, Class<?>> genericParameters) {
        Type genericType = field.getGenericType();
        if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            Type genericComponentType = genericArrayType.getGenericComponentType();
            Class<?> type = genericParameters.get(genericComponentType);
            if (type != null) {
                return type;
            }
        }
        return field.getType().getComponentType();
    }

    private static Class<?> getListComponentType(Field field, Map<Type, Class<?>> genericParameters) {
        Class<?> fieldType = field.getType();
        if (fieldType.equals(List.class)) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type listElementType = paramType.getActualTypeArguments()[0];
                Class<?> type = genericParameters.get(listElementType);
                if (type != null) {
                    return type;
                }
            }
        }
        return null;
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
            Map<Type, Class<?>> genericParameters) {
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
                    Class<?> actualType = genericParameters.get((TypeVariable<?>)actualTypeArguments[i]);
                    if (actualType != null) {
                        genericParameters.put(typeParameters[i], actualType);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TypeDef getTypeDef(Class<?> type, Class<?> componentType, Sequence sequenceAnot, Enumeration enumAnot, Time timeAnot,
            Dynamic dynamicAnot, Unsigned unsignedAnot, SmallDecimal smallDecimalAnot, MaxSize maxSizeAnot,
            Map<String, NamedType> namedTypes, Set<Class<?>> groups) {
        // sequence
        if (sequenceAnot != null || (type.isArray() && !type.equals(byte[].class))) {
            if (!List.class.equals(type) && !type.isArray()) {
                throw new IllegalArgumentException(
                        "Illegal @Sequence. Field type must be List or array.");
            }
            if (type.isArray() && sequenceAnot != null && !type.getComponentType().equals(sequenceAnot.value())) {
                throw new IllegalArgumentException(
                        "Illegal @Sequence. Field type must be array of " +
                type.getComponentType().getName());
            }
            if (strict && sequenceAnot != null && type.isArray() && !type.equals(byte[].class)) {
                throw new IllegalArgumentException("@Sequence is not needed for arrays.");
            }

            TypeDef elementType = getTypeDef(componentType, null, null, enumAnot, timeAnot,
                    dynamicAnot, unsignedAnot, smallDecimalAnot, maxSizeAnot, namedTypes, groups);
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

            assertNotAnnotated("Enum", timeAnot, dynamicAnot, unsignedAnot, smallDecimalAnot, maxSizeAnot);

            return new TypeDef.Reference(enumType.getSimpleName());
        }

        // time
        if (type.equals(Date.class) || timeAnot != null) {
            assertNotAnnotated("Time", dynamicAnot, unsignedAnot, smallDecimalAnot, maxSizeAnot);
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
            assertNotAnnotated(type.getName(), dynamicAnot, smallDecimalAnot, maxSizeAnot);
            return isUnsigned ? TypeDef.UINT8 : TypeDef.INT8;
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            assertNotAnnotated(type.getName(), dynamicAnot, smallDecimalAnot, maxSizeAnot);
            return isUnsigned ? TypeDef.UINT16 : TypeDef.INT16;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            assertNotAnnotated(type.getName(), dynamicAnot, smallDecimalAnot, maxSizeAnot);
            return isUnsigned ? TypeDef.UINT32 : TypeDef.INT32;
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            assertNotAnnotated(type.getName(), dynamicAnot, smallDecimalAnot, maxSizeAnot);
            return isUnsigned ? TypeDef.UINT64 : TypeDef.INT64;
        } else if (type.equals(BigInteger.class)) {
            assertNotAnnotated("BigInteger", unsignedAnot, dynamicAnot, smallDecimalAnot, maxSizeAnot);
            return TypeDef.BIGINT;
        } else if (type.equals(BigDecimal.class)) {
            assertNotAnnotated("BigDecimal", unsignedAnot, dynamicAnot, maxSizeAnot);
            if (smallDecimalAnot != null) {
                return TypeDef.DECIMAL;
            } else {
                return TypeDef.BIGDECIMAL;
            }
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot, maxSizeAnot);
            return TypeDef.FLOAT32;
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot, maxSizeAnot);
            return TypeDef.FLOAT64;
        } else if (type.equals(String.class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot);
            if (maxSizeAnot != null) {
                return new TypeDef.StringUnicode(maxSizeAnot.value());
            } else {
                return TypeDef.STRING;
            }
        } else if (type.equals(byte[].class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot);
            if (maxSizeAnot != null) {
                return new TypeDef.Binary(maxSizeAnot.value());
            } else {
                return TypeDef.BINARY;
            }
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            assertNotAnnotated(type.getName(), unsignedAnot, dynamicAnot, smallDecimalAnot, maxSizeAnot);
            return TypeDef.BOOLEAN;
        }

        // reference
        assertNotAnnotated("Group", unsignedAnot, smallDecimalAnot, maxSizeAnot);
        if (type.equals(Object.class)) {
            if (strict && dynamicAnot != null) {
                throw new IllegalArgumentException("@Dynamic is not needed for Object.");
            }
            return new TypeDef.DynamicReference(null);
        }
        if (groups.contains(type)) {
            String groupName = getName(type);
            if (dynamicAnot == null) {
                return new TypeDef.Reference(groupName);
            } else {
                return new TypeDef.DynamicReference(groupName);
            }
        }

        throw new IllegalArgumentException("Illegal field type. " + type.getName());
    }

    private static int getId(AnnotatedElement elem) {
        Id id = elem.getAnnotation(Id.class);
        return id != null ? id.value() : -1;
    }
    private static String getName(Field field) {
        return getName(field, field::getName);
    }
    private static String getName(Class<?> groupClass) {
        return getName(groupClass, groupClass::getSimpleName);
    }
    private static String getName(AnnotatedElement elem, Supplier<String> nameFn) {
        Name name = elem.getAnnotation(Name.class);
        return name != null ? name.value() : nameFn.get();
    }

    private static void assertNotAnnotated(String notApplicableFor, Annotation ... annotations) {
        for (Annotation annotation : annotations) {
            if (annotation != null) {
                throw new IllegalArgumentException("Illegal @" + annotation.annotationType().getSimpleName() +
                        ". Not applicable for " + notApplicableFor + ".");
            }
        }
    }

    private static class FieldComparator implements Comparator<Field> {
        @Override
        public int compare(Field field1, Field field2) {
            int id1 = getId(field1);
            int id2 = getId(field2);
            if (id1 == -1) {
                if (id2 == -1) {
                    return getName(field1).compareTo(getName(field2));
                } else {
                    return 1;
                }
            } else if (id2 == -1) {
                return -1;
            } else {
                return Integer.compareUnsigned(id1, id2);
            }
        }
    }
}
