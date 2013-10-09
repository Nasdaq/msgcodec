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
import java.util.List;
import java.util.Properties;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;

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
        fields = new ArrayList<FieldDefDoc>();
        for (FieldDoc field : getFields(classDoc)) {
            if (field.isStatic()) {
                continue;
            }
            String name = DocAnnotationDoclet.getNameAnnotation(field.annotations(), field.name());

            fields.add(new FieldDefDoc(name, field));
        }

    }

    private static Collection<FieldDoc> getFields(ClassDoc classDoc) {
        ArrayList<FieldDoc> fields = new ArrayList<FieldDoc>();
        addFields(classDoc, fields);
        return fields;
    }
    private static void addFields(ClassDoc classDoc, Collection<FieldDoc> fields) {
        ClassDoc superClass = classDoc.superclass();
        if (superClass != null) {
            addFields(superClass, fields);
        }
        for (FieldDoc method : classDoc.fields()) {
            fields.add(method);
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
