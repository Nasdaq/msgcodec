/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.xml;

import com.cinnober.msgcodec.StreamCodecInstantiationException;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.StreamCodecFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Factory for XmlCodec.
 * 
 * @author mikael.brannstrom
 */
public class XmlCodecFactory implements StreamCodecFactory {

    private final ProtocolDictionary dictionary;

    /**
     * Create a Blink codec factory.
     * 
     * @param dictionary the protocol dictionary to be used by all codec instances, not null.
     */
    public XmlCodecFactory(ProtocolDictionary dictionary) {
        if (!dictionary.isBound()) {
            throw new IllegalArgumentException("Dictionary must be bound");
        }
        this.dictionary = dictionary;
    }

    @Override
    public XmlCodec createStreamCodec() throws StreamCodecInstantiationException {
        try {
            return new XmlCodec(dictionary);
        } catch (ParserConfigurationException | SAXException e) {
            throw new StreamCodecInstantiationException("Could not create codec", e);
        }
    }
    
}
