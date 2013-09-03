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

import java.text.BreakIterator;
import java.util.Properties;

import com.sun.javadoc.MethodDoc;

/**
 * @author mikael.brannstrom
 *
 */
public class FieldDefDoc {
    private final String name;
    private final MethodDoc getMethodDoc;
    @SuppressWarnings("unused")
    private final MethodDoc setMethodDoc;
    /**
     * @param name
     * @param getMethodDoc
     * @param setMethodDoc
     */
    public FieldDefDoc(String name, MethodDoc getMethodDoc,
            MethodDoc setMethodDoc) {
        this.name = name;
        this.getMethodDoc = getMethodDoc;
        this.setMethodDoc = setMethodDoc;
    }

    public void addDoc(Properties properties, String annotationName, String groupName) {
        String doc = getMethodDoc.commentText();
        BreakIterator sentence = BreakIterator.getSentenceInstance();
        sentence.setText(doc);
        int index = sentence.next();
        if (index == BreakIterator.DONE) {
            doc = "";
        } else {
            doc = doc.substring(index);
        }

        if (doc.trim().length() > 0) {
            properties.setProperty(groupName + "." + name + "@" + annotationName,
                    doc);
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}
