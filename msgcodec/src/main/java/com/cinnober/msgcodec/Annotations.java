/*
 * $Id: codetemplates.xml,v 1.4 2006/04/05 12:25:17 maal Exp $
 *
 * Copyright (c) 2009 Cinnober Financial Technology AB, Stockholm,
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Collection of annotations in a protocol dictionary.
 *
 * <p>Sample usage:
 * <pre>
 * Annotations annotations = new Annotations();
 * annotations.path("MyGroup").put("doc", "My group doc");
 * annotations.path("MyGroup", "MyField").put("doc", "My field doc");
 * </pre>
 *
 *
 * @author fredrik.bromee, Cinnober Financial Technology
 */
public class Annotations {

    private final Map<Path, Map<String,String>> map;
    private final Path currentPath;

    public Annotations() {
        this(new HashMap<Path, Map<String,String>>(), Path.EMPTY_PATH);
    }
    private Annotations(Map<Path, Map<String,String>> annotations, Path path) {
        this.map = annotations;
        this.currentPath = path;
    }

    /**
     * Get the annotation map for the specified absolute path.
     * @param absolutePath the absolute path (not relative to the current path)
     * @param create true if the map should be created and stored, otherwise false.
     * @return the map, not null (possibly an unmodifiable empty map).
     */
    private Map<String, String> getMap(Path absolutePath, boolean create) {
        Map<String, String> annotations = map.get(absolutePath);
        if (annotations == null){
            if (create) {
                annotations = new HashMap<>();
                map.put(absolutePath, annotations);
            } else {
                annotations = Collections.emptyMap();
            }
        }
        return annotations;
    }
    /**
     * Get the annotation map for the current path.
     * @param create true if the map should be created and stored, otherwise false.
     * @return the map, not null (possibly an unmodifiable empty map).
     */
    private Map<String, String> getMap(boolean create) {
    	return getMap(currentPath, create);
    }

    /** Returns the annotation map for the current path.
     * <p>Note: this is a unmodifiable map.
     *
     * @return the map from annotation name to annotation value, not null.
     */
    public Map<String, String> map() {
        return Collections.unmodifiableMap(getMap(false));
    }

    /** Navigate to the specified sub path.
     *
     * @param path the path to navigate to
     * @return the annotations collection for the specified path.
     */
    public Annotations path(String... path) {
        return new Annotations(map, currentPath.append(path));
    }

    /** Put an annotation name-value pair for the current path.
     *
     * @param name the annotation name, not null.
     * @param value the annotation value, not null.
     */
    public void put(String name, String value) {
        getMap(true).put(Objects.requireNonNull(name), Objects.requireNonNull(value));
    }

    public void putAll(Properties properties) {
    	for (Map.Entry<Object, Object> entry : properties.entrySet()) {
    		String name = (String) entry.getKey();
    		String value = (String) entry.getValue();
    		int atIdx = name.indexOf('@');
    		if (atIdx == -1) {
    			continue;
    		}
    		String annot = name.substring(atIdx + 1);
    		name = name.substring(0, atIdx);
    		Path path = currentPath.append(name.split("\\."));
    		getMap(path, true).put(annot, value);
    	}
    }

    /**
     * Return annotations as a property map.
     *
     * <p>The format of property names is <em>path.separated.by.dots@annotationName</em>.
     *
     * @return the properties, not null.
     */
    public Properties toProperties() {
    	Properties properties = new Properties();
    	for (Map.Entry<Path, Map<String,String>> entry : map.entrySet()) {
    		Path path = entry.getKey();
    		if (!path.startsWith(currentPath)) {
    			continue;
    		}
    		path = path.subPath(currentPath.length());
    		String namePrefix = path.toString();
    		for (Map.Entry<String, String> annot : entry.getValue().entrySet()) {
    			properties.put(namePrefix + "@" + annot.getKey(), annot.getValue());
    		}
    	}
    	return properties;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<Path, Map<String, String>> entry : map.entrySet()) {
            if (str.length() != 0) {
                str.append('\n');
            }
            for (Map.Entry<String, String> anotEntry : entry.getValue().entrySet()) {
                str.append(entry.getKey()).append(" <- ").append(toString(anotEntry.getKey(), anotEntry.getValue()));
            }

        }
        return str.toString();
    }

    /** Returns a string representation for the annotation name-value pair.
     *
     * @param name the annotation name, not null.
     * @param value the annotation value, not null.
     * @return the string representation, not null.
     */
    public static String toString(String name, String value) {
        StringBuilder str = new StringBuilder();
        str.append('@').append(name).append('=');

        str.append('"').append(escape(value)).append('"');

        return str.toString();
    }

    /** Escape an annotation value. */
    public static String escape(String str) {
    	StringBuilder sb = new StringBuilder((int) (str.length() * 1.2));
    	int length = str.length();
    	for (int i = 0; i < length; i++) {
    		char ch = str.charAt(i);
    		switch (ch) {
    		case '"':
    			sb.append("\\\"");
    			break;
    		case '\\':
    			sb.append("\\\\");
    			break;
    		case '\n':
    			sb.append("\\n");
    			break;
    		case '\r':
    			sb.append("\\r");
    			break;
    		case '\t':
    			sb.append("\\t");
    			break;
    		case '\0':
    			sb.append("\\0");
    			break;
			default:
				sb.append(ch);
				break;
    		}
    	}
    	return sb.toString();
    }

}
