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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.GroupTypeAccessor;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.StreamCodec;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Enum;
import com.cinnober.msgcodec.util.TimeFormat;
import com.cinnober.msgcodec.xml.XmlElementHandler.ArraySequenceValueField;
import com.cinnober.msgcodec.xml.XmlElementHandler.ListSequenceValueField;
import com.cinnober.msgcodec.xml.XmlElementHandler.DynamicGroupField;
import com.cinnober.msgcodec.xml.XmlElementHandler.DynamicGroupValue;
import com.cinnober.msgcodec.xml.XmlElementHandler.ElementValueField;
import com.cinnober.msgcodec.xml.XmlElementHandler.FieldHandler;
import com.cinnober.msgcodec.xml.XmlElementHandler.InlineElementValueField;
import com.cinnober.msgcodec.xml.XmlElementHandler.SimpleField;
import com.cinnober.msgcodec.xml.XmlElementHandler.StaticGroupValue;
import com.cinnober.msgcodec.xml.XmlElementHandler.StringItemValue;
import com.cinnober.msgcodec.xml.XmlElementHandler.ValueHandler;
import java.util.Date;

/**
 * The XML codec can serialize and deserialize Java objects to/from XML.
 * 
 * <p>XML codec support a number of annotations to the protocol dictionary that control the encoding.
 * <table>
 * <caption>XML specific annotations</caption>
 * <tr style="text-align: left">
 * <th>Annotation</th><th>Location</th><th>Description</th>
 * </tr>
 * <tr>
 * <td><code>xml:ns</code></td>
 * <td>ProtocolDictionary</td>
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
 * <tr><td>binary</td><td>xs:binary. TODO: not implemented yet</td></tr>
 * <tr><td>sequence</td><td>TODO: To be documented.</td></tr>
 * <tr><td>group</td><td>element with group name as element name. 
 * Fields are represented as nested attributes or elements.</td></tr>
 * <tr
 * </table>
 * 
 * <p><b>Note:</b> missing fields are not checked. (TODO)
 *
 * @author mikael.brannstrom
 *
 */
public class XmlCodec implements StreamCodec {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String ANOT_XML_NAMESPACE = "xml:ns";
    private static final String ANOT_FIELD = "xml:field";
    private static final String ANOTVALUE_FIELD_ATTRIBUTE = "attribute";
    private static final String ANOTVALUE_FIELD_ELEMENT = "element";
    private static final String ANOTVALUE_FIELD_INLINE_ELEMENT = "inline";

    private final GroupTypeAccessor groupTypeAccessor;
    private final Map<NsName, StaticGroupValue> staticGroupsByNsName;
    private final Map<String, StaticGroupValue> staticGroupsByName;
    private final Map<Object, StaticGroupValue> staticGroupsByGroupType;

    private String namespace;

    private final XmlDocumentHandler saxHandler;
    private final SAXParser saxParser;

    public XmlCodec(ProtocolDictionary dictionary) throws ParserConfigurationException, SAXException {
        if (!dictionary.isBound()) {
            throw new IllegalArgumentException("ProtocolDictionary not bound");
        }

        groupTypeAccessor = dictionary.getBinding().getGroupTypeAccessor();
        int mapSize = dictionary.getGroups().size() * 2;
        staticGroupsByNsName = new HashMap<>(mapSize);
        staticGroupsByName = new HashMap<>(mapSize);
        staticGroupsByGroupType = new HashMap<>(mapSize);

        for (GroupDef groupDef : dictionary.getGroups()) {
            StaticGroupValue groupInstruction = new StaticGroupValue(getNsName(groupDef), groupDef);
            staticGroupsByGroupType.put(groupDef.getGroupType(), groupInstruction);
            staticGroupsByName.put(groupDef.getName(), groupInstruction);
            staticGroupsByNsName.put(groupInstruction.getNsName(), groupInstruction);
        }

        // create field instructions for all groups
        for (GroupDef groupDef : dictionary.getGroups()) {
            StaticGroupValue groupInstruction = staticGroupsByGroupType.get(groupDef.getGroupType());
            Map<NsName, FieldHandler> elementFields = new LinkedHashMap<>();
            Map<NsName, SimpleField> attributeFields = new LinkedHashMap<>();
            List<SimpleField> inlineField = new ArrayList<>(1);
            if (groupDef.getSuperGroup() != null) {
                StaticGroupValue superGroupInstruction = staticGroupsByName.get(groupDef.getSuperGroup());
                attributeFields.putAll(superGroupInstruction.getAttributeFields());
                elementFields.putAll(superGroupInstruction.getElementFields());
                if (superGroupInstruction.getInlineField() != null) {
                    inlineField.add(superGroupInstruction.getInlineField());
                }
            }

            for (FieldDef fieldDef : groupDef.getFields()) {
                createFieldInstruction(dictionary, fieldDef, fieldDef.getType(), attributeFields, elementFields, inlineField);
            }
            groupInstruction.init(attributeFields, elementFields, inlineField.isEmpty() ? null : inlineField.get(0));
        }

        saxHandler = new XmlDocumentHandler(this);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxParser = saxFactory.newSAXParser();
    }

    protected NsName getNsName(GroupDef groupDef) {
        String nsAnot = groupDef.getAnnotation(ANOT_XML_NAMESPACE);
        return new NsName(nsAnot != null ? nsAnot : namespace, toElementName(groupDef.getName()));
    }
    protected NsName getNsName(FieldDef fieldDef) {
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
    private void createFieldInstruction(ProtocolDictionary dictionary, FieldDef field, TypeDef type,
            Map<NsName, SimpleField> attributeFields, Map<NsName, FieldHandler> elementFields,
            List<SimpleField> inlineField) {
        NsName nsName = getNsName(field);

        type = dictionary.resolveToType(type, true);
        GroupDef typeGroup = dictionary.resolveToGroup(type);

        if (type instanceof TypeDef.Sequence) {
            TypeDef.Sequence sequenceType = (TypeDef.Sequence) type;
            TypeDef componentType = sequenceType.getComponentType();
            componentType = dictionary.resolveToType(componentType, true);
            GroupDef componentGroup = dictionary.resolveToGroup(componentType);

            ValueHandler valueInstr = null;
            if (componentType instanceof TypeDef.Reference) {
                valueInstr = staticGroupsByName.get(componentGroup.getName());
            } else if (componentType instanceof TypeDef.DynamicReference) {
                valueInstr = new DynamicGroupValue(this);
            } else if (componentType.getType() == TypeDef.Type.STRING) {
                valueInstr = new StringItemValue(new NsName(null, "i"));
            } else if (componentType.getType() == TypeDef.Type.BINARY) {
                throw new RuntimeException("Sequence of binary not implemented yet"); // TODO
            }


            if (valueInstr != null) {
                if (field.getJavaClass().isArray()) {
                    ArraySequenceValueField fieldInstr = new ArraySequenceValueField(nsName, field, valueInstr,
                            field.getJavaClass().getComponentType());
                    putElement(elementFields, fieldInstr);
                } else {
                    ListSequenceValueField fieldInstr =
                            new ListSequenceValueField(nsName, field, valueInstr);
                    putElement(elementFields, fieldInstr);
                }
            } else {
                //XmlFormat format = getXmlFormat(type, field.getComponentJavaClass());
                throw new RuntimeException("Sequence of simple type not implemented yet"); // TODO
                // TODO: implement this (uncomment the following)
//                if (field.getJavaClass().isArray()) {
//                    ArraySequenceSimpleField fieldInstr = new ArraySequenceSimpleField(nsName, field, valueInstr,
//                            field.getJavaClass().getComponentType());
//                    putElement(elementFields, fieldInstr);
//                } else {
//                    CollectionSequenceSimpleField fieldInstr =
//                            new CollectionSequenceSimpleField(nsName, field, valueInstr);
//                    putElement(elementFields, fieldInstr);
//                }
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
                fieldInstr = new InlineElementValueField(nsName, field, valueInstr);
            } else {
                fieldInstr = new ElementValueField(nsName, field, valueInstr);
            }
            putElement(elementFields, fieldInstr);
        } else if (type instanceof TypeDef.DynamicReference) {
            // --- DYNAMIC GROUP REFERENCE ---
            boolean inline = ANOTVALUE_FIELD_INLINE_ELEMENT.equals(field.getAnnotation(ANOT_FIELD));

            if (inline) {
                for (GroupDef subGroup : dictionary.getDynamicGroups(typeGroup != null ? typeGroup.getName() : null)) {
                    StaticGroupValue valueInstr = staticGroupsByName.get(subGroup.getName());
                    FieldHandler fieldInstr = new InlineElementValueField(getNsName(subGroup), field, valueInstr);
                    putElement(elementFields, fieldInstr);
                }
            } else {
                putElement(elementFields, new DynamicGroupField(nsName, field, this));
            }
        } else if (type.getType() == TypeDef.Type.BINARY) {
            throw new RuntimeException("BINARY not implemented"); // TODO
            // inline element:
            // <fieldname encoding="hex">AABBCC</fieldname>
            // <fieldname encoding="base64">qeqt24rwr=</fieldname>

        } else {
            // --- SIMPLE TYPE ---
            // xml schema:
            // string, decimal, boolean,
            // base64Binary, hexBinary
            // long, int, short, byte, unsignedLong, unsignedInt, unsignedShort, unsignedByte,
            // float, double,
            // date, time, dateTime,
            // token (enum)

            XmlFormat format = getXmlFormat(type, field.getJavaClass());
            SimpleField fieldInstr = new SimpleField(nsName, field, format);

            boolean attribute = !ANOTVALUE_FIELD_ELEMENT.equals(field.getAnnotation(ANOT_FIELD));
            boolean inline = ANOTVALUE_FIELD_INLINE_ELEMENT.equals(field.getAnnotation(ANOT_FIELD));

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
    private XmlFormat getXmlFormat(TypeDef type, Class<?> javaClass) {
        switch (type.getType()) {
        case ENUM:
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
            throw new IOException(e);
        }
    }

    public StaticGroupValue lookupGroup(NsName name) {
        return staticGroupsByNsName.get(name);
    }
    public StaticGroupValue lookupGroup(Class<?> javaClass) {
        return staticGroupsByGroupType.get(javaClass);
    }

}
