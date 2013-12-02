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
