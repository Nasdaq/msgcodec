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
package com.cinnober.msgcodec.examples.messages;

import java.util.Objects;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;

/** Simple hello message.
 *
 * @author Mikael Brannstrom
 */
@Id(1) // <- Numeric identifier for this message type
public class Hello extends MsgObject {
    @Id(1) // <- Numeric identifier for this field
    @Required // <- Make the field required
    @Annotate({"maxLength=100"}) // <- some codecs interpret this as max string length
    public String greeting;

    public Hello() { // <- A default constructor must exist.
    }

    /** Create a new hello message.
     * @param greeting the greeting
     */
    public Hello(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Hello other = (Hello) obj;
        return Objects.equals(greeting, other.greeting);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.greeting);
        return hash;
    }
}
