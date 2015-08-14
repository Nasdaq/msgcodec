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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.cinnober.msgcodec.TypeDef.Symbol;

/**
 * Represents a mapping between Name, ID and java symbol representation. The mapping is immutable.
 * 
 * @author Tommy Norling
 */
public interface SymbolMapping<E> {
    public E lookup(Integer id) throws IllegalArgumentException;
    public E lookup(String name) throws IllegalArgumentException;
    public Integer getId(E value) throws IllegalArgumentException;
    public String getName(E value) throws IllegalArgumentException;
    
    /**
     * An identity mapping between a java enum and it's codec representation.
     * 
     * Note: Codec implementations may detect this mapping and replace it with an optimized version.
     * 
     * @author Tommy Norling
     *
     * @param <E> The enum class used to represent this enum in java.
     */
    public static class IdentityEnumMapping<E extends Enum<E>> implements SymbolMapping<E> {
        private final Map<E, Symbol> enumToSymbol;
        private final Map<Integer, E> idToEnum = new HashMap<>();
        private final Map<String, E> nameToEnum = new HashMap<>();
        
        private final Class<E> enumClass; 
        
        /**
         * Create an IdentityEnumMapping for the given enum.
         * @param enumClass The enum class to create a mapping for.
         */
        public IdentityEnumMapping(Class<E> enumClass) {
            Objects.requireNonNull(enumClass);
            
            this.enumClass = enumClass;
            
            enumToSymbol = EnumSymbols.createSymbolMap(enumClass);
            for (Entry<E, Symbol> entry : enumToSymbol.entrySet()) {
                idToEnum.put(entry.getValue().getId(), entry.getKey());
                nameToEnum.put(entry.getValue().getName(), entry.getKey());
            }
        }
        
        @Override
        public E lookup(Integer id) throws IllegalArgumentException {
            E value = idToEnum.get(id);
            
            if (value == null) {
                throw new IllegalArgumentException("No enum value mapped to " + id);
            }
            
            return value;
        }

        @Override
        public E lookup(String name) throws IllegalArgumentException {
            E value = nameToEnum.get(name);
            
            if (value == null) {
                throw new IllegalArgumentException("No enum value named " + name);
            }
            
            return value;
        }

        @Override
        public Integer getId(E value) {
            return enumToSymbol.get(value).getId();
        }

        @Override
        public String getName(E value) {
            return enumToSymbol.get(value).getName();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(IdentityEnumMapping.class)) {
                return false;
            }
            IdentityEnumMapping<?> other = (IdentityEnumMapping<?>) obj;
            
            return Objects.equals(enumClass, other.enumClass);
        }
        
        @Override
        public int hashCode() {
            return 97 + Objects.hash(enumClass);
        }
    }
    
    /**
     * An identity mapping between a java integer enum and it's codec representation.
     * 
     * Note: Codec implementations may detect this mapping and replace it with an optimized version.
     * 
     * @author Tommy Norling
     */
    public static class IdentityIntegerEnumMapping implements SymbolMapping<Integer> {
        private final Map<String, Integer> nameToInteger = new HashMap<>();
        private final Map<Integer, String> integerToName = new HashMap<>();
        private final TypeDef.Enum enumDef;
        
        /**
         * Create an IdentityEnumMapping for the given enum.
         * @param enumDef The enum definition to create a mapping for.
         */
        public IdentityIntegerEnumMapping(TypeDef.Enum enumDef) {
            Objects.requireNonNull(enumDef);
            this.enumDef = enumDef;
            
            for (Symbol symbol : enumDef.getSymbols()) {
                nameToInteger.put(symbol.getName(), symbol.getId());
                integerToName.put(symbol.getId(), symbol.getName());
            }
        }
        
        @Override
        public Integer lookup(Integer id) {
            return id;
        }

        @Override
        public Integer lookup(String name) throws IllegalArgumentException {
            Integer value = nameToInteger.get(name);
            
            if (value == null) {
                throw new IllegalArgumentException("No enum value named " + name);
            }
            
            return value;
        }

        @Override
        public Integer getId(Integer value) {
            return value;
        }

        @Override
        public String getName(Integer value) {
            return integerToName.get(value);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(IdentityIntegerEnumMapping.class)) {
                return false;
            }
            IdentityIntegerEnumMapping other = (IdentityIntegerEnumMapping) obj;
            
            return Objects.equals(enumDef, other.enumDef);
        }
        
        @Override
        public int hashCode() {
            return 97 + Objects.hash(enumDef);
        }
    }
}
