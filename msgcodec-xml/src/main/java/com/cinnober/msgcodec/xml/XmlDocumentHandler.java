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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cinnober.msgcodec.xml.XmlElementHandler.DynamicGroupValue;

/**
 * @author mikael.brannstrom
 *
 */
class XmlDocumentHandler extends DefaultHandler {

    private final Stack<XmlElementHandler> elementHandlerStack = new Stack<>();
    private final DynamicGroupValue rootElementHandler;
    private final StringBuilder text = new StringBuilder();
    private final XmlContext context = new XmlContext();

    /**
     * Create a new XML Document handler.
     * 
     * @param codec the codec, not null.
     */
    public XmlDocumentHandler(XmlCodec codec) {
        rootElementHandler = new DynamicGroupValue(codec);
    }

    @Override
    public void startDocument() throws SAXException {
        elementHandlerStack.clear();
        elementHandlerStack.push(rootElementHandler);
        context.clear();
    }

    @Override
    public void endDocument() throws SAXException {
    }

    public Object getValue() {
        return context.peekValue();
    }

    private NsName toNsName(String uri, String localName, String qName) {
        if (uri == null || uri.length() == 0) {
            return new NsName(null, qName);
        } else {
            return new NsName(uri, localName);
        }
    }

    private void clearText() {
        text.setLength(0);
    }

    private String getText() {
        return text.toString();
    }
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        text.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        NsName nsName = toNsName(uri, localName, qName);
        XmlElementHandler elementHandler = elementHandlerStack.peek().lookupElement(context, nsName);
        if (elementHandler == null) {
            throw new SAXException("Unknown element: " + nsName);
        }

        Map<NsName, String> attrMap = new LinkedHashMap<>(attributes.getLength());
        for (int i=0; i<attributes.getLength(); i++) {
            attrMap.put(toNsName(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i)),
                    attributes.getValue(i));
        }

        elementHandlerStack.peek().startChildElement(context, elementHandler);
        elementHandlerStack.push(elementHandler);
        elementHandlerStack.peek().startElement(context, nsName, attrMap);
        clearText();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        XmlElementHandler elementHandler = elementHandlerStack.pop();
        elementHandler.endElement(context, getText());
        elementHandlerStack.peek().endChildElement(context, elementHandler);
    }


}
