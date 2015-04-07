/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
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

package com.cinnober.msgcodec.visitor;

import com.cinnober.msgcodec.FieldBinding;
import com.cinnober.msgcodec.TypeDef;

/**
 * A visitor to visit a group definition.
 *
 * <p>The methods of this class must be called in the following order:
 * <code>( visitAnnotation | visitField )* visitEnd</code>
 *
 * @author mikael.brannstrom
 */
public abstract class GroupDefVisitor implements AnnotatedVisitor {

    protected GroupDefVisitor gv;

    public GroupDefVisitor(GroupDefVisitor gv) {
        this.gv = gv;
    }

    public GroupDefVisitor() {
        this(null);
    }

    @Override
    public void visitAnnotation(String key, String value) {
        if (gv != null) {
            gv.visitAnnotation(key, value);
        }

    }
    public FieldDefVisitor visitField(String name, int id, boolean required, TypeDef type, FieldBinding binding) {
        if (gv != null) {
            return gv.visitField(name, id, required, type, binding);
        }
        return null;
    }

    public void visitEnd() {
        if (gv != null) {
            gv.visitEnd();
        }
    }
}
