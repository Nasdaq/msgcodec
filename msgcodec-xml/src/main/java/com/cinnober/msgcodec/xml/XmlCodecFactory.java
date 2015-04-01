/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.xml;

import com.cinnober.msgcodec.MsgCodecInstantiationException;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.MsgCodecFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Factory for XmlCodec.
 * 
 * @author mikael.brannstrom
 */
public class XmlCodecFactory implements MsgCodecFactory {

    private final Schema schema;

    /**
     * Create an XML codec factory.
     * 
     * @param schema the schema to be used by all codec instances, not null.
     */
    public XmlCodecFactory(Schema schema) {
        if (!schema.isBound()) {
            throw new IllegalArgumentException("Schema must be bound");
        }
        this.schema = schema;
    }

    @Override
    public XmlCodec createCodec() throws MsgCodecInstantiationException {
        try {
            return new XmlCodec(schema);
        } catch (ParserConfigurationException | SAXException e) {
            throw new MsgCodecInstantiationException("Could not create codec", e);
        }
    }
    
}
