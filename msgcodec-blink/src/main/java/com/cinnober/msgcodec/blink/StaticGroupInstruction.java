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
package com.cinnober.msgcodec.blink;

import java.io.IOException;

import com.cinnober.msgcodec.Factory;
import com.cinnober.msgcodec.GroupDef;

/** The static group instruction can encode and decode the fields of a group.
 * Any dynamic behaviour lies outside this class, e.g. lookup of <em>which</em>
 * static group to encode/decode.
 *
 * @author mikael.brannstrom
 */
@SuppressWarnings("rawtypes")
class StaticGroupInstruction {
    protected final GroupDef groupDef;
    protected final FieldInstruction[] fieldInstructions;
    protected final Factory factory;
    protected final StaticGroupInstruction superGroup;
    public StaticGroupInstruction(GroupDef groupDef, StaticGroupInstruction superGroup) {
        this.groupDef = groupDef;
        this.fieldInstructions = new FieldInstruction[groupDef.getFields().size()];
        this.factory = groupDef.getFactory();
        this.superGroup = superGroup;
    }
    void initFieldInstruction(int index, FieldInstruction fieldInstruction) {
        fieldInstructions[index] = fieldInstruction;
    }
    /** Write the group id to the specified stream. Only done for dynamic groups.
     *
     * @param out
     * @throws IOException
     */
    public void encodeGroupId(BlinkOutputStream out) throws IOException {
        int id = groupDef.getId();
        if (id == -1) {
            throw new IllegalArgumentException("Missing group id, cannot encode as dynamic group: " +
                    groupDef.getName());
        }
        out.writeUInt32(id);
    }
    /** Write the fields of the group, including any super groups.
     *
     * @param group the group to be encoded
     * @param out
     * @throws IOException
     */
    public void encodeGroup(Object group, BlinkOutputStream out) throws IOException {
        if (superGroup != null) {
            superGroup.encodeGroup(group, out);
        }
        for (int i = 0; i < fieldInstructions.length; i++) {
            fieldInstructions[i].encodeField(group, out);
        }
    }
    /** Read the fields of a group, including any super groups.
     *
     * @param in
     * @return the group, not null
     * @throws IOException
     */
    public Object decodeGroup(BlinkInputStream in) throws IOException {
        Object group = factory.newInstance();
        decodeGroup(group, in);
        return group;
    }
    /** Read the fields of a group, including any super groups.
     *
     * @param group the group where field values should be stored.
     * @param in
     * @throws IOException
     */
    private void decodeGroup(Object group, BlinkInputStream in) throws IOException {
        if (superGroup != null) {
            superGroup.decodeGroup(group, in);
        }
        for (int i = 0; i < fieldInstructions.length; i++) {
            fieldInstructions[i].decodeField(group, in);
        }
    }
}
