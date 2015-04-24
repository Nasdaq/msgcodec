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
        fields = new ArrayList<>();
        for (FieldDoc field : getFields(classDoc)) {
            if (field.isStatic()) {
                continue;
            }
            String fieldName = DocAnnotationDoclet.getNameAnnotation(field.annotations(), field.name());

            fields.add(new FieldDefDoc(fieldName, field));
        }

    }

    private static Collection<FieldDoc> getFields(ClassDoc classDoc) {
        ArrayList<FieldDoc> fields = new ArrayList<>();
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
