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

/**
 * A visitor to visit a field definition.
 *
 * <p>The methods of this class must be called in the following order:
 * <code>visitAnnotation* visitEnd</code>
 *
 * @author mikael.brannstrom
 */
public abstract class FieldDefVisitor implements AnnotatedVisitor {

    protected FieldDefVisitor fv;

    public FieldDefVisitor(FieldDefVisitor fv) {
        this.fv = fv;
    }

    public FieldDefVisitor() {
        this(null);
    }

    @Override
    public void visitAnnotation(String key, String value) {
        if (fv != null) {
            fv.visitAnnotation(key, value);
        }
    }

    /**
     * Visit the end of this field definition.
     */
    public void visitEnd() {
        if (fv != null) {
            fv.visitEnd();
        }
    }

}
