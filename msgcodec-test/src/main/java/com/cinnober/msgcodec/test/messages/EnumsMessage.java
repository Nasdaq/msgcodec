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
import com.cinnober.msgcodec.anot.Enumeration;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mikael.brannstrom
 *
 */
@Id(103)
public class EnumsMessage extends MsgObject {
    public Color myColor;
    @Required
    public Color myColorReq;
    
    @Enumeration(Color.class)
    public Integer myIntColor;
    @Enumeration(Color.class)
    public int myIntColorReq;

    public Country myCountry;
    @Required
    public Country myCountryReq;

    @Enumeration(Country.class)
    public Integer myIntCountry;
    @Enumeration(Country.class)
    public int myIntCountryReq;
    @Enumeration(Country.class) @Required
    public Integer myIntCountryObjReq;

    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, EnumsMessage> createMessages() {
        Map<String, EnumsMessage> messages = new LinkedHashMap<>();
        EnumsMessage msg;

        msg = new EnumsMessage();
        messages.put("clean", msg);
        msg.myColorReq = Color.RED;
        msg.myCountryReq = Country.DENMARK;
        msg.myIntColorReq = 0;
        msg.myIntCountryReq = 208;
        msg.myIntCountryObjReq = 208;

        msg = new EnumsMessage();
        messages.put("assigned", msg);
        msg.myColorReq = Color.RED;
        msg.myCountryReq = Country.DENMARK;
        msg.myIntColorReq = 0;
        msg.myIntCountryReq = 208;
        msg.myColor = Color.RED;
        msg.myCountry = Country.DENMARK;
        msg.myIntColor = 0;
        msg.myIntCountry = 208;
        msg.myIntCountryObjReq = 208;

        return messages;
    }
}
