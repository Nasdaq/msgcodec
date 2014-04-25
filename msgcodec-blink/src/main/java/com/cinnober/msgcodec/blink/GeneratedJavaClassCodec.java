/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.ProtocolDictionary;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

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

    protected IllegalArgumentException unknownObjectType(Class javaClass) {
        return new IllegalArgumentException("Unknown object type: " + javaClass);
    }

    protected DecodeException unknownGroupId(int groupId) {
        return new DecodeException("Unknown group id: " + groupId);
    }

}
