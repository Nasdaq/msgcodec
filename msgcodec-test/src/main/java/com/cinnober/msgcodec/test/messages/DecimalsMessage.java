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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.SmallDecimal;

/**
 * @author mikael.brannstrom
 *
 */
@Id(102)
public class DecimalsMessage extends MsgObject {
    @Id(1)
    public BigDecimal myDecimal;
    @Required
    @Id(2)
    public BigDecimal myDecimalReq;

    @Id(3) @SmallDecimal
    public BigDecimal mySmallDecimal;
    @Required
    @Id(4) @SmallDecimal
    public BigDecimal mySmallDecimalReq;

    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, DecimalsMessage> createMessages() {
        Map<String, DecimalsMessage> messages = new LinkedHashMap<>();
        DecimalsMessage msg;

        msg = new DecimalsMessage();
        messages.put("clean", msg);
        msg.myDecimalReq = BigDecimal.ZERO;
        msg.mySmallDecimalReq = BigDecimal.ZERO;

        msg = new DecimalsMessage();
        messages.put("zero", msg);
        msg.myDecimalReq = BigDecimal.ZERO;
        msg.mySmallDecimalReq = BigDecimal.ZERO;
        msg.myDecimal = BigDecimal.ZERO;
        msg.mySmallDecimal = BigDecimal.ZERO;

        msg = new DecimalsMessage();
        messages.put("zero 0.0", msg);
        msg.myDecimalReq = BigDecimal.valueOf(0, 1);
        msg.mySmallDecimalReq = BigDecimal.valueOf(0, 1);
        msg.myDecimal = BigDecimal.valueOf(0, 1);
        msg.mySmallDecimal = BigDecimal.valueOf(0, 1);

        msg = new DecimalsMessage();
        messages.put("zero 0.00", msg);
        msg.myDecimalReq = BigDecimal.valueOf(0, 2);
        msg.mySmallDecimalReq = BigDecimal.valueOf(0, 2);
        msg.myDecimal = BigDecimal.valueOf(0, 2);
        msg.mySmallDecimal = BigDecimal.valueOf(0, 2);

        msg = new DecimalsMessage();
        messages.put("one", msg);
        msg.myDecimalReq = BigDecimal.ONE;
        msg.mySmallDecimalReq = BigDecimal.ONE;
        msg.myDecimal = BigDecimal.ONE;
        msg.mySmallDecimal = BigDecimal.ONE;

        msg = new DecimalsMessage();
        messages.put("one 1.0", msg);
        msg.myDecimalReq = BigDecimal.valueOf(10, 1);
        msg.mySmallDecimalReq = BigDecimal.valueOf(10, 1);
        msg.myDecimal = BigDecimal.valueOf(10, 1);
        msg.mySmallDecimal = BigDecimal.valueOf(10, 1);

        msg = new DecimalsMessage();
        messages.put("one 1.00", msg);
        msg.myDecimalReq = BigDecimal.valueOf(100, 2);
        msg.mySmallDecimalReq = BigDecimal.valueOf(100, 2);
        msg.myDecimal = BigDecimal.valueOf(100, 2);
        msg.mySmallDecimal = BigDecimal.valueOf(100, 2);

        msg = new DecimalsMessage();
        messages.put("0.01", msg);
        msg.myDecimalReq = BigDecimal.valueOf(1, 2);
        msg.mySmallDecimalReq = BigDecimal.valueOf(1, 2);
        msg.myDecimal = BigDecimal.valueOf(1, 2);
        msg.mySmallDecimal = BigDecimal.valueOf(1, 2);

        return messages;
    }

}
