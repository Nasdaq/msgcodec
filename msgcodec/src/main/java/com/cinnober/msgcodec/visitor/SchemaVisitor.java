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
