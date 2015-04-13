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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Unsigned;

/**
 *
 * @author mikael.brannstrom
 */
@Id('O')
@Name("EnterOrder")
public class Ouch42EnterOrder extends MsgObject {
    @Annotate("maxLength=14")
    @Required
    public String token;
    @Unsigned
    public byte buySell;
    @Unsigned
    public int shares;
    @Annotate("maxLength=8")
    @Required
    public String stock;
    @Unsigned
    public int price;
    @Unsigned
    public int timeInForce;
    @Annotate("maxLength=4")
    @Required
    public String firm;
    @Unsigned
    public byte display;
    @Unsigned
    public byte capacity;
    @Unsigned
    public byte intermarketSweep;
    @Unsigned
    public int minimumQuantity;
    @Unsigned
    public byte crossType;
    @Unsigned
    public byte customerType;


}
