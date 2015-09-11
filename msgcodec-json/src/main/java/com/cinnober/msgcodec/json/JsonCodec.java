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
package com.cinnober.msgcodec.json;

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.GroupTypeAccessor;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SymbolMapping;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Sequence;
import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSinkOutputStream;
import com.cinnober.msgcodec.io.ByteSource;
import com.cinnober.msgcodec.io.ByteSourceInputStream;
import com.cinnober.msgcodec.json.JsonValueHandler.ArraySequenceHandler;
import com.cinnober.msgcodec.json.JsonValueHandler.DynamicGroupHandler;
import com.cinnober.msgcodec.json.JsonValueHandler.FieldHandler;
import com.cinnober.msgcodec.json.JsonValueHandler.ListSequenceHandler;
import com.cinnober.msgcodec.json.JsonValueHandler.StaticGroupHandler;
import com.cinnober.msgcodec.util.TimeFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The JSON codec can serialize and deserialize Java objects to/from JSON.
 * 
 * <p>JsonCodec is thread safe.
 * Null values are supported in encode and decode.
 * 
 * <p>The following mapping between msgcodec and JSON types applies.
 * <table>
 * <caption>Mapping between msgcodec and JSON data types.</caption>
 * <tr style="text-align: left"><th>Msgcodec type</th><th>JSON type</th></tr>
 * <tr><td>int, float and decimal</td><td>number or string (see below)</td></tr>
 * <tr><td>boolean</td><td>true/false</td></tr>
 * <tr><td>string</td><td>string</td></tr>
 * <tr><td>binary</td><td>string (base64)</td></tr>
 * <tr><td>enum</td><td>string (name)</td></tr>
 * <tr><td>time</td><td>string (see {@link TimeFormat})</td></tr>
 * <tr><td>sequence</td><td>array</td></tr>
 * <tr><td>static group</td><td>object.</td></tr>
 * <tr>
 * <td>dynamic group</td>
 * <td>object, with an additional field <code>$type</code> with the group name as a string.
 * Best decoding performance is gained when this field appears first in an object.
 * </tr>
 * </table>
 *
 * <p>Optional fields with null values are left out in the encoded output (field is absent).
 *
 * <p>Numbers must be encoded as strings in the following situations:
 * <ul>
 * <li>The float32 and float64 values NaN, Infinity and -Infinity are encoded as the strings
 * "NaN", "Infinity" and "-Infinity" respectively.
 * <li>If safe JavaScript numbers (see {@link JsonCodecFactory#setJavaScriptSafe(boolean)}) are enabled (default),
 * then the following number values are also encoded as strings:
 * <ul>
 * <li>Values of int64, uint64 and bigint that are outside the range [-9007199254740991, 9007199254740991]
 * <li>Values of decimal and big decimal that have an mantissa with more than 15 decimals.
 * </ul>
 * </ul>
 * 
 *
 * @author mikael.brannstrom
 * @see JsonCodecFactory
 *
 */
public class JsonCodec implements MsgCodec {

    private static final byte[] NULL_BYTES = new byte[] { 'n', 'u', 'l', 'l' };
    private final GroupTypeAccessor groupTypeAccessor;
    private final Map<String, StaticGroupHandler> staticGroupsByName;
    private final Map<Object, StaticGroupHandler> staticGroupsByGroupType;
    private final DynamicGroupHandler dynamicGroupHandler;
    private final Schema schema;

    @SuppressWarnings("rawtypes")
    JsonCodec(Schema schema, boolean jsSafe) {
        if (!schema.isBound()) {
            throw new IllegalArgumentException("Schema not bound");
        }

        this.schema = schema;
        dynamicGroupHandler = new DynamicGroupHandler(this);
        groupTypeAccessor = schema.getBinding().getGroupTypeAccessor();
        int mapSize = schema.getGroups().size() * 2;
        staticGroupsByName = new HashMap<>(mapSize);
        staticGroupsByGroupType = new HashMap<>(mapSize);

        for (GroupDef groupDef : schema.getGroups()) {
            StaticGroupHandler groupInstruction = new StaticGroupHandler(groupDef);
            staticGroupsByGroupType.put(groupDef.getGroupType(), groupInstruction);
            staticGroupsByName.put(groupDef.getName(), groupInstruction);
        }

        // create field instructions for all groups
        for (GroupDef groupDef : schema.getGroups()) {
            StaticGroupHandler groupInstruction = staticGroupsByName.get(groupDef.getName());
            Map<String, FieldHandler> fields = new LinkedHashMap<>();
            int nextRequiredSlot = 0;
            if (groupDef.getSuperGroup() != null) {
                StaticGroupHandler superGroupInstruction = staticGroupsByName.get(groupDef.getSuperGroup());
                fields.putAll(superGroupInstruction.getFields());
                nextRequiredSlot = superGroupInstruction.getNumRequiredFields();
            }

            for (FieldDef fieldDef : groupDef.getFields()) {
                JsonValueHandler valueHandler = createValueHandler(
                        schema,
                        fieldDef.getType(),
                        fieldDef.getJavaClass(),
                        fieldDef.getComponentJavaClass(),
                        fieldDef.getBinding().getSymbolMapping(),
                        fieldDef.getAccessor(),
                        jsSafe);
                boolean required = fieldDef.isRequired();
                FieldHandler fieldHandler = new FieldHandler(
                        fieldDef.getName(),
                        fieldDef.getBinding().getAccessor(),
                        required,
                        required ? nextRequiredSlot++ : -1,
                        valueHandler
                );
                fields.put(fieldDef.getName(), fieldHandler);
            }
            groupInstruction.init(fields);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private JsonValueHandler createValueHandler(
            Schema schema,
            TypeDef type,
            Class<?> javaClass,
            Class<?> componentJavaClass,
            SymbolMapping<?> symbolMapping,
            Accessor accessor,
            boolean jsSafe) {
        type = schema.resolveToType(type, true);
        GroupDef group = schema.resolveToGroup(type);
        switch (type.getType()) {
        case SEQUENCE:
            if (javaClass.isArray()) {
                return new JsonValueHandler.ArraySequenceHandler(
                        createValueHandler(schema, ((Sequence)type).getComponentType(), componentJavaClass, null, symbolMapping, accessor, jsSafe),
                        componentJavaClass);
            } else { // collection
                return new JsonValueHandler.ListSequenceHandler(
                        createValueHandler(schema, ((Sequence)type).getComponentType(), componentJavaClass, null, symbolMapping, accessor, jsSafe));
            }
        case REFERENCE:
            return lookupGroupByName(group.getName());
        case DYNAMIC_REFERENCE:
            return dynamicGroupHandler; // TODO: restrict to some base type (if group is not null)
        default:
            return JsonValueHandler.getValueHandler(type, javaClass, (SymbolMapping) symbolMapping, jsSafe, accessor);
        }
    }

    /**
     * Returns the JSON value handler for the specified group name and any field names.
     * The value handler can be used to encode and decode a field value.
     *
     * @param groupName the name of the group, not null.
     * @param fieldNames the field names, if any.
     * @return the JSON value handler.
     */
    public JsonValueHandler<?> getValueHandler(String groupName, String ... fieldNames) {
        JsonValueHandler<?> valueHandler = lookupGroupByName(groupName);
        for (String fieldName : fieldNames) {
            if (valueHandler instanceof StaticGroupHandler) {
                StaticGroupHandler groupHandler = (StaticGroupHandler) valueHandler;
                FieldHandler fieldHandler = groupHandler.getFields().get(fieldName);
                if (fieldHandler == null) {
                    throw new IllegalArgumentException("No such field '" + fieldName + "'");
                }
                valueHandler = fieldHandler.getValueHandler();
            } else if (valueHandler instanceof ArraySequenceHandler) {
                valueHandler = ((ArraySequenceHandler)valueHandler).getComponentHandler();
            } else if (valueHandler instanceof ListSequenceHandler) {
                valueHandler = ((ListSequenceHandler)valueHandler).getComponentHandler();
            } else {
                throw new IllegalArgumentException("Cannot get sub field '" + fieldName + "' of primitive type");
            }
        }
        return valueHandler;
    }

    @Override
    public void encode(Object group, OutputStream out) throws IOException {
        if (group == null) {
            out.write(NULL_BYTES);
        } else {
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createGenerator(out);
            dynamicGroupHandler.writeValue(group, g);
            g.flush();
        }
    }
    @Override
    public void encode(Object group, ByteSink out) throws IOException {
        if (group == null) {
            out.write(NULL_BYTES);
        } else {
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createGenerator(new ByteSinkOutputStream(out));
            dynamicGroupHandler.writeValue(group, g);
            g.flush();
        }
    }

    /**
     * Write the group to the stream, but without adding the '$type' field.
     * To decode the JSON the receiver must know what group type to expect.
     *
     * @param group the group to encode.
     * @param out the stream to write to, not null.
     * @throws IOException if the underlying byte sink throws an exception.
     * @throws IllegalArgumentException if the group is not correct or complete, e.g. a required field is missing.
     * Partial data may have been written to the stream.
     */
    public void encodeStatic(Object group, OutputStream out) throws IOException {
        if (group == null) {
            out.write(NULL_BYTES);
        } else {
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createGenerator(out);
            StaticGroupHandler groupHandler = lookupGroupByValue(group);
            if (groupHandler == null) {
                throw new IllegalArgumentException("Cannot encode group (unknown type)");
            }
            groupHandler.writeValue(group, g, false);
            g.flush();
        }
    }
    /**
     * Write the group to the byte sink, but without adding the '$type' field.
     * To decode the JSON the receiver must know what group type to expect.
     *
     * @param group the group to encode.
     * @param out the byte sink to write to, not null.
     * @throws IOException if the underlying byte sink throws an exception.
     * @throws IllegalArgumentException if the group is not correct or complete, e.g. a required field is missing.
     * Partial data may have been written to the byte sink.
     */
    public void encodeStatic(Object group, ByteSink out) throws IOException {
        if (group == null) {
            out.write(NULL_BYTES);
        } else {
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createGenerator(new ByteSinkOutputStream(out));
            StaticGroupHandler groupHandler = lookupGroupByValue(group);
            if (groupHandler == null) {
                throw new IllegalArgumentException("Cannot encode group (unknown type)");
            }
            groupHandler.writeValue(group, g, false);
            g.flush();
        }
    }

    @Override
    public Object decode(InputStream in) throws IOException {
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(in);
        JsonToken token = p.nextToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        } else if (token != JsonToken.START_OBJECT) {
            throw new DecodeException("Expected {");
        }
        return dynamicGroupHandler.readValue(p);
    }
    @Override
    public Object decode(ByteSource in) throws IOException {
        return decode(new ByteSourceInputStream(in));
    }

    /**
     * Read a static group from the specified stream, when the JSON does not contain the '$type' field.
     *
     * @param groupType the expected group type, not null.
     * @param in the stream to read from, not null.
     * @return the decoded value.
     * @throws IOException if the underlying stream throws an exception.
     * @throws DecodeException if the value could not be decoded, or if a required field is missing.
     */
    public Object decodeStatic(Object groupType, InputStream in) throws IOException {
        StaticGroupHandler groupHandler = staticGroupsByGroupType.get(groupType);
        if (groupHandler == null) {
            throw new IllegalArgumentException("Unknown group type");
        }

        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(in);
        JsonToken token = p.nextToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        } else if (token != JsonToken.START_OBJECT) {
            throw new DecodeException("Expected {");
        }
        return groupHandler.readValue(p);
    }

    /**
     * Read a static group from the specified byte source, when the JSON does not contain the '$type' field.
     *
     * @param groupType the expected group type, not null.
     * @param in the byte source to read from, not null.
     * @return the decoded value.
     * @throws IOException if the underlying byte source throws an exception.
     * @throws DecodeException if the value could not be decoded, or if a required field is missing.
     */
    public Object decodeStatic(Object groupType, ByteSource in) throws IOException {
        return decodeStatic(groupType, new ByteSourceInputStream(in));
    }

    /**
     * Read a static group from the specified stream, when the JSON does not contain the '$type' field.
     *
     * @param groupName the expected group name, not null.
     * @param in the stream to read from, not null.
     * @return the decoded value.
     * @throws IOException if the underlying stream throws an exception.
     * @throws DecodeException if the value could not be decoded, or if a required field is missing.
     */
    public Object decodeStatic(String groupName, InputStream in) throws IOException {
        StaticGroupHandler groupHandler = lookupGroupByName(groupName);
        if (groupHandler == null) {
            throw new IllegalArgumentException("Unknown group name");
        }

        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(in);
        JsonToken token = p.nextToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        } else if (token != JsonToken.START_OBJECT) {
            throw new DecodeException("Expected {");
        }
        return groupHandler.readValue(p);
    }

    /**
     * Read a static group from the specified byte source, when the JSON does not contain the '$type' field.
     *
     * @param groupName the expected group name, not null.
     * @param in the byte source to read from, not null.
     * @return the decoded value.
     * @throws IOException if the underlying byte source throws an exception.
     * @throws DecodeException if the value could not be decoded, or if a required field is missing.
     */
    public Object decodeStatic(String groupName, ByteSource in) throws IOException {
        return decodeStatic(groupName, new ByteSourceInputStream(in));
    }

    StaticGroupHandler lookupGroupByName(String name) {
        return staticGroupsByName.get(name);
    }
    StaticGroupHandler lookupGroupByValue(Object group) {
        Object groupType = groupTypeAccessor.getGroupType(group);
        StaticGroupHandler handler = staticGroupsByGroupType.get(groupType);
        if (handler == null) {
            GroupDef groupDef = schema.getGroup((Object)(groupType));
            handler = groupDef != null ? staticGroupsByName.get(groupDef.getName()) : null;
        }
        return handler;
    }

}
