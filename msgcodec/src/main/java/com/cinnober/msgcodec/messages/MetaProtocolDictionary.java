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
package com.cinnober.msgcodec.messages;

import java.util.ArrayList;
import java.util.Collections;

import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.NamedType;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import java.util.List;

/**
 * Message for a protocol dictionary.
 *
 * @author mikael.brannstrom
 */
@Name("ProtocolDictionary")
@Id(16000)
public class MetaProtocolDictionary extends MetaAnnotated {
    /**
     * The groups in the dictionary.
     */
    @Required
    @Sequence(MetaGroupDef.class)
    @Id(1)
    public List<MetaGroupDef> groups;

    /**
     * The named types in the dictionary.
     */
    @Sequence(MetaNamedType.class)
    @Id(2)
    public List<MetaNamedType> namedTypes;

    public MetaProtocolDictionary() {}

    public MetaProtocolDictionary(List<MetaGroupDef> groups,
            List<MetaNamedType> namedTypes) {
        this.groups = groups;
        this.namedTypes = namedTypes;
    }


    public List<NamedType> toNamedTypes() {
        if (namedTypes == null) {
            return Collections.emptyList();
        }
        List<NamedType> list = new ArrayList<>(namedTypes.size());
        for (MetaNamedType namedType : namedTypes) {
            list.add(new NamedType(namedType.name, namedType.type.toTypeDef(), namedType.toAnnotationsMap()));
        }
        return list;
    }

    public List<GroupDef> toGroupDefs() {
        if (groups == null) {
            return Collections.emptyList();
        }
        List<GroupDef> list = new ArrayList<>(groups.size());
        for (MetaGroupDef group : groups) {
            list.add(group.toGroupDef());
        }
        return list;
    }

    public ProtocolDictionary toProtocolDictionary() {
        return new ProtocolDictionary(toGroupDefs(), toNamedTypes(), toAnnotationsMap(), null);
    }
}
