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

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;

/**
 * A key-value annotation.
 *
 * @author mikael.brannstrom
 */
@Name("Annotation")
public class MetaAnnotation extends MsgObject {
    /**
     * The annotation name.
     */
    @Required
    @Id(1)
    public String name;
    /**
     * The annotation value.
     */
    @Annotate("xml:field=inline")
    @Required
    @Id(2)
    public String value;

    public MetaAnnotation() {
    }

    public MetaAnnotation(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
