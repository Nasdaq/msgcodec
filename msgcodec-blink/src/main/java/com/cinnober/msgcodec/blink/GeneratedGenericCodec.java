/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.GroupTypeAccessor;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.util.LimitInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for a dynamically generated codec for a specific dictionary, where group type is any object.
 *
 * @author mikael brannstrom
 */
class GeneratedGenericCodec extends GeneratedCodec {
    protected final GroupTypeAccessor groupTypeAccessor;

    public GeneratedGenericCodec(BlinkCodec codec, ProtocolDictionary dict) {
        super(codec);
        this.groupTypeAccessor = dict.getBinding().getGroupTypeAccessor();
    }

    @Override
    protected void writeStaticGroupWithId(OutputStream out, Object obj) throws IOException {
        Object groupType = groupTypeAccessor.getGroupType(obj);
        // invokedynamic writeStaticGroupWithId groupType (out, obj) -> writeStaticGroupWithId_groupname(out, Object obj)
    }

    @Override
    protected Object readStaticGroup(int groupId, LimitInputStream in) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
}
