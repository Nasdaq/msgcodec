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
package com.cinnober.msgcodec.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.Static;

/**
 * @author mikael.brannstrom
 *
 */
@Name("Annotated")
public class MetaAnnotated {
    private Collection<MetaAnnotation> annotations;

    /**
     * @return the annotations
     */
    @Id(100)
    @Static
    @Sequence(MetaAnnotation.class)
    public Collection<MetaAnnotation> getAnnotations() {
        return annotations;
    }
    /**
     * @param annotations the annotations to set
     */
    public void setAnnotations(Collection<MetaAnnotation> annotations) {
        this.annotations = annotations;
    }

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
            map.put(annotation.getName(), annotation.getValue());
        }
        return map;
    }
}
