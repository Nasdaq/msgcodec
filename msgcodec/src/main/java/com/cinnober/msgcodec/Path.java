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
