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
import java.util.List;

import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;

/**
 * @author mikael.brannstrom
 *
 */
@Name("GroupDef")
@Id(16001)
public class MetaGroupDef extends MetaAnnotated {
    @Required
    @Id(1)
    private String name;
    @Id(2)
    private Integer id;
    @Id(3)
    private String superGroup;
    @Required
    @Sequence(MetaFieldDef.class)
    @Id(4)
    private Collection<MetaFieldDef> fields;

    public MetaGroupDef() {}

    /**
     * @param name
     * @param id
     * @param superGroup
     * @param fields
     */
    public MetaGroupDef(String name, Integer id, String superGroup,
            Collection<MetaFieldDef> fields) {
        super();
        this.name = name;
        this.id = id;
        this.superGroup = superGroup;
        this.fields = fields;
    }



    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
    /**
     * @return the superGroup
     */
    public String getSuperGroup() {
        return superGroup;
    }
    /**
     * @param superGroup the superGroup to set
     */
    public void setSuperGroup(String superGroup) {
        this.superGroup = superGroup;
    }
    /**
     * @return the fields
     */
    public Collection<MetaFieldDef> getFields() {
        return fields;
    }
    /**
     * @param fields the fields to set
     */
    public void setFields(Collection<MetaFieldDef> fields) {
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
