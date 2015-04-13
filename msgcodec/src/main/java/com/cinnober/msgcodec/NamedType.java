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

    @Override
    public int hashCode() {
        return Objects.hash(name, type, annotations);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NamedType other = (NamedType) obj;
        return Objects.equals(this.name, other.name) &&
                Objects.equals(this.type, other.type) &&
                Objects.equals(this.annotations, other.annotations);
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
