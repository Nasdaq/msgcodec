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
public class XmlBooleanFormat implements XmlFormat<Boolean> {

    public static final XmlBooleanFormat BOOLEAN = new XmlBooleanFormat();

    @Override
    public String format(Boolean value) {
        return value.booleanValue() ? "true" : "false";
    }

    @Override
    public Boolean parse(String str) throws FormatException {
        switch (str) {
        case "1":
        case "true":
            return true;
        case "0":
        case "false":
            return false;
        default:
            throw new FormatException("Expected boolean: " + str);
        }
    }

}
