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
package com.cinnober.msgcodec;

import java.util.Objects;

/**
 * Protocol dictionary binding.
 * ProtocolDictionaryBinding is immutable.
 *
 * @author mikael.brannstrom
 *
 */
public class SchemaBinding {
    private final GroupTypeAccessor groupTypeAccessor;

    /**
     * Create a protocol dictionary binding.
     *
     * @param groupTypeAccessor the group type accessor, not null
     */
    public SchemaBinding(GroupTypeAccessor groupTypeAccessor) {
        this.groupTypeAccessor = Objects.requireNonNull(groupTypeAccessor);
    }

    /**
     * @return the groupTypeAccessor
     */
    public GroupTypeAccessor getGroupTypeAccessor() {
        return groupTypeAccessor;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(SchemaBinding.class)) {
            return false;
        }
        SchemaBinding other = (SchemaBinding) obj;
        return groupTypeAccessor.equals(other.groupTypeAccessor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.groupTypeAccessor);
    }
}
