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
import java.util.List;

import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;

/**
 * A group definition.
 *
 * @author mikael.brannstrom
 *
 */
@Name("GroupDef")
@Id(16001)
public class MetaGroupDef extends MetaAnnotated {
    /**
     * The group name.
     */
    @Required
    @Id(1)
    public String name;
    /**
     * The numeric group identifier.
     */
    @Id(2)
    public Integer id;
    /**
     * The name of the super group, or null if there is no super group.
     */
    @Id(3)
    public String superGroup;
    /**
     * The declared fields of this group.
     * Inherited fields are not included here.
     */
    @Required
    @Sequence(MetaFieldDef.class)
    @Id(4)
    public List<MetaFieldDef> fields;

    public MetaGroupDef() {}

    public MetaGroupDef(String name, Integer id, String superGroup,
            List<MetaFieldDef> fields) {
        this.name = name;
        this.id = id;
        this.superGroup = superGroup;
        this.fields = fields;
    }

    public GroupDef toGroupDef() {
        List<FieldDef> fieldDefs = new ArrayList<>(fields.size());
        for (MetaFieldDef field : fields) {
            fieldDefs.add(field.toFieldDef());
        }
        return new GroupDef(name, id != null ? id.intValue() : -1, superGroup, fieldDefs, toAnnotationsMap(), null);
    }


}
