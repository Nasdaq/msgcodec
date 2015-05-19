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
package com.cinnober.msgcodec.xml;



/**
 * @author mikael.brannstrom
 *
 */
class XmlStringFormat implements XmlFormat<String> {

    public static final XmlStringFormat STRING = new XmlStringFormat();

    private static StringBuilder escape(char ch, StringBuilder appendTo) {
        switch (ch) {
        case '<':
            appendTo.append("&lt;");
            break;
        case '>':
            appendTo.append("&gt;");
            break;
        case '&':
            appendTo.append("&amp;");
            break;
        case '"':
            appendTo.append("&quot;");
            break;
        default:
            appendTo.append(ch);
        }
        return appendTo;
    }

    public static String escape(String value) {
        StringBuilder str = new StringBuilder((int) (value.length() * 1.2));
        for (int i=0; i<value.length(); i++) {
            escape(value.charAt(i), str);
        }
        return str.toString();
    }

    @Override
    public String format(String value) {
        return escape(value);
    }

    @Override
    public String parse(String str) throws FormatException {
        return str;
    }

}
