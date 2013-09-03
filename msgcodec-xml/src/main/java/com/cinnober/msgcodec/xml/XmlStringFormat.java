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
package com.cinnober.msgcodec.xml;



/**
 * @author mikael.brannstrom
 *
 */
public class XmlStringFormat implements XmlFormat<String> {

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
