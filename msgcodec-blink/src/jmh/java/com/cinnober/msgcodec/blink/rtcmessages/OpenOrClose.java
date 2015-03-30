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


/**
 * <p>Describes whether a Trade is meant to open a new position
 * or close an existing position.</p>
 * <p>Some functions in the system operate
 * on gross positions rather than net positions and in those cases there
 * is a principal difference between on one hand a position that is long 100 and
 * short 50 and on the other hand a position that is long 50. Examples of such functions
 * include:
 * </p>
 * <ul>
 * <li>Risk calculations on risk tree nodes labeled "gross", where risk is calculated
 * separately for every position and then aggregated.</li>
 * <li>Some option plug-ins may choose to base the option exercise quantities
 * on gross rather than net positions, making one and the same account both deliver
 * and receive the underlying asset.</li>
 * </ul>
 * <p>A leg of a deal reported into the system can use this enumeration to
 * define how the resulting trade should
 * affect the gross position.</p>
 * @author hannes, Cinnober Financial Technology
 */
public enum OpenOrClose {
    /**
     * Open a new position. A buy trade will increase the long position, a sell trade will
     * increase the absolute value of the short position (short positions are expressed as negative
     * numbers).
     */
    OPEN,

    /**
     * Close an existing position. A buy trade will decrease the absolute value of the short
     * position. A sell trade will decrease the long position.
     */
    CLOSE
}
