/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.json;

import com.cinnober.msgcodec.MsgCodecInstantiationException;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.MsgCodecFactory;

/**
 * Factory for JsonCodec.
 * 
 * @author mikael.brannstrom
 */
public class JsonCodecFactory implements MsgCodecFactory {

    private final Schema schema;

    /**
     * Create a JSON codec factory.
     * 
     * @param schema the schema to be used by all codec instances, not null.
     */
    public JsonCodecFactory(Schema schema) {
        if (!schema.isBound()) {
            throw new IllegalArgumentException("Schema must be bound");
        }
        this.schema = schema;
    }

    @Override
    public JsonCodec createCodec() throws MsgCodecInstantiationException {
        return new JsonCodec(schema);
    }
    
}
