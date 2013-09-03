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
package com.cinnober.msgcodec;

import java.util.List;

import com.cinnober.msgcodec.anot.Enumeration;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.Static;

/**
 * @author mikael.brannstrom
 *
 */
@Id(200)
public class BarMessage extends FooMessage {
    private Color myEnum;
    private int myEnumInteger;
    private List<Thing> myThings;
    private Thing[] myThingsArray;
    private Thing mySomeThing;
    private Thing mySomeThingReq;
    
    @Id(40)
    public Color getMyEnum() {
        return myEnum;
    }
    public void setMyEnum(Color myEnum) {
        this.myEnum = myEnum;
    }

    @Id(41)
    @Enumeration(Size.class)
    public int getMyEnumInteger() {
        return myEnumInteger;
    }
    public void setMyEnumInteger(int myEnumInteger) {
        this.myEnumInteger = myEnumInteger;
    }
    @Id(42)
    @Static @Required @Sequence(Thing.class)
    public List<Thing> getMyThings() {
        return myThings;
    }
    public void setMyThings(List<Thing> myThings) {
        this.myThings = myThings;
    }
    
    @Id(43)
    @Sequence(Thing.class)
    public Thing[] getMyThingsArray() {
        return myThingsArray;
    }
    public void setMyThingsArray(Thing[] myThingsArray) {
        this.myThingsArray = myThingsArray;
    }
    @Id(44)
    @Static
    public Thing getMySomeThing() {
        return mySomeThing;
    }
    public void setMySomeThing(Thing mySomeThing) {
        this.mySomeThing = mySomeThing;
    }
    @Id(45)
    @Static @Required
    public Thing getMySomeThingReq() {
        return mySomeThingReq;
    }
    public void setMySomeThingReq(Thing mySomeThingReq) {
        this.mySomeThingReq = mySomeThingReq;
    }
    
}
