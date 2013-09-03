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
package com.cinnober.msgcodec.examples.messages;

import java.util.Collection;
import java.util.Collections;

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;

/**
 * @author Mikael Brannstrom
 *
 */
@Id(4)
public class SpecialNode extends Node {
    private Collection<String> bunchOfStrings = Collections.emptyList();
    private String[] moreStrings;
    private Object ext;

    @Id(10)
    @Sequence(String.class) // <- this is a sequence of string
    @Required
    public Collection<String> getBunchOfStrings() {
        return bunchOfStrings;
    }

    public void setBunchOfStrings(Collection<String> bunchOfStrings) {
        this.bunchOfStrings = bunchOfStrings;
    }

    @Id(11)
    @Sequence(String.class) // <- also a sequence of string
    public String[] getMoreStrings() {
        return moreStrings;
    }

    public void setMoreStrings(String[] moreStrings) {
        this.moreStrings = moreStrings;
    }

    // This is a dynamic group reference, i.e. it may contain any group
    @Id(100)
    public Object getExt() {
        return ext;
    }
    public void setExt(Object ext) {
        this.ext = ext;
    }

}
