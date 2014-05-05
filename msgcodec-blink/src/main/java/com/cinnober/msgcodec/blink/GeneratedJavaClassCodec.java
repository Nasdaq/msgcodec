/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.ProtocolDictionary;

/**
 * Base class for a dynamically generated codec for a specific dictionary, where group type is the Java class.
 * 
 *
 * @author mikael brannstrom
 */
public abstract class GeneratedJavaClassCodec extends GeneratedCodec {

    public GeneratedJavaClassCodec(BlinkCodec codec, ProtocolDictionary dict) {
        super(codec);
    }
}
