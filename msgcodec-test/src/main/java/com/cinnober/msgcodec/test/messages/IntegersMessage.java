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
package com.cinnober.msgcodec.test.messages;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Unsigned;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mikael.brannstrom
 *
 */
@Id(105)
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

    @Id(20) @Required
    public Byte myByteObjReq;
    @Id(21) @Required
    public Short myShortObjReq;
    @Id(22) @Required
    public Integer myIntegerObjReq;
    @Id(23) @Required
    public Long myLongObjReq;

    @Unsigned
    @Id(24) @Required
    public Byte myByteUObjReq;
    @Unsigned
    @Id(25) @Required
    public Short myShortUObjReq;
    @Unsigned
    @Id(26) @Required
    public Integer myIntegerUObjReq;
    @Unsigned
    @Id(27) @Required
    public Long myLongUObjReq;

    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, IntegersMessage> createMessages() {
        Map<String, IntegersMessage> messages = new LinkedHashMap<>();

        IntegersMessage msg;

        msg = new IntegersMessage();
        messages.put("clean", msg);
        msg.myByteObjReq = (byte)0;
        msg.myByteUObjReq = (byte)0;
        msg.myShortObjReq = (short)0;
        msg.myShortUObjReq = (short)0;
        msg.myIntegerObjReq = 0;
        msg.myIntegerUObjReq = 0;
        msg.myLongObjReq = 0L;
        msg.myLongUObjReq = 0L;

        messages.put("zero", msg);
        msg.myByteObjReq = (byte)0;
        msg.myByteUObjReq = (byte)0;
        msg.myShortObjReq = (short)0;
        msg.myShortUObjReq = (short)0;
        msg.myIntegerObjReq = 0;
        msg.myIntegerUObjReq = 0;
        msg.myLongObjReq = 0L;
        msg.myLongUObjReq = 0L;

        msg.myByteObj = (byte)0;
        msg.myByteUObj = (byte)0;
        msg.myShortObj = (short)0;
        msg.myShortUObj = (short)0;
        msg.myIntegerObj = 0;
        msg.myIntegerUObj = 0;
        msg.myLongObj = 0L;
        msg.myLongUObj = 0L;

        return messages;
    }
}
