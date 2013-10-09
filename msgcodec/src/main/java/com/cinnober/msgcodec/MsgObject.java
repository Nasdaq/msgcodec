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

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;

/**
 * Base class or stand-alone utility for annotated messages.
 * Inherited classes will get an auto-generated toString that includes all fields in the class.
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

    private static final ConcurrentHashMap<Class<?>, ClassToString> classToStringByClass = new ConcurrentHashMap<>();

    private static ClassToString getClassToString(Class<?> javaClass) {
        ClassToString classToString = classToStringByClass.get(javaClass);
        if (classToString == null) {

            // init
            Class<?> superClass = javaClass.getSuperclass();
            ArrayList<FieldToString> fields = new ArrayList<>();

            // copy fields from superclass
            if (!superClass.equals(MsgObject.class) && !superClass.equals(Object.class)) {
                @SuppressWarnings("unchecked")
                ClassToString superClassToString = getClassToString(superClass);
                for (FieldToString fieldToString : superClassToString.fields) {
                    fields.add(fieldToString);
                }
            }

            // add declared fields
            ArrayList<FieldToString> declaredFields = new ArrayList<>();
            for (Field field : javaClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                declaredFields.add(new FieldToString(field));
            }
            Collections.sort(declaredFields);
            fields.addAll(declaredFields);

            classToString = new ClassToString(javaClass, fields.toArray(new FieldToString[fields.size()]));

            // store
            ClassToString prev = classToStringByClass.putIfAbsent(javaClass, classToString);
            classToString = prev != null ? prev : classToString;
        }
        return classToString;
    }

    /**
     * Returns a string representation of this object.
     */
    @Override
    public String toString() {
        return getClassToString(getClass()).toString(this);
    }

    /**
     * Returns a string representation of the specified object.
     * @param obj the obj, or null.
     * @return the string
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return getClassToString(obj.getClass()).toString(obj);
    }

    /**
     * Encapsulation of a field, which can generate a string representation of a field value.
     *
     * @author mikael.brannstrom
     */
    private static class FieldToString implements Comparable<FieldToString> {
        private final Field field;
        private final String name;
        private final int id;
        FieldToString(Field field) {
            this.field = field;
            field.setAccessible(true);
            Name nameAnot = field.getAnnotation(Name.class);
            this.name = nameAnot != null ? nameAnot.value() : field.getName();
            Id idAnot = field.getAnnotation(Id.class);
            this.id = idAnot != null ? -idAnot.value() : -1;
        }
        /**
         * Appends the field name and value to the specified string builder.
         *
         * @param obj the object the field belongs to, not null.
         * @param appendTo where to append the string, not null.
         */
        void append(Object obj, StringBuilder appendTo) {
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                value = e.toString();
            }
            appendTo.append(name).append('=').append(value);
        }
        @Override
        public int compareTo(FieldToString other) {
            int id1 = this.id;
            int id2 = other.id;
            if (id1 == -1) {
                if (id2 == -1) {
                    return this.name.compareTo(other.name);
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

    /**
     * Encapsulation of a class, which can generate a string representation of an object of the class.
     *
     * @author mikael.brannstrom
     */
    private static class ClassToString {
        private final Class<?> javaClass;
        private final String name;
        private final FieldToString[] fields;
        ClassToString(Class<?> javaClass, FieldToString[] fields) {
            this.javaClass = javaClass;
            Name nameAnot = javaClass.getAnnotation(Name.class);
            this.name = nameAnot != null ? nameAnot.value() : javaClass.getSimpleName();
            this.fields = fields;
        }

        /**
         * Returns a string representation for the specified object.
         *
         * @param obj the object, not null
         * @return the string
         */
        String toString(Object obj) {
            StringBuilder str = new StringBuilder();
            str.append(name).append(" [");
            for (int i=0; i<fields.length; i++) {
                if (i != 0) {
                    str.append(", ");
                }
                fields[i].append(obj, str);
            }
            return str.append(']').toString();
        }
    }
}
