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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.Factory;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import java.util.List;

/**
 * @author mikael.brannstrom
 *
 */
class XmlElementHandler {

    private final NsName nsName;

    /**
     * @param nsName the name, or null.
     */
    public XmlElementHandler(NsName nsName) {
        this.nsName = nsName;
    }

    /**
     * @return the nsName, or null if none.
     */
    public NsName getNsName() {
        return nsName;
    }

    public void startElement(XmlContext ctx, NsName nsName, Map<NsName, String> attributes) {
    }
    public void endElement(XmlContext ctx, String text) {
    }
    public XmlElementHandler lookupElement(XmlContext ctx, NsName nsName) {
        return null;
    }
    public void startChildElement(XmlContext ctx, XmlElementHandler element) {
    }
    public void endChildElement(XmlContext ctx, XmlElementHandler element) {
    }
    protected void appendElementName(PrintWriter writer, NsName elementName) {
        writer.append(elementName.getName()); // TODO: check special chars, namespace etc
    }
    protected void appendAttributeName(PrintWriter writer, NsName attributeName) {
        writer.append(attributeName.getName()); // TODO: check special chars, namespace etc
    }

    static abstract class FieldHandler extends XmlElementHandler {
        protected FieldDef field;
        @SuppressWarnings("rawtypes")
        protected Accessor accessor;

        /**
         * @param nsName the name, not null.
         * @param field the field, not null.
         */
        public FieldHandler(NsName nsName, FieldDef field) {
            super(nsName);
            this.field = field;
            this.accessor = field.getAccessor();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void endElement(XmlContext ctx, String text) {
            Object value = ctx.popValue();
            Object group = ctx.peekValue();
            accessor.setValue(group, value);
        }
        @SuppressWarnings("unchecked")
        public Object getValue(Object group) {
            return accessor.getValue(group);
        }
        public abstract void writeElement(Object value, NsName name, PrintWriter appendTo) throws IOException;
    }
    static abstract class ValueHandler extends XmlElementHandler {
        /**
         * @param nsName the name, or null.
         */
        public ValueHandler(NsName nsName) {
            super(nsName);
        }
        public abstract void writeElementValue(Object value, NsName name, PrintWriter appendTo) throws IOException;
    }
    static class StringItemValue extends ValueHandler {

        /**
         * @param nsName the name, or null.
         */
        public StringItemValue(NsName nsName) {
            super(nsName);
        }
        @Override
        public void endElement(XmlContext ctx, String text) {
            ctx.pushValue(text);
        }
        @Override
        public void writeElementValue(Object value, NsName name, PrintWriter appendTo) throws IOException {
            appendTo.append('<');
            appendElementName(appendTo, name);
            appendTo.append('>');
            appendTo.append(XmlStringFormat.escape((String) value));
            appendTo.append("</");
            appendElementName(appendTo, name);
            appendTo.append('>');
        }
    }

    static class StaticGroupValue extends ValueHandler {
        private final GroupDef groupDef;
        @SuppressWarnings("rawtypes")
        private final Factory factory;
        private Map<NsName, SimpleField> attributeFields;
        private Map<NsName, FieldHandler> elementFields;
        private SimpleField inlineField;

        /**
         * @param nsName the name, not null.
         * @param groupDef the group, not null.
         */
        public StaticGroupValue(NsName nsName, GroupDef groupDef) {
            super(nsName);
            this.groupDef = groupDef;
            this.factory = groupDef.getFactory();
        }

        public void init(Map<NsName, SimpleField> attributeFields,
                Map<NsName, FieldHandler> elementFields, SimpleField inlineField) {
            if (!elementFields.isEmpty() && inlineField != null) {
                throw new IllegalArgumentException("Cannot have both elements and inline text fields in group");
            }

            this.attributeFields = attributeFields;
            this.elementFields = elementFields;
            this.inlineField = inlineField;
        }
        Map<NsName, SimpleField> getAttributeFields() {
            return attributeFields;
        }
        Map<NsName, FieldHandler> getElementFields() {
            return elementFields;
        }
        SimpleField getInlineField() {
            return inlineField;
        }

        @Override
        public void startElement(XmlContext ctx, NsName name,
                Map<NsName, String> attributes) {
            ctx.pushValue(factory.newInstance());
            for (Map.Entry<NsName, String> attribute : attributes.entrySet()) {
                SimpleField field = attributeFields.get(attribute.getKey());
                if (field == null) {
                    throw new RuntimeException("Unknown attribute: " + attribute.getKey()); // TODO: exception type
                }
                field.handleAttribute(ctx, attribute.getKey(), attribute.getValue());
                //field.copyValueToGroup(value);
                // TODO: remove attribute.getKey() from missing fields
            }
        }

        @Override
        public XmlElementHandler lookupElement(XmlContext ctx, NsName name) {
            return elementFields.get(name);
        }

        @Override
        public void endChildElement(XmlContext ctx, XmlElementHandler element) {
            //            FieldHandler field = (FieldHandler) element;
            //            field.copyValueToGroup(value);
            // TODO: remove element.getName() from missing fields
        }

        @Override
        public void endElement(XmlContext ctx, String text) {
            if (inlineField != null) {
                inlineField.handleText(ctx, text);
            }
            // TODO: check missing fields
        }

        @Override
        public void writeElementValue(Object value, NsName name, PrintWriter appendTo) throws IOException {
            if (value == null || !value.getClass().equals(groupDef.getGroupType())) {
                return;
            }
            appendTo.append('<');
            appendElementName(appendTo, name);
            for (Map.Entry<NsName, SimpleField> attrEntry : attributeFields.entrySet()) {
                final NsName attrName = attrEntry.getKey();
                final SimpleField attrInstr = attrEntry.getValue();
                Object fieldValue = attrInstr.getValue(value);
                if (fieldValue != null) {
                    attrInstr.writeAttribute(fieldValue, attrName, appendTo);
                }
            }
            boolean startElementOpen = true;

            if (inlineField != null) {
                Object fieldValue = inlineField.getValue(value);
                if (fieldValue != null) {
                    appendTo.append('>');
                    inlineField.writeText(fieldValue, appendTo);
                    appendTo.append("</");
                    appendElementName(appendTo, name);
                    appendTo.println('>');
                } else {
                    appendTo.println("/>");
                }
            } else {
                for (Map.Entry<NsName, FieldHandler> elemEntry : elementFields.entrySet()) {
                    final NsName elemName = elemEntry.getKey();
                    final FieldHandler elemInstr = elemEntry.getValue();
                    Object fieldValue = elemInstr.getValue(value);
                    if (fieldValue != null) {
                        if (startElementOpen) {
                            appendTo.println('>');
                            startElementOpen = false;
                        }
                        elemInstr.writeElement(fieldValue, elemName, appendTo);
                    }
                }
                if (startElementOpen) {
                    appendTo.println("/>");
                } else {
                    appendTo.append("</");
                    appendElementName(appendTo, name);
                    appendTo.println('>');
                }
            }
        }
    }

    static class DynamicGroupValue extends ValueHandler {

        private final XmlCodec codec;
        /**
         * @param codec the codec, not null.
         */
        public DynamicGroupValue(XmlCodec codec) {
            super(null);
            this.codec = codec;
        }

        @Override
        public XmlElementHandler lookupElement(XmlContext ctx, NsName nsName) {
            return codec.lookupGroup(nsName);
        }
        @Override
        public void writeElementValue(Object value, NsName name, PrintWriter appendTo) throws IOException {
            if (value == null) {
                return;
            }
            StaticGroupValue groupInstr = codec.lookupGroup(value.getClass());
            groupInstr.writeElementValue(value, groupInstr.getNsName(), appendTo);
        }
    }
    static class DynamicGroupField extends FieldHandler {

        private final XmlCodec codec;
        /**
         * @param nsName
         * @param valueHandler
         */
        public DynamicGroupField(NsName name, FieldDef fieldDef, XmlCodec codec) {
            super(name, fieldDef);
            this.codec = codec;
        }

        @Override
        public XmlElementHandler lookupElement(XmlContext ctx, NsName nsName) {
            return codec.lookupGroup(nsName);
        }

        @Override
        public void writeElement(Object value, NsName name, PrintWriter appendTo)
                throws IOException {
            if (value == null) {
                return;
            }
            appendTo.append('<');
            appendElementName(appendTo, name);
            appendTo.println('>');

            StaticGroupValue groupInstr = codec.lookupGroup(value.getClass());
            groupInstr.writeElementValue(value, groupInstr.getNsName(), appendTo);

            appendTo.append("</");
            appendElementName(appendTo, name);
            appendTo.println('>');
        }
    }

    static class SimpleField extends FieldHandler {
        @SuppressWarnings("rawtypes")
        private final XmlFormat format;

        /**
         * @param nsName
         * @param accessor
         * @param format
         */
        @SuppressWarnings("rawtypes")
        public SimpleField(NsName nsName, FieldDef field, XmlFormat format) {
            super(nsName, field);
            this.format = format;
        }

        public void handleAttribute(XmlContext ctx, NsName attribute, String text) {
            startElement(ctx, attribute, null);
            endElement(ctx, text);
        }

        @SuppressWarnings("unchecked")
        public void handleText(XmlContext ctx, String text) {
            try {
                Object value = format.parse(text);
                Object group = ctx.peekValue();
                accessor.setValue(group, value);
            } catch (FormatException e) {
                throw new RuntimeException(e); // TODO: exception type
            }
        }

        @Override
        public void endElement(XmlContext ctx, String text) {
            handleText(ctx, text);
        }

        @Override
        public void writeElement(Object value, NsName name, PrintWriter appendTo) throws IOException {
            if (value == null) {
                return;
            }
            appendTo.append('<');
            appendElementName(appendTo, name);
            appendTo.append('>');
            writeText(value, appendTo);
            appendTo.append("</");
            appendElementName(appendTo, name);
            appendTo.println('>');
        }
        public void writeAttribute(Object value, NsName name, PrintWriter writer) throws IOException {
            if (value == null) {
                return;
            }
            writer.append(' ');
            appendAttributeName(writer, name);
            writer.append('=');
            writer.append('"');
            writeText(value, writer);
            writer.append('"');
        }
        @SuppressWarnings("unchecked")
        public void writeText(Object value, PrintWriter writer) throws IOException {
            if (value == null) {
                return;
            }
            try {
                writer.append(format.format(value));
            } catch (FormatException e) {
                throw new IOException(e);
            }
        }

    }

    static class ElementValueField extends FieldHandler {
        private final ValueHandler valueHandler;

        /**
         * @param nsName
         * @param accessor
         * @param valueHandler
         */
        public ElementValueField(NsName nsName, FieldDef field,
                ValueHandler valueHandler) {
            super(nsName, field);
            this.valueHandler = valueHandler;
        }

        @Override
        public XmlElementHandler lookupElement(XmlContext ctx, NsName name) {
            if (valueHandler.getNsName() != null || valueHandler.getNsName().equals(name)) {
                return valueHandler;
            }
            return null;
        }

        @Override
        public void writeElement(Object value, NsName name, PrintWriter appendTo)
                throws IOException {
            if (value == null) {
                return;
            }
            appendTo.append('<');
            appendElementName(appendTo, name);
            appendTo.append('>');
            valueHandler.writeElementValue(value, valueHandler.getNsName(), appendTo);
            appendTo.append("</");
            appendElementName(appendTo, name);
            appendTo.println('>');
        }

    }

    static class ListSequenceValueField extends FieldHandler {
        protected final ValueHandler valueHandler;

        /**
         * @param nsName
         * @param accessor
         * @param valueHandler
         */
        public ListSequenceValueField(NsName nsName, FieldDef field,
                ValueHandler valueHandler) {
            super(nsName, field);
            this.valueHandler = valueHandler;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void startElement(XmlContext ctx, NsName nsName, Map<NsName, String> attributes) {
            super.startElement(ctx, nsName, attributes);
            ctx.pushValue(new ArrayList());
        }

        @Override
        public XmlElementHandler lookupElement(XmlContext ctx, NsName name) {
            if (valueHandler.getNsName() != null || valueHandler.getNsName().equals(name)) {
                return valueHandler;
            }
            return null;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void endChildElement(XmlContext ctx, XmlElementHandler element) {
            Object item = ctx.popValue();
            List list = (List) ctx.peekValue();
            list.add(item);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void writeElement(Object value, NsName name, PrintWriter appendTo)
                throws IOException {
            if (value == null) {
                return;
            }
            appendTo.append('<');
            appendElementName(appendTo, name);
            List list = (List)value;
            if (list.isEmpty()) {
                appendTo.println("/>");
            } else {
                appendTo.println('>');
                for (Object item : list) {
                    valueHandler.writeElementValue(item, valueHandler.getNsName(), appendTo);
                }
                appendTo.append("</");
                appendElementName(appendTo, name);
                appendTo.println('>');
            }
        }
    }

    static class ArraySequenceValueField extends ListSequenceValueField {

        private final Class<?> componentType;
        /**
         * @param nsName
         * @param field
         * @param valueHandler
         */
        public ArraySequenceValueField(NsName nsName, FieldDef field,
                ValueHandler valueHandler, Class<?> componentType) {
            super(nsName, field, valueHandler);
            this.componentType = componentType;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void endElement(XmlContext ctx, String text) {
            // convert from list to array
            List list = (List) ctx.popValue();
            Object array = Array.newInstance(componentType, list.size());
            int i=0;
            for (Object value : list) {
                Array.set(array, i++, value);
            }
            ctx.pushValue(array);
            super.endElement(ctx, text);
        }

        @Override
        public void writeElement(Object array, NsName name, PrintWriter appendTo)
                throws IOException {
            if (array == null) {
                return;
            }
            appendTo.append('<');
            appendElementName(appendTo, name);
            int length = Array.getLength(array);
            if (length == 0) {
                appendTo.println("/>");
            } else {
                appendTo.println('>');
                for (int i=0; i<length; i++) {
                    Object item = Array.get(array, i);
                    valueHandler.writeElementValue(item, valueHandler.getNsName(), appendTo);
                }
                appendTo.append("</");
                appendElementName(appendTo, name);
                appendTo.println('>');
            }
        }
    }

    static class ListSequenceSimpleField extends FieldHandler {
        @SuppressWarnings("rawtypes")
        protected final XmlFormat valueFormat;

        /**
         * @param nsName
         * @param accessor
         * @param valueHandler
         */
        public ListSequenceSimpleField(NsName nsName, FieldDef field,
                XmlFormat<?> valueHandler) {
            super(nsName, field);
            this.valueFormat = valueHandler;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void endElement(XmlContext ctx, String text) {
            ArrayList list = new ArrayList();
            String[] split = text.split("\\s+");
            for (String str : split) {
                if (!str.isEmpty()) {
                    try {
                        list.add(valueFormat.parse(str));
                    } catch (FormatException e) {
                        throw new RuntimeException(e); // TODO: exception type
                    }
                }
            }
            accessor.setValue(ctx.peekValue(), list);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void writeElement(Object value, NsName name, PrintWriter appendTo)
                throws IOException {
            if (value == null) {
                return;
            }
            appendTo.append('<');
            appendElementName(appendTo, name);
            List list = (List)value;
            if (list.isEmpty()) {
                appendTo.println("/>");
            } else {
                appendTo.println('>');
                boolean whitespace = false;
                for (Object item : list) {
                    if (whitespace) {
                        appendTo.append(' ');
                    } else {
                        whitespace = true;
                    }
                    try {
                        appendTo.append(valueFormat.format(item));
                    } catch (FormatException e) {
                        throw new RuntimeException(e); // TODO: exception type
                    }
                }
                appendTo.append("</");
                appendElementName(appendTo, name);
                appendTo.println('>');
            }
        }
    }

    static class ArraySequenceSimpleField extends FieldHandler {
        @SuppressWarnings("rawtypes")
        protected final XmlFormat valueFormat;
        private final Class<?> componentType;

        /**
         * @param nsName
         * @param accessor
         * @param valueHandler
         */
        public ArraySequenceSimpleField(NsName nsName, FieldDef field,
                XmlFormat<?> valueHandler, Class<?> componentType) {
            super(nsName, field);
            this.valueFormat = valueHandler;
            this.componentType = componentType;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void endElement(XmlContext ctx, String text) {
            ArrayList list = new ArrayList();
            String[] split = text.split("\\s+");
            for (String str : split) {
                if (!str.isEmpty()) {
                    try {
                        list.add(valueFormat.parse(str));
                    } catch (FormatException e) {
                        throw new RuntimeException(e); // TODO: exception type
                    }
                }
            }
            // convert list to array
            Object array = Array.newInstance(componentType, list.size());
            int i=0;
            for (Object value : list) {
                Array.set(array, i++, value);
            }

            accessor.setValue(ctx.peekValue(), array);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void writeElement(Object value, NsName name, PrintWriter appendTo)
                throws IOException {
            if (value == null) {
                return;
            }
            appendTo.append('<');
            appendElementName(appendTo, name);
            Object array = value;
            int length = Array.getLength(array);
            if (length == 0) {
                appendTo.println("/>");
            } else {
                appendTo.print('>');
                boolean whitespace = false;
                for (int i=0; i<length; i++) {
                    Object item = Array.get(array, i);
                    if (whitespace) {
                        appendTo.append(' ');
                    } else {
                        whitespace = true;
                    }
                    try {
                        appendTo.append(valueFormat.format(item));
                    } catch (FormatException e) {
                        throw new RuntimeException(e); // TODO: exception type
                    }
                }
                appendTo.append("</");
                appendElementName(appendTo, name);
                appendTo.println('>');
            }
        }
    }

    static class InlineElementValueField extends FieldHandler {
        protected ValueHandler valueHandler;

        /**
         * @param nsName the name, or null
         * @param valueHandler the value handler, not null.
         */
        public InlineElementValueField(NsName nsName, FieldDef field,
                ValueHandler valueHandler) {
            super(nsName, field);
            this.valueHandler = valueHandler;
        }
        @Override
        public void startElement(XmlContext ctx, NsName name, Map<NsName, String> attributes) {
            super.startElement(ctx, name, attributes);
            valueHandler.startElement(ctx, name, attributes);
        }
        @Override
        public void endElement(XmlContext ctx, String text) {
            valueHandler.endElement(ctx, text);
            super.endElement(ctx, text);
        }
        @Override
        public XmlElementHandler lookupElement(XmlContext ctx, NsName name) {
            return valueHandler.lookupElement(ctx, name);
        }
        @Override
        public void startChildElement(XmlContext ctx, XmlElementHandler element) {
            valueHandler.startChildElement(ctx, element);
        }
        @Override
        public void endChildElement(XmlContext ctx, XmlElementHandler element) {
            valueHandler.endChildElement(ctx, element);
        }

        @Override
        public void writeElement(Object value, NsName name, PrintWriter appendTo)
                throws IOException {
            valueHandler.writeElementValue(value, name, appendTo);
        }
    }

    static class InlineElementValue extends ValueHandler {
        protected ValueHandler valueHandler;

        /**
         * @param nsName the name, or null.
         * @param valueHandler the value handler, not null.
         */
        public InlineElementValue(NsName nsName, ValueHandler valueHandler) {
            super(nsName);
            this.valueHandler = valueHandler;
        }
        @Override
        public void startElement(XmlContext ctx, NsName name, Map<NsName, String> attributes) {
            valueHandler.startElement(ctx, name, attributes);
        }
        @Override
        public void endElement(XmlContext ctx, String text) {
            valueHandler.endElement(ctx, text);
        }
        @Override
        public XmlElementHandler lookupElement(XmlContext ctx, NsName name) {
            return valueHandler.lookupElement(ctx, name);
        }
        @Override
        public void startChildElement(XmlContext ctx, XmlElementHandler element) {
            valueHandler.startChildElement(ctx, element);
        }
        @Override
        public void endChildElement(XmlContext ctx, XmlElementHandler element) {
            valueHandler.endChildElement(ctx, element);
        }
        @Override
        public void writeElementValue(Object value, NsName name, PrintWriter appendTo)
                throws IOException {
            valueHandler.writeElementValue(value, name, appendTo);
        }
    }




}
