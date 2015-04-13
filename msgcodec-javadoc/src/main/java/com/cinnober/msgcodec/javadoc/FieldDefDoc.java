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

import java.util.Properties;

import com.sun.javadoc.FieldDoc;

/**
 * @author mikael.brannstrom
 *
 */
public class FieldDefDoc {
    private final String name;
    private final FieldDoc fieldDoc;

    public FieldDefDoc(String name, FieldDoc fieldDoc) {
        this.name = name;
        this.fieldDoc = fieldDoc;
    }

    public void addDoc(Properties properties, String annotationName, String groupName) {
        String doc = fieldDoc.commentText();

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
