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

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.CreateAccessor;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.GroupTypeAccessor;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Enum;
import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSinkOutputStream;
import com.cinnober.msgcodec.io.ByteSource;
import com.cinnober.msgcodec.io.ByteSourceInputStream;
import com.cinnober.msgcodec.util.TimeFormat;
import com.cinnober.msgcodec.xml.XmlElementHandler.ArraySequenceSimpleField;
import com.cinnober.msgcodec.xml.XmlElementHandler.ArraySequenceValueField;
import com.cinnober.msgcodec.xml.XmlElementHandler.DynamicGroupField;
import com.cinnober.msgcodec.xml.XmlElementHandler.DynamicGroupValue;
import com.cinnober.msgcodec.xml.XmlElementHandler.ElementValueField;
import com.cinnober.msgcodec.xml.XmlElementHandler.FieldHandler;
import com.cinnober.msgcodec.xml.XmlElementHandler.InlineElementValueField;
import com.cinnober.msgcodec.xml.XmlElementHandler.ListSequenceSimpleField;
import com.cinnober.msgcodec.xml.XmlElementHandler.ListSequenceValueField;
import com.cinnober.msgcodec.xml.XmlElementHandler.SequenceItemValue;
import com.cinnober.msgcodec.xml.XmlElementHandler.SimpleField;
import com.cinnober.msgcodec.xml.XmlElementHandler.StaticGroupValue;
import com.cinnober.msgcodec.xml.XmlElementHandler.ValueHandler;
import com.cinnober.msgcodec.xml.XmlEnumFormat.DummyJavaEnumFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/**
 * The XML codec can serialize and deserialize Java objects to/from XML.
 * 
 * <p>XML codec support a number of annotations to the schema that control the encoding.
 * <table>
 * <caption>XML specific annotations</caption>
 * <tr style="text-align: left">
 * <th>Annotation</th><th>Location</th><th>Description</th>
 * </tr>
 * <tr>
 * <td><code>xml:ns</code></td>
 * <td>Schema</td>
 * <td>Set the XML namespace of all elements. Default is none.</td>
 * </tr>
 * <tr>
 * <td><code>xml:field</code></td>
 * <td>FieldDef</td>
 * <td>Values:
 * <br><code>attribute</code> = the field is encoded as an attribute in the group element. 
 * Default for simple field types. Not applicable to group types.
 * <br><code>element</code> = the field is encoded as an child element in the group element.
 * Default for complex field types (group, sequence).
 * <br><code>inlineElement</code> = the field, which is a group type, is inlined within the contained group element.
 * This requires that there is no potential conflict with another field.
 * </td>
 * </tr>
 * </table>
 * 
 * <p>The following mapping between msgcodec types and XML constructs applies:
 * <table>
 * <caption>Mapping between msgcodec and XML types.</caption>
 * <tr style="text-align: left"><th>Msgcodec</th><th>XML</th></tr>
 * <tr><td>uInt8</td><td>xs:unsignedByte</td></tr>
 * <tr><td>uInt16</td><td>xs:unsignedShort</td></tr>
 * <tr><td>uInt32</td><td>xs:unsignedInt</td></tr>
 * <tr><td>uInt64</td><td>xs:unsignedLong</td></tr>
 * <tr><td>int8</td><td>xs:byte</td></tr>
 * <tr><td>int16</td><td>xs:short</td></tr>
 * <tr><td>int32</td><td>xs:int</td></tr>
 * <tr><td>int64</td><td>xs:long</td></tr>
 * <tr><td>bigInt</td><td>xs:integer</td></tr>
 * <tr><td>decimal</td><td>xs:decimal</td></tr>
 * <tr><td>bigDecimal</td><td>xs:decimal</td></tr>
 * <tr><td>boolean</td><td>xs:boolean</td></tr>
 * <tr><td>string</td><td>xs:string</td></tr>
 * <tr><td>enum</td><td>xs:string (name)</td></tr>
 * <tr><td>time</td><td>xs:string, see {@link TimeFormat}. PENDING: use xs:time and xs:date?</td></tr>
 * <tr><td>binary</td><td>xs:base64Binary</td></tr>
 * <tr><td>sequence</td><td>XML list (tokens separated by whitespace). 
 * String and binary values are wrapped by <code>&lt;i&gt;</code> elements.
 * Groups are already represented as elements.</td></tr>
 * <tr><td>group</td><td>element with group name as element name. 
 * Fields are represented as nested attributes or elements.</td></tr>
 * </table>
 * 
 * @author mikael.brannstrom
 *
 */
public class XmlCodec implements MsgCodec {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String ANOT_XML_NAMESPACE = "xml:ns";
    private static final String ANOT_FIELD = "xml:field";
    private static final String ANOTVALUE_FIELD_ATTRIBUTE = "attribute";
    private static final String ANOTVALUE_FIELD_ELEMENT = "element";
    private static final String ANOTVALUE_FIELD_INLINE = "inline";

    private final GroupTypeAccessor groupTypeAccessor;
    private final Map<NsName, StaticGroupValue> staticGroupsByNsName;
    private final Map<String, StaticGroupValue> staticGroupsByName;
    private final Map<Object, StaticGroupValue> staticGroupsByGroupType;

    private String namespace;

    private final XmlDocumentHandler saxHandler;
    private final SAXParser saxParser;

    XmlCodec(Schema schema) throws ParserConfigurationException, SAXException {
        if (!schema.isBound()) {
            throw new IllegalArgumentException("Schema not bound");
        }

        groupTypeAccessor = schema.getBinding().getGroupTypeAccessor();
        int mapSize = schema.getGroups().size() * 2;
        staticGroupsByNsName = new HashMap<>(mapSize);
        staticGroupsByName = new HashMap<>(mapSize);
        staticGroupsByGroupType = new HashMap<>(mapSize);

        for (GroupDef groupDef : schema.getGroups()) {
            StaticGroupValue groupInstruction = new StaticGroupValue(getNsName(groupDef), groupDef);
            staticGroupsByGroupType.put(groupDef.getGroupType(), groupInstruction);
            staticGroupsByName.put(groupDef.getName(), groupInstruction);
            staticGroupsByNsName.put(groupInstruction.getNsName(), groupInstruction);
        }

        // create field instructions for all groups
        for (GroupDef groupDef : schema.getGroups()) {
            StaticGroupValue groupInstruction = staticGroupsByGroupType.get(groupDef.getGroupType());
            Map<NsName, FieldHandler> elementFields = new LinkedHashMap<>();
            Map<NsName, SimpleField> attributeFields = new LinkedHashMap<>();
            List<SimpleField> inlineField = new ArrayList<>(1);
            int nextRequiredFieldSlot = 0;
            if (groupDef.getSuperGroup() != null) {
                StaticGroupValue superGroupInstruction = staticGroupsByName.get(groupDef.getSuperGroup());
                attributeFields.putAll(superGroupInstruction.getAttributeFields());
                elementFields.putAll(superGroupInstruction.getElementFields());
                if (superGroupInstruction.getInlineField() != null) {
                    inlineField.add(superGroupInstruction.getInlineField());
                }
                nextRequiredFieldSlot = superGroupInstruction.getNumRequiredFields();
            }
            for (FieldDef fieldDef : groupDef.getFields()) {
                createFieldInstruction(
                        schema,
                        fieldDef,
                        fieldDef.isRequired() ? nextRequiredFieldSlot++ : -1,
                        attributeFields,
                        elementFields,
                        inlineField);
            }
            groupInstruction.init(attributeFields, elementFields, inlineField.isEmpty() ? null : inlineField.get(0));
        }

        saxHandler = new XmlDocumentHandler(this);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxParser = saxFactory.newSAXParser();
    }

    private NsName getNsName(GroupDef groupDef) {
        String nsAnot = groupDef.getAnnotation(ANOT_XML_NAMESPACE);
        return new NsName(nsAnot != null ? nsAnot : namespace, toElementName(groupDef.getName()));
    }
    private NsName getNsName(FieldDef fieldDef) {
        String nsAnot = fieldDef.getAnnotation(ANOT_XML_NAMESPACE);
        return new NsName(nsAnot != null ? nsAnot : namespace, toElementName(fieldDef.getName()));
    }

    private String toElementName(String name) {
        if (name.length() == 0) {
            return "";
        } else {
            StringBuilder str = new StringBuilder(name);
            str.setCharAt(0, Character.toLowerCase(str.charAt(0)));
            return str.toString();
        }
    }

    @SuppressWarnings("rawtypes")
    private void createFieldInstruction(
            Schema schema,
            FieldDef field,
            int requiredFieldSlot,
            Map<NsName, SimpleField> attributeFields,
            Map<NsName, FieldHandler> elementFields,
            List<SimpleField> inlineField) {

        NsName nsName = getNsName(field);
        TypeDef type = schema.resolveToType(field.getType(), true);
        GroupDef typeGroup = schema.resolveToGroup(type);

        if (type instanceof TypeDef.Sequence) {
            TypeDef.Sequence sequenceType = (TypeDef.Sequence) type;
            TypeDef componentType = sequenceType.getComponentType();
            componentType = schema.resolveToType(componentType, true);
            GroupDef componentGroup = schema.resolveToGroup(componentType);

            ValueHandler valueInstr = null;
            if (componentType instanceof TypeDef.Reference) {
                valueInstr = staticGroupsByName.get(componentGroup.getName());
            } else if (componentType instanceof TypeDef.DynamicReference) {
                valueInstr = new DynamicGroupValue(this);
            } else if (componentType.getType() == TypeDef.Type.STRING) {
                valueInstr = new SequenceItemValue(new NsName(null, "i"), XmlStringFormat.STRING);
            } else if (componentType.getType() == TypeDef.Type.BINARY) {
                valueInstr = new SequenceItemValue(new NsName(null, "i"), XmlBinaryFormat.BINARY);
            }


            if (valueInstr != null) {
                if (field.getJavaClass().isArray()) {
                    ArraySequenceValueField fieldInstr = new ArraySequenceValueField(
                            nsName,
                            field,
                            requiredFieldSlot,
                            valueInstr,
                            field.getComponentJavaClass());
                    putElement(elementFields, fieldInstr);
                } else {
                    ListSequenceValueField fieldInstr = new ListSequenceValueField(
                            nsName,
                            field,
                            requiredFieldSlot,
                            valueInstr);
                    putElement(elementFields, fieldInstr);
                }
            } else {
                XmlFormat format = getXmlFormat(componentType, field.getComponentJavaClass(), field.getAccessor());
                if (field.getJavaClass().isArray()) {
                    ArraySequenceSimpleField fieldInstr = new ArraySequenceSimpleField(
                            nsName,
                            field,
                            requiredFieldSlot,
                            format,
                            field.getJavaClass().getComponentType());
                    putElement(elementFields, fieldInstr);
                } else {
                    ListSequenceSimpleField fieldInstr = new ListSequenceSimpleField(
                            nsName,
                            field,
                            requiredFieldSlot,
                            format);
                    putElement(elementFields, fieldInstr);
                }
            }



            // --- SEQUENCE ---
            // xml schema: <list itemType='...'/>
            // <sequence><element name='i' type='...' minOccurs="0" maxOccurs="unbounded"></sequence>

            // example, sequence of int: <field>123 456 -12 45</field>
            // example, sequence of string: <field>abc def qwerty</field>
            // example, sequence of string: <field><i>abc</i><i>def</i><i>qwerty</i></field>
            // example, sequence of binary: <field encoding="hex">ff010277 aabbccdd</field>
            // example, sequence of binary: <field encoding="hex"><i>ff010277</i><i>aabbccdd</i></field>
            // example, sequence of binary: <field><i encoding="hex">ff010277</i><i encoding="hex">aabbccdd</i></field>

        } else if (type instanceof TypeDef.Reference && typeGroup != null ) {
            // --- STATIC GROUP REFERENCE ---
            boolean inline = !ANOTVALUE_FIELD_ELEMENT.equals(field.getAnnotation(ANOT_FIELD));

            StaticGroupValue valueInstr = staticGroupsByName.get(typeGroup.getName());
            FieldHandler fieldInstr;
            if (inline) {
                fieldInstr = new InlineElementValueField(nsName, field, requiredFieldSlot, valueInstr);
            } else {
                fieldInstr = new ElementValueField(nsName, field, requiredFieldSlot, valueInstr);
            }
            putElement(elementFields, fieldInstr);
        } else if (type instanceof TypeDef.DynamicReference) {
            // --- DYNAMIC GROUP REFERENCE ---
            boolean inline = ANOTVALUE_FIELD_INLINE.equals(field.getAnnotation(ANOT_FIELD));

            if (inline) {
                for (GroupDef subGroup : schema.getDynamicGroups(typeGroup != null ? typeGroup.getName() : null)) {
                    StaticGroupValue valueInstr = staticGroupsByName.get(subGroup.getName());
                    FieldHandler fieldInstr = new InlineElementValueField(
                            getNsName(subGroup),
                            field,
                            requiredFieldSlot,
                            valueInstr);
                    putElement(elementFields, fieldInstr);
                }
            } else {
                putElement(elementFields, new DynamicGroupField(nsName, field, requiredFieldSlot, this));
            }
        } else {
            // --- SIMPLE TYPE ---
            // xml schema:
            // string, decimal, boolean,
            // base64Binary, hexBinary
            // long, int, short, byte, unsignedLong, unsignedInt, unsignedShort, unsignedByte,
            // float, double,
            // date, time, dateTime,
            // token (enum)

            XmlFormat format = getXmlFormat(type, field.getJavaClass(), field.getAccessor());
            SimpleField fieldInstr = new SimpleField(nsName, field, requiredFieldSlot, format);

            boolean attribute = !ANOTVALUE_FIELD_ELEMENT.equals(field.getAnnotation(ANOT_FIELD));
            boolean inline = ANOTVALUE_FIELD_INLINE.equals(field.getAnnotation(ANOT_FIELD));

            if (inline) {
                    putInline(inlineField, fieldInstr);
            } else if (attribute) {
                    putAttribute(attributeFields, fieldInstr);
            } else {
                    putElement(elementFields, fieldInstr);
            }
        }
    }

    private void putElement(Map<NsName, FieldHandler> elementFields, FieldHandler fieldInstr) {
        if (elementFields.put(fieldInstr.getNsName(), fieldInstr) != null) {
            throw new IllegalArgumentException("Duplicate elements: " + fieldInstr.getNsName());
        }
    }
    private void putAttribute(Map<NsName, SimpleField> attributeFields, SimpleField fieldInstr) {
        if (attributeFields.put(fieldInstr.getNsName(), fieldInstr) != null) {
            throw new IllegalArgumentException("Duplicate attributes: " + fieldInstr.getNsName());
        }
    }
    private void putInline(List<SimpleField> inlineField, SimpleField fieldInstr) {
        if (!inlineField.isEmpty()) {
            throw new IllegalArgumentException("Duplicate inline text field: " + fieldInstr.getNsName());
        }
        inlineField.add(fieldInstr);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private XmlFormat getXmlFormat(TypeDef type, Class<?> javaClass, Accessor accessor) {
        switch (type.getType()) {
        case ENUM:
            if(accessor instanceof CreateAccessor) {
                return new DummyJavaEnumFormat();
            }
            
            if (javaClass.isEnum()) {
                return new XmlEnumFormat.JavaEnumFormat((Enum) type, javaClass);
            } else if (javaClass.equals(int.class) || javaClass.equals(Integer.class)) {
                return new XmlEnumFormat.IntEnumFormat((Enum) type);
            } else {
                throw new RuntimeException("Unhandled enum type: " + type);
            }
        case TIME:
            if (javaClass.equals(Date.class)) {
                return new XmlTimeFormat.DateTimeFormat((TypeDef.Time) type);
            } else if (javaClass.equals(int.class) || javaClass.equals(Integer.class)) {
                return new XmlTimeFormat.UInt32TimeFormat((TypeDef.Time) type);
            } else if (javaClass.equals(long.class) || javaClass.equals(Long.class)) {
                return new XmlTimeFormat.UInt64TimeFormat((TypeDef.Time) type);
            } else {
                throw new RuntimeException("Unhandled time type: " + type);
            }
        default:
            return getSimpleXmlFormat(type.getType());
        }
    }

    @SuppressWarnings("rawtypes")
    private XmlFormat getSimpleXmlFormat(TypeDef.Type type) {
        switch (type) {
        case INT8:
            return XmlNumberFormat.INT8;
        case INT16:
            return XmlNumberFormat.INT16;
        case INT32:
            return XmlNumberFormat.INT32;
        case INT64:
            return XmlNumberFormat.INT64;
        case UINT8:
            return XmlNumberFormat.UINT8;
        case UINT16:
            return XmlNumberFormat.UINT16;
        case UINT32:
            return XmlNumberFormat.UINT32;
        case UINT64:
            return XmlNumberFormat.UINT64;
        case FLOAT32:
            return XmlNumberFormat.FLOAT32;
        case FLOAT64:
            return XmlNumberFormat.FLOAT64;
        case BIGINT:
            return XmlNumberFormat.BIGINT;
        case DECIMAL:
            return XmlNumberFormat.DECIMAL;
        case BIGDECIMAL:
            return XmlNumberFormat.BIGDECIMAL;
        case BOOLEAN:
            return XmlBooleanFormat.BOOLEAN;
        case STRING:
            return XmlStringFormat.STRING;
        case BINARY:
            return XmlBinaryFormat.BINARY;
        default:
            throw new RuntimeException("Unhandled type: " + type);
        }
    }


    @Override
    public void encode(Object group, OutputStream out) throws IOException {
        Object groupType = groupTypeAccessor.getGroupType(group);
        StaticGroupValue groupInstr = staticGroupsByGroupType.get(groupType);
        if (groupInstr == null) {
            throw new IllegalArgumentException("Unknown Java class: " + group.getClass());
        }
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, UTF8));
        groupInstr.writeElementValue(group, groupInstr.getNsName(), writer);
        writer.flush();
    }

    @Override
    public Object decode(InputStream in) throws IOException {
        try {
            saxParser.parse(in, saxHandler);
            return saxHandler.getValue();
        } catch (SAXException e) {
            throw new DecodeException(e);
        }
    }

    @Override
    public void encode(Object group, ByteSink out) throws IOException {
        encode(group, new ByteSinkOutputStream(out));
    }
    @Override
    public Object decode(ByteSource in) throws IOException {
        return decode(new ByteSourceInputStream(in));
    }

    StaticGroupValue lookupGroup(NsName name) {
        return staticGroupsByNsName.get(name);
    }
    StaticGroupValue lookupGroup(Class<?> javaClass) {
        return staticGroupsByGroupType.get(javaClass);
    }

}
