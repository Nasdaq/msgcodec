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

        return messages;
    }
}
