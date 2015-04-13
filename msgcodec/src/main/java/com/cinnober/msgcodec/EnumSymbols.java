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

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;

/**
 * EnumSymbols is a mapping between Java enumerations and enumeration symbols.
 *
 * @author mikael.brannstrom
 * @param <E> the enumeration type.
 */
public class EnumSymbols<E extends Enum<E>> {
    private final TypeDef.Enum typeDef;
    private final Class<E> enumClass;
    private final EnumMap<E, TypeDef.Symbol> symbolByEnum;
    private final Map<Integer, E> enumById;
    private final Map<String, E> enumByName;

    /** Create a enum symbol mapping.
     * @param typeDef the enum type to create a mapping for.
     * @param enumClass the corresponding Java enumeration to create a mapping for.
     */
    public EnumSymbols(TypeDef.Enum typeDef, Class<E> enumClass) {
        this.typeDef = typeDef;
        this.enumClass = enumClass;

        symbolByEnum = new EnumMap<>(enumClass);
        enumById = new HashMap<>(typeDef.getSymbols().size() * 2);
        enumByName = new HashMap<>(typeDef.getSymbols().size() * 2);

        Map<TypeDef.Symbol, E> enumBySymbol = inverse(createSymbolMap(enumClass));
        for (TypeDef.Symbol symbol : typeDef.getSymbols()) {
            E en = enumBySymbol.get(symbol);
            if (en == null) {
                // PENDING: complain that there is not enum constant for this symbol?
                continue;
            }
            symbolByEnum.put(en, symbol);
            enumById.put(symbol.getId(), en);
            enumByName.put(symbol.getName(), en);
        }
    }

    /** Returns the Java enumeration for the symbol name.
     *
     * @param name the symbol name, not null.
     * @return the Java enum value, or null if none was found.
     */
    public E getEnum(String name) {
        return enumByName.get(name);
    }

    /** Returns the Java enumeration for the symbol id.
    *
    * @param id the symbol id.
    * @return the Java enum value, or null if none was found.
    */
    public E getEnum(int id) {
        return enumById.get(id);
    }

    /** Returns the symbol for the Java enumeration value.
     *
     * @param en the enumeration value, not null.
     * @return the symbol, or null if none was found.
     */
    public TypeDef.Symbol getSymbol(E en) {
        return symbolByEnum.get(en);
    }

    /** Returns the type definition.
     *
     * @return the type definition, not null.
     */
    public TypeDef.Enum getTypeDef() {
        return typeDef;
    }

    /** Returns the Java enumeration class.
     *
     * @return the Java enumeration class, not null.
     */
    public Class<E> getEnumClass() {
        return enumClass;
    }

    /** Create a mapping from Java enumeration to the corresponding symbols.
     * The annotations {@link Id} and {@link Name} are scanned for and applied.
     *
     * @param <E> the enum type
     * @param enumClass the Java enumeration class.
     * @return the map, not null.
     */
    public static <E extends Enum<E>> Map<E, TypeDef.Symbol> createSymbolMap(Class<E> enumClass) {
        Set<Integer> usedIds = new HashSet<>();
        Set<String> usedNames = new HashSet<>();
        EnumMap<E, TypeDef.Symbol> symbols = new EnumMap<>(enumClass);
        int id = 0;
        for (E en : enumClass.getEnumConstants()) {
            Field field;
            try {
                field = enumClass.getField(en.name());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            Id idAnot = field.getAnnotation(Id.class);
            Name nameAnot = field.getAnnotation(Name.class);
            if (idAnot != null) {
                id = idAnot.value();
            }
            String name = nameAnot != null ? nameAnot.value() : en.name();
            if (!usedIds.add(id)) {
                throw new IllegalArgumentException("Duplicate id: " + id);
            }
            if (!usedNames.add(name)) {
                throw new IllegalArgumentException("Duplicate name: " + name);
            }
            TypeDef.Symbol symbol = new TypeDef.Symbol(name, id);
            symbols.put(en, symbol);
            id++;
        }
        return symbols;
    }

    private static <K, V> Map<V, K> inverse(Map<K, V> map) {
        Map<V, K> inverse = new HashMap<>(map.size() * 2);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            inverse.put(entry.getValue(), entry.getKey());
        }
        return inverse;
    }

}
