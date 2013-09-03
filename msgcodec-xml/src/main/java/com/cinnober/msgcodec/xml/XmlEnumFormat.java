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
