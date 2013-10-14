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

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 * @author mikael.brannstrom
 *
 */
@Id(100)
public class IntegersMessage extends MsgObject {
    @Id(1)
    public byte myByte;
    @Id(2)
    public short myShort;
    @Id(3)
    public int myInteger;
    @Id(4)
    public long myLong;

    @Unsigned
    @Id(5)
    public byte myByteU;
    @Unsigned
    @Id(6)
    public short myShortU;
    @Unsigned
    @Id(7)
    public int myIntegerU;
    @Unsigned
    @Id(8)
    public long myLongU;

    @Id(12)
    public Byte myByteObj;
    @Id(13)
    public Short myShortObj;
    @Id(14)
    public Integer myIntegerObj;
    @Id(15)
    public Long myLongObj;

    @Unsigned
    @Id(16)
    public Byte myByteUObj;
    @Unsigned
    @Id(17)
    public Short myShortUObj;
    @Unsigned
    @Id(18)
    public Integer myIntegerUObj;
    @Unsigned
    @Id(19)
    public Long myLongUObj;

}
