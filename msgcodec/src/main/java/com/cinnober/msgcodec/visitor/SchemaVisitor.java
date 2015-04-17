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

import com.cinnober.msgcodec.GroupBinding;
import com.cinnober.msgcodec.SchemaBinding;
import com.cinnober.msgcodec.TypeDef;

/**
 * A visitor to visit a schema.
 *
 * <p>The methods of this class must be called in the following order:
 * <code>visit ( visitAnnotation | visitNamedType | visitGroup )* visitEnd</code>
 *
 * @author mikael.brannstrom
 */
public abstract class SchemaVisitor implements AnnotatedVisitor {

    protected SchemaVisitor sv;

    public SchemaVisitor(SchemaVisitor sv) {
        this.sv = sv;
    }

    public SchemaVisitor() {
        this(null);
    }

    /**
     * Visit the beginning of the schema.
     * @param binding the schema binding, or null.
     */
    public void visit(SchemaBinding binding) {
        if (sv != null) {
            sv.visit(binding);
        }
    }

    @Override
    public void visitAnnotation(String key, String value) {
        if (sv != null) {
            sv.visitAnnotation(key, value);
        }
    }

    /**
     * Visit a named type.
     * @param name the name, not null.
     * @param type the type, not null.
     * @return the named type visitor to receive further events, or null.
     */
    public NamedTypeVisitor visitNamedType(String name, TypeDef type) {
        if (sv != null) {
            return sv.visitNamedType(name, type);
        }
        return null;
    }

    /**
     * Visit a group.
     * @param name the name, not null.
     * @param id the id, or -1 for unspecified.
     * @param superGroup the super group name, or null if none.
     * @param binding the binding, or null if none.
     * @return the group definition visitor to recieve further events, or null.
     */
    public GroupDefVisitor visitGroup(String name, int id, String superGroup, GroupBinding binding) {
        if (sv != null) {
            return sv.visitGroup(name, id, superGroup, binding);
        }
        return null;
    }

    /**
     * Visit the end of the schema.
     */
    public void visitEnd() {
        if (sv != null) {
            sv.visitEnd();
        }
    }
}
