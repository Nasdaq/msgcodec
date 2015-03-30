/*
 * $Id: codetemplates.xml,v 1.4 2006/04/05 12:25:17 maal Exp $
 *
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
package com.cinnober.msgcodec.blink.rtcmessages;


import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Unsigned;

public class Request extends MsgObject {
    /**
     * Client assigned request identifier which must be unique for the client session.
     * Can be used for mapping the response message to this request.
     */
    @Unsigned
    public long requestId;

    public SessionToken sessionToken;
}