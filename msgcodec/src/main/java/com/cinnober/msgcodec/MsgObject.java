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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Arrays;
import com.cinnober.msgcodec.MsgObjectValueHandler.*;
import static com.cinnober.msgcodec.MsgObjectValueHandler.*;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;
import java.lang.reflect.AnnotatedElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class or stand-alone utility for annotated messages.
 * Inherited classes will get an auto-generated toString, equals and hashCode that includes all fields in the class.
 * Name annotations of fields are honored.
 *
 * <p>Example:
 * <pre>
 * public class Hello extends MsgObject {
 *     {@literal}Required
 *     public String greeting;
 * }
 * </pre>
 *
 * The toString of this class would be e.g. <code>Hello [greeting=the text]</code>.
 *
 * @author mikael.brannstrom
 */
public class MsgObject {

    private static final ConcurrentHashMap<Class<?>, GroupHandler> groupHandlerByClass = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MsgObjectValueHandler> unsignedTypes = initUnsignedTypes();
    private static final Set<Class<?>> simpleTypes = initSimpleTypes();

    private static GroupHandler getGroupValueHandler(Class<?> javaClass) {
        GroupHandler classToString = groupHandlerByClass.get(javaClass);
        if (classToString == null) {
            classToString = new GroupHandler(javaClass);
            groupHandlerByClass.put(javaClass, classToString);

            // init
            Class<?> superClass = javaClass.getSuperclass();
            ArrayList<FieldHandler> fields = new ArrayList<>();

            // copy fields from superclass
            if (!superClass.equals(MsgObject.class) && !superClass.equals(Object.class)) {
                @SuppressWarnings("unchecked")
                GroupHandler superClassToString = getGroupValueHandler(superClass);
                for (FieldHandler fieldToString : superClassToString.fields()) {
                    fields.add(fieldToString);
                }
            }

            // add declared fields
            ArrayList<FieldHandler> declaredFields = new ArrayList<>();
            for (Field field : javaClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                declaredFields.add(new FieldHandler(field, getValueHandler(field)));
            }
            Collections.sort(declaredFields);
            fields.addAll(declaredFields);

            classToString.init(fields.toArray(new FieldHandler[fields.size()]));
        }
        return classToString;
    }

    private static MsgObjectValueHandler getValueHandler(Field field) {
        Class<?> type = field.getType();
        Sequence seqAnot = field.getAnnotation(Sequence.class);
        if (type.equals(byte.class) && seqAnot != null) {
            return BINARY;
        }
        if (seqAnot != null || type.isArray()) {
            // sequence
            Class<?> componentType = type.isArray() ? type.getComponentType() : seqAnot.value();
            MsgObjectValueHandler componentHandler = getNoSequenceValueHandler(field, componentType);
            if (type.isArray()) {
                return new ArraySequenceHandler(componentHandler);
            } else if (type.getClass().equals(Collection.class)) {
                return new CollectionSequenceHandler(componentHandler);
            } else {
                return SIMPLE;
            }
        }
        return getNoSequenceValueHandler(field, type);
    }

    private static MsgObjectValueHandler getNoSequenceValueHandler(AnnotatedElement field, Class<?> type) {
        if (field.getAnnotation(Unsigned.class) != null) {
            MsgObjectValueHandler handler = unsignedTypes.get(type);
            if (handler != null) {
                return handler;
            }
        }
        if (type.isEnum()) {
            return SIMPLE;
        } // PENDING: int enums?

        Time timeAnot = field.getAnnotation(Time.class);
        if (type.equals(Date.class)) {
            return new DateTimeHandler(timeAnot);
        }
        if (timeAnot != null) {
            if (type.equals(int.class) || type.equals(Integer.class)) {
                return new IntTimeHandler(timeAnot);
            } else if (type.equals(long.class) || type.equals(Long.class)) {
                return new LongTimeHandler(timeAnot);
            }
        }

        if (simpleTypes.contains(type)) {
            return SIMPLE;
        }

        return getGroupValueHandler(type);
    }

    /**
     * Returns a string representation of this object.
     * All fields are included in the string.
     * 
     * @return the string, not null.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        getGroupValueHandler(getClass()).appendToString(this, str);
        return str.toString();
    }

    /**
     * Check if the this is equal the other.
     * All fields are compared.
     *
     * @param obj the other object, or null
     * @return true if this and obje are equal, otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        return getGroupValueHandler(getClass()).equals(this, obj);
    }

    /**
     * Returns the hash code for this specified object.
     * All fields are included in the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return getGroupValueHandler(getClass()).hashCode(this);
    }



    /**
     * Returns a string representation of the specified object.
     * All fields are included in the string.
     * 
     * @param obj the object, or null.
     * @return the string, not null.
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return "null";
        }
        StringBuilder str = new StringBuilder();
        getGroupValueHandler(obj.getClass()).appendToString(obj, str);
        return str.toString();
    }

    /**
     * Check if the object are equal to each other.
     * All fields are compared.
     *
     * @param o1 the first object, or null
     * @param o2 the second object, or null.
     * @return true if o1 and o2 are equal (or both null), otherwise false.
     */
    public static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        if (o1 == o2) {
            return true;
        }
        if (o1.getClass() != o2.getClass()) {
            return false;
        }
        return getGroupValueHandler(o1.getClass()).equals(o1, o2);
    }

    /**
     * Returns the hash code for the specified object.
     * All fields are included in the hash code.
     * 
     * @param obj the object, or null.
     * @return the hash code.
     */
    public static int hashCode(Object obj) {
        if (obj == null) {
            return 13;
        } else {
            return getGroupValueHandler(obj.getClass()).hashCode(obj);
        }
    }


    private static Map<Class<?>, MsgObjectValueHandler> initUnsignedTypes() {
        HashMap<Class<?>, MsgObjectValueHandler> map = new HashMap<>();
        map.put(byte.class, UINT8);
        map.put(Byte.class, UINT8);
        map.put(short.class, UINT16);
        map.put(Short.class, UINT16);
        map.put(int.class, UINT32);
        map.put(Integer.class, UINT32);
        map.put(long.class, UINT64);
        map.put(Long.class, UINT64);
        return map;
    }

    private static Set<Class<?>>  initSimpleTypes() {
        HashSet<Class<?>> set = new HashSet<>();
        set.addAll(Arrays.asList(
                byte.class, Byte.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                BigInteger.class,
                BigDecimal.class,
                String.class));
        return set;
    }
}
