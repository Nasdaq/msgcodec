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
package com.cinnober.msgcodec.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.NamedType;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.Static;

/**
 * @author mikael.brannstrom
 *
 */
@Name("ProtocolDictionary")
@Id(16000)
public class MetaProtocolDictionary extends MetaAnnotated {
    private Collection<MetaGroupDef> groups;
    private Collection<MetaNamedType> namedTypes;

    public MetaProtocolDictionary() {}

    /**
     * @param groups
     * @param namedTypes
     */
    public MetaProtocolDictionary(Collection<MetaGroupDef> groups,
            Collection<MetaNamedType> namedTypes) {
        this.groups = groups;
        this.namedTypes = namedTypes;
    }


    /**
     * @return the groups
     */
    @Static
    @Required
    @Sequence(MetaGroupDef.class)
    @Id(1)
    public Collection<MetaGroupDef> getGroups() {
        return groups;
    }
    /**
     * @param groups the groups to set
     */
    public void setGroups(Collection<MetaGroupDef> groups) {
        this.groups = groups;
    }

    /**
     * @return the namedTypes
     */
    @Static
    @Sequence(MetaNamedType.class)
    @Id(2)
    public Collection<MetaNamedType> getNamedTypes() {
        return namedTypes;
    }
    /**
     * @param namedTypes the namedTypes to set
     */
    public void setNamedTypes(Collection<MetaNamedType> namedTypes) {
        this.namedTypes = namedTypes;
    }

    public Collection<NamedType> toNamedTypes() {
        if (namedTypes == null) {
            return Collections.emptyList();
        }
        Collection<NamedType> list = new ArrayList<>(namedTypes.size());
        for (MetaNamedType namedType : namedTypes) {
            list.add(new NamedType(namedType.getName(), namedType.getType().toTypeDef(), namedType.toAnnotationsMap()));
        }
        return list;
    }

    public Collection<GroupDef> toGroupDefs() {
        if (groups == null) {
            return Collections.emptyList();
        }
        Collection<GroupDef> list = new ArrayList<>(groups.size());
        for (MetaGroupDef group : groups) {
            list.add(group.toGroupDef());
        }
        return list;
    }

    public ProtocolDictionary toProtocolDictionary() {
        return new ProtocolDictionary(toGroupDefs(), toNamedTypes(), toAnnotationsMap(), null);
    }
}
