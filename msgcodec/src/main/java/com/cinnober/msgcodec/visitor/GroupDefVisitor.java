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

    /**
     * Visit a field.
     * @param name the name, not null.
     * @param id the id, or -1 if unspecified.
     * @param required true if required, otherwise false.
     * @param type the type, not null.
     * @param binding the binding, or null if unbound.
     * @return the field definition visitor to receive further events, or null.
     */
    public FieldDefVisitor visitField(String name, int id, boolean required, TypeDef type, FieldBinding binding) {
        if (gv != null) {
            return gv.visitField(name, id, required, type, binding);
        }
        return null;
    }

    /**
     * Visit the end of this group definition.
     */
    public void visitEnd() {
        if (gv != null) {
            gv.visitEnd();
        }
    }
}
