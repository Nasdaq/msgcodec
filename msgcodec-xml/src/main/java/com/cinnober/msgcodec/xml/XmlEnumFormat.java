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
package com.cinnober.msgcodec.xml;

import java.util.Objects;

import com.cinnober.msgcodec.SymbolMapping;

/**
 * @author mikael.brannstrom
 *
 */
abstract class XmlEnumFormat<T> implements XmlFormat<T> {
    static class SymbolMappingEnumFormat<T> extends XmlEnumFormat<T> {
        private final SymbolMapping<T> symbolMapping;

        public SymbolMappingEnumFormat(SymbolMapping<T> symbolMapping) {
            Objects.requireNonNull(symbolMapping);
            this.symbolMapping = symbolMapping;
        }

        @Override
        public String format(T value) throws FormatException {
            try {
                String name = symbolMapping.getName(value);
                if (name == null) {
                    throw new FormatException("Not a valid enum: " + value);
                }
                return name;
            } catch (IllegalArgumentException e) {
                throw new FormatException("Not a valid enum: " + value, e);
            }
        }

        @Override
        public T parse(String str) throws FormatException {
            try {
                T value = symbolMapping.lookup(str);
                if (value == null) {
                }
                return value;
            } catch (IllegalArgumentException e) {
                throw new FormatException("Not a valid symbol: " + str, e);
            }
        }
    }
    
    static class DummyJavaEnumFormat<E extends Enum<E>> extends XmlEnumFormat<E> {
        public DummyJavaEnumFormat() {
        }

        @Override
        public String format(E value) throws FormatException {
            System.out.println("DummyJavaEnumFormat: " + value);
            return null;
        }

        @Override
        public E parse(String str) throws FormatException {
            System.out.println("DummyJavaEnumFormat: " + str);
            return null;
        }
    }
    
}
