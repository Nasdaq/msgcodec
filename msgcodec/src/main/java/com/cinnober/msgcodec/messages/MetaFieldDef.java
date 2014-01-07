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
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;

/**
 * A field definition.
 *
 * @author mikael.brannstrom
 */
@Name("FieldDef")
public class MetaFieldDef extends MetaAnnotated {
    /**
     * The field name.
     */
    @Required
    @Id(1)
    public String name;
    /**
     * The numeric field identifier.
     */
    @Id(2)
    public Integer id;
    /**
     * True if the field is required, otherwise false.
     */
    @Id(3)
    public boolean required;
    /**
     * The type of the field.
     */
    @Annotate("xml:field=inline")
    @Required @Dynamic
    @Id(4)
    public MetaTypeDef type;

    public MetaFieldDef() {
    }

    public MetaFieldDef(String name, Integer id, boolean required,
            MetaTypeDef type) {
        super();
        this.name = name;
        this.id = id;
        this.required = required;
        this.type = type;
    }


    public FieldDef toFieldDef() {
        return new FieldDef(name, id != null ? id.intValue() : -1, required, type.toTypeDef(), toAnnotationsMap(),
                null);
    }


}
