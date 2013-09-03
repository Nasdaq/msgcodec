/*
 * $Id: codetemplates.xml,v 1.4 2006/04/05 12:25:17 maal Exp $
 *
 * Copyright (c) 2009 Cinnober Financial Technology AB, Stockholm, Sweden. All rights reserved.
 *
 * This software is the confidential and proprietary information of Cinnober Financial Technology AB, Stockholm, Sweden.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cinnober.
 *
 * Cinnober makes no representations or warranties about the suitability of the software, either expressed or implied,
 * including, but not limited to, the implied warranties of merchantibility, fitness for a particular purpose, or
 * non-infringement. Cinnober shall not be liable for any damages suffered by licensee as a result of using, modifying,
 * or distributing this software or its derivatives.
 */
package com.cinnober.msgcodec;

import java.util.Arrays;
import java.util.Objects;

/**
 * Defines an absolute location in a {@link ProtocolDictionary}
 *
 * @author fredrik.bromee, Cinnober Financial Technology
 */
class Path {
    public static final Path EMPTY_PATH = new Path();

    private final String[] path;

    public Path(String... name) {
        this.path = name;
    }

    public String[] getNames() {
        String[] names = new String[path.length];
        System.arraycopy(path, 0, names, 0, path.length);
        return names;
    }

    public boolean startsWith(Path prefix) {
    	int length = prefix.path.length;
    	if (length > path.length) {
    		return false;
    	}
    	for (int i=0; i<length; i++) {
    		if (!path[i].equals(prefix.path[i])) {
    			return false;
    		}
    	}
    	return true;
    }

    public Path subPath(int start) {
    	if (start > path.length) {
    		throw new IndexOutOfBoundsException();
    	}
    	if (start == 0) {
    		return this;
    	}
    	if (start == path.length) {
    		return EMPTY_PATH;
    	}

    	String[] newNames = new String[path.length - start];
    	System.arraycopy(path, start, newNames, 0, newNames.length);
    	return new Path(newNames);
    }

    public String getName(int level) {
        return path[level];
    }

    public int length() {
        return path.length;
    }

    public boolean isEmpty() {
        return path.length == 0;
    }

    public Path append(String... name) {
        String[] newPath = new String[path.length+name.length];
        System.arraycopy(path, 0, newPath, 0, path.length);
        System.arraycopy(name, 0, newPath, path.length, name.length);
        return new Path(newPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object[])path);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Path) {
            Path other = (Path) obj;
            return Arrays.equals(path, other.path);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String name : path) {
            if (str.length() != 0) {
                str.append('.');
            }
            str.append(name);
        }
        return str.toString();
    }
}
