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

/**
 * A visitor to visit a named type.
 *
 * <p>The methods of this class must be called in the following order:
 * <code>visitAnnotation* visitEnd</code>
 *
 * @author mikael.brannstrom
 */
public abstract class NamedTypeVisitor {

    protected NamedTypeVisitor fv;

    public NamedTypeVisitor(NamedTypeVisitor fv) {
        this.fv = fv;
    }

    public NamedTypeVisitor() {
        this(null);
    }

    public void visitAnnotation(String key, String value) {
        if (fv != null) {
            fv.visitAnnotation(key, value);
        }
        
    }
    public void visitEnd() {
        if (fv != null) {
            fv.visitEnd();
        }
        
    }
}
