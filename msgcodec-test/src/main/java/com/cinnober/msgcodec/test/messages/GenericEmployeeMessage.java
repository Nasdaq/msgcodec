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

import com.cinnober.msgcodec.anot.Id;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author mikael.brannstrom
 */
@Id(112)
public class GenericEmployeeMessage extends GenericBasePersonMessage<Employee> {


    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, GenericEmployeeMessage> createMessages() {
        Map<String, GenericEmployeeMessage> messages = new LinkedHashMap<>();

        GenericEmployeeMessage msg;

        msg = new GenericEmployeeMessage();
        messages.put("clean", msg);
        msg.owner = null;

        msg = new GenericEmployeeMessage();
        messages.put("clean", msg);
        msg.owner = createEmployee("Bob", 12345);

        return messages;
    }

    private static Employee createEmployee(String name, long employeeNumber) {
        Employee employee = new Employee();
        employee.name = name;
        employee.employeeNumber = employeeNumber;
        return employee;
    }
}
