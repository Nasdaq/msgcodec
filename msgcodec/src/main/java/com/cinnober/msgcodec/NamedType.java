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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import com.cinnober.msgcodec.messages.MetaNamedType;

/**
 * @author Mikael Brannstrom
 *
 */
public class NamedType implements Annotatable<NamedType> {
    private final String name;
    private final TypeDef type;
    private final Map<String, String> annotations;
    
    /**
     * Create a new named type.
     * @param name the name, not null
     * @param type the type, not null
     * @param annotations the annotations, or null for no annotations.
     */
    public NamedType(String name, TypeDef type, Map<String, String> annotations) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        if (annotations == null || annotations.isEmpty()) {
            this.annotations = Collections.emptyMap();
        } else {
            this.annotations = Collections.unmodifiableMap(new HashMap<String,String>(annotations));
        }
    }

    /**
     * Returns the name.
     * @return the name, not null.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type.
     * @return the type, not null.
     */
    public TypeDef getType() {
        return type;
    }

    @Override
    public NamedType replaceAnnotations(Annotations annotations) {
        return new NamedType(name, type, annotations.map());
    }

    @Override
    public NamedType addAnnotations(Annotations annotations) {
        Map<String, String> newAnnotations = new HashMap<>(this.annotations);
        newAnnotations.putAll(annotations.map());
        return new NamedType(name, type, newAnnotations);
    }

    @Override
    public String getAnnotation(String name) {
        return annotations.get(name);
    }

    @Override
    public Map<String, String> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if(!annotations.isEmpty()){
            for (Entry<String, String> ann : annotations.entrySet()) {
                str.append(Annotations.toString(ann.getKey(), ann.getValue()));
                str.append('\n');
            }
        }
        str.append(name).append(" = ").append(type.toString()).append("\n");
        return str.toString();
    }

    public MetaNamedType toMessage() {
        MetaNamedType message = new MetaNamedType(name, type.toMessage());
        message.setAnnotations(annotations);
        return message;
    }


    /** Convert a map of types to a collection of {@link NamedType} entries.
     *
     * @param namedTypes the named types, or null
     * @return collection of {@link NamedType}, or null if input is null.
     */
    public static Collection<NamedType> valueOf(Map<String, TypeDef> namedTypes) {
        if (namedTypes == null) {
            return null;
        }
        Collection<NamedType> namedTypesList = new ArrayList<>(namedTypes.size());
        for (Map.Entry<String, TypeDef> namedType : namedTypes.entrySet()) {
            namedTypesList.add(new NamedType(namedType.getKey(), namedType.getValue(), null));
        }
        return namedTypesList;
    }
}
