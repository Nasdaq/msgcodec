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
