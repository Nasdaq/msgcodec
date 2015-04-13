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
