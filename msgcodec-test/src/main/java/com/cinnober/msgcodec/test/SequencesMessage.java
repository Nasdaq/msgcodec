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
package com.cinnober.msgcodec.test;

import java.util.Collection;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.Static;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 * @author mikael.brannstrom
 *
 */
@Id(100)
public class SequencesMessage extends MsgObject {

    @Sequence(byte.class)
    public byte[] arrayBytes;
    public short[] arrayShorts;
    public int[] arrayInts;
    public long[] arrayLongs;

    @Unsigned
    public int[] arrayUInts;

    @Required
    @Sequence(byte.class)
    public byte[] arrayBytesReq;
    @Required
    public short[] arrayShortsReq;
    @Required
    public int[] arrayIntsReq;
    @Required
    public long[] arrayLongsReq;


    @Sequence(Integer.class)
    Collection<Integer> collectionInts;

    @Unsigned @Sequence(Integer.class)
    Collection<Integer> collectionUInts;

    @Sequence(Person.class)
    Collection<Person> collectionPeople;

    @Static @Sequence(Employee.class)
    Collection<Employee> collectionEmployees;

}
