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

import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.GroupTypeAccessor;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.MsgCodecInstantiationException;
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
 * <tr><td>int, float and decimal</td><td>number</td></tr>
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
 * Currently JSON codec expects this field to appear first in an object.
 * <br>PENDING: relax this to a suggestion for improved performance?</td>
 * </tr>
 * </table>
 * 
 * <p><b>TODO:</b> required fields are currently not checked
 * <p><b>TODO:</b> add option to encode/decode large integers as strings (JavaScript interop)
 * <p><b>TODO:</b> add option to encode/decode non-integer (non-safe) decimals as strings (JavaScript interop)
 * <p><b>TODO:</b> do not enforce the $type field to appear first (only optimize performance when it is first)
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

    @SuppressWarnings("rawtypes")
    JsonCodec(Schema schema) {
        if (!schema.isBound()) {
            throw new IllegalArgumentException("Schema not bound");
        }

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
            StaticGroupHandler groupInstruction = staticGroupsByGroupType.get(groupDef.getGroupType());
            Map<String, FieldHandler> fields = new LinkedHashMap<>();
            if (groupDef.getSuperGroup() != null) {
                StaticGroupHandler superGroupInstruction = staticGroupsByName.get(groupDef.getSuperGroup());
                fields.putAll(superGroupInstruction.getFields());
            }

            for (FieldDef fieldDef : groupDef.getFields()) {
                JsonValueHandler valueHandler = createValueHandler(schema, fieldDef.getType(), fieldDef.getJavaClass(), fieldDef.getComponentJavaClass());
                FieldHandler fieldHandler = new FieldHandler(fieldDef, valueHandler);
                fields.put(fieldDef.getName(), fieldHandler);
            }
            groupInstruction.init(fields);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private JsonValueHandler createValueHandler(Schema dictionary, TypeDef type, Class<?> javaClass, Class<?> componentJavaClass) {
        type = dictionary.resolveToType(type, true);
        GroupDef group = dictionary.resolveToGroup(type);
        switch (type.getType()) {
        case SEQUENCE:
            if (javaClass.isArray()) {
                return new JsonValueHandler.ArraySequenceHandler(
                        createValueHandler(dictionary, ((Sequence)type).getComponentType(), componentJavaClass, null),
                        componentJavaClass);
            } else { // collection
                return new JsonValueHandler.ListSequenceHandler(
                        createValueHandler(dictionary, ((Sequence)type).getComponentType(), componentJavaClass, null));
            }
        case REFERENCE:
            return lookupGroupByName(group.getName());
        case DYNAMIC_REFERENCE:
            return dynamicGroupHandler; // TODO: restrict to some base type (if group is not null)
        default:
            return JsonValueHandler.getValueHandler(type, javaClass);
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

    StaticGroupHandler lookupGroupByName(String name) {
        return staticGroupsByName.get(name);
    }
    StaticGroupHandler lookupGroupByValue(Object group) {
        return staticGroupsByGroupType.get(groupTypeAccessor.getGroupType(group));
    }

}
