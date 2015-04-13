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

import java.util.HashMap;
import java.util.Map;

import com.cinnober.msgcodec.EnumSymbols;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Symbol;

/**
 * @author mikael.brannstrom
 *
 */
public abstract class XmlEnumFormat<T> implements XmlFormat<T> {

    static class IntEnumFormat extends XmlEnumFormat<Integer> {
        private final Map<String, Integer> idByName;
        private final Map<Integer, String> nameById;

        public IntEnumFormat(TypeDef.Enum typeDef) {
            idByName = new HashMap<>(typeDef.getSymbols().size() * 2);
            nameById = new HashMap<>(typeDef.getSymbols().size() * 2);
            for (Symbol symbol : typeDef.getSymbols()) {
                idByName.put(symbol.getName(), symbol.getId());
                nameById.put(symbol.getId(), symbol.getName());
            }
        }

        @Override
        public String format(Integer value) throws FormatException {
            String name = nameById.get(value);
            if (name == null) {
                throw new FormatException("Not a valid enum: " + value);
            }
            return name;
        }

        @Override
        public Integer parse(String str) throws FormatException {
            Integer value = idByName.get(str);
            if (value == null) {
                throw new FormatException("Not a valid symbol: " + str);
            }
            return value;
        }
    }


    static class JavaEnumFormat<E extends Enum<E>> extends XmlEnumFormat<E> {

        private final EnumSymbols<E> enumSymbols;
        public JavaEnumFormat(TypeDef.Enum typeDef, Class<E> enumClass) {
            this.enumSymbols = new EnumSymbols<E>(typeDef, enumClass);
        }

        @Override
        public String format(E value) throws FormatException {
            Symbol symbol = enumSymbols.getSymbol(value);
            if (symbol == null) {
                throw new FormatException("Not a valid enum: " + value);
            }
            return symbol.getName();
        }

        @Override
        public E parse(String str) throws FormatException {
            E value = enumSymbols.getEnum(str);
            if (value == null) {
                throw new FormatException("Not a valid symbol: " + str);
            }
            return value;
        }
    }

}
