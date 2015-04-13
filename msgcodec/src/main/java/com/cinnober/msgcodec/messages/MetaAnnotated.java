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
package com.cinnober.msgcodec.messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Sequence;
import java.util.List;

/**
 * Base class for annotated messages.
 *
 * @author mikael.brannstrom
 */
@Name("Annotated")
public class MetaAnnotated extends MsgObject {
    /**
     * Annotations. The same annotation name must not occur twice.
     */
    @Id(100)
    @Sequence(MetaAnnotation.class)
    public List<MetaAnnotation> annotations;

    public void setAnnotations(Map<String, String> annotations) {
        if (annotations == null) {
            this.annotations = null;
        } else {
            this.annotations = new ArrayList<>(annotations.size());
            for (Map.Entry<String, String> entry : annotations.entrySet()) {
                this.annotations.add(new MetaAnnotation(entry.getKey(), entry.getValue()));
            }
        }
    }

    public Map<String, String> toAnnotationsMap() {
        if (annotations == null) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<>(annotations.size() * 2);
        for (MetaAnnotation annotation : annotations) {
            map.put(annotation.name, annotation.value);
        }
        return map;
    }
}
