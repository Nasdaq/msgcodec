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

import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;

/**
 * @author mikael.brannstrom
 *
 */
@Name("FieldDef")
public class MetaFieldDef extends MetaAnnotated {
    private String name;
    private Integer id;
    private boolean required;
    private MetaTypeDef type;

    /**
     *
     */
    public MetaFieldDef() {
    }


    /**
     * @param name
     * @param id
     * @param required
     * @param type
     */
    public MetaFieldDef(String name, Integer id, boolean required,
            MetaTypeDef type) {
        super();
        this.name = name;
        this.id = id;
        this.required = required;
        this.type = type;
    }


    /**
     * @return the name
     */
    @Required
    @Id(1)
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
    @Id(2)
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
     * @return the required
     */
    @Id(3)
    public boolean isRequired() {
        return required;
    }
    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }
    /**
     * @return the type
     */
    @Required
    @Id(4)
    public MetaTypeDef getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(MetaTypeDef type) {
        this.type = type;
    }

    public FieldDef toFieldDef() {
        return new FieldDef(name, id != null ? id.intValue() : -1, required, type.toTypeDef(), toAnnotationsMap(),
                null);
    }


}
