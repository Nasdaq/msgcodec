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
package com.cinnober.msgcodec.javadoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;

/**
 * @author mikael.brannstrom
 *
 */
public class GroupDefDoc {
    private final String className;
    private final String name;
    private final ClassDoc classDoc;
    private final List<FieldDefDoc> fields;

    public GroupDefDoc(ClassDoc doc) {
        this.classDoc = doc;
        this.name = DocAnnotationDoclet.getNameAnnotation(classDoc.annotations(), classDoc.simpleTypeName());
        this.className = classDoc.qualifiedName();

        // fields
        MethodDoc[] methods = getMethods(classDoc);
        Map<String, MethodDoc> getMethodsByPropertyName = new HashMap<>(methods.length);
        Map<String, MethodDoc> setMethodsByPropertyName = new HashMap<>(methods.length);

        // clear all non-public or static methods
        for (int i=0; i<methods.length; i++) {
            if (!methods[i].isPublic() || methods[i].isStatic()) {
                methods[i] = null;
            }
        }

        // find get methods
        for (MethodDoc method : methods) {
            if (method == null) {
                continue;
            }
            if (method.parameters().length != 0) {
                continue; // must not have any parameters
            }
            if (method.returnType().equals("void")) { // TODO
                continue; // must have a return type
            }
            // check prefix
            String propertyName = null;
            if (method.name().startsWith("get")) {
                propertyName = toPropertyName(method.name().substring(3));
            } else if (method.name().startsWith("is") &&
                    (method.returnType().equals("boolean") || // TODO
                            method.returnType().equals("Boolean"))) { // TODO
                propertyName = toPropertyName(method.name().substring(2));
            }

            if (propertyName != null) {
                getMethodsByPropertyName.put(propertyName, method);
            }
        }

        // find set methods
        for (MethodDoc method : methods) {
            if (method == null) {
                continue;
            }
            if (method.parameters().length != 1) {
                continue; // must have one parameter
            }
            // relax: ignore return type (should be void)

            // check prefix
            if (method.name().startsWith("set")) {
                String propertyName = toPropertyName(method.name().substring(3));
                MethodDoc getMethod = getMethodsByPropertyName.get(propertyName);
                if (getMethod == null) {
                    continue; // no matching get method
                }
                if (!getMethod.returnType().qualifiedTypeName().equals(
                        method.parameters()[0].type().qualifiedTypeName())) {
                    continue; // parameter type of set-method must match return type of get-method
                }
                setMethodsByPropertyName.put(propertyName, method);
            }
        }

        fields = new ArrayList<FieldDefDoc>(setMethodsByPropertyName.size());
        for (Map.Entry<String, MethodDoc> entry : setMethodsByPropertyName.entrySet()) {
            MethodDoc setMethod = entry.getValue();
            MethodDoc getMethod = getMethodsByPropertyName.get(entry.getKey());
            String name = entry.getKey();

            name = DocAnnotationDoclet.getNameAnnotation(getMethod.annotations(), name);
            name = DocAnnotationDoclet.getNameAnnotation(setMethod.annotations(), name);

            fields.add(new FieldDefDoc(name, getMethod, setMethod));
        }

    }

    private MethodDoc[] getMethods(ClassDoc classDoc) {
        ArrayList<MethodDoc> methods = new ArrayList<MethodDoc>(classDoc.methods().length * 3);
        addMethods(classDoc, methods);
        return methods.toArray(new MethodDoc[methods.size()]);
    }
    private void addMethods(ClassDoc classDoc, Collection<MethodDoc> methods) {
        ClassDoc superClass = classDoc.superclass();
        if (superClass != null) {
            addMethods(superClass, methods);
        }
        for (MethodDoc method : classDoc.methods()) {
            methods.add(method);
        }
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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the fields
     */
    public List<FieldDefDoc> getFields() {
        return fields;
    }

    public void addDoc(Properties properties, String annotationName) {
        String doc = classDoc.commentText();
        if (doc.trim().length() > 0) {
            properties.setProperty(name + "@" + annotationName, doc);
        }

        for (FieldDefDoc field : fields) {
            field.addDoc(properties, annotationName, name);
        }
    }

}
