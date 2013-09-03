/*
 * Copyright (c) 2013 Cinnober Financial Technology AB, Stockholm,
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
package com.cinnober.msgcodec.tap;

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
    /** Write the group name to the specified stream. Only done for dynamic groups.
     *
     * @param out
     * @throws IOException
     */
    public void encodeGroupClassId(TapOutputStream out) throws IOException {
        String classId = groupDef.getName();
        int size = classId.length();
        int preSize = TapOutputStream.sizeOfModelLength(size) + size;
        out.writeModelLength(preSize, false);
        out.writeStringLatin1(classId);
    }
    /** Write the fields of the group, including any super groups.
     *
     * @param group the group to be encoded
     * @param out
     * @throws IOException
     */
    public void encodeGroup(Object group, TapOutputStream out) throws IOException {
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
    public Object decodeGroup(TapInputStream in) throws IOException {
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
    private void decodeGroup(Object group, TapInputStream in) throws IOException {
        if (superGroup != null) {
            superGroup.decodeGroup(group, in);
        }
        for (int i = 0; i < fieldInstructions.length; i++) {
            fieldInstructions[i].decodeField(group, in);
        }
    }
}